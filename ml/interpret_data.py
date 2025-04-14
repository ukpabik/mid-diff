import pandas as pd




def interpret_cluster(row):
  desc = []

  duration_mins = row["gameDuration"] / 60.0
  if duration_mins == 0:
      # Edge case: no valid game length
      return "Invalid game duration"

  kills_per_min = row["kills"] / duration_mins
  deaths_per_min = row["deaths"] / duration_mins
  assists_per_min = row["assists"] / duration_mins
  gold_per_min = row["goldEarned"] / duration_mins

  if row["gameDuration"] <= 1200:
      desc.append("short stomp")
  elif row["gameDuration"] >= 2700:
      desc.append("very long game") 

  if kills_per_min >= 0.3:
      desc.append("carry performance")
  elif kills_per_min <= 0.1:
      desc.append("low kill participation")

  if deaths_per_min >= 0.3:
      desc.append("frequent deaths")
  elif deaths_per_min <= 0.07:
      desc.append("very low deaths")

  if assists_per_min >= 0.5:
      desc.append("heavy team support")
  elif assists_per_min <= 0.1:
      desc.append("low team involvement")

  if row["kda"] >= 5:
      desc.append("high KDA (clean execution)")
  elif row["kda"] <= 1.5:
      desc.append("low KDA (high risk or feeding)")

  if gold_per_min >= 500:
      desc.append("gold fed")
  elif gold_per_min <= 233:
      desc.append("gold-starved")

  if row["goldSpent"] < row["goldEarned"] * 0.8:
      desc.append("inefficient spending")

  if row["csPerMin"] >= 8.5:
      desc.append("elite farming")
  elif row["csPerMin"] <= 4.5:
      desc.append("low CS rate")

  total_cs = row["totalMinionsKilled"] + row["neutralMinionsKilled"]
  if total_cs >= 300:
      desc.append("farm-heavy role (mid/ADC)")
  elif total_cs <= 80:
      desc.append("low minion control")

  dmg_per_min = row["damageDealtToChampions"] / duration_mins
  if dmg_per_min >= 2000:
      desc.append("very high damage")
  elif dmg_per_min <= 500:
      desc.append("low damage output")

  dmg_taken_per_min = row["totalDamageTaken"] / duration_mins
  if dmg_taken_per_min >= 1333: 
      desc.append("frontline tanking")
  elif dmg_taken_per_min <= 333:
      desc.append("avoids frontline or squishy")

  if row["visionScore"] >= 50:
      desc.append("excellent map awareness")
  elif row["visionScore"] <= 15:
      desc.append("poor vision control")

  if row["wardsPlaced"] >= 30:
      desc.append("vision-focused role (support)")
  elif row["wardsPlaced"] <= 5:
      desc.append("no warding")

  if row["wardsKilled"] >= 7:
      desc.append("vision denial expert")
  elif row["wardsKilled"] <= 1:
      desc.append("poor enemy vision control")

  if row["turretTakedowns"] >= 5:
      desc.append("strong objective pressure")
  elif row["turretTakedowns"] == 0:
      desc.append("no turret participation")

  if row["inhibitorTakedowns"] >= 2:
      desc.append("closes games")
  elif row["inhibitorTakedowns"] == 0:
      desc.append("never reached inhib")

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

  
def export_labeled_data():
  df = pd.read_csv("labeled_data.csv")
  cluster_summary = df.groupby("cluster").mean(numeric_only=True).round(2)
  cluster_summary["description"] = cluster_summary.apply(interpret_cluster, axis=1)
  enrichments = cluster_summary.reset_index().apply(enrich_cluster, axis=1, result_type='expand')
  cluster_summary[["label", "archetype_description", "advice"]] = enrichments.values
  
  cluster_summary.to_csv("descriptive_data.csv")
