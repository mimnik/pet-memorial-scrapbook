param(
    [switch]$UseMySQL,
    [switch]$SkipTests = $true,
    [switch]$CleanPorts = $true,
    [switch]$SkipFrp = $false
)

$ErrorActionPreference = 'Stop'

function Stop-ProcessOnPort {
    param([int]$Port)

    $conn = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue
    if ($null -eq $conn) {
        return
    }

    $pids = $conn | Select-Object -ExpandProperty OwningProcess -Unique
    foreach ($processId in $pids) {
        try {
            Stop-Process -Id $processId -Force -ErrorAction Stop
            Write-Host "Stopped process on port $Port (PID=$processId)."
        }
        catch {
            Write-Warning "Failed to stop PID $processId on port ${Port}: $($_.Exception.Message)"
        }
    }
}

function Ensure-NodeToolchain {
    param([string]$RepoRoot)

    $nodeReady = $false
    try {
        if ((Get-Command node -ErrorAction SilentlyContinue) -and (Get-Command npm -ErrorAction SilentlyContinue)) {
            $null = & node -v
            $null = & npm -v
            $nodeReady = $true
        }
    }
    catch {
        $nodeReady = $false
    }

    if ($nodeReady) {
        return
    }

    $portableNodeDir = $null
    $preferredPortableDir = Join-Path $RepoRoot '.tools\node'
    if (Test-Path (Join-Path $preferredPortableDir 'node.exe')) {
        $portableNodeDir = $preferredPortableDir
    }

    if (-not $portableNodeDir) {
        $toolsDir = Join-Path $RepoRoot '.tools'
        if (Test-Path $toolsDir) {
            $candidate = Get-ChildItem -Path $toolsDir -Directory -Filter 'node-v*-win-x64' |
                Where-Object { Test-Path (Join-Path $_.FullName 'node.exe') } |
                Sort-Object Name -Descending |
                Select-Object -First 1
            if ($candidate) {
                $portableNodeDir = $candidate.FullName
            }
        }
    }

    if ($portableNodeDir) {
        $env:Path = "$portableNodeDir;$env:Path"
        Write-Host "Using portable Node.js from $portableNodeDir"
    }
}

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$repoRoot = Resolve-Path (Join-Path $scriptDir '..')
$backendDir = Join-Path $repoRoot 'backend'
$frontendDir = Join-Path $repoRoot 'frontend'
$frpDir = Join-Path $repoRoot 'frp'
$frpcExe = Join-Path $frpDir 'frpc.exe'
$frpcConfig = Join-Path $frpDir 'frpc.toml'

Ensure-NodeToolchain -RepoRoot $repoRoot

if ($CleanPorts) {
    Stop-ProcessOnPort -Port 8080
    Stop-ProcessOnPort -Port 3000
}

if (-not (Get-Command java -ErrorAction SilentlyContinue)) {
    throw 'java command not found. Please install JDK 21 first.'
}

try {
    $nodeVersion = (& node -v).Trim()
    $npmVersion = (& npm -v).Trim()
    Write-Host "Detected Node.js $nodeVersion, npm $npmVersion"
}
catch {
    throw 'node/npm not usable. Please install Node.js 20.19+ or 22.12+ (npm 10+) and ensure PATH is correct.'
}

Push-Location $backendDir
try {
    $buildCmd = '.\\mvnw.cmd -q package'
    if ($SkipTests) {
        $buildCmd = '.\\mvnw.cmd -q -DskipTests package'
    }

    Write-Host 'Building backend...'
    cmd /c $buildCmd
    if ($LASTEXITCODE -ne 0) {
        throw "Backend build failed, exit code: $LASTEXITCODE"
    }
}
finally {
    Pop-Location
}

$backendArgs = '-jar target\\scrapbook-backend-0.0.1-SNAPSHOT.jar'
if ($UseMySQL) {
    $backendArgs += ' --spring.profiles.active=mysql'
}

$backendCmd = "Set-Location '$backendDir'; java $backendArgs"
$frontendCmd = "Set-Location '$frontendDir'; npm run dev"

$backendProc = Start-Process -FilePath 'powershell' -ArgumentList '-NoExit', '-Command', $backendCmd -PassThru
$frontendProc = Start-Process -FilePath 'powershell' -ArgumentList '-NoExit', '-Command', $frontendCmd -PassThru

$frpcProc = $null
if (-not $SkipFrp) {
    Get-Process -Name 'frpc' -ErrorAction SilentlyContinue | ForEach-Object {
        try {
            Stop-Process -Id $_.Id -Force -ErrorAction Stop
            Write-Host "Stopped existing frpc process (PID=$($_.Id))."
        }
        catch {
            Write-Warning "Failed to stop existing frpc process PID=$($_.Id): $($_.Exception.Message)"
        }
    }

    if ((Test-Path $frpcExe) -and (Test-Path $frpcConfig)) {
        $frpcCmd = "Set-Location '$frpDir'; .\\frpc.exe -c .\\frpc.toml"
        $frpcProc = Start-Process -FilePath 'powershell' -ArgumentList '-NoExit', '-Command', $frpcCmd -PassThru
    }
    else {
        Write-Warning "frpc not started because file is missing: $frpcExe or $frpcConfig"
    }
}

Write-Host ''
Write-Host 'Dev services started:'
Write-Host "- Backend PID: $($backendProc.Id), URL: http://localhost:8080"
Write-Host "- Frontend PID: $($frontendProc.Id), URL: http://localhost:3000"
if ($frpcProc) {
    Write-Host "- FRP PID: $($frpcProc.Id), frontend tunnel: http://47.119.131.127:25565"
}
Write-Host ''
Write-Host 'Use scripts/stop-dev.ps1 to stop services.'
