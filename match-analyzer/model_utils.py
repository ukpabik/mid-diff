import pandas as pd
import joblib
from google import genai
from dotenv import load_dotenv
import os
import json

load_dotenv()

# role → custom thresholds
ROLE_THRESHOLDS = {
    "TOP":     { "high_cs": 6.0,  "low_cs": 3.0,  "high_tank": 800,  "low_tank": 200 },
    "JUNGLE":  { "high_cs": 3.5,  "low_cs": 1.0,  "high_tank": 600,  "low_tank": 150 },
    "MID":     { "high_cs": 7.0,  "low_cs": 4.0,  "high_dmg": 2200, "low_dmg": 600 },
    "ADC":     { "high_cs": 7.5,  "low_cs": 4.5,  "high_dmg": 2400, "low_dmg": 700 },
    "SUPPORT": { "high_vision": 40, "low_vision": 10, "high_assist": 0.7, "low_assist": 0.2 },
}
DEFAULT = { 
    "high_cs": 6.0, "low_cs": 3.0, 
    "high_dmg": 2000, "low_dmg": 500,
    "high_tank": 700, "low_tank": 200,
    "high_vision": 30, "low_vision": 5,
    "high_assist": 0.5, "low_assist": 0.1
}

def interpret_cluster(row):
  desc = []

  duration_m = row["gameDuration"] / 60.0
  if duration_m <= 20: desc.append("short stomp")
  elif duration_m >= 45: desc.append("very long game")

  kpm = row["kills"] / max(1, duration_m)
  if kpm >= 0.3: desc.append("carry performance")
  elif kpm <= 0.1: desc.append("low kill participation")

  dpm = row["deaths"] / max(1, duration_m)
  if dpm >= 0.3: desc.append("frequent deaths")
  elif dpm <= 0.07: desc.append("very low deaths")

  apm = row["assists"] / max(1, duration_m)
  if apm >= 0.5: desc.append("heavy team support")
  elif apm <= 0.1: desc.append("low team involvement")

  if row["kda"] >= 5: desc.append("high KDA (clean execution)")
  elif row["kda"] <= 1.5: desc.append("low KDA (feeding)")

  gpm = row["goldEarned"] / max(1, duration_m)
  if gpm >= 500: desc.append("gold‑rich economy")
  elif gpm <= 233: desc.append("gold‑starved")

  if row["csPerMin"] >= 8.5: desc.append("elite farming")
  elif row["csPerMin"] <= 4.5: desc.append("low CS rate")

  dmgpm = row["damageDealtToChampions"] / max(1, duration_m)
  if dmgpm >= 2000: desc.append("very high damage")
  elif dmgpm <= 500: desc.append("low damage output")

  dtpm = row["totalDamageTaken"] / max(1, duration_m)
  if dtpm >= 1333: desc.append("frontline tanking")
  elif dtpm <= 333: desc.append("avoids frontline")

  if row["visionScore"] >= 50: desc.append("excellent vision")
  elif row["visionScore"] <= 15: desc.append("poor vision control")

  if row["wardsPlaced"] >= 30: desc.append("vision‑focused role")
  elif row["wardsPlaced"] <= 5: desc.append("no warding")

  if row["turretTakedowns"] >= 5: desc.append("strong objective pressure")
  if row["inhibitorTakedowns"] >= 2: desc.append("game‑closing pushes")

  return ", ".join(desc)
  
