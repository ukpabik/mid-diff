import pandas as pd
from sklearn.preprocessing import StandardScaler

# load csv of training data
df = pd.read_csv("training_dataset.csv")

# select features to normalize
features = [
  "kills", "deaths", "assists", "goldEarned", "goldSpent",
  "csPerMin", "kda", "visionScore", "wardsPlaced", "wardsKilled",
  "damageDealtToChampions", "totalDamageTaken", "totalMinionsKilled",
  "neutralMinionsKilled", "turretTakedowns", "inhibitorTakedowns", "gameDuration"
]

metadata = df[["puuid", "matchId"]] if "puuid" in df.columns and "matchId" in df.columns else None

# normalize features using standard scaler
scaler = StandardScaler()
scaled_data = scaler.fit_transform(df[features])

# Create dataframe using scaled data
normalized_df = pd.DataFrame(scaled_data, columns=features)

if metadata is not None:
    normalized_df = pd.concat([metadata.reset_index(drop=True), normalized_df], axis=1)

# output csv
normalized_df.to_csv("normalized_dataset.csv", index=False)

print("✅ Normalization complete. Saved as 'normalized_dataset.csv'.")
