# Audit Log Service - Comprehensive API Test Suite
# Simple PowerShell Test Script

$baseURL = "http://localhost:8080"
$testResults = @()
$tokens = @{}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "AUDIT LOG SERVICE - COMPREHENSIVE API TEST" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# Helper function to log results
function LogResult {
    param($testName, $status, $details)
    $color = if ($status -eq "PASS") { "Green" } else { "Red" }
    Write-Host "[$status] $testName" -ForegroundColor $color
    if ($details) { Write-Host "        $details" -ForegroundColor Gray }
    $script:testResults += @{Test = $testName; Status = $status; Details = $details}
}

Write-Host "SCENARIO 1: AUTHENTICATION & AUTHORIZATION`n" -ForegroundColor Yellow

# Test 1.1
Write-Host "  [1.1] Writer Role Token Generation..."
try {
    $resp = Invoke-RestMethod -Uri "$baseURL/auth/token" -Method POST `
        -ContentType "application/json" `
        -Body '{"username":"writer","password":"writer123"}'
    if ($resp.accessToken) {
        $tokens['writer'] = $resp.accessToken
        LogResult "Writer Token" "PASS" "expiresIn: $($resp.expiresIn)s"
    }
} catch { LogResult "Writer Token" "FAIL" $_.Exception.Message }

# Test 1.2
Write-Host "  [1.2] Reader Role Token Generation..."
try {
    $resp = Invoke-RestMethod -Uri "$baseURL/auth/token" -Method POST `
        -ContentType "application/json" `
        -Body '{"username":"reader","password":"reader123"}'
    if ($resp.accessToken) {
        $tokens['reader'] = $resp.accessToken
        LogResult "Reader Token" "PASS" "Token obtained"
    }
} catch { LogResult "Reader Token" "FAIL" $_.Exception.Message }

# Test 1.3
Write-Host "  [1.3] Admin Role Token Generation..."
try {
    $resp = Invoke-RestMethod -Uri "$baseURL/auth/token" -Method POST `
        -ContentType "application/json" `
        -Body '{"username":"admin","password":"admin123"}'
    if ($resp.accessToken) {
        $tokens['admin'] = $resp.accessToken
        LogResult "Admin Token" "PASS" "Token obtained"
    }
} catch { LogResult "Admin Token" "FAIL" $_.Exception.Message }

# Test 1.4
Write-Host "  [1.4] Invalid Credentials Test..."
try {
    $resp = Invoke-RestMethod -Uri "$baseURL/auth/token" -Method POST `
        -ContentType "application/json" `
        -Body '{"username":"invalid","password":"wrong"}'
    LogResult "Invalid Credentials" "FAIL" "Should return 401"
} catch {
    if ($_.Exception.Response.StatusCode -eq 401) {
        LogResult "Invalid Credentials" "PASS" "Correctly rejected (401)"
    } else {
        LogResult "Invalid Credentials" "FAIL" "Wrong status"
    }
}

Write-Host "`nSCENARIO 2: EVENT CREATION & VALIDATION`n" -ForegroundColor Yellow

# Test 2.1
Write-Host "  [2.1] Create First Event (USER_LOGIN)..."
$event1 = $null
try {
    $headers = @{"Authorization" = "Bearer $($tokens['writer'])"}
    $body = @{
        eventType = "USER_LOGIN"
        actorId = "user_001"
        resourceType = "ACCOUNT"
        resourceId = "account_12345"
        payload = '{"method":"password","ip":"192.168.1.1"}'
    } | ConvertTo-Json
    
    $resp = Invoke-RestMethod -Uri "$baseURL/audit/events" -Method POST `
        -Headers $headers -ContentType "application/json" -Body $body
    
    if ($resp.id) {
        $event1 = $resp
        LogResult "Create First Event" "PASS" "ID: $($resp.id), Seq: $($resp.chainSequence)"
    }
} catch { LogResult "Create First Event" "FAIL" $_.Exception.Message }

# Test 2.2
Write-Host "  [2.2] Verify Genesis Hash..."
if ($event1 -and $event1.previousHash -eq "GENESIS") {
    LogResult "Genesis Hash" "PASS" "First event has GENESIS"
} else {
    LogResult "Genesis Hash" "FAIL" "Expected GENESIS"
}

# Test 2.3
Write-Host "  [2.3] Create Second Event (DATA_ACCESS)..."
$event2 = $null
try {
    $headers = @{"Authorization" = "Bearer $($tokens['writer'])"}
    $body = @{
        eventType = "DATA_ACCESS"
        actorId = "user_002"
        resourceType = "ACCOUNT"
        resourceId = "account_12345"
        payload = '{"action":"read","fields":["email"]}'
    } | ConvertTo-Json
    
    $resp = Invoke-RestMethod -Uri "$baseURL/audit/events" -Method POST `
        -Headers $headers -ContentType "application/json" -Body $body
    
    if ($resp.id) {
        $event2 = $resp
        LogResult "Create Second Event" "PASS" "ID: $($resp.id), Seq: $($resp.chainSequence)"
    }
} catch { LogResult "Create Second Event" "FAIL" $_.Exception.Message }

