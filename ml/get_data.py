import requests
import json
from dotenv import load_dotenv
import os

load_dotenv()

API_KEY = os.getenv("RIOT_API_KEY")
REGION = 'na1'
API_BASE = "http://localhost:8080/user"
AMERICAS_REGION = "americas"
TIERS = ['IRON', 'BRONZE', 'SILVER', 'GOLD', 'PLATINUM', 'DIAMOND', 'MASTER', 'GRANDMASTER', 'CHALLENGER']


# Gets summoner puuids by tier
def get_summoners_by_tier(tier):
  url = f'https://{REGION}.api.riotgames.com/lol/league/v4/entries/RANKED_SOLO_5x5/{tier}/I?page=1&api_key={API_KEY}'
  response = requests.get(url)
  
  if response.status_code == 200:
    
    data = json.loads(response.content)
  
    puuids = [entry["puuid"] for entry in data]
    return puuids


# Returns A BUNCH of puuids to train with
def puuid_script():
  total = []
  for tier in TIERS:
    summoners = get_summoners_by_tier(tier)
    if summoners:
      total.append(summoners)
      
  if total and isinstance(total[0], list):
    total = [p for sublist in total for p in sublist]
  return total

def get_riot_id_from_puuid(puuid):
  url = f"https://{AMERICAS_REGION}.api.riotgames.com/riot/account/v1/accounts/by-puuid/{puuid}?api_key={API_KEY}"
  res = requests.get(url)
  if res.status_code != 200:
      return None
  data = res.json()
  return data["gameName"], data["tagLine"]


def call_search(riot_id, tag_line):
  res = requests.get(f"{API_BASE}/search/{riot_id}/{tag_line}")
  if res.status_code == 200:
      print(f"✅ Searched {riot_id}#{tag_line}")
      return res.json()["puuid"]
  else:
      print(f"❌ Search failed for {riot_id}#{tag_line}: {res.status_code}")
      return None
      
      
def add_match_data_to_db():
  puuids = puuid_script()
  
  for puuid in puuids:
    result = get_riot_id_from_puuid(puuid)
    
    if not result:
      print(f"⚠️ Skipping PUUID (no Riot ID info found): {puuid}")
      continue

    game_name, tag_line = result
    call_search(game_name, tag_line)

add_match_data_to_db()