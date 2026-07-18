# Stop existing processes
$owningProcesses = Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess -Unique
foreach ($procId in $owningProcesses) { Stop-Process -Id $procId -Force -ErrorAction SilentlyContinue }
Get-Process -Name java -ErrorAction SilentlyContinue | Stop-Process -Force
Stop-Process -Name "mysqld" -Force -ErrorAction SilentlyContinue

# Clear MySQL PID to allow safe restart
Start-Sleep -Seconds 2
Remove-Item "C:\Users\riddh\mysql-9.7.0-winx64\data\*.pid" -Force -ErrorAction SilentlyContinue

# 1. Start MySQL Backend Service in Background
Write-Host "Starting MySQL Database..." -ForegroundColor Cyan
Start-Process -FilePath "C:\Users\riddh\mysql-9.7.0-winx64\bin\mysqld.exe" -ArgumentList "--defaults-file=C:\Users\riddh\mysql-9.7.0-winx64\my-omnibot.ini" -WindowStyle Hidden

Start-Sleep -Seconds 6

# 2. Start Spring Boot in Background Terminal
Write-Host "Starting Spring Boot Backend..." -ForegroundColor Cyan
Set-Location "C:\Users\riddh\omnibot-platform\backend-core"
Start-Process -FilePath "C:\Users\riddh\maven\apache-maven-3.9.16\bin\mvn.cmd" -ArgumentList "spring-boot:run -DskipTests" -WindowStyle Normal

# 3. Start React Frontend in Current Terminal
Write-Host "Starting React Frontend..." -ForegroundColor Cyan
Set-Location "C:\Users\riddh\omnibot-platform\frontend"
Remove-Item -Recurse -Force "node_modules/.vite" -ErrorAction SilentlyContinue
cmd /c "npm run dev -- --host"
