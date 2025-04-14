import pandas as pd
import joblib
from interpret_data import enrich_cluster, interpret_cluster


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
