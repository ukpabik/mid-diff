import requests
import json
from dotenv import load_dotenv
import os
import time
import pandas as pd

load_dotenv()

API_KEY = os.getenv("RIOT_API_KEY")

regions = [
  'br1', 'eun1', 'euw1', 'jp1', 'kr',
  'la1', 'la2', 'na1', 'oc1', 'ph2',
  'ru', 'sg2', 'th2', 'tr1', 'tw2', 'vn2'
]

platform_to_routing = {
  "na1": "americas", "br1": "americas", "la1": "americas", "la2": "americas",
  "oc1": "sea", "ph2": "sea", "sg2": "sea", "th2": "sea", "tw2": "sea", "vn2": "sea",
  "euw1": "europe", "eun1": "europe", "tr1": "europe", "ru": "europe",
  "jp1": "asia", "kr": "asia"
}

API_BASE = "http://localhost:8080/user"
TIERS = ['BRONZE', 'SILVER', 'GOLD', 'PLATINUM', 'DIAMOND', 'MASTER', 'GRANDMASTER', 'CHALLENGER']


# Checks if a given puuid is in the database already
def is_user_in_db(puuid):
  url = f"{API_BASE}/db/{puuid}"
  try:
    res = requests.get(url)
    return res.status_code == 200
  except Exception as e:
    print(f"❗ DB check failed: {e}")
    return False

# Fetches all player PUUIDs from Riot for a given ranked tier
def get_summoners_by_tier(tier):
  all_summoners = []
  for region in regions:
    if tier in ['MASTER', 'GRANDMASTER', 'CHALLENGER']:
      endpoint = f"https://{region}.api.riotgames.com/lol/league/v4/{tier.lower()}leagues/by-queue/RANKED_SOLO_5x5?api_key={API_KEY}"
    else:
      endpoint = f"https://{region}.api.riotgames.com/lol/league/v4/entries/RANKED_SOLO_5x5/{tier}/I?page=1&api_key={API_KEY}"
    try:
      response = requests.get(endpoint)
      if response.status_code == 200:
        data = response.json()
        if isinstance(data, list):
            entries = data
        else:
            entries = data.get("entries", [])
        for entry in entries:
          if "puuid" in entry:
            all_summoners.append((entry["puuid"], region))
          print(f"Summoner List Size: {len(all_summoners)}")
      else:
        print(f"⚠️ Failed for {tier} in {region}: {response.status_code}")
    except Exception as e:
      print(f"❗ Exception for {tier} in {region}: {e}")
  return all_summoners

# Aggregates PUUIDs from all tiers into one flat list
def puuid_script():
  total = set()
  for tier in TIERS:
    summoners = get_summoners_by_tier(tier)
    if summoners:
      total.update(summoners)
  print(f"{len(total)} summoners acquired")
  return total


# Converts a player's PUUID into a Riot ID (gameName and tagLine)
def get_riot_id_from_puuid(puuid, platform_region):
  routing_region = platform_to_routing.get(platform_region)
  url = f"https://{routing_region}.api.riotgames.com/riot/account/v1/accounts/by-puuid/{puuid}?api_key={API_KEY}"
  backoff = 1
  while True:
    res = requests.get(url)
    if res.status_code == 429:
      print(f"⏳ Riot API 429 — backing off {backoff}s...")
      time.sleep(backoff)
      backoff = min(backoff * 2, 20)
    elif res.status_code == 200:
      data = res.json()
      return data["gameName"], data["tagLine"]
    else:
      print(f"⚠️ Riot API error {res.status_code} for PUUID: {puuid}")
      return None

# Triggers /search on backend to upsert user and cache match history
def call_search(riot_id, tag_line, idx, platform_region):
  routing_region = platform_to_routing.get(platform_region)
  url = f"https://{API_BASE}/{riot_id}/{tag_line}/{platform_region}/{routing_region}"

  backoff = 1
  while True:
    res = requests.get(url)
    if res.status_code == 429:
      print(f"⏳ API 429 — backing off {backoff}s...")
      time.sleep(backoff)
      backoff = min(backoff * 2, 15)
    elif res.status_code == 200:
      print(f"#{idx}: ✅ Searched {riot_id}#{tag_line}")
      return res.json().get("puuid")
    else:
      print(f"❌ Search failed for {riot_id}#{tag_line}: {res.status_code}")
      return None

# Main function to add match data into DB
def add_match_data_to_db():
  puuids = puuid_script()
  idx = 0
  for puuid, platform_region in puuids:
    if idx > 0 and idx % 95 == 0:
      print("🛑 Reached 95 requests, sleeping....")
      time.sleep(60)

    if is_user_in_db(puuid):
      print(f"⏭️ Skipping (already in DB): {puuid}")
      continue

    result = get_riot_id_from_puuid(puuid, platform_region)
    if not result:
      print(f"⚠️ Skipping PUUID (no Riot ID info found): {puuid}")
      continue

    game_name, tag_line = result
    call_search(game_name, tag_line, idx, platform_region)
    idx += 1
    time.sleep(0.5)

def get_training_data():
  dfs = []
  # read all matches and load into one csv for ML model training
  url = f"http://localhost:8080/user/matches/csv/all"
  df = pd.read_csv(url)
  df.drop(df.columns[[0, 1, 3]], axis=1, inplace=True)
  dfs.append(df)
    
  full_df = pd.concat(dfs)
  full_df.to_csv("training_dataset.csv", index=False)
  
add_match_data_to_db()