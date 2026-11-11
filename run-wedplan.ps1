$ErrorActionPreference = "Stop"
Set-Location -LiteralPath $PSScriptRoot

$savedDataSource = @{}
$savedProperties = & git -C $PSScriptRoot show "HEAD:src/main/resources/application.properties" 2>$null
if ($LASTEXITCODE -eq 0) {
    foreach ($line in $savedProperties) {
        if ($line -match '^\s*(spring\.datasource\.(url|username|password))\s*=\s*(.*)\s*$') {
            $savedDataSource[$Matches[1]] = $Matches[3]
        }
    }
}

$databaseUrl = $env:DB_URL
if ([string]::IsNullOrWhiteSpace($databaseUrl)) {
    $databaseUrl = $savedDataSource["spring.datasource.url"]
    if ($databaseUrl -match '^\$\{DB_URL:([^}]*)\}$') {
        $databaseUrl = $Matches[1]
    }
}
if ([string]::IsNullOrWhiteSpace($databaseUrl)) {
    $databaseUrl = Read-Host "MySQL URL [jdbc:mysql://localhost:3306/wedplan]"
}
if ([string]::IsNullOrWhiteSpace($databaseUrl)) {
    $databaseUrl = "jdbc:mysql://localhost:3306/wedplan"
}

$databaseUsername = $env:DB_USERNAME
if ([string]::IsNullOrWhiteSpace($databaseUsername)) {
    $databaseUsername = $savedDataSource["spring.datasource.username"]
}
if ([string]::IsNullOrWhiteSpace($databaseUsername)) {
    $databaseUsername = Read-Host "MySQL username"
}
if ([string]::IsNullOrWhiteSpace($databaseUsername)) {
    throw "No saved MySQL username was found and none was entered."
}

$databasePasswordSecure = $null
if (-not [string]::IsNullOrWhiteSpace($env:DB_PASSWORD)) {
    $databasePasswordSecure = ConvertTo-SecureString $env:DB_PASSWORD -AsPlainText -Force
} elseif (-not [string]::IsNullOrWhiteSpace($savedDataSource["spring.datasource.password"])) {
    $databasePasswordSecure = ConvertTo-SecureString $savedDataSource["spring.datasource.password"] -AsPlainText -Force
} else {
    $databasePasswordSecure = Read-Host "MySQL password (input is hidden)" -AsSecureString
}
if ($databasePasswordSecure.Length -eq 0) {
    throw "No saved MySQL password was found and none was entered."
}

$adminUsername = $env:WEDPLAN_ADMIN_USERNAME
if ([string]::IsNullOrWhiteSpace($adminUsername)) {
    $adminUsername = Read-Host "WedPlan admin username"
}
if ([string]::IsNullOrWhiteSpace($adminUsername)) {
    throw "An admin username is required."
}

$adminPasswordSecure = $null
if (-not [string]::IsNullOrWhiteSpace($env:WEDPLAN_ADMIN_PASSWORD)) {
    $adminPasswordSecure = ConvertTo-SecureString $env:WEDPLAN_ADMIN_PASSWORD -AsPlainText -Force
} else {
    $adminPasswordSecure = Read-Host "WedPlan admin password (input is hidden)" -AsSecureString
}
if ($adminPasswordSecure.Length -eq 0) {
    throw "An admin password is required."
}

$databasePasswordPointer = [IntPtr]::Zero
$adminPasswordPointer = [IntPtr]::Zero
try {
    $databasePasswordPointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($databasePasswordSecure)
    $adminPasswordPointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($adminPasswordSecure)
    $env:DB_URL = $databaseUrl
    $env:DB_USERNAME = $databaseUsername
    $env:DB_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($databasePasswordPointer)
    $env:WEDPLAN_ADMIN_USERNAME = $adminUsername
    $env:WEDPLAN_ADMIN_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($adminPasswordPointer)

    $listeningPorts = @(Get-NetTCPConnection -State Listen -ErrorAction SilentlyContinue |
        Select-Object -ExpandProperty LocalPort -Unique)
    $port = 8080
    while ($listeningPorts -contains $port -and $port -le 8090) {
        $port++
    }
    if ($port -gt 8090) {
        throw "Ports 8080 through 8090 are in use. Stop an existing app and try again."
    }

    Write-Host "Starting the updated WedPlan project on port $port."
    Write-Host "After startup, open http://localhost:$port/ and http://localhost:$port/admin/login"
    Write-Host "Keep this PowerShell window open. Press Ctrl+C to stop the application."
    & .\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--server.port=$port"
    if ($LASTEXITCODE -ne 0) {
        throw "WedPlan exited with code $LASTEXITCODE. Check the startup error above; a MySQL access-denied message means the entered database username or password is incorrect."
    }
}
finally {
    foreach ($name in @("DB_URL", "DB_USERNAME", "DB_PASSWORD", "WEDPLAN_ADMIN_USERNAME", "WEDPLAN_ADMIN_PASSWORD")) {
        Remove-Item "Env:$name" -ErrorAction SilentlyContinue
    }
    if ($databasePasswordPointer -ne [IntPtr]::Zero) {
        [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($databasePasswordPointer)
    }
    if ($adminPasswordPointer -ne [IntPtr]::Zero) {
        [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($adminPasswordPointer)
    }
    $databasePasswordSecure.Dispose()
    $adminPasswordSecure.Dispose()
}
