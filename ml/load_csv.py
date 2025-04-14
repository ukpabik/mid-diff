import pandas as pd
from sklearn.preprocessing import StandardScaler

puuids = []
dfs = []

#TODO: Add functionality here to generate list of puuids.
#TODO: I want this script to essentially train the data on a list of riot ids and taglines i give it

# read all matches and load into one csv for ML model training
for puuid in puuids:
  url = f"http://localhost:8080/user/matches/csv/{puuid}"
  df = pd.read_csv(url)
  df.drop(df.columns[[0, 1]], axis=1, inplace=True)
  dfs.append(df)
  
full_df = pd.concat(dfs)
full_df.to_csv("training_dataset.csv", index=False)