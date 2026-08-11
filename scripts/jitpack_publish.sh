#!/bin/bash
set -euo pipefail

SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
REPO_ROOT=$(cd "$SCRIPT_DIR/.." && pwd)
VERSION=${VERSION:-}

case "$VERSION" in
    agent_* | tw_agent_*)
        publish_task=:ft-sdk:publishToMavenLocal
        ;;
    native_* | tw_native_*)
        publish_task=:ft-native:publishToMavenLocal
        ;;
    plugin_* | tw_plugin_*)
        publish_task=:ft-plugin:publishToMavenLocal
        ;;
    replay_material_*)
        publish_task=:ft-session-replay-material:publishToMavenLocal
        ;;
    replay_compose_*)
        publish_task=:ft-session-replay-compose:publishToMavenLocal
        ;;
    replay_* | tw_replay_*)
        publish_task=:ft-session-replay:publishToMavenLocal
        ;;
    *)
        echo "Unsupported JitPack version: ${VERSION:-<empty>}" >&2
        exit 1
        ;;
esac

echo "Publishing $VERSION with $publish_task"
cd "$REPO_ROOT"
exec ./gradlew clean "$publish_task" -x test -x lint
