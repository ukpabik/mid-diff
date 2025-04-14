from get_data import get_training_data, add_match_data_to_db
from normalize_data import normalize, run_k_means
from interpret_data import export_labeled_data, interpret_cluster



"""
  Full ML pipeline
  
  1. add_match_data_to_db():
    - Queries for players of different ranks, and adds matches to db
      to use for training data.
      
  2. get_training_data():
    - Returns all matches from db and exports to csv.
  
  3. normalize():
    - Uses StandardScaler to normalize features in training data

  4. run_k_means():
    - Uses kmeans unsupervised learning algorithm to cluster data based
      on similarities.
  
  5. interpret_cluster():
    - Uses specific game metrics to label each cluster.
  
  6. export_labeled_data():
    - Interprets clusters and exports the labeled data to csv.

"""

# Call this to get clustered data and export it to csv
normalize()
run_k_means()
export_labeled_data()
