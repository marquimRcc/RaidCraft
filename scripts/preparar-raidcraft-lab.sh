#!/usr/bin/env bash
# Local Paper 1.21.11 lab. Run with --accept-eula only after reviewing the Minecraft EULA.
set -Eeuo pipefail

ROOT="$HOME/RaidCraft-Lab"
ACCEPT_EULA=false
START_SERVER=false
while (( $# )); do
  case "$1" in
    --root) (( $# >= 2 )) || { echo "Missing --root path"; exit 2; }; ROOT="$2"; shift 2 ;;
    --accept-eula) ACCEPT_EULA=true; shift ;;
    --start) START_SERVER=true; shift ;;
    -h|--help) echo "Usage: bash preparar-raidcraft-lab.sh [--root PATH] [--accept-eula] [--start]"; exit 0 ;;
    *) echo "Unknown option: $1"; exit 2 ;;
  esac
done

for tool in curl unzip sha256sum python3 java javac; do
  command -v "$tool" >/dev/null || { echo "Missing dependency: $tool"; exit 1; }
done
JAVA_VERSION="$(java -version 2>&1 | head -n 1)"
JAVAC_VERSION="$(javac -version 2>&1)"
if ! grep -Eq 'version "21([.]|")|^(openjdk|java) 21([.]| )' <<< "$JAVA_VERSION" ||
   ! grep -Eq '^javac 21([.]|$)' <<< "$JAVAC_VERSION"; then
  echo "Java 21 JDK is required. Found: $JAVA_VERSION; $JAVAC_VERSION"
  echo "Install Temurin 21: https://adoptium.net/temurin/releases/?version=21"
  exit 1
fi

mkdir -p "$ROOT/downloads" "$ROOT/source" "$ROOT/tools" "$ROOT/server/plugins"
ZIP="$ROOT/downloads/RaidCraft-main.zip"
echo "[1/4] Downloading public RaidCraft source..."
curl --fail --silent --show-error --location --retry 3 \
  -o "$ZIP" "https://github.com/marquimRcc/RaidCraft/archive/refs/heads/main.zip"
unzip -oq "$ZIP" -d "$ROOT/source"
SOURCE="$ROOT/source/RaidCraft-main"
[[ -f "$SOURCE/build.gradle.kts" ]] || { echo "RaidCraft source not found."; exit 1; }

if command -v gradle >/dev/null && gradle --version | grep -q '^Gradle 8[.]14'; then
  GRADLE="$(command -v gradle)"
else
  echo "[2/4] Getting portable Gradle 8.14..."
  DIST="$ROOT/downloads/gradle-8.14-bin.zip"
  EXPECTED="$(curl --fail --silent --show-error --location --retry 3 \
    https://services.gradle.org/distributions/gradle-8.14-bin.zip.sha256)"
  if [[ ! -f "$DIST" ]] || ! echo "$EXPECTED  $DIST" | sha256sum -c --status; then
    curl --fail --silent --show-error --location --retry 3 \
      -o "$DIST" https://services.gradle.org/distributions/gradle-8.14-bin.zip
    echo "$EXPECTED  $DIST" | sha256sum -c
  fi
  unzip -oq "$DIST" -d "$ROOT/tools"
  GRADLE="$ROOT/tools/gradle-8.14/bin/gradle"
fi

echo "[2/4] Compiling the RaidCraft plugin..."
"$GRADLE" --project-dir "$SOURCE" --no-daemon clean build
JAR="$SOURCE/build/libs/RaidCraft-0.1.0.jar"
[[ -s "$JAR" ]] || { echo "Plugin JAR was not created."; exit 1; }
cp "$JAR" "$ROOT/server/plugins/RaidCraft-0.1.0.jar"

echo "[3/4] Downloading verified Paper 1.21.11..."
META="$ROOT/downloads/paper-builds.json"
curl --fail --silent --show-error --location --retry 3 \
  --header 'User-Agent: RaidCraft-Local-Lab/0.1 (https://github.com/marquimRcc/RaidCraft)' \
  -o "$META" https://fill.papermc.io/v3/projects/paper/versions/1.21.11/builds
PAPER_META="$(python3 - "$META" <<'PY'
import json, sys
with open(sys.argv[1], encoding="utf-8") as f:
    builds = json.load(f)
for build in builds:
    info = build.get("downloads", {}).get("server:default", {})
    if build.get("channel") == "STABLE" and info.get("url") and info.get("checksums", {}).get("sha256"):
        print(info["url"], info["checksums"]["sha256"])
        break
else:
    sys.exit("No stable Paper 1.21.11 build with SHA-256 found.")
PY
)"
read -r PAPER_URL PAPER_SHA <<< "$PAPER_META"
PAPER_JAR="$ROOT/server/paper.jar"
if [[ ! -s "$PAPER_JAR" ]] || ! echo "$PAPER_SHA  $PAPER_JAR" | sha256sum -c --status; then
  curl --fail --silent --show-error --location --retry 3 \
    --header 'User-Agent: RaidCraft-Local-Lab/0.1 (https://github.com/marquimRcc/RaidCraft)' \
    -o "$PAPER_JAR.tmp" "$PAPER_URL"
  echo "$PAPER_SHA  $PAPER_JAR.tmp" | sha256sum -c
  mv -f "$PAPER_JAR.tmp" "$PAPER_JAR"
fi

# Bind to loopback only; keep Minecraft account authentication enabled.
if [[ ! -f "$ROOT/server/server.properties" ]]; then
  cat > "$ROOT/server/server.properties" <<'PROPERTIES'
server-ip=127.0.0.1
server-port=25565
online-mode=true
max-players=2
view-distance=4
simulation-distance=4
spawn-protection=0
motd=RaidCraft local test server
PROPERTIES
fi

EULA_FILE="$ROOT/server/eula.txt"
if [[ "$ACCEPT_EULA" == true ]]; then
  printf 'eula=true\n' > "$EULA_FILE"
elif [[ ! -f "$EULA_FILE" ]]; then
  printf 'eula=false\n' > "$EULA_FILE"
fi

echo "[4/4] Ready: $ROOT/server"
echo "To join, start Minecraft Java 1.21.11 and connect to 127.0.0.1."
echo "In the Paper console, execute: op YOUR_MINECRAFT_NICK"
echo "Then look at a stone block and run: /rc create diamond01 DIAMOND 2 30"

if [[ "$START_SERVER" == true ]]; then
  if ! grep -Eq '^[[:space:]]*eula=true[[:space:]]*$' "$EULA_FILE"; then
    echo "EULA not accepted. Read https://aka.ms/MinecraftEULA and re-run with --accept-eula --start."
    exit 2
  fi
  echo "Starting Paper. Press Ctrl+C to stop."
  cd "$ROOT/server"
  exec java -Xms512M -Xmx2G -jar paper.jar --nogui
else
  echo "Start later: bash scripts/preparar-raidcraft-lab.sh --accept-eula --start"
fi
