#!/usr/bin/env bash
# Calcule la prochaine version (Conventional Commits) et génère le changelog bilingue (EN + FR) en Markdown.
# Sorties : version, tag, skip (dans $GITHUB_OUTPUT) et le fichier release-notes.md
set -euo pipefail

REPO_URL="${GITHUB_SERVER_URL:-https://github.com}/${GITHUB_REPOSITORY:-}"
LAST_TAG=$(git describe --tags --abbrev=0 --match 'v[0-9]*' 2>/dev/null || true)

if [[ -z "$LAST_TAG" ]]; then
  RANGE="HEAD"
  CURRENT="0.0.0"
else
  RANGE="${LAST_TAG}..HEAD"
  CURRENT="${LAST_TAG#v}"
fi

# Commits à prendre en compte (hors commits de release automatiques)
mapfile -t SHAS < <(git log --no-merges --format='%H' "$RANGE" | while read -r sha; do
  subject=$(git log -1 --format='%s' "$sha")
  [[ "$subject" == chore\(release\)* ]] && continue
  # Commits publiés sans version (« [skip release] ») : absents des notes suivantes
  [[ "$subject" == *"[skip release]"* ]] && continue
  echo "$sha"
done)

out() { [[ -n "${GITHUB_OUTPUT:-}" ]] && echo "$1" >> "$GITHUB_OUTPUT" || echo "$1"; }

if [[ ${#SHAS[@]} -eq 0 ]]; then
  out "skip=true"
  exit 0
fi

IFS=. read -r MAJOR MINOR PATCH <<< "$CURRENT"
BUMP=patch
for sha in "${SHAS[@]}"; do
  subject=$(git log -1 --format='%s' "$sha")
  body=$(git log -1 --format='%b' "$sha")
  if [[ "$subject" =~ ^[a-z]+(\([^\)]*\))?!: ]] || grep -q 'BREAKING CHANGE' <<< "$body"; then
    BUMP=major
  elif [[ "$subject" =~ ^feat(\([^\)]*\))?: ]] && [[ "$BUMP" != major ]]; then
    BUMP=minor
  fi
done

if [[ -z "$LAST_TAG" ]]; then
  VERSION="1.0.0"
else
  case "$BUMP" in
    major) VERSION="$((MAJOR + 1)).0.0" ;;
    minor) VERSION="${MAJOR}.$((MINOR + 1)).0" ;;
    *)     VERSION="${MAJOR}.${MINOR}.$((PATCH + 1))" ;;
  esac
fi

# Notes bilingues : anglais d'abord, puis français.
# Convention du corps de commit (facultative) :
#   - English detail line          <- détails anglais
#   FR: résumé en français         <- remplace le sujet dans la partie française
#   - ligne de détail en français  <- détails français (après FR:)
# Sans ligne FR:, la partie française reprend le texte anglais.
declare -A TITLES_EN=(
  [feat]="✨ New features" [fix]="🐛 Fixes" [perf]="⚡ Performance" [refactor]="♻️ Refactoring"
  [docs]="📚 Documentation" [style]="🎨 Style" [test]="🧪 Tests" [build]="🔧 Build" [ci]="🔧 CI"
  [chore]="🔧 Maintenance" [other]="📝 Other changes")
declare -A TITLES_FR=(
  [feat]="✨ Nouveautés" [fix]="🐛 Corrections" [perf]="⚡ Performances" [refactor]="♻️ Refactorisation"
  [docs]="📚 Documentation" [style]="🎨 Style" [test]="🧪 Tests" [build]="🔧 Build" [ci]="🔧 CI"
  [chore]="🔧 Maintenance" [other]="📝 Autres changements")
ORDER=(feat fix perf refactor docs style test build ci chore other)
declare -A SECTIONS_EN=() SECTIONS_FR=()
BREAKING_EN=""
BREAKING_FR=""

for sha in "${SHAS[@]}"; do
  subject=$(git log -1 --format='%s' "$sha")
  body=$(git log -1 --format='%b' "$sha")
  short=${sha:0:7}
  if [[ "$subject" =~ ^([a-z]+)(\(([^\)]*)\))?(!)?:[[:space:]]*(.*)$ ]]; then
    type="${BASH_REMATCH[1]}"; scope="${BASH_REMATCH[3]}"; bang="${BASH_REMATCH[4]}"; desc="${BASH_REMATCH[5]}"
  else
    type="other"; scope=""; bang=""; desc="$subject"
  fi
  [[ -z "${TITLES_EN[$type]+x}" ]] && type="other"

  # Sépare le corps en partie anglaise (avant FR:) et partie française (après FR:)
  body_en=$(awk '/^[[:space:]]*FR:/{exit} {print}' <<< "$body")
  body_fr=$(awk 'f{print} /^[[:space:]]*FR:/{f=1}' <<< "$body")
  desc_fr=$(grep -m1 -E '^[[:space:]]*FR:' <<< "$body" | sed -E 's/^[[:space:]]*FR:[[:space:]]*//' || true)
  bullets() { grep -E '^[[:space:]]*[-*] ' <<< "$1" | sed -E 's/^[[:space:]]*[-*] /  - /' || true; }
  details_en=$(bullets "$body_en")
  details_fr=$(bullets "$body_fr")
  [[ -z "$desc_fr" ]] && desc_fr="$desc"
  [[ -z "$details_fr" ]] && details_fr="$details_en"

  line_en="- ${scope:+**${scope}**: }${desc} ([\`${short}\`](${REPO_URL}/commit/${sha}))"
  line_fr="- ${scope:+**${scope}** : }${desc_fr}"
  [[ -n "$details_en" ]] && line_en+=$'\n'"$details_en"
  [[ -n "$details_fr" ]] && line_fr+=$'\n'"$details_fr"
  SECTIONS_EN[$type]+="${line_en}"$'\n'
  SECTIONS_FR[$type]+="${line_fr}"$'\n'
  if [[ -n "$bang" ]] || grep -q 'BREAKING CHANGE' <<< "$body"; then
    BREAKING_EN+="- ${desc}"$'\n'
    BREAKING_FR+="- ${desc_fr}"$'\n'
  fi
done

DATE=$(date -u +%Y-%m-%d)
{
  if [[ -n "$LAST_TAG" ]]; then
    echo "## [${VERSION}](${REPO_URL}/compare/${LAST_TAG}...v${VERSION}) (${DATE})"
  else
    echo "## ${VERSION} (${DATE})"
  fi
  echo
  if [[ -n "$BREAKING_EN" && -n "$LAST_TAG" ]]; then
    echo "### ⚠️ Breaking changes"
    echo
    printf '%s\n' "$BREAKING_EN"
  fi
  for t in "${ORDER[@]}"; do
    if [[ -n "${SECTIONS_EN[$t]:-}" ]]; then
      echo "### ${TITLES_EN[$t]}"
      echo
      printf '%s\n' "${SECTIONS_EN[$t]}"
    fi
  done
  echo "### 🇫🇷 En français"
  echo
  if [[ -n "$BREAKING_FR" && -n "$LAST_TAG" ]]; then
    echo "#### ⚠️ Changements majeurs"
    echo
    printf '%s\n' "$BREAKING_FR"
  fi
  for t in "${ORDER[@]}"; do
    if [[ -n "${SECTIONS_FR[$t]:-}" ]]; then
      echo "#### ${TITLES_FR[$t]}"
      echo
      printf '%s\n' "${SECTIONS_FR[$t]}"
    fi
  done
} > release-notes.md

out "skip=false"
out "version=${VERSION}"
out "tag=v${VERSION}"
