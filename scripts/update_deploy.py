#!/usr/bin/env python3
"""Execute deployment commands on the server after jar upload completes."""
import subprocess
import sys
import os

HOST = "124.222.155.20"
USERNAME = "root"
KEY_PATH = os.path.expandvars(r"D:\chrome_download\server_ssh_key_2026.pem")

commands = """
set -e
cd /data/wealth-service-platform/update

# Extract jar
echo "=== Extracting ==="
tar -xzf wealth-service.tar.gz

# Build updated image
echo "=== Building wealth-service:updated ==="
cat > Dockerfile.service << 'DEOF'
FROM ghcr.io/renjianfeng8/wealth-service-platform/wealth-service:latest
COPY wealth-service-1.0.0.jar /app/app.jar
DEOF

docker build -t wealth-service:updated -f Dockerfile.service .
echo "  done"

echo "=== Image built ==="
docker images --format '{{.Repository}}:{{.Tag}}' | grep ':updated'
"""

ssh_cmd = [
    "ssh",
    "-i", KEY_PATH,
    "-o", "StrictHostKeyChecking=no",
    "-o", "ConnectTimeout=30",
    f"{USERNAME}@{HOST}",
    commands,
]

print("Starting deployment on server...")
try:
    result = subprocess.run(ssh_cmd, capture_output=True, text=True, timeout=600)
    if result.stdout:
        print(result.stdout)
    if result.stderr:
        print("STDERR:", result.stderr[:500], file=sys.stderr)
    print(f"Exit code: {result.returncode}")
    sys.exit(result.returncode)
except subprocess.TimeoutExpired:
    print("Deployment timed out", file=sys.stderr)
    sys.exit(1)
except FileNotFoundError:
    print("SSH client not found — ensure OpenSSH Client is installed", file=sys.stderr)
    sys.exit(1)
except Exception as e:
    print(f"Error: {e}", file=sys.stderr)
    sys.exit(1)
