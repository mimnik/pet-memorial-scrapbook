$ErrorActionPreference = 'Stop'

function Stop-ProcessOnPort {
    param([int]$Port)

    $conn = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue
    if ($null -eq $conn) {
        Write-Host "No listener found on port $Port."
        return
    }

    $pids = $conn | Select-Object -ExpandProperty OwningProcess -Unique
    foreach ($processId in $pids) {
        try {
            Stop-Process -Id $processId -Force -ErrorAction Stop
            Write-Host "Stopped PID $processId on port $Port."
        }
        catch {
            Write-Warning "Failed to stop PID $processId on port ${Port}: $($_.Exception.Message)"
        }
    }
}

Stop-ProcessOnPort -Port 8080
Stop-ProcessOnPort -Port 3000

Get-Process -Name 'frpc' -ErrorAction SilentlyContinue | ForEach-Object {
    try {
        Stop-Process -Id $_.Id -Force -ErrorAction Stop
        Write-Host "Stopped frpc PID $($_.Id)."
    }
    catch {
        Write-Warning "Failed to stop frpc PID $($_.Id): $($_.Exception.Message)"
    }
}

Write-Host 'Done.'
