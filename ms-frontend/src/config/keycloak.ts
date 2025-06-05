import Keycloak from 'keycloak-js';

const keycloakConfig = {
    url: 'http://localhost:8080/auth',
    realm: 'cdr-platform',
    clientId: 'ms-frontend',
    onLoad: 'login-required',
    silentCheckSsoRedirectUri: window.location.origin + '/silent-check-sso.html',
    pkceMethod: 'S256',
    checkLoginIframe: false,
    enableLogging: true,
    flow: 'standard',
    responseMode: 'query',
    scope: 'openid profile email'
};

// Create a single instance of Keycloak
const keycloak = new Keycloak(keycloakConfig);

// Add error handling
keycloak.onAuthError = () => {
    console.error('Keycloak auth error');
};

keycloak.onAuthRefreshError = () => {
    console.error('Keycloak refresh error');
};

keycloak.onAuthRefreshSuccess = () => {
    console.log('Keycloak refresh success');
};

keycloak.onAuthSuccess = () => {
    console.log('Keycloak auth success');
};

keycloak.onAuthLogout = () => {
    console.log('Keycloak logout');
};

keycloak.onReady = (authenticated) => {
    console.log('Keycloak ready:', { authenticated });
};

keycloak.onTokenExpired = () => {
    console.log('Keycloak token expired');
};

// Export the instance
export default keycloak; 