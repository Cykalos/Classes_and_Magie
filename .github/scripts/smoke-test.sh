#!/usr/bin/env bash
# Starts a real Paper server with the plugin and checks that it enables without errors, twice:
#   1. fresh install       → English files and English folder names
#   2. old French install  → files at the root are moved to the French folders, language: fr is added
# Usage: smoke-test.sh <Paper version (e.g. 26.2)> <plugin jar>
set -euo pipefail
VERSION="$1"
JAR="$(realpath "$2")"
ROOT="$(pwd)"
WORK="$(mktemp -d)"
cd "$WORK"

URL=$(curl -fsSL "https://fill.papermc.io/v3/projects/paper/versions/${VERSION}/builds" \
  | jq -r '[.[] | select(.channel == "STABLE" or .channel == "BETA" or .channel == "ALPHA")][0].downloads."server:default".url')
if [[ -z "$URL" || "$URL" == "null" ]]; then
  echo "::warning::No Paper ${VERSION} build found: smoke test skipped."
  exit 0
fi
curl -fsSL -o paper.jar "$URL"

FAIL=0

# run_server <folder> <label>: starts the server in <folder>, reloads the plugin, stops it.
run_server() {
  local dir="$1" label="$2"
  cd "$dir"
  cp "$WORK/paper.jar" .
  echo "eula=true" > eula.txt
  printf 'online-mode=false\nlevel-type=minecraft\\:flat\ngenerate-structures=false\nspawn-protection=0\n' > server.properties
  mkdir -p plugins
  cp "$JAR" plugins/
  mkfifo console
  java -Xmx2G -jar paper.jar --nogui < console > server.log 2>&1 &
  local pid=$!
  exec 3>console
  for _ in $(seq 1 300); do
    grep -q "Done (" server.log && break
    kill -0 "$pid" 2>/dev/null || break
    sleep 1
  done
  echo "classe admin reload" >&3
  sleep 5
  echo "stop" >&3
  wait "$pid" || true
  exec 3>&-

  echo "----- [$label] server log (plugin) -----"
  grep -iE "Classes_and_Magie|cykclasses|quests loaded|titles loaded|moved" server.log || true
  echo "----- [$label] plugin folder -----"
  find plugins/Classes_and_Magie -maxdepth 2 | sort

  summary() { grep -m1 -E "$1" server.log 2>/dev/null | sed -E 's/^\[[^]]*\] *//' | cut -c1-180; }
  echo "::notice title=$label · Paper::$(summary 'This server is running|Starting minecraft server version')"
  echo "::notice title=$label · Content::$(summary 'races, .* traits')"
  echo "::notice title=$label · Quests::$(grep -E 'quests loaded' server.log | tail -1 | sed -E 's/^\[[^]]*\] *//')"
  echo "::notice title=$label · Titles::$(grep -E 'titles loaded' server.log | tail -1 | sed -E 's/^\[[^]]*\] *//')"
  echo "::notice title=$label · Files::$(cd plugins/Classes_and_Magie && find . -type f | sort | tr '\n' ' ' | cut -c1-900)"
  grep -iE "WARN.*(cykclasses|Classes_and_Magie)|Quest ignored|Title ignored" server.log | head -10 \
    | while IFS= read -r l; do echo "::warning::[$label] ${l:0:300}"; done || true
  grep -q "Done (" server.log || { echo "::error::[$label] The server did not start"; FAIL=1; }
  grep -q "quests loaded" server.log || { echo "::error::[$label] Quests were not loaded"; FAIL=1; }
  grep -q "titles loaded" server.log || { echo "::error::[$label] Titles were not loaded"; FAIL=1; }
  if grep -nE "Error occurred while enabling|Could not load 'plugins/Classes_and_Magie|Quest ignored|Title ignored|Exception" server.log \
      | grep -iE "cykclasses|Classes_and_Magie|Quest|Title" ; then
    echo "::error::[$label] Plugin error during startup"; FAIL=1
  fi
  cd "$WORK"
}

# expect <folder> <label> <file…>: files that must exist in the plugin folder
expect() {
  local dir="$1" label="$2"; shift 2
  for f in "$@"; do
    [[ -e "$dir/plugins/Classes_and_Magie/$f" ]] || { echo "::error::[$label] Missing file: $f"; FAIL=1; }
  done
}

# ── 1. Fresh install: English ─────────────────────────────────────────────────
mkdir -p fresh
run_server "$WORK/fresh" "Fresh install"
expect fresh "Fresh install" config.yml classes/classes.yml character/races.yml character/titles.yml jobs/jobs.yml \
  items/kits.yml display/messages.yml display/hud.yml quests/tutorial.yml quests/main.yml
grep -q "^language: en" fresh/plugins/Classes_and_Magie/config.yml || { echo "::error::[Fresh install] language: en missing"; FAIL=1; }
grep -q "warrior:" fresh/plugins/Classes_and_Magie/classes/classes.yml || { echo "::error::[Fresh install] classes.yml is not in English"; FAIL=1; }

# ── 2. Old French install: files at the root, no language key ─────────────────
mkdir -p old/plugins/Classes_and_Magie/playerdata
for f in classes races jobs messages hud; do
  cp "$ROOT/src/main/resources/lang/fr/$f.yml" "old/plugins/Classes_and_Magie/$f.yml"
done
grep -v "^language:" "$ROOT/src/main/resources/lang/fr/config.yml" > old/plugins/Classes_and_Magie/config.yml
echo "name: Ancien" > old/plugins/Classes_and_Magie/playerdata/00000000-0000-0000-0000-000000000000.yml
run_server "$WORK/old" "Old French install"
expect old "Old French install" classes/classes.yml personnage/races.yml metiers/jobs.yml affichage/messages.yml \
  affichage/hud.yml quetes/tutoriel.yml personnage/titles.yml donnees/joueurs/00000000-0000-0000-0000-000000000000.yml
for f in classes.yml races.yml jobs.yml messages.yml hud.yml playerdata; do
  [[ ! -e "old/plugins/Classes_and_Magie/$f" ]] || { echo "::error::[Old French install] Old file not moved: $f"; FAIL=1; }
done
grep -q "^language: fr" old/plugins/Classes_and_Magie/config.yml || { echo "::error::[Old French install] language: fr was not added"; FAIL=1; }
grep -q "guerrier:" old/plugins/Classes_and_Magie/classes/classes.yml || { echo "::error::[Old French install] classes.yml is no longer French"; FAIL=1; }

exit $FAIL
