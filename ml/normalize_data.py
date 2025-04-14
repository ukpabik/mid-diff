import pandas as pd
from sklearn.preprocessing import StandardScaler
from sklearn.cluster import KMeans
import joblib
from sklearn.decomposition import PCA
import matplotlib.pyplot as plt



def normalize():
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


# Function to run kmeans algorithm on our normalized dataset
def run_k_means():
  df = pd.read_csv("normalized_dataset.csv")
  
  
  # run kmeans and get our model
  kmeans = KMeans(n_clusters=8, random_state=42)
  cluster_labels = kmeans.fit_predict(df.select_dtypes(include='number'))
  joblib.dump(kmeans, "kmeans_model.pkl")
  
  # save normalized data to csv
  df['cluster'] = cluster_labels
  df.to_csv("clustered_dataset.csv", index=False)

  
  # use raw data and label it with cluster labels
  raw_df = pd.read_csv("training_dataset.csv")
  raw_df['cluster'] = cluster_labels
  raw_df.to_csv("labeled_data.csv", index=False)

# ONLY FOR VISUALIZATION
def scatter_():
  # Load the clustered dataset
  df = pd.read_csv("clustered_dataset.csv")
  X = df.select_dtypes(include='number')

  # Use the best k to cluster
  best_k = 5
  kmeans = KMeans(n_clusters=best_k, random_state=42)
  df['cluster'] = kmeans.fit_predict(X)

  # Use PCA to reduce to 2 components for visualization
  pca = PCA(n_components=2)
  X_pca = pca.fit_transform(X)

  # Plot the clusters for visualization
  plt.figure(figsize=(8, 6))
  scatter = plt.scatter(X_pca[:, 0], X_pca[:, 1], c=df['cluster'], cmap='tab10', alpha=0.7)
  plt.title(f'KMeans Clustering (k={best_k}) with PCA-reduced Data')
  plt.xlabel('PCA Component 1')
  plt.ylabel('PCA Component 2')
  plt.colorbar(scatter, label='Cluster')
  plt.grid(True)
  plt.show()  