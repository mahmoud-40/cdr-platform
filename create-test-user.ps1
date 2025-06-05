# Get admin token
$tokenResponse = Invoke-RestMethod -Uri "http://localhost:8080/auth/realms/master/protocol/openid-connect/token" `
    -Method Post `
    -ContentType "application/x-www-form-urlencoded" `
    -Body @{
        username = "admin"
        password = "admin"
        grant_type = "password"
        client_id = "admin-cli"
    }

$adminToken = $tokenResponse.access_token

# Create test user
$user = @{
    username = "testuser"
    enabled = $true
    email = "test@example.com"
    firstName = "Test"
    lastName = "User"
    credentials = @(
        @{
            type = "password"
            value = "testpassword"
            temporary = $false
        }
    )
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/auth/admin/realms/cdr-platform/users" `
    -Method Post `
    -Headers @{
        "Authorization" = "Bearer $adminToken"
        "Content-Type" = "application/json"
    } `
    -Body $user

Write-Host "Test user created successfully!" 