# Getting Started

This documentation provides an overview of the API and guides you through setting up and using the application.

## Overview

This application is designed to integrate with the HH API to manage resumes and vacancies through OAuth authentication. The application allows you to start and stop monitoring vacancies based on resumes and handles OAuth token management.

## Table of Contents

- [Getting Started](#getting-started)
- [API Endpoints](#api-endpoints)
    - [Monitoring API](#monitoring-api)
    - [OAuth API](#oauth-api)
    - [Token API](#token-api)
- [Reference Documentation](#reference-documentation)
- [Guides](#guides)
- [Additional Links](#additional-links)

## Setup

### Prerequisites

- Java 21 or higher
- Gradle 8.x or higher

### Build and Run the Application

1. Clone the repository:
    ```bash
    git clone <repository-url>
    cd <project-directory>
    ```

2. Build the application:
    ```bash
    ./gradlew build
    ```

3. Run the application:
    ```bash
    ./gradlew bootRun
    ```

The application will start on the default port (8080). You can access it at `http://localhost:8080`.

## API Endpoints

### Monitoring API

- **Start Monitoring Vacancies**

  **Endpoint:** `GET /start_monitoring`  
  **Description:** Starts monitoring vacancies for the specified resume ID and refreshes the OAuth access token.  
  **Responses:**
    - `200 OK` - Monitoring started successfully.
    - `400 BAD REQUEST` - Error occurred while starting monitoring.

- **Stop Monitoring Vacancies**

  **Endpoint:** `GET /stop_monitoring`  
  **Description:** Stops monitoring vacancies and stops refreshing the OAuth access token.  
  **Responses:**
    - `200 OK` - Monitoring stopped successfully.
    - `400 BAD REQUEST` - Error occurred while stopping monitoring.

### OAuth API

- **Get OAuth URL**

  **Endpoint:** `GET /oauht_url`  
  **Description:** Returns the OAuth URL for user authentication.  
  **Responses:**
    - `200 OK` - Returns the OAuth URL.

- **OAuth Callback**

  **Endpoint:** `GET /callback?code={code}`  
  **Description:** Handles the OAuth callback with the provided code.  
  **Responses:**
    - `200 OK` - Callback processed successfully.
    - `400 BAD REQUEST` - Error occurred during the callback processing.

### Token API

- **Register Token**

  **Endpoint:** `PUT /register`  
  **Description:** Registers the OAuth token for the specified user ID.  
  **Headers:**
    - `Authorization: Bearer {token}`
    - `User-ID: {userId}`
      **Responses:**
    - `200 OK` - Token registered successfully.
    - `400 BAD REQUEST` - Error occurred while registering the token.

## Reference Documentation

For further reference, please consider the following sections:

- [Official Gradle documentation](https://docs.gradle.org)
- [Spring Boot Gradle Plugin Reference Guide](https://docs.spring.io/spring-boot/3.3.4/gradle-plugin)
- [Create an OCI image](https://docs.spring.io/spring-boot/3.3.4/gradle-plugin/packaging-oci-image.html)
- [codecentric's Spring Boot Admin (Server)](https://codecentric.github.io/spring-boot-admin/current/#getting-started)

## Guides

The following guides illustrate how to use some features concretely:

- [Spring Boot and OAuth2](https://spring.io/guides/tutorials/spring-boot-oauth2/)
- [Authenticating a User with LDAP](https://spring.io/guides/gs/authenticating-ldap/)

## Additional Links

These additional references should also help you:

- [Gradle Build Scans – insights for your project's build](https://scans.gradle.com#gradle)
