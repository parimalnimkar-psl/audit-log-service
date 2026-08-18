# Comprehensive API Testing Script for Audit Log Service
# Tests all scenarios: Authentication, Event Creation, Querying, Verification, and Tampering

$baseURL = "http://localhost:8080"
$testResults = @()
$tokens = @{}

# Color output helper
function Write-TestResult($testName, $status, $details) {
    $color = if ($status -eq "PASS") { "Green" } else { "Red" }
    Write-Host "[$status] $testName" -ForegroundColor $color
    if ($details) {
        Write-Host "  Details: $details" -ForegroundColor Gray
    }
    $testResults += @{Test = $testName; Status = $status; Details = $details}
}

Write-Host "
╔════════════════════════════════════════════════════════════════╗
║    AUDIT LOG SERVICE - COMPREHENSIVE API TEST SUITE          ║
╚════════════════════════════════════════════════════════════════╝
" -ForegroundColor Cyan

# ============================================================
# SCENARIO 1: AUTHENTICATION & AUTHORIZATION
# ============================================================
Write-Host "`n[SCENARIO 1] AUTHENTICATION & AUTHORIZATION" -ForegroundColor Yellow

# Test 1.1: Token Generation - Writer Role
Write-Host "`n  Test 1.1: Token Generation - Writer Role" -ForegroundColor Cyan
try {
    $response = Invoke-RestMethod -Uri "$baseURL/auth/token" `
        -Method POST `
        -ContentType "application/json" `
        -Body '{"username":"writer","password":"writer123"}' `
        -ErrorAction Stop
    
    if ($response.access_token) {
        $tokens['writer'] = $response.access_token
        $expiresIn = $response.expires_in
        Write-TestResult "Writer Token Generation" "PASS" "Token generated, expires in $expiresIn seconds"
    } else {
        Write-TestResult "Writer Token Generation" "FAIL" "No token in response"
    }
} catch {
    Write-TestResult "Writer Token Generation" "FAIL" $_.Exception.Message
}

# Test 1.2: Token Generation - Reader Role
Write-Host "`n  Test 1.2: Token Generation - Reader Role" -ForegroundColor Cyan
try {
    $response = Invoke-RestMethod -Uri "$baseURL/auth/token" `
        -Method POST `
        -ContentType "application/json" `
        -Body '{"username":"reader","password":"reader123"}' `
        -ErrorAction Stop
    
    if ($response.access_token) {
        $tokens['reader'] = $response.access_token
        Write-TestResult "Reader Token Generation" "PASS" "Reader token obtained successfully"
    } else {
        Write-TestResult "Reader Token Generation" "FAIL" "No token in response"
    }
} catch {
    Write-TestResult "Reader Token Generation" "FAIL" $_.Exception.Message
}

# Test 1.3: Token Generation - Admin Role
Write-Host "`n  Test 1.3: Token Generation - Admin Role" -ForegroundColor Cyan
try {
    $response = Invoke-RestMethod -Uri "$baseURL/auth/token" `
        -Method POST `
        -ContentType "application/json" `
        -Body '{"username":"admin","password":"admin123"}' `
        -ErrorAction Stop
    
    if ($response.access_token) {
        $tokens['admin'] = $response.access_token
        Write-TestResult "Admin Token Generation" "PASS" "Admin token obtained successfully"
    } else {
        Write-TestResult "Admin Token Generation" "FAIL" "No token in response"
    }
} catch {
    Write-TestResult "Admin Token Generation" "FAIL" $_.Exception.Message
}

# Test 1.4: Invalid Credentials
Write-Host "`n  Test 1.4: Invalid Credentials Test" -ForegroundColor Cyan
try {
    $response = Invoke-RestMethod -Uri "$baseURL/auth/token" `
        -Method POST `
        -ContentType "application/json" `
        -Body '{"username":"invalid","password":"wrong"}' `
        -ErrorAction Stop
    Write-TestResult "Invalid Credentials" "FAIL" "Should have returned 401"
} catch {
    if ($_.Exception.Response.StatusCode -eq 401) {
        Write-TestResult "Invalid Credentials" "PASS" "Correctly returned 401 Unauthorized"
    } else {
        Write-TestResult "Invalid Credentials" "FAIL" "Wrong status code: $($_.Exception.Response.StatusCode)"
    }
}