# Test 2.4
Write-Host "  [2.4] Verify Hash Chaining..."
if ($event1 -and $event2 -and $event2.previousHash -eq $event1.contentHash) {
    LogResult "Hash Chaining" "PASS" "Event2.previousHash matches Event1.contentHash"
} else {
    LogResult "Hash Chaining" "FAIL" "Hash chain broken"
}

# Test 2.5
Write-Host "  [2.5] Verify Sequence Increment..."
if ($event1 -and $event2 -and ($event2.chainSequence -eq $event1.chainSequence + 1)) {
    LogResult "Sequence Increment" "PASS" "Seq: $($event1.chainSequence) -> $($event2.chainSequence)"
} else {
    LogResult "Sequence Increment" "FAIL" "Sequence not incremented correctly"
}

# Test 2.6
Write-Host "  [2.6] Create Third Event..."
$event3 = $null
try {
    $headers = @{"Authorization" = "Bearer $($tokens['writer'])"}
    $body = @{
        eventType = "DATA_MODIFICATION"
        actorId = "user_003"
        resourceType = "TRANSACTION"
        resourceId = "txn_99999"
        payload = '{"amount":500,"currency":"USD"}'
    } | ConvertTo-Json
    
    $resp = Invoke-RestMethod -Uri "$baseURL/audit/events" -Method POST `
        -Headers $headers -ContentType "application/json" -Body $body
    
    if ($resp.id) {
        $event3 = $resp
        LogResult "Create Third Event" "PASS" "ID: $($resp.id)"
    }
} catch { LogResult "Create Third Event" "FAIL" $_.Exception.Message }

# Test 2.7
Write-Host "  [2.7] Invalid Request - Missing Fields..."
try {
    $headers = @{"Authorization" = "Bearer $($tokens['writer'])"}
    $body = '{"eventType":"TEST"}' | ConvertTo-Json
    
    $resp = Invoke-RestMethod -Uri "$baseURL/audit/events" -Method POST `
        -Headers $headers -ContentType "application/json" -Body $body
    LogResult "Validation Error" "FAIL" "Should return 400"
} catch {
    if ($_.Exception.Response.StatusCode -eq 400) {
        LogResult "Validation Error" "PASS" "Correctly returned 400"
    } else {
        LogResult "Validation Error" "FAIL" "Wrong status"
    }
}

# Test 2.8
Write-Host "  [2.8] Unauthorized Access (No Token)..."
try {
    $body = '{"eventType":"TEST","actorId":"test","resourceType":"TEST","resourceId":"test","payload":"{}"}' | ConvertTo-Json
    $resp = Invoke-RestMethod -Uri "$baseURL/audit/events" -Method POST `
        -ContentType "application/json" -Body $body
    LogResult "No Token Access" "FAIL" "Should return 401"
} catch {
    if ($_.Exception.Response.StatusCode -eq 401) {
        LogResult "No Token Access" "PASS" "Correctly returned 401"
    } else {
        LogResult "No Token Access" "FAIL" "Wrong status"
    }
}

Write-Host "`nSCENARIO 3: QUERY & FILTERING`n" -ForegroundColor Yellow

# Test 3.1
Write-Host "  [3.1] Query All Events..."
try {
    $headers = @{"Authorization" = "Bearer $($tokens['admin'])"}
    $resp = Invoke-RestMethod -Uri "$baseURL/audit/events?page=0&size=10" `
        -Method GET -Headers $headers
    
    if ($resp.content.Count -ge 3) {
        LogResult "Query All Events" "PASS" "Retrieved $($resp.content.Count) events"
    } else {
        LogResult "Query All Events" "WARN" "Only $($resp.content.Count) events found"
    }
} catch { LogResult "Query All Events" "FAIL" $_.Exception.Message }

