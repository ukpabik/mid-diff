import pandas as pd




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
      "label": "Struggling Laner",
      "archetype_description": "Average farm and kills but poor vision control in lane.",
      "advice": "Improve ward coverage and focus on objectives to regain lane control."
    }
  elif cluster == 1:
    return {
      "label": "Overextended Duelist",
      "archetype_description": "High damage potential but frequently dies due to risky plays.",
      "advice": "Prioritize safety and only engage when you have vision and support."
    }
  elif cluster == 2:
    return {
      "label": "Visionary Support",
      "archetype_description": "Exceptional vision control with high assist counts enabling team plays.",
      "advice": "Maintain deep ward coverage and coordinate engages to maximize impact."
    }
  elif cluster == 3:
    return {
      "label": "High‑Impact Carry",
      "archetype_description": "Massive damage output and kill counts driving team fights.",
      "advice": "Position carefully to deal damage safely and secure key objectives."
    }
  elif cluster == 4:
    return {
      "label": "Frontline Bruiser",
      "archetype_description": "Durable initiator who soaks damage and contributes solid kills.",
      "advice": "Lead engages and build resistances to protect your team."
    }
  elif cluster == 5:
    return {
      "label": "Minimal Impact",
      "archetype_description": "Low kills, assists, and vision—struggles to influence games early.",
      "advice": "Work on fundamental farming and warding to scale into mid-game."
    }
  elif cluster == 6:
    return {
      "label": "Flawless Performer",
      "archetype_description": "Near-perfect KDA and win rate showcasing exceptional execution.",
      "advice": "Review your decision-making in these games to replicate success."
    }
  elif cluster == 7:
    return {
      "label": "Roaming Support",
      "archetype_description": "High assist and vision scores emphasizing map presence and rotations.",
      "advice": "Coordinate roams with laners and secure key objectives with timely wards."
    }
  else:
    return {
      "label": "Unknown",
      "archetype_description": "",
      "advice": ""
    }


  
def export_labeled_data():
  df = pd.read_csv("labeled_data.csv")
  cluster_summary = df.groupby("cluster").mean(numeric_only=True).round(2)
  cluster_summary["description"] = cluster_summary.apply(interpret_cluster, axis=1)
  enrichments = cluster_summary.reset_index().apply(enrich_cluster, axis=1, result_type='expand')
  cluster_summary[["label", "archetype_description", "advice"]] = enrichments.values
  
  cluster_summary.to_csv("descriptive_data.csv")
