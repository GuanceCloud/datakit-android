#!/usr/bin/env bash

set -euo pipefail

ADB_BIN="${ADB_BIN:-adb}"
TARGET_PACKAGE="com.ft"
CALLER_PACKAGE="com.ft.test"
TARGET_ACTIVITY="com.ft/.DebugMainActivity"
CALLER_ACTIVITY="com.ft.test/com.ft.launchtest.BackgroundComponentCallerActivity"
TARGET_APK="app/build/outputs/apk/prodTest/debug/app-prodTest-debug.apk"
CALLER_APK="app/build/outputs/apk/androidTest/prodTest/debug/app-prodTest-debug-androidTest.apk"

if [[ ! -f "${TARGET_APK}" || ! -f "${CALLER_APK}" ]]; then
    echo "Build APKs first:"
    echo "  ./gradlew :app:assembleProdTestDebug :app:assembleProdTestDebugAndroidTest"
    exit 1
fi

"${ADB_BIN}" install -r "${TARGET_APK}" >/dev/null
"${ADB_BIN}" install -r -t "${CALLER_APK}" >/dev/null

reset_scenario() {
    "${ADB_BIN}" shell am force-stop "${CALLER_PACKAGE}"
    "${ADB_BIN}" shell pm clear "${TARGET_PACKAGE}" >/dev/null
    "${ADB_BIN}" logcat -c
}

read_launch_logs() {
    sleep 1
    "${ADB_BIN}" logcat -d -v brief \
        -s '[FT-SDK]AppStartCounter' \
        BackgroundLaunchReceiver \
        BackgroundLaunchService \
        BackgroundLaunchJob \
        BackgroundLaunchProvider \
        BackgroundBoundService \
        BackgroundCaller
}

assert_final_type() {
    local scenario="$1"
    local expected="$2"
    local logs="$3"
    local expected_line="launchFromBackground:${expected}"
    if [[ "${logs}" != *"coldStart:"*"${expected_line}"* ]]; then
        echo "[FAIL] ${scenario}: expected final ${expected}"
        echo "${logs}"
        exit 1
    fi
    echo "[PASS] ${scenario}: final ${expected}"
}

run_normal_activity() {
    reset_scenario
    "${ADB_BIN}" shell am start -W -S -n "${TARGET_ACTIVITY}" >/dev/null
    local logs
    logs="$(read_launch_logs)"
    assert_final_type "normal activity" "false" "${logs}"
}

run_receiver() {
    reset_scenario
    "${ADB_BIN}" shell am broadcast \
        -n com.ft/.BackgroundLaunchReceiver \
        -a com.ft.action.SIMULATE_BACKGROUND_BROADCAST_LAUNCH >/dev/null
    sleep 1
    "${ADB_BIN}" shell am start -W -n "${TARGET_ACTIVITY}" >/dev/null
    local logs
    logs="$(read_launch_logs)"
    assert_final_type "broadcast receiver" "true" "${logs}"
}

start_caller() {
    local mode="$1"
    "${ADB_BIN}" shell am start -W -n "${CALLER_ACTIVITY}" --es mode "${mode}" >/dev/null
    sleep 1
    "${ADB_BIN}" shell am start -W -n "${TARGET_ACTIVITY}" >/dev/null
}

run_started_service() {
    reset_scenario
    start_caller "start_service"
    local logs
    logs="$(read_launch_logs)"
    assert_final_type "started service" "true" "${logs}"
}

run_bound_service() {
    reset_scenario
    start_caller "bind_service"
    local logs
    logs="$(read_launch_logs)"
    assert_final_type "bound service" "true" "${logs}"
}

run_provider() {
    reset_scenario
    start_caller "provider"
    local logs
    logs="$(read_launch_logs)"
    assert_final_type "content provider" "true" "${logs}"
}

run_job() {
    reset_scenario
    "${ADB_BIN}" shell am broadcast \
        -n com.ft/.BackgroundLaunchReceiver \
        -a com.ft.action.SCHEDULE_BACKGROUND_LAUNCH_JOB >/dev/null
    "${ADB_BIN}" shell am kill "${TARGET_PACKAGE}"
    "${ADB_BIN}" logcat -c
    "${ADB_BIN}" shell cmd jobscheduler run -f "${TARGET_PACKAGE}" 4242 >/dev/null
    sleep 1
    "${ADB_BIN}" shell am start -W -n "${TARGET_ACTIVITY}" >/dev/null
    local logs
    logs="$(read_launch_logs)"
    assert_final_type "job service" "true" "${logs}"
}

run_normal_activity
run_receiver
run_started_service
run_bound_service
run_provider
run_job

echo "All app launch type scenarios passed."