# Test 3.2
Write-Host "  [3.2] Filter by ActorId..."
try {
    $headers = @{"Authorization" = "Bearer $($tokens['admin'])"}
    $resp = Invoke-RestMethod -Uri "$baseURL/audit/events?actorId=writer&page=0&size=10" `
        -Method GET -Headers $headers
    
    if ($resp.content.Count -ge 1) {
        LogResult "Filter ActorId" "PASS" "Found $($resp.content.Count) events for writer"
    } else {
        LogResult "Filter ActorId" "WARN" "No events found for writer"
    }
} catch { LogResult "Filter ActorId" "FAIL" $_.Exception.Message }

# Test 3.3
Write-Host "  [3.3] Filter by ResourceType..."
try {
    $headers = @{"Authorization" = "Bearer $($tokens['admin'])"}
    $resp = Invoke-RestMethod -Uri "$baseURL/audit/events?resourceType=ACCOUNT&page=0&size=10" `
        -Method GET -Headers $headers
    
    if ($resp.content.Count -ge 1) {
        LogResult "Filter ResourceType" "PASS" "Found $($resp.content.Count) ACCOUNT events"
    } else {
        LogResult "Filter ResourceType" "WARN" "No ACCOUNT events found"
    }
} catch { LogResult "Filter ResourceType" "FAIL" $_.Exception.Message }

# Test 3.4
Write-Host "  [3.4] Filter by EventType..."
try {
    $headers = @{"Authorization" = "Bearer $($tokens['admin'])"}
    $resp = Invoke-RestMethod -Uri "$baseURL/audit/events?eventType=USER_LOGIN&page=0&size=10" `
        -Method GET -Headers $headers
    
    if ($resp.content.Count -ge 1) {
        LogResult "Filter EventType" "PASS" "Found $($resp.content.Count) USER_LOGIN events"
    } else {
        LogResult "Filter EventType" "WARN" "No USER_LOGIN events found"
    }
} catch { LogResult "Filter EventType" "FAIL" $_.Exception.Message }

# Test 3.5
Write-Host "  [3.5] Pagination Test..."
try {
    $headers = @{"Authorization" = "Bearer $($tokens['admin'])"}
    $resp = Invoke-RestMethod -Uri "$baseURL/audit/events?page=0&size=2" `
        -Method GET -Headers $headers
    
    LogResult "Pagination" "PASS" "Page 0, Size 2: $($resp.content.Count) items"
} catch { LogResult "Pagination" "FAIL" $_.Exception.Message }

# Test 3.6
Write-Host "  [3.6] JSON Export..."
try {
    $headers = @{"Authorization" = "Bearer $($tokens['admin'])"}
    $resp = Invoke-RestMethod -Uri "$baseURL/audit/export" -Method GET -Headers $headers

    if ($resp.recordCount -eq 3 -and $resp.hashAlgorithm -eq "SHA-256") {
        LogResult "JSON Export" "PASS" "Exported $($resp.recordCount) records with $($resp.hashAlgorithm)"
    } else {
        LogResult "JSON Export" "FAIL" "Unexpected export response"
    }
} catch { LogResult "JSON Export" "FAIL" $_.Exception.Message }

Write-Host "`nSCENARIO 4: CHAIN VERIFICATION`n" -ForegroundColor Yellow

# Test 4.1
Write-Host "  [4.1] Verify Intact Chain..."
try {
    $headers = @{"Authorization" = "Bearer $($tokens['admin'])"}
    $resp = Invoke-RestMethod -Uri "$baseURL/audit/verify" -Method GET -Headers $headers
    
    if ($resp.intact -eq $true) {
        LogResult "Chain Intact" "PASS" "Checked $($resp.checkedRecordCount) records"
    } else {
        LogResult "Chain Intact" "FAIL" "Violation: $($resp.violationType)"
    }
} catch { LogResult "Chain Intact" "FAIL" $_.Exception.Message }

