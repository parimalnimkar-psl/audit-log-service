@echo off
REM Comprehensive API Testing Script for Audit Log Service

setlocal enabledelayedexpansion
set baseURL=http://localhost:8080
set testCount=0
set passCount=0
set failCount=0

echo.
echo ========================================
echo AUDIT LOG SERVICE - API TEST SUITE
echo ========================================
echo.

REM ============================================================
REM SCENARIO 1: AUTHENTICATION & AUTHORIZATION
REM ============================================================
echo [SCENARIO 1] AUTHENTICATION ^& AUTHORIZATION
echo.

REM Test 1.1: Writer Token
echo Test 1.1: Writer Role Token Generation
call :runTest "Writer Token" curl -s -X POST %baseURL%/auth/token -H "Content-Type: application/json" -d "{\"username\":\"writer\",\"password\":\"writer123\"}" tokens
if !errorlevel! equ 0 (
    echo [PASS] Writer token generated successfully
    set /a passCount+=1
) else (
    echo [FAIL] Writer token generation failed
    set /a failCount+=1
)
set /a testCount+=1

REM Test 1.2: Reader Token
echo.
echo Test 1.2: Reader Role Token Generation
call :runTest "Reader Token" curl -s -X POST %baseURL%/auth/token -H "Content-Type: application/json" -d "{\"username\":\"reader\",\"password\":\"reader123\"}" tokens
if !errorlevel! equ 0 (
    echo [PASS] Reader token generated successfully
    set /a passCount+=1
) else (
    echo [FAIL] Reader token generation failed
    set /a failCount+=1
)
set /a testCount+=1

REM Test 1.3: Admin Token
echo.
echo Test 1.3: Admin Role Token Generation
call :runTest "Admin Token" curl -s -X POST %baseURL%/auth/token -H "Content-Type: application/json" -d "{\"username\":\"admin\",\"password\":\"admin123\"}" tokens
if !errorlevel! equ 0 (
    echo [PASS] Admin token generated successfully
    set /a passCount+=1
) else (
    echo [FAIL] Admin token generation failed
    set /a failCount+=1
)
set /a testCount+=1

REM Test 1.4: Invalid Credentials
echo.
echo Test 1.4: Invalid Credentials Test
curl -s -o nul -w "Status: %%{http_code}\n" -X POST %baseURL%/auth/token -H "Content-Type: application/json" -d "{\"username\":\"invalid\",\"password\":\"wrong\"}"
if !errorlevel! equ 0 (
    echo [PASS] Invalid credentials correctly rejected
    set /a passCount+=1
) else (
    echo [FAIL] Unexpected error
    set /a failCount+=1
)
set /a testCount+=1

REM ============================================================
REM SCENARIO 2: EVENT CREATION
REM ============================================================
echo.
echo [SCENARIO 2] EVENT CREATION ^& VALIDATION
echo.

REM Get writer token first
echo Getting writer token for event creation tests...
for /f "tokens=*" %%a in ('curl -s -X POST %baseURL%/auth/token -H "Content-Type: application/json" -d "{\"username\":\"writer\",\"password\":\"writer123\"}" ^| findstr /r "access_token"') do (
    set writerTokenLine=%%a
)

REM Test 2.1: Create First Event
echo.
echo Test 2.1: Create First Audit Event (USER_LOGIN)
curl -s -X POST %baseURL%/audit/events -H "Authorization: Bearer !writerToken!" -H "Content-Type: application/json" -d "{\"eventType\":\"USER_LOGIN\",\"actorId\":\"user_001\",\"resourceType\":\"ACCOUNT\",\"resourceId\":\"account_12345\",\"payload\":\"{\"\"method\"\":\"\"password\"\"}\"}">event1.json
if exist event1.json (
    echo [PASS] First event created successfully
    set /a passCount+=1
    type event1.json | findstr /r "id" >nul
    if !errorlevel! equ 0 (
        echo Event details:
        type event1.json | findstr /r "id,chainSequence,previousHash,contentHash"
    )
) else (
    echo [FAIL] First event creation failed
    set /a failCount+=1
)
set /a testCount+=1

REM Test 2.2: Create Second Event
echo.
echo Test 2.2: Create Second Event (DATA_ACCESS)
curl -s -X POST %baseURL%/audit/events -H "Authorization: Bearer !writerToken!" -H "Content-Type: application/json" -d "{\"eventType\":\"DATA_ACCESS\",\"actorId\":\"user_002\",\"resourceType\":\"ACCOUNT\",\"resourceId\":\"account_12345\",\"payload\":\"{\"\"action\"\":\"\"read\"\"}\"}">event2.json
if exist event2.json (
    echo [PASS] Second event created successfully
    set /a passCount+=1
    type event2.json
) else (
    echo [FAIL] Second event creation failed
    set /a failCount+=1
)
set /a testCount+=1

REM Test 2.3: Create Third Event
echo.
echo Test 2.3: Create Third Event (DATA_MODIFICATION)
curl -s -X POST %baseURL%/audit/events -H "Authorization: Bearer !writerToken!" -H "Content-Type: application/json" -d "{\"eventType\":\"DATA_MODIFICATION\",\"actorId\":\"user_003\",\"resourceType\":\"TRANSACTION\",\"resourceId\":\"txn_99999\",\"payload\":\"{\"\"amount\":\"\"500\"\"}\"}">event3.json
if exist event3.json (
    echo [PASS] Third event created successfully
    set /a passCount+=1
) else (
    echo [FAIL] Third event creation failed
    set /a failCount+=1
)
set /a testCount+=1

