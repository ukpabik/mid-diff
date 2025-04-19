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
      "label": "Visionary Control",
      "archetype_description": "Low damage output, high assist count, strong map control via vision.",
      "advice": "Maintain deep vision wards and use them to create opportunities for your team."
    }

  elif cluster == 1:
    return {
      "label": "Early Struggler",
      "archetype_description": "Low kill involvement and vision score—struggles to impact early game.",
      "advice": "Focus on safe farming and incremental vision to regain control in your lane."
    }

  elif cluster == 2:
    return {
      "label": "High-Risk DPS",
      "archetype_description": "High damage outputs but inconsistent positioning leading to extra deaths.",
      "advice": "Balance aggressive play with better positioning and vision awareness."
    }

  elif cluster == 3:
    return {
      "label": "Vision Specialist",
      "archetype_description": "Exceptional vision metrics and assist counts, enabling coordinated plays.",
      "advice": "Continue securing key vision points and guide teammates to capitalize on information."
    }

  elif cluster == 4:
    return {
      "label": "Engage Initiator",
      "archetype_description": "High durability and solid damage trade-offs when initiating fights.",
      "advice": "Lead team engagements and prioritize build paths that enhance survivability."
    }

  elif cluster == 5:
    return {
      "label": "Scaling Carry",
      "archetype_description": "Moderate stats early but explosive late-game performance and damage spikes.",
      "advice": "Prioritize safe scaling until you hit your power spikes in the late game."
    }

  elif cluster == 6:
    return {
      "label": "Precision Execution",
      "archetype_description": "Near-zero deaths and consistently high KDA reflecting flawless game execution.",
      "advice": "Study decision-making in these matches to replicate optimal play patterns."
    }

  elif cluster == 7:
    return {
      "label": "Steady Performer",
      "archetype_description": "Balanced stats across kills, assists, and vision—consistent impact.",
      "advice": "Maintain consistency and look for small edges, like lane control and vision timing."
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
