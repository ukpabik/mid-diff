from flask import Flask, request, jsonify
from model_utils import analyze_match

app = Flask(__name__)

@app.route('/analyze', methods=['POST'])
def analyze():
  match_data = request.get_json()
  result = analyze_match(match_data)
  return jsonify(result)

if __name__ == '__main__':
  app.run()