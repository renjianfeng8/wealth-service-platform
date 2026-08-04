# Generate SSL certificates for development or production
#
# Usage:
#   .\deploy\ssl\gen-cert.ps1                     # ECDSA self-signed dev cert (default)
#   .\deploy\ssl\gen-cert.ps1 -Type prod -Domain example.com   # RSA CSR for CA signing
#   .\deploy\ssl\gen-cert.ps1 -Type dev -Algorithm RSA  # RSA self-signed dev cert
#   .\deploy\ssl\gen-cert.ps1 -Type pfx           # Convert existing .crt+.key to PKCS12
#
# Production renewal workflow:
#   1. .\deploy\ssl\gen-cert.ps1 -Type prod -Domain rjfwealth.cn
#   2. Submit deploy\ssl\wealth.csr to CA → receive wealth.crt + chain
#   3. Place signed cert in deploy\ssl\wealth.crt
#   4. .\deploy\ssl\gen-cert.ps1 -Type pfx
#   5. Copy deploy\ssl\wealth.pfx to gateway resources
#   6. Restart gateway

param(
    [ValidateSet("dev", "prod", "pfx")]
    [string]$Type = "dev",

    [ValidateSet("RSA", "ECDSA")]
    [string]$Algorithm = "ECDSA",

    [string]$Domain = "localhost"
)

$projectRoot = Split-Path $PSScriptRoot -Parent
$sslDir = Join-Path $projectRoot "ssl"
$gatewaySslDir = Join-Path $projectRoot "..\backend\wealth-gateway\src\main\resources\ssl"

if (-not (Test-Path $sslDir)) {
    New-Item -ItemType Directory -Path $sslDir -Force | Out-Null
}

if ($Type -eq "dev") {
    if ($Algorithm -eq "ECDSA") {
        Write-Host "Generating ECDSA P-256 self-signed cert (dev)..." -ForegroundColor Green
        docker run --rm -v "${sslDir}:/certs" alpine:latest sh -c "apk add --no-cache openssl >/dev/null 2>&1 && openssl ecparam -genkey -name prime256v1 -out /certs/wealth.key && openssl req -x509 -days 1825 -key /certs/wealth.key -out /certs/wealth.crt -subj '/CN=localhost' -addext 'subjectAltName=DNS:localhost,IP:127.0.0.1'"
    } else {
        Write-Host "Generating RSA 2048 self-signed cert (dev)..." -ForegroundColor Green
        docker run --rm -v "${sslDir}:/certs" alpine:latest sh -c "apk add --no-cache openssl >/dev/null 2>&1 && openssl req -x509 -nodes -days 1825 -newkey rsa:2048 -keyout /certs/wealth.key -out /certs/wealth.crt -subj '/CN=localhost' -addext 'subjectAltName=DNS:localhost,IP:127.0.0.1'"
    }
} elseif ($Type -eq "prod") {
    # Production: generate a CSR for CA signing / renewal
    Write-Host "Generating RSA 2048 key + CSR for $Domain ..." -ForegroundColor Yellow
    Write-Host "Submit wealth.csr to your CA (TrustAsia / Let's Encrypt, etc.)" -ForegroundColor Yellow
    # Preserve existing key if present (renewal without losing the key)
    $keyExists = Test-Path (Join-Path $sslDir "wealth.key")
    if ($keyExists) {
        Write-Host "Using existing wealth.key (preserve for cert renewal)" -ForegroundColor Cyan
        docker run --rm -v "${sslDir}:/certs" alpine:latest sh -c "apk add --no-cache openssl >/dev/null 2>&1 && openssl req -new -key /certs/wealth.key -out /certs/wealth.csr -subj '/CN=$Domain' -addext 'subjectAltName=DNS:$Domain,DNS:www.$Domain'"
    } else {
        docker run --rm -v "${sslDir}:/certs" alpine:latest sh -c "apk add --no-cache openssl >/dev/null 2>&1 && openssl req -nodes -new -newkey rsa:2048 -keyout /certs/wealth.key -out /certs/wealth.csr -subj '/CN=$Domain' -addext 'subjectAltName=DNS:$Domain,DNS:www.$Domain'"
    }
    Write-Host "CSR generated: $sslDir\wealth.csr" -ForegroundColor Yellow
    Write-Host "After CA signs it, replace deploy\ssl\wealth.crt with the issued certificate + chain." -ForegroundColor Yellow
    Write-Host "Then run: .\deploy\ssl\gen-cert.ps1 -Type pfx" -ForegroundColor Yellow
} else {
    # PKCS12 conversion: convert existing .crt + .key → .pfx for SpringBoot
    $crtPath = Join-Path $sslDir "wealth.crt"
    $keyPath = Join-Path $sslDir "wealth.key"
    if (-not (Test-Path $crtPath) -or -not (Test-Path $keyPath)) {
        Write-Host "ERROR: wealth.crt and wealth.key must exist in $sslDir" -ForegroundColor Red
        exit 1
    }
    # Generate random password
    $pass = -join ((65..90) + (97..122) + (48..57) | Get-Random -Count 32 | ForEach-Object {[char]$_})
    $pass | Out-File (Join-Path $sslDir ".pfx-pass.txt") -Encoding utf8 -NoNewline
    Write-Host "PKCS12 password saved to deploy\ssl\.pfx-pass.txt" -ForegroundColor Cyan
    # Convert
    docker run --rm -v "${sslDir}:/certs" alpine:latest sh -c "apk add --no-cache openssl >/dev/null 2>&1 && openssl pkcs12 -export -in /certs/wealth.crt -inkey /certs/wealth.key -out /certs/wealth.pfx -passout pass:$pass"
    Write-Host "PKCS12 generated: $sslDir\wealth.pfx" -ForegroundColor Green
    # Copy to gateway resources
    if (-not (Test-Path $gatewaySslDir)) {
        New-Item -ItemType Directory -Path $gatewaySslDir -Force | Out-Null
    }
    Copy-Item (Join-Path $sslDir "wealth.pfx") (Join-Path $gatewaySslDir "wealth.pfx") -Force
    Write-Host "Copied to $gatewaySslDir\wealth.pfx" -ForegroundColor Green
    Write-Host ""
    Write-Host "Update gateway's application-prod.yml with this password:" -ForegroundColor Yellow
    Write-Host "  key-store-password: $pass" -ForegroundColor Yellow
}

Write-Host "`nFiles in $sslDir :"
Get-ChildItem $sslDir | Select-Object Name, Length
