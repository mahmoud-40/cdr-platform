# Get admin token
$tokenResponse = Invoke-RestMethod -Uri "http://localhost:8080/auth/realms/master/protocol/openid-connect/token" -Method Post -Body @{
    client_id  = "admin-cli"
    username   = "admin"
    password   = "admin"
    grant_type = "password"
} -ContentType "application/x-www-form-urlencoded"

$token = $tokenResponse.access_token

# Create cdr-platform realm if it doesn't exist
try {
    $realm = Invoke-RestMethod -Uri "http://localhost:8080/auth/admin/realms/cdr-platform" -Headers @{
        "Authorization" = "Bearer $token"
    }
}
catch {
    # Realm doesn't exist, create it
    $realmConfig = @{
        realm           = "cdr-platform"
        enabled         = $true
        displayName     = "CDR Platform"
        displayNameHtml = '<div class="kc-logo-text"><span>CDR Platform</span></div>'
    }

    Invoke-RestMethod -Uri "http://localhost:8080/auth/admin/realms" -Method Post -Headers @{
        "Authorization" = "Bearer $token"
    } -Body ($realmConfig | ConvertTo-Json) -ContentType "application/json"
    Write-Host "Created cdr-platform realm"
}

# Get existing clients
$clients = Invoke-RestMethod -Uri "http://localhost:8080/auth/admin/realms/cdr-platform/clients" -Headers @{
    "Authorization" = "Bearer $token"
}

# Find ms-frontend client
$frontendClient = $clients | Where-Object { $_.clientId -eq "ms-frontend" }

if ($frontendClient) {
    # Update ms-frontend client
    $frontendConfig = @{
        clientId                  = "ms-frontend"
        publicClient              = $true
        redirectUris              = @(
            "http://localhost:8083/*",
            "http://localhost:8083",
            "http://localhost:8083/",
            "http://localhost:8083/index.html",
            "http://localhost:8083/ms-frontend/*"
        )
        webOrigins                = @(
            "http://localhost:8083",
            "http://localhost:8083/",
            "http://localhost:8083/ms-frontend/*"
        )
        standardFlowEnabled       = $true
        implicitFlowEnabled       = $true
        directAccessGrantsEnabled = $true
    }

    Invoke-RestMethod -Uri "http://localhost:8080/auth/admin/realms/cdr-platform/clients/$($frontendClient.id)" -Method Put -Headers @{
        "Authorization" = "Bearer $token"
    } -Body ($frontendConfig | ConvertTo-Json) -ContentType "application/json"
}
else {
    # Create ms-frontend client
    $frontendConfig = @{
        clientId                  = "ms-frontend"
        publicClient              = $true
        redirectUris              = @(
            "http://localhost:8083/*",
            "http://localhost:8083",
            "http://localhost:8083/",
            "http://localhost:8083/index.html",
            "http://localhost:8083/ms-frontend/*"
        )
        webOrigins                = @(
            "http://localhost:8083",
            "http://localhost:8083/",
            "http://localhost:8083/ms-frontend/*"
        )
        standardFlowEnabled       = $true
        implicitFlowEnabled       = $true
        directAccessGrantsEnabled = $true
    }

    Invoke-RestMethod -Uri "http://localhost:8080/auth/admin/realms/cdr-platform/clients" -Method Post -Headers @{
        "Authorization" = "Bearer $token"
    } -Body ($frontendConfig | ConvertTo-Json) -ContentType "application/json"
}

# Find ms-backend client
$backendClient = $clients | Where-Object { $_.clientId -eq "ms-backend" }

if ($backendClient) {
    # Update ms-backend client
    $backendConfig = @{
        clientId                  = "ms-backend"
        publicClient              = $false
        redirectUris              = @(
            "http://localhost:8082/*",
            "http://localhost:8082",
            "http://localhost:8082/",
            "http://localhost:8082/ms-backend/*"
        )
        webOrigins                = @(
            "http://localhost:8082",
            "http://localhost:8082/",
            "http://localhost:8082/ms-backend/*"
        )
        standardFlowEnabled       = $true
        implicitFlowEnabled       = $true
        directAccessGrantsEnabled = $true
        serviceAccountsEnabled    = $true
    }

    Invoke-RestMethod -Uri "http://localhost:8080/auth/admin/realms/cdr-platform/clients/$($backendClient.id)" -Method Put -Headers @{
        "Authorization" = "Bearer $token"
    } -Body ($backendConfig | ConvertTo-Json) -ContentType "application/json"
}
else {
    # Create ms-backend client
    $backendConfig = @{
        clientId                  = "ms-backend"
        publicClient              = $false
        redirectUris              = @(
            "http://localhost:8082/*",
            "http://localhost:8082",
            "http://localhost:8082/",
            "http://localhost:8082/ms-backend/*"
        )
        webOrigins                = @(
            "http://localhost:8082",
            "http://localhost:8082/",
            "http://localhost:8082/ms-backend/*"
        )
        standardFlowEnabled       = $true
        implicitFlowEnabled       = $true
        directAccessGrantsEnabled = $true
        serviceAccountsEnabled    = $true
    }

    Invoke-RestMethod -Uri "http://localhost:8080/auth/admin/realms/cdr-platform/clients" -Method Post -Headers @{
        "Authorization" = "Bearer $token"
    } -Body ($backendConfig | ConvertTo-Json) -ContentType "application/json"
}

Write-Host "Keycloak clients updated successfully!" 