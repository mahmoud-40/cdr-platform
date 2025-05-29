$clientId = "cdr-client"
$clientSecret = "YOUR_CLIENT_SECRET" # Replace with the client secret you copied
$username = "test-user"
$password = "password"
$tokenUrl = "http://localhost:8081/realms/cdr-realm/protocol/openid-connect/token"

$body = @{
    grant_type = "password"
    client_id = $clientId
    client_secret = $clientSecret
    username = $username
    password = $password
}

$response = Invoke-RestMethod -Uri $tokenUrl -Method Post -Body $body
$response.access_token 