def enrich_cluster(row):
  cluster = row['cluster']
  
  if cluster == 0:
    return {
      "label": "Visionary Support",
      "archetype_description": "Low damage, high assists, strong map control via vision.",
      "advice": "Keep up the vision and assists! Work on early-game safety."
    }
  elif cluster == 1:
    return {
      "label": "Low Impact Laner",
      "archetype_description": "Low kill contribution, weak vision — possibly stomped.",
      "advice": "Play more proactive early lanes or roam to influence the map."
    }
  elif cluster == 2:
    return {
      "label": "Aggressive DPS",
      "archetype_description": "High kills and damage with risky positioning.",
      "advice": "Be aggressive, but tighten positioning to reduce deaths."
    }
  elif cluster == 3:
    return {
      "label": "Enchanter Support",
      "archetype_description": "Classic support — high assists, strong vision, but squishy.",
      "advice": "Great map presence! Try to avoid overextending mid-game."
    }
  elif cluster == 4:
    return {
      "label": "Frontline Bruiser",
      "archetype_description": "Heavy damage taken with solid kill contribution.",
      "advice": "Anchor your team in fights. Consider building more utility/tank."
    }
  elif cluster == 5:
    return {
      "label": "Late Game Hypercarry",
      "archetype_description": "Dominates in late-game with massive damage and kills.",
      "advice": "Try building early safety to guarantee scaling into late-game."
    }
  elif cluster == 6:
    return {
      "label": "Non-Factor",
      "archetype_description": "Minimal stats across the board — very low impact.",
      "advice": "Focus on fundamentals: farm more, die less, and contribute to team fights."
    }
  elif cluster == 7:
    return {
      "label": "Perfect Game",
      "archetype_description": "High KDA, great CS, low deaths — nearly flawless.",
      "advice": "You're playing clean! Study this game to replicate the decision-making."
    }
  else:
    return {
      "label": "Unknown",
      "archetype_description": "",
      "advice": ""
    }



def analyze_match(new_match_row: dict):

  # List of features used during training
  features = [
    "kills", "deaths", "assists", "goldEarned", "goldSpent",
    "csPerMin", "kda", "visionScore", "wardsPlaced", "wardsKilled",
    "damageDealtToChampions", "totalDamageTaken", "totalMinionsKilled",
    "neutralMinionsKilled", "turretTakedowns", "inhibitorTakedowns", "gameDuration"
  ]

  # Convert the input into a DataFrame
  match_df = pd.DataFrame([new_match_row])

  # Load the scaler and model
  scaler = joblib.load("scaler.pkl")
  kmeans = joblib.load("kmeans_model.pkl")

  # Predict the cluster
  scaled = scaler.transform(match_df[features])
  cluster_id = kmeans.predict(scaled)[0]
  description = interpret_cluster(match_df.iloc[0])
  enrichment = enrich_cluster({"cluster": cluster_id})

  return {
    "cluster": int(cluster_id),
    "label": enrichment["label"],
    "archetype_description": enrichment["archetype_description"],
    "description": description,
    "advice": enrichment["advice"]
  }

def parse_gemini(response):
  text = response.strip()
  if text.startswith("```"):
      text = text.strip("```").replace("json", "").strip()
  data = json.loads(text)
  
  return data

def generate_advice(cluster_json, build_item_names, position, player_rank, role, championName):
  client = genai.Client(api_key=os.getenv("GEMINI_API_KEY"))


  prompt = f"""
  You are an expert League of Legends coach. A player just finished a game in the {position} lane as a {role}, currently ranked {player_rank}. They built these items:

  {build_item_names}

  Their play style cluster is:

  {json.dumps(cluster_json, indent=2)}

  Using all of that information, produce a concise, tactical improvement tip tailored to:
    1. Their cluster profile.
    2. Their current rank.
    3. The specific build they ran.
    4. The {role} role in {position}.
    5. The champion {championName}. 

  Respond ONLY in JSON, following this schema exactly:

  {{
    "cluster": int,
    "label": string,
    "archetype_description": string,
    "description": string,
    "advice": string
  }}

  Keep the “advice” field to 2–3 sentences, focused on what to practice or adjust next game.
  
  Make sure to talk like how a human would.
  """

  response = client.models.generate_content(
      model="gemini-2.0-flash",
      contents=[prompt]
  )

  return parse_gemini(response.text)
