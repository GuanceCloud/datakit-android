#!/bin/bash
set -euo pipefail

SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
REPO_ROOT=$(cd "$SCRIPT_DIR/.." && pwd)
TEST_ROOT=$(mktemp -d "${TMPDIR:-/tmp}/jitpack-publish-test.XXXXXX")
trap 'rm -rf "$TEST_ROOT"' EXIT

FIXTURE_REPO="$TEST_ROOT/repo"
GRADLE_CALLS="$TEST_ROOT/gradle-calls"

grep -Eq '^[[:space:]]+- openjdk11$' "$REPO_ROOT/jitpack.yml"
grep -Eq '^[[:space:]]+- ./scripts/jitpack_publish\.sh$' "$REPO_ROOT/jitpack.yml"

mkdir -p "$FIXTURE_REPO/scripts"
cp "$SCRIPT_DIR/jitpack_publish.sh" "$FIXTURE_REPO/scripts/jitpack_publish.sh"

cat >"$FIXTURE_REPO/gradlew" <<'EOF_GRADLEW'
#!/bin/bash
set -euo pipefail
printf '%s\n' "$*" >>"$GRADLE_CALLS"
EOF_GRADLEW
chmod +x "$FIXTURE_REPO/gradlew"

assert_route() {
    local version="$1"
    local expected_task="$2"
    local actual

    : >"$GRADLE_CALLS"
    GRADLE_CALLS="$GRADLE_CALLS" VERSION="$version" \
        bash "$FIXTURE_REPO/scripts/jitpack_publish.sh"

    actual=$(<"$GRADLE_CALLS")
    [[ "$actual" == "clean $expected_task -x test -x lint" ]] || {
        echo "Expected $version to run $expected_task, got: $actual" >&2
        return 1
    }
}

assert_route agent_1.7.5 :ft-sdk:publishToMavenLocal
assert_route tw_agent_1.7.5 :ft-sdk:publishToMavenLocal
assert_route native_1.1.3 :ft-native:publishToMavenLocal
assert_route tw_native_1.1.3 :ft-native:publishToMavenLocal
assert_route plugin_1.3.8 :ft-plugin:publishToMavenLocal
assert_route tw_plugin_1.3.8 :ft-plugin:publishToMavenLocal
assert_route replay_material_0.1.8 :ft-session-replay-material:publishToMavenLocal
assert_route replay_compose_0.1.8 :ft-session-replay-compose:publishToMavenLocal
assert_route replay_0.1.8 :ft-session-replay:publishToMavenLocal
assert_route tw_replay_0.1.8 :ft-session-replay:publishToMavenLocal

: >"$GRADLE_CALLS"
if GRADLE_CALLS="$GRADLE_CALLS" VERSION=v1.7.5 \
    bash "$FIXTURE_REPO/scripts/jitpack_publish.sh" >"$TEST_ROOT/unknown.out" 2>&1; then
    echo "Unknown tag prefix unexpectedly succeeded" >&2
    exit 1
fi

grep -Fq "Unsupported JitPack version: v1.7.5" "$TEST_ROOT/unknown.out"
[[ ! -s "$GRADLE_CALLS" ]] || {
    echo "Unknown tag prefix invoked Gradle" >&2
    exit 1
}

echo "JitPack module routing tests passed"