# Test 4.2
Write-Host "  [4.2] Admin Authorization..."
try {
    $headers = @{"Authorization" = "Bearer $($tokens['admin'])"}
    $resp = Invoke-RestMethod -Uri "$baseURL/audit/verify" -Method GET -Headers $headers
    LogResult "Admin Verify Access" "PASS" "Admin can verify chain"
} catch { LogResult "Admin Verify Access" "FAIL" $_.Exception.Message }

# Test 4.3
Write-Host "  [4.3] Reader Cannot Access Verify..."
try {
    $headers = @{"Authorization" = "Bearer $($tokens['reader'])"}
    $resp = Invoke-RestMethod -Uri "$baseURL/audit/verify" -Method GET -Headers $headers
    LogResult "Reader Verify Access" "FAIL" "Should return 403"
} catch {
    if ($_.Exception.Response.StatusCode -eq 403) {
        LogResult "Reader Verify Access" "PASS" "Correctly returned 403"
    } else {
        LogResult "Reader Verify Access" "FAIL" "Wrong status"
    }
}

Write-Host "`nSCENARIO 5: ACTUATOR & HEALTH ENDPOINTS`n" -ForegroundColor Yellow

# Test 5.1
Write-Host "  [5.1] Health Check..."
try {
    $resp = Invoke-RestMethod -Uri "$baseURL/actuator/health" -Method GET
    if ($resp.status -eq "UP") {
        LogResult "Health Check" "PASS" "Application is UP"
    } else {
        LogResult "Health Check" "FAIL" "Status: $($resp.status)"
    }
} catch { LogResult "Health Check" "FAIL" $_.Exception.Message }

# Test 5.2
Write-Host "  [5.2] Info Endpoint..."
try {
    $resp = Invoke-RestMethod -Uri "$baseURL/actuator/info" -Method GET
    LogResult "Info Endpoint" "PASS" "Info retrieved"
} catch { LogResult "Info Endpoint" "FAIL" $_.Exception.Message }

Write-Host "`nSCENARIO 6: SWAGGER & OPENAPI`n" -ForegroundColor Yellow

# Test 6.1
Write-Host "  [6.1] Swagger UI Access..."
try {
    $resp = Invoke-WebRequest -Uri "$baseURL/swagger-ui/index.html" -Method GET -UseBasicParsing
    if ($resp.StatusCode -eq 200) {
        LogResult "Swagger UI" "PASS" "Accessible"
    }
} catch { LogResult "Swagger UI" "FAIL" $_.Exception.Message }

# Test 6.2
Write-Host "  [6.2] OpenAPI Schema..."
try {
    $resp = Invoke-RestMethod -Uri "$baseURL/v3/api-docs" -Method GET
    if ($resp.openapi) {
        LogResult "OpenAPI Schema" "PASS" "Version: $($resp.openapi)"
    }
} catch { LogResult "OpenAPI Schema" "FAIL" $_.Exception.Message }

# Print Summary
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "TEST SUMMARY" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

$passCount = ($testResults | Where-Object { $_.Status -eq "PASS" }).Count
$failCount = ($testResults | Where-Object { $_.Status -eq "FAIL" }).Count
$totalTests = $testResults.Count
$successRate = [math]::Round(($passCount / $totalTests) * 100, 2)

 $summaryColor = if ($failCount -gt 0) { "Red" } else { "Green" }
 $rateColor = if ($passCount -eq $totalTests) { "Green" } else { "Yellow" }
 Write-Host "Total Tests: $totalTests" -ForegroundColor White
 Write-Host "Passed: $passCount" -ForegroundColor Green
 Write-Host "Failed: $failCount" -ForegroundColor $summaryColor
 Write-Host "Success Rate: $successRate%" -ForegroundColor $rateColor

 if ($failCount -eq 0) {
     Write-Host "ALL TESTS PASSED" -ForegroundColor Green
 } else {
     Write-Host "SOME TESTS FAILED" -ForegroundColor Red
     $testResults | Where-Object { $_.Status -eq "FAIL" } | ForEach-Object {
         Write-Host "- $($_.Test)" -ForegroundColor Red
     }
 }

Write-Host " "
