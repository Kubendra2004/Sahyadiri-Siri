<div align="center">
  <img src="./app/src/main/res/drawable/logo.png" alt="Sahyadri-Siri Logo" width="220"/>

# 🌊 Sahyadri-Siri

### **AI-Powered Water Quality Monitoring & Early Warning System**

<p>
  <strong>Next-Generation Environmental Intelligence Platform</strong>
</p>

<p>
  <a href="#-overview">Overview</a> •
  <a href="#-features">Features</a> •
  <a href="#-tech-stack">Tech Stack</a> •
  <a href="#-architecture">Architecture</a> •
  <a href="#-project-structure">Project Structure</a> •
  <a href="#-getting-started">Getting Started</a>
</p>

</div>

---

# ✨ Overview

**Sahyadri-Siri** is a premium AI-driven water quality monitoring ecosystem designed to crowdsource, analyze, and visualize ecological health data across environmentally sensitive regions.

The platform combines:

* 📱 Modern Android development
* 🤖 Machine Learning
* 🧠 Generative AI (Google Gemini)
* 🗺️ Geospatial visualization
* ⚡ Real-time alerts
* ☁️ Scalable cloud-ready backend infrastructure

to create a smart and accessible environmental monitoring solution.

The application enables users to report water conditions using simple observations such as clarity, smell, flow, images, and GPS location. The backend processes this data using Water Quality Index (WQI) models and Gemini-powered advisory generation to provide contextual alerts and environmental insights.

---

# 🚀 Features

## 📱 Android Application

* 🎨 **Premium Glassmorphism UI**

  * Frosted glass cards
  * Material 3 theming
  * Rounded modern design
  * Adaptive dark/light mode

* 🗺️ **Live Water Quality Mapping**

  * Real-time geospatial visualization
  * Clean / Moderate / Polluted indicators
  * Interactive Google Maps integration

* 🤖 **AI-Powered Advisory Feed**

  * Shorts-style swipeable vertical cards
  * Gemini-generated environmental insights
  * Predictive warnings & recommendations

* ⚡ **Real-Time Alerts**

  * Timeline-based ecological alerts
  * Location-aware notifications
  * Water contamination warnings

* 🌍 **Bilingual Support**

  * English + Kannada language support
  * Accessible regional-first design

* 💾 **Offline Support**

  * Room Database caching
  * DataStore preferences
  * Offline-first architecture

---

# 🧠 AI & Generative AI Features

* 📊 Water Quality Index (WQI) prediction
* 🔍 Pollution anomaly detection
* 🤖 Google Gemini advisory generation
* 📝 AI-generated explanations & alerts
* 🌊 Context-aware environmental recommendations

---

# 🛠️ Tech Stack

## 📱 Frontend (Android Native)

| Component            | Technology                   |
| -------------------- | ---------------------------- |
| Language             | Kotlin 1.9+                  |
| UI Toolkit           | Jetpack Compose (Material 3) |
| Architecture         | MVVM + Clean Architecture    |
| State Management     | StateFlow + Coroutines       |
| Dependency Injection | Dagger Hilt                  |
| Local Storage        | Room DB + DataStore          |
| Networking           | Retrofit + OkHttp            |
| Maps                 | Google Maps SDK              |
| Image Loading        | Coil                         |
| Background Tasks     | WorkManager                  |

---

## ⚙️ Backend Infrastructure

| Component        | Technology                   |
| ---------------- | ---------------------------- |
| Framework        | FastAPI + Uvicorn            |
| Language         | Python 3.11+                 |
| ORM              | SQLAlchemy Async             |
| Migrations       | Alembic                      |
| Database         | PostgreSQL 16                |
| Cache            | Redis                        |
| Authentication   | JWT + python-jose            |
| Storage          | Firebase Storage / Amazon S3 |
| Containerization | Docker                       |

---

## 🤖 AI / ML Stack

| Component         | Technology        |
| ----------------- | ----------------- |
| WQI Prediction    | Scikit-learn      |
| Generative AI     | Google Gemini Pro |
| Advisory Engine   | Gemini API        |
| Spatial Analytics | PostGIS           |

---

# 🏗️ Architecture

Sahyadri-Siri follows a scalable **Clean Architecture** pattern combined with **MVVM** principles.

```text
Android Application
        ↓
ViewModel Layer
        ↓
Repository Layer
        ↓
FastAPI Backend
        ↓
ML + Gemini AI Engine
        ↓
PostgreSQL + Redis
        ↓
Alerts & Analytics
```

---

# 🔁 AI Pipeline

```text
User Report
(clarity + smell + flow + image + GPS)
        ↓
Data Validation
        ↓
WQI Prediction Model
        ↓
Anomaly Detection
        ↓
Google Gemini Processing
        ↓
AI Advisory Generation
        ↓
Structured JSON Response
        ↓
Android UI Rendering
```

---

# 📂 Project Structure

```bash
Sahyadri-Siri/
│
├── app/                         # Android application
│
├── backend/
│   ├── src/sahyadri_siri/
│   │   ├── routers/             # HTTP & WebSocket routes
│   │   ├── services/            # Auth, Gemini, WQI, cache services
│   │   ├── middleware/          # Logging & rate limiting
│   │   ├── ml/                  # ML model logic
│   │   └── main.py              # FastAPI entry point
│   │
│   ├── alembic/                 # Database migrations
│   ├── tests/                   # Pytest test cases
│   └── docker-compose.yml
│
└── README.md
```

---

# ⚙️ Environment Variables

Create a `.env` file inside the backend directory.

```env
DATABASE_URL=
REDIS_URL=
JWT_SECRET=
GEMINI_API_KEY=
STORAGE_PROVIDER=
```

---

# 🏁 Getting Started

## 📱 Android Setup

### Requirements

* Android Studio Iguana or newer
* JDK 17
* Minimum SDK: API 24

### Installation

```bash
git clone https://github.com/Kubendra2004/Sahyadiri-Siri.git
```

Open the project in Android Studio and sync Gradle files.

Run the application on:

* Android Emulator
* Physical Device (USB Debugging)

---

# ⚙️ Backend Setup

```bash
cd backend

python -m venv .venv

.venv\Scripts\activate

pip install -e .[dev]

alembic upgrade head

uvicorn sahyadri_siri.main:app --reload --app-dir src
```

---

# 🐳 Docker Setup

```bash
cd backend

docker compose up --build
```

Run migrations:

```bash
docker compose exec api alembic upgrade head
```

---

# 🧪 WQI Model Training

Train and serialize the Water Quality Index model:

```bash
python src/sahyadri_siri/ml/train_wqi.py
```

---

# 📡 API Notes

* All routes are mounted under `/api`
* JSON fields use camelCase
* Report timestamps use epoch milliseconds
* `GET /api/health` is the only public endpoint

---

# 🌱 Future Enhancements

* IoT sensor integration
* Predictive contamination analytics
* Government monitoring dashboard
* Weather-aware recommendations
* Community moderation system

---

<div align="center">

### 🌊 Built with ❤️ for the Preservation of the Sahyadri Ecology

</div>
