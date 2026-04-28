# Project Plan

Build Sahyadri-Siri, a community-driven water quality monitoring system.
The system includes:
1. Android App: Feature-rich Kotlin app using Jetpack Compose, Google Maps, Room, Retrofit, and Hilt. Features include report submission (GPS + Image), Map view, and AI advisory flash-cards.
2. FastAPI Backend: A Python backend handling reports, map data, alerts, and history. It integrates with Gemini API for GenAI advisories and implements ML logic for water health scoring.
3. Data Flow: User Input -> Backend -> ML + GenAI -> DB -> Android UI.
The goal is to provide a production-ready solution with clean architecture and robust offline support.

## Project Brief

# Sahyadri-Siri: Project Brief

Sahyadri-Siri is a community-driven water quality monitoring system designed to empower users with AI-powered analysis and GenAI-generated safety advisories. The application facilitates environmental stewardship by allowing citizens to report water conditions and receive immediate, actionable insights.

### Features

*   **Smart Report Submission**: Allows users to capture and submit water quality data, including clarity, smell, flow, GPS coordinates, and images for AI-driven analysis.
*   **Interactive Quality Map**: Provides a visual overview of regional water health through an interactive map with color-coded markers based on reported quality levels.
*   **AI Advisories & Flash-Cards**: Delivers GenAI-generated safety explanations and advisories through an intuitive flash-card interface.
*   **Offline Data Support**: Ensures reliability in remote areas by allowing users to create reports offline, which are synchronized once a connection is established.

### High-Level Tech Stack
*   **Language**: Kotlin
*   **UI Framework**: Jetpack Compose (Material 3)
*   **Navigation**: Jetpack Navigation 3 (State-driven)
*   **Adaptive Layouts**: Compose Material Adaptive Library
*   **Asynchronous Logic**: Kotlin Coroutines & Flow
*   **Networking**: Retrofit & OkHttp
*   **Persistence**: Room (for offline support and caching)
*   **Mapping**: Google Maps SDK for Android
*   **Image Handling**: Coil

### Backend Tech Stack
*   **Framework**: FastAPI (Python)
*   **Database**: PostgreSQL or SQLite
*   **AI Integration**: Gemini API for advisories and explanations.
*   **ML Logic**: Health score calculation and anomaly detection.

## Implementation Steps

### Task_1_Infrastructure_and_Data: Setup project infrastructure: add missing dependencies (Hilt, Google Maps), configure Room database for offline support, define Retrofit interfaces for backend communication, and initialize Navigation 3 scaffold. Create core data models for reports and advisories.
- **Status:** COMPLETED
- **Updates:** Task 1 completed:
- **Acceptance Criteria:**
  - Hilt and Google Maps dependencies added and configured
  - Room database and entities defined
  - Retrofit service interface for Sahyadri-Siri backend created
  - Navigation 3 host and basic routing setup
  - Project builds successfully

### Task_2_Reporting_Feature: Implement the Smart Report Submission flow. Integrate CameraX for image capture, FusedLocationProvider for GPS metadata, and a Jetpack Compose form for clarity, smell, and flow details. Implement the repository to handle offline saving to Room.
- **Status:** COMPLETED
- **Updates:** Task 2 completed:
- **Acceptance Criteria:**
  - CameraX integration allows photo capture
  - GPS location is correctly retrieved and attached to reports
  - Report submission UI saves to local database
  - Offline persistence works as expected

### Task_3_Visualization_AI_and_Theme: Develop the Interactive Quality Map and AI Advisory screens. Integrate Google Maps with color-coded markers for water health. Create a Flash-card UI for GenAI advisories. Apply a vibrant Material 3 theme, Full Edge-to-Edge display, and create an adaptive app icon.
- **Status:** COMPLETED
- **Updates:** Task 3 completed:
- **Acceptance Criteria:**
  - Google Maps displays markers based on reported water quality
  - AI Advisory Flash-cards display data correctly
  - Material 3 vibrant theme and Edge-to-Edge implemented
  - Adaptive app icon created and functional

### Task_4_Final_Integration_and_Verify: Integrate all components and verify end-to-end functionality. Perform stability testing to ensure no crashes, verify Material Design 3 compliance, and ensure all user requirements are met.
- **Status:** IN_PROGRESS
- **Updates:** Coder agent has fixed the build and UI issues. Proceeding with final verification on the user's connected device.
- **Acceptance Criteria:**
  - End-to-end data flow (Report -> Map/Advisory) is functional
  - Application is stable and does not crash
  - Build passes and all tests are successful
  - App follows Material Design 3 and full Edge-to-Edge guidelines
- **StartTime:** 2026-04-28 09:08:33 IST

