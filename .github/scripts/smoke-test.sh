#!/usr/bin/env bash
# Démarre un vrai serveur Paper avec le plugin et vérifie qu'il s'active sans erreur.
# Simule aussi une ancienne installation (fichiers à la racine) pour tester le rangement automatique.
# Usage : smoke-test.sh <version Paper (ex. 26.2)> <jar du plugin>
set -euo pipefail
VERSION="$1"
JAR="$(realpath "$2")"
ROOT="$(pwd)"
DIR="$(mktemp -d)"
cd "$DIR"

URL=$(curl -fsSL "https://fill.papermc.io/v3/projects/paper/versions/${VERSION}/builds" \
  | jq -r '[.[] | select(.channel == "STABLE" or .channel == "BETA" or .channel == "ALPHA")][0].downloads."server:default".url')
if [[ -z "$URL" || "$URL" == "null" ]]; then
  echo "::warning::Aucun build Paper ${VERSION} trouvé : test de démarrage ignoré."
  exit 0
fi
curl -fsSL -o paper.jar "$URL"
echo "eula=true" > eula.txt
printf 'online-mode=false\nlevel-type=minecraft\\:flat\ngenerate-structures=false\nspawn-protection=0\n' > server.properties
mkdir -p plugins/Classes_and_Magie/playerdata
cp "$JAR" plugins/
# Ancienne installation : fichiers à la racine du dossier du plugin
for f in classes races jobs messages hud; do
  src=$(find "$ROOT/src/main/resources" -name "$f.yml" | head -1)
  cp "$src" "plugins/Classes_and_Magie/$f.yml"
done
echo "name: Ancien" > plugins/Classes_and_Magie/playerdata/00000000-0000-0000-0000-000000000000.yml

mkfifo console
java -Xmx2G -jar paper.jar --nogui < console > server.log 2>&1 &
PID=$!
exec 3>console
for _ in $(seq 1 300); do
  grep -q "Done (" server.log && break
  kill -0 "$PID" 2>/dev/null || break
  sleep 1
done
echo "classe admin reload" >&3
sleep 5
echo "stop" >&3
wait "$PID" || true

echo "----- Journal du serveur (plugin) -----"
grep -iE "Classes_and_Magie|cykclasses|quêtes|rangé" server.log || true
echo "----- Dossier du plugin -----"
find plugins/Classes_and_Magie -maxdepth 2 | sort

FAIL=0
summary() { grep -m1 -E "$1" server.log 2>/dev/null | sed -E 's/^\[[^]]*\] *//' | cut -c1-180; }
echo "::notice title=Paper::$(summary 'This server is running|Starting minecraft server version')"
echo "::notice title=Plugin::$(summary 'Enabling Classes_and_Magie')"
echo "::notice title=Rangement::$(summary 'rangé')"
echo "::notice title=Contenu::$(summary 'races, .* traits')"
echo "::notice title=Quêtes::$(grep -E 'quêtes chargées' server.log | tail -1 | sed -E 's/^\[[^]]*\] *//')"
echo "::notice title=Titres::$(grep -E 'titres chargés' server.log | tail -1 | sed -E 's/^\[[^]]*\] *//')"
echo "::notice title=Démarrage::$(summary 'Done \(')"
echo "::notice title=Fichiers::$(cd plugins/Classes_and_Magie && find . -type f | sort | tr '\n' ' ' | cut -c1-900)"
grep -iE "WARN.*(cykclasses|Classes_and_Magie)|Quête ignorée|Titre ignoré" server.log | head -10 | while IFS= read -r l; do echo "::warning::${l:0:300}"; done || true
grep -q "Done (" server.log || { echo "::error::Le serveur n'a pas démarré"; FAIL=1; }
grep -q "quêtes chargées" server.log || { echo "::error::Les quêtes ne se sont pas chargées"; FAIL=1; }
grep -q "titres chargés" server.log || { echo "::error::Les titres ne se sont pas chargés"; FAIL=1; }
if grep -nE "Error occurred while enabling|Could not load 'plugins/Classes_and_Magie|Quête ignorée|Titre ignoré|Exception" server.log \
    | grep -iE "cykclasses|Classes_and_Magie|Quête|Titre" ; then
  echo "::error::Erreur du plugin pendant le démarrage"; FAIL=1
fi
for f in classes/classes.yml personnage/races.yml metiers/jobs.yml affichage/messages.yml affichage/hud.yml \
         quetes/tutoriel.yml personnage/titles.yml donnees/joueurs/00000000-0000-0000-0000-000000000000.yml; do
  [[ -f "plugins/Classes_and_Magie/$f" ]] || { echo "::error::Fichier manquant après rangement : $f"; FAIL=1; }
done
for f in classes.yml races.yml jobs.yml messages.yml hud.yml playerdata; do
  [[ ! -e "plugins/Classes_and_Magie/$f" ]] || { echo "::error::Ancien fichier non déplacé : $f"; FAIL=1; }
done
exit $FAIL
