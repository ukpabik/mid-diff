# Mid Diff 🧠

A full-stack League of Legends analytics tool that uses machine learning to analyze player behavior, cluster gameplay styles, and deliver personalized feedback. No login is required; just enter your Riot ID and instantly get ML-powered insights from your recent ranked matches.

---

## 🔍 Features

- 🎮 **Match Analysis Dashboard**
  - View your 20 most recent *ranked* matches.
  - Drill into individual matches for detailed ML-backed insights.

- 🧠 **ML-Based Playstyle Detection**
  - KMeans clustering on normalized match data to detect your dominant playstyles.
  - Personalized feedback using NLP + rule-based logic per cluster.

- 📊 **General and Specific Feedback**
  - Get an overall summary of your playstyle across recent games.
  - Click into any match to see how your performance deviated from your usual patterns.

- 🗃️ **Fully Cached Riot Match Data**
  - Match data is fetched and cached in a Supabase PostgreSQL instance.
  - Data ingestion is performed via Python and served via Spring Boot endpoints.

---

## 🧱 Tech Stack

### Backend
- **Spring Boot** — API server, database access, and Riot API integration.
- **PostgreSQL via Supabase** — stores user and match data.

### Frontend
- **React (TypeScript)** — interactive dashboard, match detail pages.
- **Shadcn UI** — modern component library for sleek, responsive UI.

### Machine Learning
- **Python (Flask)** — microservice for real-time match analysis.
- **scikit-learn** — KMeans clustering and normalization.
- **Pandas** — preprocessing and aggregation.

---

### 🧪 Machine Learning Pipeline

- Elbow method + silhouette score used to find optimal `k`.
+ Optimal cluster count selected using standard evaluation metrics.

---

## 📥 Getting Started (Dev)

```bash
# Frontend
cd frontend
npm install
npm run dev

# Backend (Spring Boot)
./mvnw spring-boot:run

# ML Microservice
cd match-analyzer
pip install -r requirements.txt
flask run
```
---

## About
This project was created by Kelechi Ukpabi (me) <kelechiukp@gmail.com>.

## 📄 Disclaimer

> **Mid Diff** is not endorsed by Riot Games and does not reflect the views or opinions of Riot Games or anyone officially involved in producing or managing Riot Games properties. Riot Games and all associated properties are trademarks or registered trademarks of Riot Games, Inc.
