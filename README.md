<div align="center">
  <img src="./app/src/main/res/drawable/logo.png" alt="Sahyadri-Siri Logo" width="200"/>
  <h1>🌊 Sahyadri-Siri</h1>
  <p><strong>Next-Generation Water Quality Monitoring Ecosystem</strong></p>
  
  <p>
    <a href="#-features">Features</a> •
    <a href="#-tech-stack">Tech Stack</a> •
    <a href="#-architecture">Architecture</a> •
    <a href="#-getting-started">Getting Started</a>
  </p>
</div>

---

## ✨ Overview

**Sahyadri-Siri** is a premium, AI-driven Android application designed to crowdsource and monitor water quality across the Sahyadri region. It bridges the gap between community reporting and advanced machine learning to provide real-time, actionable insights into ecological health.

The platform employs a stunning **Glassmorphism-based UI** for maximum user engagement and utilizes **Google Gemini** to generate hyper-contextual environmental advisories based on incoming data streams.

---

## 🚀 Features

- 📱 **Futuristic Dashboard**: A completely redesigned, high-fidelity grid dashboard that provides an immediate overview of regional Water Quality Index (WQI) scores.
- 🤖 **AI-Powered Advisories**: An immersive "shorts-style" vertical pager integrating Google Gemini AI to deliver critical, actionable alerts and predictive environmental warnings.
- 🗺️ **Live Geospatial Mapping**: Real-time visualization of clean, moderate, and polluted water sources across an interactive map.
- 🎨 **Adaptive Premium Theming**: Seamless 3-way theme switching (Light / Dark / System) with frosted glass meshes ensuring pixel-perfect rendering across Android 7 through 11.
- ⚡ **Real-time Timeline Alerts**: A modern, scrollable timeline tracking ecological reports, localized directly to the user's geographic proximity.
- 🌍 **Bilingual Support**: Instantaneous switching between English and Kannada for broader regional accessibility.

---

## 🛠️ Tech Stack

### Frontend (Android Native)
- **Language**: Kotlin 1.9+
- **UI Toolkit**: Jetpack Compose (Material 3)
- **Architecture**: MVVM with Clean Architecture principles
- **Asynchronous Processing**: Kotlin Coroutines & StateFlow
- **Dependency Injection**: Dagger Hilt
- **Local Storage**: Room Database & DataStore Preferences

### Backend (Future Proofing)
- **Framework**: FastAPI (Python 3.10+)
- **Machine Learning**: Scikit-learn (Water Quality Index calculation models)
- **GenAI**: Google Gemini Pro (Automated insight generation)
- **Database**: PostgreSQL with PostGIS for spatial queries

---

## 🏗️ Architecture

Sahyadri-Siri strictly follows a **Clean Architecture** paradigm combined with the **MVVM** pattern:

1.  **UI Layer**: Jetpack Compose screens reacting to `StateFlow` updates.
2.  **ViewModel Layer**: Manages UI state, handles user interactions, and interacts with repositories.
3.  **Data Layer**: Repositories orchestrating data between local caches (Room) and the remote FastAPI endpoints.

---

## 🏁 Getting Started

### Prerequisites
- Android Studio Iguana | 2023.2.1 or newer
- JDK 17
- Minimum SDK: API 24 (Android 7.0 Nougat)

### Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/Kubendra2004/Sahyadiri-Siri.git
   ```
2. Open the project in Android Studio.
3. Sync the Gradle files.
4. Build and run on your Android emulator or physical device via USB debugging.

```bash
# To install a debug APK directly via command line
.\gradlew.bat installDebug
```

---

<div align="center">
  <p>Built with ❤️ for the preservation of the Sahyadri Ecology.</p>
</div>
