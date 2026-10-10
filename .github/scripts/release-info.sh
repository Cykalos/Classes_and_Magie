#!/usr/bin/env bash
# Calcule la prochaine version (Conventional Commits) et génère le changelog en Markdown.
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

declare -A TITLES=(
  [feat]="✨ Nouveautés" [fix]="🐛 Corrections" [perf]="⚡ Performances" [refactor]="♻️ Refactorisation"
  [docs]="📚 Documentation" [style]="🎨 Style" [test]="🧪 Tests" [build]="🔧 Build" [ci]="🔧 CI"
  [chore]="🔧 Maintenance" [other]="📝 Autres changements")
ORDER=(feat fix perf refactor docs style test build ci chore other)
declare -A SECTIONS=()
BREAKING=""

for sha in "${SHAS[@]}"; do
  subject=$(git log -1 --format='%s' "$sha")
  body=$(git log -1 --format='%b' "$sha")
  short=${sha:0:7}
  if [[ "$subject" =~ ^([a-z]+)(\(([^\)]*)\))?(!)?:[[:space:]]*(.*)$ ]]; then
    type="${BASH_REMATCH[1]}"; scope="${BASH_REMATCH[3]}"; bang="${BASH_REMATCH[4]}"; desc="${BASH_REMATCH[5]}"
  else
    type="other"; scope=""; bang=""; desc="$subject"
  fi
  [[ -z "${TITLES[$type]+x}" ]] && type="other"
  line="- ${scope:+**${scope}** : }${desc} ([\`${short}\`](${REPO_URL}/commit/${sha}))"
  # Détails : lignes « - ... » du corps du commit
  details=$(grep -E '^[[:space:]]*[-*] ' <<< "$body" | sed -E 's/^[[:space:]]*[-*] /  - /' || true)
  [[ -n "$details" ]] && line+=$'\n'"$details"
  SECTIONS[$type]+="${line}"$'\n'
  if [[ -n "$bang" ]] || grep -q 'BREAKING CHANGE' <<< "$body"; then
    BREAKING+="- ${desc}"$'\n'
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
  if [[ -n "$BREAKING" && -n "$LAST_TAG" ]]; then
    echo "### ⚠️ Changements majeurs"
    echo
    printf '%s\n' "$BREAKING"
  fi
  for t in "${ORDER[@]}"; do
    if [[ -n "${SECTIONS[$t]:-}" ]]; then
      echo "### ${TITLES[$t]}"
      echo
      printf '%s\n' "${SECTIONS[$t]}"
    fi
  done
} > release-notes.md

out "skip=false"
out "version=${VERSION}"
out "tag=v${VERSION}"
