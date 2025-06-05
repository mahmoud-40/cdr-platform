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
let keycloakInstance: Keycloak | null = null;

export const getKeycloakInstance = () => {
    if (!keycloakInstance) {
        keycloakInstance = new Keycloak(keycloakConfig);
    }
    return keycloakInstance;
};

// Add error handling
getKeycloakInstance().onAuthError = () => {
    console.error('Keycloak auth error');
};

getKeycloakInstance().onAuthRefreshError = () => {
    console.error('Keycloak refresh error');
};

getKeycloakInstance().onAuthRefreshSuccess = () => {
    console.log('Keycloak refresh success');
};

getKeycloakInstance().onAuthSuccess = () => {
    console.log('Keycloak auth success');
};

getKeycloakInstance().onAuthLogout = () => {
    console.log('Keycloak logout');
};

getKeycloakInstance().onReady = (authenticated) => {
    console.log('Keycloak ready:', { authenticated });
};

getKeycloakInstance().onTokenExpired = () => {
    console.log('Keycloak token expired');
};

// Export the instance getter
export default getKeycloakInstance(); 