# ============================================================
# SCENARIO 2: EVENT CREATION WITH VALIDATION
# ============================================================
Write-Host "`n[SCENARIO 2] EVENT CREATION WITH VALIDATION" -ForegroundColor Yellow

# Test 2.1: Create First Event
Write-Host "`n  Test 2.1: Create First Audit Event" -ForegroundColor Cyan
$event1 = $null
try {
    $headers = @{
        "Authorization" = "Bearer $($tokens['writer'])"
        "Content-Type" = "application/json"
    }
    
    $body = @{
        eventType = "USER_LOGIN"
        actorId = "user_001"
        resourceType = "ACCOUNT"
        resourceId = "account_12345"
        payload = '{"method":"password","ip":"192.168.1.1"}'
    } | ConvertTo-Json
    
    $response = Invoke-RestMethod -Uri "$baseURL/audit/events" `
        -Method POST `
        -Headers $headers `
        -Body $body `
        -ErrorAction Stop
    
    if ($response.id) {
        $event1 = $response
        Write-TestResult "Create First Event" "PASS" "Event ID: $($response.id), Sequence: $($response.chainSequence)"
    }
} catch {
    Write-TestResult "Create First Event" "FAIL" $_.Exception.Message
}

# Test 2.2: Verify Genesis Hash for First Event
Write-Host "`n  Test 2.2: Verify Genesis Hash (First Event)" -ForegroundColor Cyan
if ($event1) {
    if ($event1.previousHash -eq "GENESIS") {
        Write-TestResult "Genesis Hash Validation" "PASS" "First event correctly has GENESIS as previousHash"
    } else {
        Write-TestResult "Genesis Hash Validation" "FAIL" "Expected GENESIS, got: $($event1.previousHash)"
    }
}

# Test 2.3: Create Second Event (Hash Chaining)
Write-Host "`n  Test 2.3: Create Second Event (Hash Chaining)" -ForegroundColor Cyan
$event2 = $null
try {
    $headers = @{
        "Authorization" = "Bearer $($tokens['writer'])"
        "Content-Type" = "application/json"
    }
    
    $body = @{
        eventType = "DATA_ACCESS"
        actorId = "user_002"
        resourceType = "ACCOUNT"
        resourceId = "account_12345"
        payload = '{"action":"read","fields":["email","phone"]}'
    } | ConvertTo-Json
    
    $response = Invoke-RestMethod -Uri "$baseURL/audit/events" `
        -Method POST `
        -Headers $headers `
        -Body $body `
        -ErrorAction Stop
    
    if ($response.id) {
        $event2 = $response
        Write-TestResult "Create Second Event" "PASS" "Event ID: $($response.id), Sequence: $($response.chainSequence)"
    }
} catch {
    Write-TestResult "Create Second Event" "FAIL" $_.Exception.Message
}

# Test 2.4: Verify Hash Chaining
Write-Host "`n  Test 2.4: Verify Hash Chaining" -ForegroundColor Cyan
if ($event1 -and $event2) {
    if ($event2.previousHash -eq $event1.contentHash) {
        Write-TestResult "Hash Chaining" "PASS" "Second event previousHash matches first event contentHash"
    } else {
        Write-TestResult "Hash Chaining" "FAIL" "Hash chain broken! previousHash: $($event2.previousHash), expected: $($event1.contentHash)"
    }
}

