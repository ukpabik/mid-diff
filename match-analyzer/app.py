from flask import Flask, request, jsonify
from flask_cors import CORS
from model_utils import analyze_match, generate_advice

app = Flask(__name__)
cors = CORS(app)

@app.route('/analyze', methods=['POST'])
def analyze():
  match_data = request.get_json()
  result = analyze_match(match_data)
  updated = generate_advice(result)
  print(updated)
  return jsonify(updated), 200

if __name__ == '__main__':
  app.run()