from flask import Flask, request, jsonify
from flask_cors import CORS
from model_utils import analyze_match, generate_advice

app = Flask(__name__)
cors = CORS(app)

@app.route('/analyze', methods=['POST'])
def analyze():
  match_data = request.get_json()
  print(match_data)
  result = analyze_match(match_data)
  build_items = match_data.get("buildItemNames", [])
  position = match_data.get("teamPosition")
  playerRank = match_data.get("playerRank")
  role = match_data.get("role")
  champName = match_data.get("championName")
  updated = generate_advice(result, build_items, position, playerRank, role, champName)
  return jsonify(updated), 200

if __name__ == '__main__':
  app.run()
  