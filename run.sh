#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

usage() {
  cat <<'EOF'
Usage:
  ./run.sh [--mode mvn|jar] [--profile <name>] [--skip-tests] [--] [extra args...]

Modes:
  mvn   Runs: mvn -pl jinjin-server -am spring-boot:run
  jar   Builds (if needed) then runs the jar in jinjin-server/target

Profile:
  --profile dev   -> passes --spring.profiles.active=dev
  (default: no explicit profile; Spring uses application.yml)

Examples:
  ./run.sh --mode mvn --profile dev
  ./run.sh --mode jar --profile dev
EOF
}

normalize_java_home() {
  # Some environments mistakenly set JAVA_HOME to the java binary path.
  # Also guard against accidental trailing '$' from copy/paste.
  local value="${1:-}"
  value="${value%$'\r'}"
  value="${value%\$}"

  if [[ -z "$value" ]]; then
    return 0
  fi

  if [[ "$value" == */bin/java ]]; then
    value="$(dirname "$(dirname "$value")")"
  fi

  echo "$value"
}

ensure_java_home() {
  local current="${JAVA_HOME-}"
  current="$(normalize_java_home "$current")"

  if [[ -z "$current" ]]; then
    local java_bin
    java_bin="$(command -v java || true)"
    if [[ -n "$java_bin" ]]; then
      java_bin="$(readlink -f "$java_bin" 2>/dev/null || echo "$java_bin")"
      current="$(dirname "$(dirname "$java_bin")")"
    fi
  fi

  if [[ -z "$current" || ! -x "$current/bin/java" ]]; then
    cat >&2 <<EOF
ERROR: JAVA_HOME is not set correctly.
Current JAVA_HOME: ${JAVA_HOME-<unset>}
Expected JAVA_HOME to point to a JDK directory containing bin/java.

Examples:
  export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
  export PATH=\"$JAVA_HOME/bin:\$PATH\"
EOF
    exit 1
  fi

  export JAVA_HOME="$current"
}

MODE=""
PROFILE=""
SKIP_TESTS=false
EXTRA_ARGS=()

while [[ $# -gt 0 ]]; do
  case "$1" in
    -h|--help)
      usage
      exit 0
      ;;
    --mode)
      MODE="${2:-}"
      shift 2
      ;;
    --profile)
      PROFILE="${2:-}"
      shift 2
      ;;
    --skip-tests)
      SKIP_TESTS=true
      shift
      ;;
    --)
      shift
      EXTRA_ARGS+=("$@")
      break
      ;;
    *)
      EXTRA_ARGS+=("$1")
      shift
      ;;
  esac
done

ensure_java_home

cd "$ROOT_DIR"

if [[ -z "$MODE" ]]; then
  if compgen -G "jinjin-server/target/*.jar" >/dev/null; then
    MODE="jar"
  else
    MODE="mvn"
  fi
fi

MVN_ARGS=("-pl" "jinjin-server" "-am")
if $SKIP_TESTS; then
  MVN_ARGS+=("-DskipTests")
fi

SPRING_ARGS=()
if [[ -n "$PROFILE" ]]; then
  SPRING_ARGS+=("--spring.profiles.active=${PROFILE}")
fi

case "$MODE" in
  mvn)
    echo "Using JAVA_HOME=$JAVA_HOME"
    exec mvn "${MVN_ARGS[@]}" spring-boot:run -Dspring-boot.run.arguments="${SPRING_ARGS[*]} ${EXTRA_ARGS[*]}"
    ;;
  jar)
    echo "Using JAVA_HOME=$JAVA_HOME"

    # Build if no runnable jar exists
    if ! compgen -G "jinjin-server/target/*.jar" >/dev/null; then
      mvn "${MVN_ARGS[@]}" package
    fi

    # Pick newest jar, excluding the .original file if present
    JAR_PATH="$(ls -t jinjin-server/target/*.jar 2>/dev/null | grep -v '\.jar\.original$' | head -n 1 || true)"
    if [[ -z "$JAR_PATH" ]]; then
      echo "ERROR: Could not find a runnable jar under jinjin-server/target" >&2
      exit 1
    fi

    exec java -jar "$JAR_PATH" "${SPRING_ARGS[@]}" "${EXTRA_ARGS[@]}"
    ;;
  *)
    echo "ERROR: Unknown --mode '$MODE' (expected mvn or jar)" >&2
    usage
    exit 2
    ;;
esac
