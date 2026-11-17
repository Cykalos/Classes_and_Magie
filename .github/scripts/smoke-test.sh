#!/usr/bin/env bash
# Starts a real Paper server with the plugin and checks that it enables without errors, in two cases:
#   1. an old French installation (files at the root, config.yml without "language"): the files must be
#      moved to the French theme folders, the player data kept, and "language: fr" added;
#   2. a new installation: every file must be created in English, in the English folders.
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
  echo "::warning::No Paper ${VERSION} build found: startup test skipped."
  exit 0
fi
curl -fsSL -o paper.jar "$URL"
FAIL=0

# run <folder>: starts the server in that folder, reloads the plugin, stops it
run() {
  local dir="$1"
  cd "$WORK/$dir"
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
  echo "----- [$dir] server log (plugin) -----"
  grep -iE "Classes_and_Magie|cykclasses" server.log || true
  echo "----- [$dir] plugin folder -----"
  find plugins/Classes_and_Magie -maxdepth 2 | sort
  summary() { grep -m1 -E "$1" server.log 2>/dev/null | sed -E 's/^\[[^]]*\] *//' | cut -c1-180; }
  echo "::notice title=$dir - Paper::$(summary 'This server is running|Starting minecraft server version')"
  echo "::notice title=$dir - Content::$(summary 'races, .* traits')"
  echo "::notice title=$dir - Classes::$(grep -E 'skills loaded' server.log | tail -1 | sed -E 's/^\[[^]]*\] *//')"
  echo "::notice title=$dir - Quests::$(grep -E 'quests loaded' server.log | tail -1 | sed -E 's/^\[[^]]*\] *//')"
  echo "::notice title=$dir - Titles::$(grep -E 'titles loaded' server.log | tail -1 | sed -E 's/^\[[^]]*\] *//')"
  echo "::notice title=$dir - Files::$(cd plugins/Classes_and_Magie && find . -type f | sort | tr '\n' ' ' | cut -c1-900)"
  grep -iE "WARN.*(cykclasses|Classes_and_Magie)|Quest ignored|Title ignored|missing message" server.log | head -10 \
    | while IFS= read -r l; do echo "::warning::$dir: ${l:0:300}"; done || true
  grep -q "Done (" server.log || { echo "::error::$dir: the server did not start"; FAIL=1; }
  grep -q "quests loaded" server.log || { echo "::error::$dir: quests were not loaded"; FAIL=1; }
  grep -q "titles loaded" server.log || { echo "::error::$dir: titles were not loaded"; FAIL=1; }
  if grep -nE "Error occurred while enabling|Could not load 'plugins/Classes_and_Magie|Quest ignored|Title ignored|Exception" server.log \
      | grep -iE "cykclasses|Classes_and_Magie|Quest|Title" ; then
    echo "::error::$dir: plugin error during startup"; FAIL=1
  fi
  cd "$WORK"
}

expect_files() {
  local dir="$1"; shift
  for f in "$@"; do
    [[ -f "$WORK/$dir/plugins/Classes_and_Magie/$f" ]] || { echo "::error::$dir: missing file $f"; FAIL=1; }
  done
}

# ---------------------------------------------------------------- 1. Old French installation
mkdir -p old/plugins/Classes_and_Magie/playerdata
FR="$ROOT/src/main/resources/lang/fr"
grep -v "^language:" "$FR/config.yml" > old/plugins/Classes_and_Magie/config.yml
for f in classes races jobs messages hud; do
  cp "$FR/$f.yml" "old/plugins/Classes_and_Magie/$f.yml"
done
echo "name: Ancien" > old/plugins/Classes_and_Magie/playerdata/00000000-0000-0000-0000-000000000000.yml
run old
expect_files old classes/classes.yml personnage/races.yml metiers/jobs.yml affichage/messages.yml affichage/hud.yml \
  quetes/tutoriel.yml personnage/titles.yml donnees/joueurs/00000000-0000-0000-0000-000000000000.yml
for f in classes.yml races.yml jobs.yml messages.yml hud.yml playerdata; do
  [[ ! -e "old/plugins/Classes_and_Magie/$f" ]] || { echo "::error::old: file not moved: $f"; FAIL=1; }
done
grep -q "^language: fr" old/plugins/Classes_and_Magie/config.yml || { echo "::error::old: language: fr was not added"; FAIL=1; }
grep -q "language: fr" old/server.log || { echo "::error::old: the plugin did not start in French"; FAIL=1; }

# ---------------------------------------------------------------- 2. New installation (English)
mkdir -p new
run new
expect_files new config.yml classes/classes.yml character/races.yml character/titles.yml jobs/jobs.yml \
  items/kits.yml display/messages.yml display/hud.yml quests/tutorial.yml quests/main.yml
grep -q "^language: en" new/plugins/Classes_and_Magie/config.yml || { echo "::error::new: config.yml is not in English"; FAIL=1; }
grep -q "Choose your class" new/plugins/Classes_and_Magie/display/messages.yml || { echo "::error::new: messages.yml is not in English"; FAIL=1; }
grep -q "language: en" new/server.log || { echo "::error::new: the plugin did not start in English"; FAIL=1; }
for d in personnage metiers objets affichage quetes donnees; do
  [[ ! -e "new/plugins/Classes_and_Magie/$d" ]] || { echo "::error::new: French folder created: $d"; FAIL=1; }
done

exit $FAIL
