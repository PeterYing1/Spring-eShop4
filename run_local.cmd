@echo off
setlocal EnableDelayedExpansion

:: ============================================================
::  eShop Spring Boot  —  Local Run Script
::  Usage:
::    run_local.cmd              — build all + start full stack
::    run_local.cmd infra        — start infrastructure only (SQL, Redis, Mongo, RabbitMQ, Keycloak)
::    run_local.cmd build        — Maven build only (no Docker Compose start)
::    run_local.cmd up           — docker compose up (skip Maven build)
::    run_local.cmd down         — stop and remove all containers
::    run_local.cmd logs [svc]   — tail logs (all services, or one named service)
::    run_local.cmd status       — show running containers and open URLs
::    run_local.cmd clean        — stop containers and remove volumes (full reset)
:: ============================================================

set SCRIPT_DIR=%~dp0
set COMPOSE_FILE=%SCRIPT_DIR%docker-compose.yml
set OVERRIDE_FILE=%SCRIPT_DIR%docker-compose.override.yml

:: ─── Colour helpers (Windows 10+) ───────────────────────────
set GREEN=[32m
set YELLOW=[33m
set RED=[31m
set CYAN=[36m
set RESET=[0m

:: ─── Parse argument ─────────────────────────────────────────
set MODE=%~1
if "%MODE%"=="" set MODE=all

:: ─── Prerequisites check ────────────────────────────────────
call :check_prereqs
if errorlevel 1 goto :error_exit

if /i "%MODE%"=="infra"  goto :start_infra
if /i "%MODE%"=="build"  goto :maven_build
if /i "%MODE%"=="up"     goto :compose_up
if /i "%MODE%"=="down"   goto :compose_down
if /i "%MODE%"=="logs"   goto :show_logs
if /i "%MODE%"=="status" goto :show_status
if /i "%MODE%"=="clean"  goto :clean_all
if /i "%MODE%"=="all"    goto :run_all

echo %RED%Unknown mode: %MODE%%RESET%
echo Usage: run_local.cmd [infra ^| build ^| up ^| down ^| logs ^| status ^| clean]
goto :error_exit

:: ============================================================
:run_all
:: ============================================================
echo.
echo %CYAN%============================================================%RESET%
echo %CYAN%  eShop Spring Boot — Full Stack Startup%RESET%
echo %CYAN%============================================================%RESET%
echo.

call :maven_build
if errorlevel 1 goto :error_exit

call :compose_up
if errorlevel 1 goto :error_exit

call :wait_for_keycloak

call :show_status
goto :eof

:: ============================================================
:maven_build
:: ============================================================
echo.
echo %CYAN%[1/3] Building Maven project ...%RESET%
echo.

cd /d "%SCRIPT_DIR%"
if not defined MAVEN_OPTS set MAVEN_OPTS=-Xmx2g
call mvn clean package -Dmaven.test.skip=true ^
    -Dmaven.wagon.http.connectionTimeout=30000 ^
    -Dmaven.wagon.http.readTimeout=60000
if errorlevel 1 (
    echo.
    echo %RED%Maven build failed. Fix compile errors before starting.%RESET%
    exit /b 1
)
echo.
echo %GREEN%Maven build successful.%RESET%
exit /b 0

:: ============================================================
:start_infra
:: ============================================================
echo.
echo %CYAN%Starting infrastructure services only ...%RESET%
echo %YELLOW%(SQL Server, MongoDB, Redis, RabbitMQ, Keycloak, Seq)%RESET%
echo.

docker compose -f "%COMPOSE_FILE%" -f "%OVERRIDE_FILE%" ^
    up -d sqldata nosqldata basketdata rabbitmq seq identity-api
if errorlevel 1 goto :error_exit

call :wait_for_keycloak
echo.
echo %GREEN%Infrastructure is up.%RESET%
call :print_infra_urls
goto :eof

:: ============================================================
:compose_up
:: ============================================================
echo.
echo %CYAN%[2/3] Starting all containers ...%RESET%
echo.

docker compose -f "%COMPOSE_FILE%" -f "%OVERRIDE_FILE%" up -d --build
if errorlevel 1 (
    echo %RED%docker compose up failed.%RESET%
    exit /b 1
)
exit /b 0

:: ============================================================
:compose_down
:: ============================================================
echo.
echo %YELLOW%Stopping all containers ...%RESET%
docker compose -f "%COMPOSE_FILE%" -f "%OVERRIDE_FILE%" down
echo %GREEN%All containers stopped.%RESET%
goto :eof

:: ============================================================
:show_logs
:: ============================================================
set SVC=%~2
if "%SVC%"=="" (
    echo %CYAN%Tailing logs for all services (Ctrl+C to stop) ...%RESET%
    docker compose -f "%COMPOSE_FILE%" -f "%OVERRIDE_FILE%" logs -f
) else (
    echo %CYAN%Tailing logs for: %SVC% (Ctrl+C to stop) ...%RESET%
    docker compose -f "%COMPOSE_FILE%" -f "%OVERRIDE_FILE%" logs -f %SVC%
)
goto :eof

:: ============================================================
:show_status
:: ============================================================
echo.
echo %CYAN%============================================================%RESET%
echo %CYAN%  Running containers%RESET%
echo %CYAN%============================================================%RESET%
docker compose -f "%COMPOSE_FILE%" -f "%OVERRIDE_FILE%" ps
echo.
echo %CYAN%============================================================%RESET%
echo %CYAN%  Service URLs%RESET%
echo %CYAN%============================================================%RESET%
echo.
echo   %GREEN%Web Applications%RESET%
echo     WebMVC (shopping site)    http://localhost:5100
echo     WebSPA (Angular SPA)      http://localhost:5104
echo     WebStatus (health dash)   http://localhost:5107
echo     Webhook Client            http://localhost:5114
echo.
echo   %GREEN%API Services%RESET%
echo     Catalog API               http://localhost:5101/swagger-ui.html
echo     Ordering API              http://localhost:5102/swagger-ui.html
echo     Basket API                http://localhost:5103/swagger-ui.html
echo     Payment API               http://localhost:5108/hc
echo     Marketing API             http://localhost:5110/swagger-ui.html
echo     Locations API             http://localhost:5109/swagger-ui.html
echo     Webhooks API              http://localhost:5113/swagger-ui.html
echo.
echo   %GREEN%Gateways%RESET%
echo     Web Shopping Gateway      http://localhost:5200
echo     Web Marketing Gateway     http://localhost:5201
echo     Mobile Shopping Gateway   http://localhost:5202
echo     Web Shopping Aggregator   http://localhost:5121/swagger-ui.html
echo     Mobile Shopping Aggreg.   http://localhost:5122/swagger-ui.html
echo.
echo   %GREEN%Infrastructure%RESET%
echo     Keycloak Admin            http://localhost:5105  (admin/admin)
echo     RabbitMQ Management       http://localhost:15672 (guest/guest)
echo     Seq Log Viewer            http://localhost:5380
echo     SQL Server                localhost:1433         (sa/Pass@word)
echo     Redis                     localhost:6379
echo     MongoDB                   localhost:27017
echo.
echo   %YELLOW%Demo user: demouser@microsoft.com / Pass@word1%RESET%
echo.
goto :eof

:: ============================================================
:clean_all
:: ============================================================
echo.
echo %RED%WARNING: This will stop all containers AND delete all data volumes.%RESET%
set /p CONFIRM=Type YES to confirm:
if /i not "%CONFIRM%"=="YES" (
    echo Cancelled.
    goto :eof
)
echo.
echo %YELLOW%Removing containers and volumes ...%RESET%
docker compose -f "%COMPOSE_FILE%" -f "%OVERRIDE_FILE%" down -v --remove-orphans
echo %GREEN%Clean complete. All data volumes deleted.%RESET%
goto :eof

:: ============================================================
:wait_for_keycloak
:: ============================================================
echo.
echo %CYAN%[3/3] Waiting for Keycloak to be ready ...%RESET%
echo %YELLOW%(This can take up to 90 seconds on first startup)%RESET%

set MAX_RETRIES=30
set RETRY=0
:kc_retry
set /a RETRY+=1
if %RETRY% gtr %MAX_RETRIES% (
    echo %YELLOW%Keycloak health check timed out. It may still be starting.%RESET%
    echo %YELLOW%Check: http://localhost:5105/realms/eshop/.well-known/openid-configuration%RESET%
    exit /b 0
)

curl -sf -o nul http://localhost:5105/realms/eshop/.well-known/openid-configuration 2>nul
if errorlevel 1 (
    <nul set /p =.
    timeout /t 3 /nobreak >nul
    goto :kc_retry
)
echo.
echo %GREEN%Keycloak is ready.%RESET%
exit /b 0

:: ============================================================
:check_prereqs
:: ============================================================
echo.
echo %CYAN%Checking prerequisites ...%RESET%

:: Docker
docker version >nul 2>&1
if errorlevel 1 (
    echo %RED%ERROR: Docker is not running or not installed.%RESET%
    echo        Install Docker Desktop: https://www.docker.com/products/docker-desktop
    exit /b 1
)

:: Docker Compose v2 (plugin form)
docker compose version >nul 2>&1
if errorlevel 1 (
    echo %RED%ERROR: Docker Compose v2 is required.%RESET%
    echo        Update Docker Desktop or install the compose plugin.
    exit /b 1
)

:: Java (only needed for Maven build)
if /i "%MODE%"=="all" goto :check_java
if /i "%MODE%"=="build" goto :check_java
goto :check_done

:check_java
java -version >nul 2>&1
if errorlevel 1 (
    echo %RED%ERROR: Java 21+ is required for Maven build.%RESET%
    echo        Install: https://adoptium.net/
    exit /b 1
)
for /f "tokens=3 delims= " %%v in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    set JAVA_VER=%%v
)
:: Strip quotes
set JAVA_VER=%JAVA_VER:"=%
echo %GREEN%  Java: %JAVA_VER%%RESET%

:: Maven (use 'where' to avoid starting a JVM just for the check)
where mvn >nul 2>&1
if errorlevel 1 (
    echo %RED%ERROR: Maven 3.9+ is required.%RESET%
    echo        Install: https://maven.apache.org/download.cgi
    exit /b 1
)

:check_done
echo %GREEN%All prerequisites satisfied.%RESET%
exit /b 0

:: ============================================================
:print_infra_urls
:: ============================================================
echo.
echo   %GREEN%Infrastructure ready:%RESET%
echo     Keycloak Admin    http://localhost:5105  (admin/admin)
echo     RabbitMQ          http://localhost:15672 (guest/guest)
echo     Seq               http://localhost:5380
echo     SQL Server        localhost:1433
echo     Redis             localhost:6379
echo     MongoDB           localhost:27017
echo.
exit /b 0

:: ============================================================
:error_exit
:: ============================================================
echo.
echo %RED%Startup failed. See errors above.%RESET%
exit /b 1