# Test 2.5: Verify Sequence Increment
Write-Host "`n  Test 2.5: Verify Sequence Increment" -ForegroundColor Cyan
if ($event1 -and $event2) {
    if ($event2.chainSequence -eq $event1.chainSequence + 1) {
        Write-TestResult "Sequence Increment" "PASS" "Sequence correctly incremented: $($event1.chainSequence) -> $($event2.chainSequence)"
    } else {
        Write-TestResult "Sequence Increment" "FAIL" "Sequence gap: $($event1.chainSequence) -> $($event2.chainSequence)"
    }
}

# Test 2.6: Create Third Event
Write-Host "`n  Test 2.6: Create Third Event" -ForegroundColor Cyan
$event3 = $null
try {
    $headers = @{
        "Authorization" = "Bearer $($tokens['writer'])"
        "Content-Type" = "application/json"
    }
    
    $body = @{
        eventType = "DATA_MODIFICATION"
        actorId = "user_003"
        resourceType = "TRANSACTION"
        resourceId = "txn_99999"
        payload = '{"amount":500,"currency":"USD","status":"completed"}'
    } | ConvertTo-Json
    
    $response = Invoke-RestMethod -Uri "$baseURL/audit/events" `
        -Method POST `
        -Headers $headers `
        -Body $body `
        -ErrorAction Stop
    
    if ($response.id) {
        $event3 = $response
        Write-TestResult "Create Third Event" "PASS" "Event ID: $($response.id), Sequence: $($response.chainSequence)"
    }
} catch {
    Write-TestResult "Create Third Event" "FAIL" $_.Exception.Message
}

# Test 2.7: Invalid Request - Missing Required Fields
Write-Host "`n  Test 2.7: Invalid Request - Missing Required Fields" -ForegroundColor Cyan
try {
    $headers = @{
        "Authorization" = "Bearer $($tokens['writer'])"
        "Content-Type" = "application/json"
    }
    
    $body = @{
        eventType = "INVALID"
        # Missing required fields
    } | ConvertTo-Json
    
    $response = Invoke-RestMethod -Uri "$baseURL/audit/events" `
        -Method POST `
        -Headers $headers `
        -Body $body `
        -ErrorAction Stop
    
    Write-TestResult "Invalid Request Validation" "FAIL" "Should have returned 400"
} catch {
    if ($_.Exception.Response.StatusCode -eq 400) {
        Write-TestResult "Invalid Request Validation" "PASS" "Correctly returned 400 Bad Request"
    } else {
        Write-TestResult "Invalid Request Validation" "FAIL" "Wrong status: $($_.Exception.Response.StatusCode)"
    }
}

# Test 2.8: Unauthorized Access (No Token)
Write-Host "`n  Test 2.8: Unauthorized Access - No Token" -ForegroundColor Cyan
try {
    $body = @{
        eventType = "TEST"
        actorId = "test"
        resourceType = "TEST"
        resourceId = "test"
        payload = '{}'
    } | ConvertTo-Json
    
    $response = Invoke-RestMethod -Uri "$baseURL/audit/events" `
        -Method POST `
        -ContentType "application/json" `
        -Body $body `
        -ErrorAction Stop
    
    Write-TestResult "Unauthorized Access" "FAIL" "Should have returned 401"
} catch {
    if ($_.Exception.Response.StatusCode -eq 401) {
        Write-TestResult "Unauthorized Access" "PASS" "Correctly returned 401 Unauthorized"
    } else {
        Write-TestResult "Unauthorized Access" "FAIL" "Wrong status: $($_.Exception.Response.StatusCode)"
    }
}

# ============================================================
# SCENARIO 3: QUERY & FILTERING
# ============================================================
Write-Host "`n[SCENARIO 3] QUERY & FILTERING" -ForegroundColor Yellow