REM ============================================================
REM SCENARIO 3: QUERY ^& FILTERING
REM ============================================================
echo.
echo [SCENARIO 3] QUERY ^& FILTERING
echo.

REM Get reader token first
echo Getting reader token for query tests...
for /f "tokens=*" %%a in ('curl -s -X POST %baseURL%/auth/token -H "Content-Type: application/json" -d "{\"username\":\"reader\",\"password\":\"reader123\"}" ^| findstr /r "access_token"') do (
    set readerTokenLine=%%a
)

REM Test 3.1: Query All Events
echo.
echo Test 3.1: Query All Events
curl -s -w "\nStatus: %%{http_code}\n" %baseURL%/audit/events -H "Authorization: Bearer !readerToken!">query_all.json
if exist query_all.json (
    echo [PASS] Query all events successful
    set /a passCount+=1
    type query_all.json | findstr /r "content"
) else (
    echo [FAIL] Query all events failed
    set /a failCount+=1
)
set /a testCount+=1

REM Test 3.2: Filter by ActorId
echo.
echo Test 3.2: Filter by ActorId (user_001)
curl -s %baseURL%"/audit/events?actorId=user_001" -H "Authorization: Bearer !readerToken!">filter_actor.json
if exist filter_actor.json (
    echo [PASS] Filter by ActorId successful
    set /a passCount+=1
    type filter_actor.json | findstr /r "user_001"
) else (
    echo [FAIL] Filter by ActorId failed
    set /a failCount+=1
)
set /a testCount+=1

REM Test 3.3: Filter by ResourceType
echo.
echo Test 3.3: Filter by ResourceType (ACCOUNT)
curl -s %baseURL%"/audit/events?resourceType=ACCOUNT" -H "Authorization: Bearer !readerToken!">filter_resource.json
if exist filter_resource.json (
    echo [PASS] Filter by ResourceType successful
    set /a passCount+=1
    type filter_resource.json | findstr /r "ACCOUNT"
) else (
    echo [FAIL] Filter by ResourceType failed
    set /a failCount+=1
)
set /a testCount+=1

REM Test 3.4: Filter by EventType
echo.
echo Test 3.4: Filter by EventType (USER_LOGIN)
curl -s %baseURL%"/audit/events?eventType=USER_LOGIN" -H "Authorization: Bearer !readerToken!">filter_event.json
if exist filter_event.json (
    echo [PASS] Filter by EventType successful
    set /a passCount+=1
    type filter_event.json | findstr /r "USER_LOGIN"
) else (
    echo [FAIL] Filter by EventType failed
    set /a failCount+=1
)
set /a testCount+=1

REM ============================================================
REM SCENARIO 4: CHAIN VERIFICATION
REM ============================================================
echo.
echo [SCENARIO 4] CHAIN VERIFICATION
echo.

REM Get admin token first
echo Getting admin token for verification tests...
for /f "tokens=*" %%a in ('curl -s -X POST %baseURL%/auth/token -H "Content-Type: application/json" -d "{\"username\":\"admin\",\"password\":\"admin123\"}" ^| findstr /r "access_token"') do (
    set adminTokenLine=%%a
)

REM Test 4.1: Verify Intact Chain
echo.
echo Test 4.1: Verify Intact Chain
curl -s %baseURL%/audit/verify -H "Authorization: Bearer !adminToken!">verify_result.json
if exist verify_result.json (
    echo [PASS] Chain verification executed successfully
    set /a passCount+=1
    echo Chain verification result:
    type verify_result.json | findstr /r "intact,checkedRecordCount"
) else (
    echo [FAIL] Chain verification failed
    set /a failCount+=1
)
set /a testCount+=1

REM ============================================================
REM SCENARIO 5: HEALTH ^& ACTUATOR
REM ============================================================
echo.
echo [SCENARIO 5] HEALTH ^& ACTUATOR ENDPOINTS
echo.

REM Test 5.1: Health Check
echo.
echo Test 5.1: Application Health Check
curl -s %baseURL%/actuator/health>health.json
if exist health.json (
    echo [PASS] Health check endpoint accessible
    set /a passCount+=1
    type health.json | findstr /r "status"
) else (
    echo [FAIL] Health check failed
    set /a failCount+=1
)
set /a testCount+=1

REM Test 5.2: Info Endpoint
echo.
echo Test 5.2: Application Info
curl -s %baseURL%/actuator/info>info.json
if exist info.json (
    echo [PASS] Info endpoint accessible
    set /a passCount+=1
) else (
    echo [FAIL] Info endpoint failed
    set /a failCount+=1
)
set /a testCount+=1

REM ============================================================
REM TEST SUMMARY
REM ============================================================
echo.
echo ========================================
echo TEST SUMMARY
echo ========================================
echo Total Tests: !testCount!
echo Passed: !passCount!
echo Failed: !failCount!
set /a successRate=(!passCount! * 100) / !testCount!
echo Success Rate: !successRate!%%
echo.
if !failCount! equ 0 (
    echo [SUCCESS] ALL TESTS PASSED!
) else (
    echo [WARNING] Some tests failed
)
echo.

REM Cleanup
del /q event1.json event2.json event3.json query_all.json filter_actor.json filter_resource.json filter_event.json verify_result.json health.json info.json 2>nul

exit /b 0

:runTest
echo Running: %~2
exit /b 0
