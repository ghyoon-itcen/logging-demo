@echo off
setlocal

echo ============================================
echo  Spring PetClinic - Build ^& Deploy Script
echo ============================================

set PROJECT_ROOT=%~dp0..
set APP_DIR=%PROJECT_ROOT%\spring-petclinic-main
set DEPLOY_DIR=%PROJECT_ROOT%\data\deploy

:: 1. 소스 확인
echo.
echo [1/3] Checking source code...
if not exist "%APP_DIR%" (
    echo [ERROR] Source directory not found: %APP_DIR%
    exit /b 1
)
echo      Source found: %APP_DIR%

:: 2. Docker 컨테이너에서 빌드
echo.
echo [2/3] Building with Maven (in Docker)...
if not exist "%DEPLOY_DIR%" mkdir "%DEPLOY_DIR%"
docker run --rm ^
    -v "%APP_DIR%":/app ^
    -v "%DEPLOY_DIR%":/output ^
    -v "%PROJECT_ROOT%\data\maven-cache":/root/.m2 ^
    -w /app ^
    eclipse-temurin:17-jdk ^
    bash -c "rm -rf target && bash mvnw spring-javaformat:apply package -DskipTests && cp target/*.jar /output/app.jar"

if %ERRORLEVEL% neq 0 (
    echo [ERROR] Build failed.
    exit /b 1
)

:: 3. 컨테이너 재시작
echo.
echo [3/3] Restarting petclinic container...
cd /d "%PROJECT_ROOT%"
docker compose restart petclinic

if %ERRORLEVEL% neq 0 (
    echo [WARN] Container restart failed. Starting it...
    docker compose up -d petclinic
)

echo.
echo ============================================
echo  Deploy complete!
echo  App: http://localhost:8080
echo ============================================

endlocal