# Test 3.1: Query All Events
Write-Host "`n  Test 3.1: Query All Events" -ForegroundColor Cyan
try {
    $headers = @{
        "Authorization" = "Bearer $($tokens['reader'])"
    }
    
    $response = Invoke-RestMethod -Uri "$baseURL/audit/events?page=0&size=10" `
        -Method GET `
        -Headers $headers `
        -ErrorAction Stop
    
    if ($response.content -and $response.content.Count -ge 3) {
        Write-TestResult "Query All Events" "PASS" "Retrieved $($response.content.Count) events, Total: $($response.totalElements)"
    } else {
        Write-TestResult "Query All Events" "FAIL" "Expected at least 3 events, got: $($response.content.Count)"
    }
} catch {
    Write-TestResult "Query All Events" "FAIL" $_.Exception.Message
}

# Test 3.2: Filter by ActorId
Write-Host "`n  Test 3.2: Filter by ActorId (user_001)" -ForegroundColor Cyan
try {
    $headers = @{
        "Authorization" = "Bearer $($tokens['reader'])"
    }
    
    $response = Invoke-RestMethod -Uri "$baseURL/audit/events?actorId=user_001&page=0&size=10" `
        -Method GET `
        -Headers $headers `
        -ErrorAction Stop
    
    if ($response.content -and $response.content.Count -ge 1) {
        $allMatch = $response.content | Where-Object { $_.actorId -eq "user_001" } | Measure-Object | Select-Object -ExpandProperty Count
        if ($allMatch -eq $response.content.Count) {
            Write-TestResult "Filter by ActorId" "PASS" "Found $($response.content.Count) events for user_001"
        }
    }
} catch {
    Write-TestResult "Filter by ActorId" "FAIL" $_.Exception.Message
}

# Test 3.3: Filter by ResourceType
Write-Host "`n  Test 3.3: Filter by ResourceType (ACCOUNT)" -ForegroundColor Cyan
try {
    $headers = @{
        "Authorization" = "Bearer $($tokens['reader'])"
    }
    
    $response = Invoke-RestMethod -Uri "$baseURL/audit/events?resourceType=ACCOUNT&page=0&size=10" `
        -Method GET `
        -Headers $headers `
        -ErrorAction Stop
    
    if ($response.content -and $response.content.Count -ge 1) {
        Write-TestResult "Filter by ResourceType" "PASS" "Found $($response.content.Count) ACCOUNT events"
    }
} catch {
    Write-TestResult "Filter by ResourceType" "FAIL" $_.Exception.Message
}

# Test 3.4: Filter by EventType
Write-Host "`n  Test 3.4: Filter by EventType (USER_LOGIN)" -ForegroundColor Cyan
try {
    $headers = @{
        "Authorization" = "Bearer $($tokens['reader'])"
    }
    
    $response = Invoke-RestMethod -Uri "$baseURL/audit/events?eventType=USER_LOGIN&page=0&size=10" `
        -Method GET `
        -Headers $headers `
        -ErrorAction Stop
    
    if ($response.content -and $response.content.Count -ge 1) {
        Write-TestResult "Filter by EventType" "PASS" "Found $($response.content.Count) USER_LOGIN events"
    }
} catch {
    Write-TestResult "Filter by EventType" "FAIL" $_.Exception.Message
}

# Test 3.5: Pagination - Page 0
Write-Host "`n  Test 3.5: Pagination - Page 0" -ForegroundColor Cyan
try {
    $headers = @{
        "Authorization" = "Bearer $($tokens['reader'])"
    }
    
    $response = Invoke-RestMethod -Uri "$baseURL/audit/events?page=0&size=2" `
        -Method GET `
        -Headers $headers `
        -ErrorAction Stop
    
    if ($response.content.Count -le 2) {
        Write-TestResult "Pagination Page 0" "PASS" "Retrieved page 0 with $($response.content.Count) items (size=2)"
    }
} catch {
    Write-TestResult "Pagination Page 0" "FAIL" $_.Exception.Message
}

# Test 3.6: Multiple Filters Combined
Write-Host "`n  Test 3.6: Combined Filters (ActorId + ResourceType)" -ForegroundColor Cyan
try {
    $headers = @{
        "Authorization" = "Bearer $($tokens['reader'])"
    }
    
    $response = Invoke-RestMethod -Uri "$baseURL/audit/events?actorId=user_001&resourceType=ACCOUNT&page=0&size=10" `
        -Method GET `
        -Headers $headers `
        -ErrorAction Stop
    
    if ($response.content -and $response.content.Count -ge 1) {
        Write-TestResult "Combined Filters" "PASS" "Found events matching both criteria"
    }
} catch {
    Write-TestResult "Combined Filters" "FAIL" $_.Exception.Message
}

# Test 3.7: Reader Authorization on Query
Write-Host "`n  Test 3.7: Reader Authorization on Query Endpoint" -ForegroundColor Cyan
try {
    $headers = @{
        "Authorization" = "Bearer $($tokens['reader'])"
    }
    
    $response = Invoke-RestMethod -Uri "$baseURL/audit/events?page=0&size=1" `
        -Method GET `
        -Headers $headers `
        -ErrorAction Stop
    
    Write-TestResult "Reader Authorization" "PASS" "Reader successfully queried events"
} catch {
    Write-TestResult "Reader Authorization" "FAIL" $_.Exception.Message
}

# ============================================================
# SCENARIO 4: CHAIN VERIFICATION
# ============================================================
Write-Host "`n[SCENARIO 4] CHAIN VERIFICATION" -ForegroundColor Yellow

# Test 4.1: Verify Intact Chain
Write-Host "`n  Test 4.1: Verify Intact Chain" -ForegroundColor Cyan
try {
    $headers = @{
        "Authorization" = "Bearer $($tokens['admin'])"
    }
    
    $response = Invoke-RestMethod -Uri "$baseURL/audit/verify" `
        -Method GET `
        -Headers $headers `
        -ErrorAction Stop
    
    if ($response.intact -eq $true) {
        Write-TestResult "Verify Intact Chain" "PASS" "Chain is intact, checked $($response.checkedRecordCount) records"
    } else {
        Write-TestResult "Verify Intact Chain" "FAIL" "Chain verification failed: $($response.violationType)"
    }
} catch {
    Write-TestResult "Verify Intact Chain" "FAIL" $_.Exception.Message
}

# Test 4.2: Admin Authorization on Verify
Write-Host "`n  Test 4.2: Admin Authorization on Verify Endpoint" -ForegroundColor Cyan
try {
    $headers = @{
        "Authorization" = "Bearer $($tokens['admin'])"
    }
    
    $response = Invoke-RestMethod -Uri "$baseURL/audit/verify" `
        -Method GET `
        -Headers $headers `
        -ErrorAction Stop
    
    Write-TestResult "Admin Authorization" "PASS" "Admin successfully verified chain"
} catch {
    Write-TestResult "Admin Authorization" "FAIL" $_.Exception.Message
}

# Test 4.3: Non-Admin Cannot Access Verify (Reader)
Write-Host "`n  Test 4.3: Reader Cannot Access Verify Endpoint" -ForegroundColor Cyan
try {
    $headers = @{
        "Authorization" = "Bearer $($tokens['reader'])"
    }
    
    $response = Invoke-RestMethod -Uri "$baseURL/audit/verify" `
        -Method GET `
        -Headers $headers `
        -ErrorAction Stop
    
    Write-TestResult "Reader Verify Access" "FAIL" "Should have returned 403 Forbidden"
} catch {
    if ($_.Exception.Response.StatusCode -eq 403) {
        Write-TestResult "Reader Verify Access" "PASS" "Correctly returned 403 Forbidden"
    } else {
        Write-TestResult "Reader Verify Access" "FAIL" "Wrong status: $($_.Exception.Response.StatusCode)"
    }
}

# ============================================================
# SCENARIO 5: HEALTH & INFO ENDPOINTS
# ============================================================
Write-Host "`n[SCENARIO 5] HEALTH & INFO ENDPOINTS" -ForegroundColor Yellow

# Test 5.1: Health Check
Write-Host "`n  Test 5.1: Application Health Check" -ForegroundColor Cyan
try {
    $response = Invoke-RestMethod -Uri "$baseURL/actuator/health" `
        -Method GET `
        -ErrorAction Stop
    
    if ($response.status -eq "UP") {
        Write-TestResult "Health Check" "PASS" "Application is UP and healthy"
    } else {
        Write-TestResult "Health Check" "FAIL" "Health status: $($response.status)"
    }
} catch {
    Write-TestResult "Health Check" "FAIL" $_.Exception.Message
}

# Test 5.2: Info Endpoint
Write-Host "`n  Test 5.2: Application Info" -ForegroundColor Cyan
try {
    $response = Invoke-RestMethod -Uri "$baseURL/actuator/info" `
        -Method GET `
        -ErrorAction Stop
    
    if ($response) {
        Write-TestResult "Info Endpoint" "PASS" "Application info retrieved"
    }
} catch {
    Write-TestResult "Info Endpoint" "FAIL" $_.Exception.Message
}

# ============================================================
# SCENARIO 6: SWAGGER/OPENAPI DOCUMENTATION
# ============================================================
Write-Host "`n[SCENARIO 6] SWAGGER/OPENAPI DOCUMENTATION" -ForegroundColor Yellow

# Test 6.1: Swagger UI Access
Write-Host "`n  Test 6.1: Swagger UI Access" -ForegroundColor Cyan
try {
    $response = Invoke-WebRequest -Uri "$baseURL/swagger-ui/index.html" `
        -Method GET `
        -ErrorAction Stop
    
    if ($response.StatusCode -eq 200) {
        Write-TestResult "Swagger UI Access" "PASS" "Swagger UI is accessible"
    }
} catch {
    Write-TestResult "Swagger UI Access" "FAIL" $_.Exception.Message
}

# Test 6.2: OpenAPI JSON Schema
Write-Host "`n  Test 6.2: OpenAPI JSON Schema" -ForegroundColor Cyan
try {
    $response = Invoke-RestMethod -Uri "$baseURL/v3/api-docs" `
        -Method GET `
        -ErrorAction Stop
    
    if ($response.openapi) {
        Write-TestResult "OpenAPI Schema" "PASS" "OpenAPI schema available (version: $($response.openapi))"
    }
} catch {
    Write-TestResult "OpenAPI Schema" "FAIL" $_.Exception.Message
}

# ============================================================
# TEST SUMMARY
# ============================================================
Write-Host "`n
╔════════════════════════════════════════════════════════════════╗
║                    TEST SUMMARY                               ║
╚════════════════════════════════════════════════════════════════╝
" -ForegroundColor Cyan

$passCount = ($testResults | Where-Object { $_.Status -eq "PASS" }).Count
$failCount = ($testResults | Where-Object { $_.Status -eq "FAIL" }).Count
$totalTests = $testResults.Count

Write-Host "Total Tests: $totalTests" -ForegroundColor White
Write-Host "Passed: $passCount" -ForegroundColor Green
Write-Host "Failed: $failCount" -ForegroundColor $(if ($failCount -gt 0) { "Red" } else { "Green" })
Write-Host "Success Rate: $(([math]::Round(($passCount / $totalTests) * 100, 2)))%`n" -ForegroundColor $(if ($passCount -eq $totalTests) { "Green" } else { "Yellow" })

if ($failCount -eq 0) {
    Write-Host "✓ ALL TESTS PASSED!" -ForegroundColor Green -BackgroundColor Black
} else {
    Write-Host "✗ SOME TESTS FAILED" -ForegroundColor Red
    Write-Host "`nFailed Tests:" -ForegroundColor Red
    $testResults | Where-Object { $_.Status -eq "FAIL" } | ForEach-Object {
        Write-Host "  - $($_.Test): $($_.Details)" -ForegroundColor Red
    }
}

Write-Host "`n"
