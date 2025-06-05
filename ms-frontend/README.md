# ms-frontend (CDR Platform Frontend)

This is the frontend application for the CDR Platform. It provides a modern, secure, and user-friendly interface for managing and analyzing Call Detail Records (CDRs).

## Features

- **Authentication via Keycloak**: Secure login/logout and session management.
- **CDR Management**: View, create, update, and delete CDR records (with proper permissions).
- **Reporting & Analytics**: Visualize usage per day and per service, with interactive charts and tables.
- **Responsive UI**: Built with React, MUI, and Vite for fast, modern user experience.

## Tech Stack
- React 18 + TypeScript
- Vite
- Material-UI (MUI)
- Keycloak (via keycloak-js and @react-keycloak/web)
- Axios
- Recharts (for analytics)

## Getting Started

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

## Environment & Configuration
- **Keycloak**: Configured in `src/config/keycloak.ts`
- **API Proxy**: Configured in `vite.config.ts` to forward `/api` requests to the backend

## Project Structure
- `src/components/` — UI components (CDR list, analytics, protected routes, etc.)
- `src/pages/` — Page-level components (Login, etc.)
- `src/services/` — API service layer
- `src/types/` — TypeScript types
- `src/config/` — App configuration (Keycloak, etc.)

## License
MIT
