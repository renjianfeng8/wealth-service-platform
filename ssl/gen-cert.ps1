# Generate SSL certificates for development or production
#
# Usage:
#   .\ssl\gen-cert.ps1              # Generate ECDSA self-signed dev cert (default)
#   .\ssl\gen-cert.ps1 -Type prod   # Generate RSA CSR for production CA signing
#   .\ssl\gen-cert.ps1 -Type dev -Algorithm RSA  # Generate RSA self-signed dev cert
#
# Production: submit the generated .csr to your CA (Let's Encrypt, etc.)
# Then place the signed cert + chain in ssl/wealth.crt and keep wealth.key secret.

param(
    [ValidateSet("dev", "prod")]
    [string]$Type = "dev",

    [ValidateSet("RSA", "ECDSA")]
    [string]$Algorithm = "ECDSA"
)

$projectRoot = Split-Path $PSScriptRoot -Parent
$sslDir = Join-Path $projectRoot "ssl"

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
} else {
    # Production: generate a CSR for CA signing
    Write-Host "Generating RSA 2048 key + CSR for production CA..." -ForegroundColor Yellow
    Write-Host "Submit wealth.csr to your CA (Let's Encrypt, etc.)" -ForegroundColor Yellow
    docker run --rm -v "${sslDir}:/certs" alpine:latest sh -c "apk add --no-cache openssl >/dev/null 2>&1 && openssl req -nodes -new -newkey rsa:2048 -keyout /certs/wealth.key -out /certs/wealth.csr -subj '/CN=your-production-domain.com' -addext 'subjectAltName=DNS:your-production-domain.com'"
    Write-Host "CSR generated: $sslDir\wealth.csr" -ForegroundColor Yellow
    Write-Host "After CA signs it, replace wealth.crt with the issued certificate + chain." -ForegroundColor Yellow
}

Write-Host "`nFiles in $sslDir :"
Get-ChildItem $sslDir | Select-Object Name, Length
