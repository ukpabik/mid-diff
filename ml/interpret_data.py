import pandas as pd




def interpret_cluster(row):
  desc = []

  # Combat
  if row['kills'] >= 10:
    desc.append("carry performance")
  elif row['kills'] <= 2:
    desc.append("low kill participation")

  if row['deaths'] >= 10:
    desc.append("frequent deaths")
  elif row['deaths'] <= 2:
    desc.append("very low deaths")

  if row['assists'] >= 15:
    desc.append("heavy team support")
  elif row['assists'] <= 3:
    desc.append("low team involvement")

  if row['kda'] >= 5:
    desc.append("high KDA (clean execution)")
  elif row['kda'] <= 1.5:
    desc.append("low KDA (high risk or feeding)")

  # Economy
  if row['goldEarned'] >= 15000:
    desc.append("gold fed")
  elif row['goldEarned'] <= 7000:
    desc.append("gold-starved")

  if row['goldSpent'] < row['goldEarned'] * 0.8:
    desc.append("inefficient spending")

  # Farming
  if row['csPerMin'] >= 8.5:
    desc.append("elite farming")
  elif row['csPerMin'] <= 4.5:
    desc.append("low CS rate")

  total_cs = row['totalMinionsKilled'] + row['neutralMinionsKilled']
  if total_cs >= 300:
    desc.append("farm-heavy role (mid/ADC)")
  elif total_cs <= 80:
    desc.append("low minion control")

  # Damage
  if row['damageDealtToChampions'] >= 35000:
    desc.append("very high damage")
  elif row['damageDealtToChampions'] <= 10000:
    desc.append("low damage output")

  if row['totalDamageTaken'] >= 40000:
    desc.append("frontline tanking")
  elif row['totalDamageTaken'] <= 10000:
    desc.append("avoids frontline or squishy")

  # Vision
  if row['visionScore'] >= 50:
    desc.append("excellent map awareness")
  elif row['visionScore'] <= 15:
    desc.append("poor vision control")

  if row['wardsPlaced'] >= 30:
    desc.append("vision-focused role (support)")
  elif row['wardsPlaced'] <= 5:
    desc.append("no warding")

  if row['wardsKilled'] >= 7:
    desc.append("vision denial expert")
  elif row['wardsKilled'] <= 1:
    desc.append("poor enemy vision control")

  # Objectives
  if row['turretTakedowns'] >= 5:
    desc.append("strong objective pressure")
  elif row['turretTakedowns'] == 0:
    desc.append("no turret participation")

  if row['inhibitorTakedowns'] >= 2:
    desc.append("closes games")
  elif row['inhibitorTakedowns'] == 0:
    desc.append("never reached inhib")

  # Game Duration
  if row['gameDuration'] >= 2200:
    desc.append("very long game")
  elif row['gameDuration'] <= 1500:
    desc.append("short stomp")

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
