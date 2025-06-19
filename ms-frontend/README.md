# ms-frontend (CDR Platform Frontend)

A modern, secure, and user-friendly web application for managing and analyzing Call Detail Records (CDRs) as part of the CDR Platform.

---

## Table of Contents
- [Glossary](#glossary)
- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Authentication](#authentication)
- [API Communication](#api-communication)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [License](#license)

---

## Glossary
- **CDR**: Call Detail Record – a log of a telecom event (call, SMS, or data session).
- **Keycloak**: An open-source identity and access management solution.
- **MUI**: Material-UI, a popular React UI framework.
- **Vite**: A fast frontend build tool and dev server.
- **Axios**: A promise-based HTTP client for the browser.
- **Recharts**: A charting library for React.

---

## Overview
This frontend provides a secure interface for users to authenticate, manage CDR records, and view analytics. It is designed for clarity, responsiveness, and ease of use, and integrates seamlessly with the backend and Keycloak for authentication.

---

## Features
- **Authentication via Keycloak**: Secure login, logout, and session management.
- **CDR Management**: View, create, update, and delete CDR records (with proper permissions).
- **Reporting & Analytics**: Visualize usage per day and per service with interactive charts and tables.
- **Responsive UI**: Works well on desktop and mobile devices.
- **Error Handling**: Uses error boundaries for robust user experience.

---

## Tech Stack
- **React 18 + TypeScript**: Modern, type-safe UI development.
- **Vite**: Fast development and build tooling.
- **Material-UI (MUI)**: For UI components and styling.
- **Keycloak**: For authentication and authorization.
- **Axios**: For HTTP requests to the backend.
- **Recharts**: For data visualization.
- **React Router**: For navigation.
- **Date-fns**: For date formatting and manipulation.

---

## Authentication
- **Keycloak** is used for all authentication and session management.
- Configuration is centralized in [`src/config/keycloak.ts`](src/config/keycloak.ts).
- The app uses `login-required` mode, PKCE, and event handlers for robust session and token management.
- Protected routes are implemented using [`ProtectedRoute.tsx`](src/components/ProtectedRoute.tsx).

---

## API Communication
- **Axios** is used for all HTTP requests to the backend, with logic in [`src/services/api.ts`](src/services/api.ts).
- **Vite Proxy** (see [`vite.config.ts`](vite.config.ts)) forwards `/api` requests to the backend (default: `http://localhost:8082`), avoiding CORS issues.

---

## Project Structure
- `src/components/` — Reusable UI components (CDR list, analytics, protected routes, etc.)
- `src/pages/` — Page-level components (e.g., Login)
- `src/services/` — API service layer (handles HTTP requests)
- `src/types/` — TypeScript type definitions
- `src/config/` — App configuration (Keycloak, etc.)
- `src/assets/` — Static assets (images, icons, etc.)

**Main Components:**
- `CdrList.tsx`: Table of CDRs with CRUD operations.
- `CdrFormDialog.tsx`: Modal for creating/editing CDRs.
- `AnalyticsDashboard.tsx`: Aggregated usage data and charts.
- `UsageReport.tsx`: Detailed usage reporting.
- `ProtectedRoute.tsx`: Restricts access to authenticated users.

---

## Getting Started

To set up and run the frontend locally:

### Prerequisites
- Node.js 18+
- Yarn or npm

### Install dependencies
```bash
npm install
# or
yarn install
```

### Run in development mode
```bash
npm run dev
# or
yarn dev
```
The app will be available at [http://localhost:8083](http://localhost:8083)

### Build for production
```bash
npm run build
# or
yarn build
```

### Lint the code
```bash
npm run lint
# or
yarn lint
```

### Preview the production build
```bash
npm run preview
# or
yarn preview
```

---

## Configuration
- **Keycloak**: Set up in [`src/config/keycloak.ts`](src/config/keycloak.ts). Adjust the URL, realm, and clientId as needed for your Keycloak server.
- **API Proxy**: Configured in [`vite.config.ts`](vite.config.ts) to forward `/api` requests to the backend, avoiding CORS issues.
- **Environment variables** can be added in a `.env` file for further customization.

---

## License

This project is licensed under the MIT License - see the LICENSE file for details. 