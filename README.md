# Cadence

Cadence is a modern Android application built to explore personal Spotify listening statistics and discover new music. This project serves as a practical exercise in building a feature-rich app using the latest Android development technologies, including Jetpack Compose and the official Spotify Android SDK.  

## ✨ Features

- Secure Spotify Login: Authenticate using the official Spotify Android SDK for a seamless and secure login experience. The app gracefully falls back to a web-based flow if the Spotify app is not installed.

  ...  (to be added)
  
## 🛠️ Tech Stack & Architecture

This project follows modern Android architecture guidelines and leverages a suite of powerful libraries:

    UI: 100% Kotlin with Jetpack Compose and Material 3 expressive for a responsive user interface.

    Architecture: Follows the MVVM (Model-View-ViewModel) pattern.

    Dependency Injection: Hilt for managing dependencies and decoupling components.

    Networking: Retrofit 2 for type-safe HTTP calls to the Spotify API, with OkHttp as the HTTP client.

    Asynchronous Programming: Kotlin Coroutines and Flow for managing background threads and data streams.

    Navigation: Jetpack Navigation Compose for navigating between screens.

    Authentication: Spotify Android SDK for a native authentication flow.

## 🚀 Setup

To build and run this project yourself, you will need to provide your own Spotify API credentials:

    Get Credentials: Go to the Spotify Developer Dashboard and create a new application to get your Client ID and Client Secret.

    Configure Your App: In the Spotify Dashboard settings for your app, add the following Redirect URI: cadence-app://callback.

    Add Your Keys: Add your credentials to the local.properties file like this:

    SPOTIFY_CLIENT_ID=your_spotify_client_id_here
    SPOTIFY_CLIENT_SECRET=your_spotify_client_secret_here

    Sync the project in Android Studio, and you should be ready to build and run the app.

## 🚧 Project Status

This project is currently a work in progress.

Phase 1: Foundation & Authentication (Complete)

    Project setup with Hilt and MVVM.

    Secure Spotify login and session persistence.

Phase 2 - "Statify" Features (In Progress)

    Fetch and display user's top tracks and artists.

    Implement caching with the Room persistence library to support offline viewing and improve performance.
