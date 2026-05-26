#!/usr/bin/env python3
"""Upload the wealth-service JAR to the server via scp."""
import subprocess
import sys
import os

HOST = "124.222.155.20"
USERNAME = "root"
KEY_PATH = os.path.expandvars(r"D:\chrome_download\server_ssh_key_2026.pem")
LOCAL_PATH = os.path.expandvars(r"D:\demo\wealth-service-platform\wealth-service\target\wealth-service-1.0.0.jar")
REMOTE_DIR = "/data/wealth-service-platform/update/"

# Prepare local archive
archive_path = os.path.expandvars(r"D:\demo\wealth-service-platform\.wealth-service.tar.gz")
import tarfile
with tarfile.open(archive_path, "w:gz") as tar:
    tar.add(LOCAL_PATH, arcname="wealth-service-1.0.0.jar")
print(f"Archived {LOCAL_PATH} -> {archive_path}")

# Ensure remote directory exists
mkdir_cmd = [
    "ssh",
    "-i", KEY_PATH,
    "-o", "StrictHostKeyChecking=no",
    "-o", "ConnectTimeout=10",
    f"{USERNAME}@{HOST}",
    f"mkdir -p {REMOTE_DIR}",
]
subprocess.run(mkdir_cmd, capture_output=True, timeout=30, check=True)

# Upload via scp
size = os.path.getsize(archive_path)
remote_path = os.path.join(REMOTE_DIR, "wealth-service.tar.gz").replace("\\", "/")
print(f"Uploading {size / 1024 / 1024:.1f}MB -> {remote_path}")

scp_cmd = [
    "scp",
    "-i", KEY_PATH,
    "-o", "StrictHostKeyChecking=no",
    "-o", "ConnectTimeout=30",
    archive_path,
    f"{USERNAME}@{HOST}:{remote_path}",
]
try:
    result = subprocess.run(scp_cmd, capture_output=True, text=True, timeout=600)
    if result.returncode == 0:
        print("Upload complete!")
    else:
        print(f"Upload failed: {result.stderr.strip()}", file=sys.stderr)
        sys.exit(1)
except subprocess.TimeoutExpired:
    print("Upload timed out", file=sys.stderr)
    sys.exit(1)
except FileNotFoundError:
    print("scp client not found — ensure OpenSSH Client is installed", file=sys.stderr)
    sys.exit(1)
finally:
    if os.path.exists(archive_path):
        os.remove(archive_path)
