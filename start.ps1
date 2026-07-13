param(
    [switch]$MySQLOnly,
    [switch]$Stop
)

$ErrorActionPreference = "SilentlyContinue"

# ---- Config ----
$MYSQL_BIN    = "C:\Users\riddh\mysql-9.7.0-winx64\bin\mysqld.exe"
$MYSQL_INI    = "C:\Users\riddh\mysql-9.7.0-winx64\my-omnibot.ini"
$MVN          = "C:\Users\riddh\maven\apache-maven-3.9.16\bin\mvn.cmd"
$APP_DIR      = "C:\Users\riddh\omnibot-platform\backend-core"
$HEALTH_PORT  = 3306

# ---- Stop everything ----
if ($Stop) {
    Write-Host "Stopping OmniBot app..." -ForegroundColor Yellow
    Get-Process -Name java -ErrorAction SilentlyContinue | Where-Object { $_.CommandLine -like "*spring-boot*" } | Stop-Process -Force
    Write-Host "Stopping MySQL..." -ForegroundColor Yellow
    Get-Process -Name mysqld -ErrorAction SilentlyContinue | Stop-Process -Force
    Write-Host "All stopped." -ForegroundColor Green
    return
}

# ---- Ensure MySQL is running ----
function Start-MySQLIfDown {
    $conn = Get-NetTCPConnection -LocalPort $HEALTH_PORT -ErrorAction SilentlyContinue
    if ($conn) {
        Write-Host "[OK] MySQL already running on port $HEALTH_PORT" -ForegroundColor Green
        return $true
    }
    Write-Host "[..] Starting MySQL..." -ForegroundColor Yellow
    Start-Process -FilePath $MYSQL_BIN -ArgumentList "--defaults-file=$MYSQL_INI"
    # Wait up to 15 seconds
    for ($i = 1; $i -le 15; $i++) {
        Start-Sleep -Seconds 1
        $conn = Get-NetTCPConnection -LocalPort $HEALTH_PORT -ErrorAction SilentlyContinue
        if ($conn) {
            Write-Host "[OK] MySQL started on port $HEALTH_PORT" -ForegroundColor Green
            return $true
        }
        Write-Host "     waiting... ($i sec)" -ForegroundColor DarkGray
    }
    Write-Host "[FAIL] MySQL failed to start!" -ForegroundColor Red
    return $false
}

# ---- Start Spring Boot ----
function Start-OmniBot {
    # Kill any existing instance
    $existing = Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue
    if ($existing) {
        Write-Host "[OK] App already running on port 8080" -ForegroundColor Green
        return
    }
    Write-Host "[..] Starting OmniBot backend..." -ForegroundColor Yellow
    Start-Process -FilePath "cmd.exe" -ArgumentList "/c", "cd /d $APP_DIR && `"$MVN`" spring-boot:run -DskipTests" -WindowStyle Normal
    Write-Host "[OK] OmniBot starting (check browser at http://localhost:8080)" -ForegroundColor Green
}

# ---- Main ----
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  OmniBot Platform Startup" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

if (-not (Start-MySQLIfDown)) { return }
Start-OmniBot

if (-not $MySQLOnly) {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "  Ready! Visit http://localhost:8080" -ForegroundColor Green
    Write-Host "  To stop: .\start.ps1 -Stop" -ForegroundColor DarkGray
    Write-Host "========================================" -ForegroundColor Cyan
}
