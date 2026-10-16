#!/usr/bin/env python3
"""Serial device acceptance; all resets apply only to this dedicated fixture package."""
import argparse
import json
import re
import subprocess
import time
from pathlib import Path

PACKAGE = "com.ft.fixture.launch"
ACTIVITY = PACKAGE + "/com.ft.launchfixture.LaunchActivity"
URI = "content://com.ft.fixture.launch.control"
MIXED = "foreground_and_background"


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--serial", required=True)
    parser.add_argument("--output", type=Path, required=True)
    args = parser.parse_args()
    root = Path(__file__).resolve().parent.parent
    apk = root / "ft-launch-fixture/build/outputs/apk/debug/ft-launch-fixture-debug.apk"
    args.output.mkdir(parents=True, exist_ok=True)
    adb = ["adb", "-s", args.serial]
    results = []

    def run(*command):
        process = subprocess.run(adb + list(command), capture_output=True, text=True, timeout=20)
        if process.returncode:
            raise RuntimeError((process.stdout + process.stderr)[-3000:])
        return process.stdout

    def control(method):
        output = run("shell", "content", "call", "--uri", URI, "--method", method)
        if "Exception" in output or "Error" in output:
            raise RuntimeError(output[-3000:])

    def status():
        control("status")
        return json.loads(run("shell", "run-as", PACKAGE, "cat", "files/launch-acceptance.json"))

    def wait_for(predicate, description):
        deadline = time.monotonic() + 18
        latest = None
        while time.monotonic() < deadline:
            latest = status()
            if predicate(latest):
                return latest
            time.sleep(.25)
        raise AssertionError(description + ": " + json.dumps(latest, ensure_ascii=False)[-3500:])

    def reset():
        run("shell", "am", "force-stop", PACKAGE)
        assert "Success" in run("shell", "pm", "clear", PACKAGE)

    def start(gated=False):
        command = ["shell", "am", "start", "--activity-single-top", "--activity-reorder-to-front",
                   "-n", ACTIVITY]
        if gated:
            command += ["--ez", "gate_first_frame", "true"]
        output = run(*command)
        if "Error" in output or "Exception" in output:
            raise RuntimeError(output[-3000:])

    def completed(expected):
        report = wait_for(lambda s: s["first_draw_done"] and len(s["cold_actions"]) == 1,
                          "cold Action must complete and reach local storage")
        assert report["launch_type"] == expected, report
        action = report["cold_actions"][0]
        assert action["launch_type"] == expected, report
        duration = re.search(r"(?:[, ])duration=(\d+)(?:i)?(?:[, ]|$)", action["line_protocol"])
        assert duration, action
        action["duration_ns"] = int(duration.group(1))
        segments = sum(report[k] for k in ["pre_application_duration_ns", "application_duration_ns",
                                          "first_frame_duration_ns"])
        assert action["duration_ns"] == segments, report
        return report

    def save(name, report, endpoint):
        report["scenario"] = name
        report["measurement_end"] = endpoint
        report["result"] = "PASS"
        (args.output / (name + ".json")).write_text(json.dumps(report, ensure_ascii=False, indent=2))
        results.append({"scenario": name, "result": "PASS", "launch_type": report["launch_type"],
                        "cold_action_count": len(report["cold_actions"]), "measurement_end": endpoint})
        print("PASS", name, report["launch_type"], "cold_actions=" + str(len(report["cold_actions"])),
              flush=True)

    try:
        print("Installing isolated fixture on", args.serial, flush=True)
        run("install", "-r", str(apk))

        reset()
        start()
        foreground = completed("foreground")
        run("shell", "input", "keyevent", "KEYCODE_HOME")
        wait_for(lambda s: not s["foreground"], "foreground control must enter background")
        assert status()["launch_type"] == "foreground"
        save("foreground_control", foreground, "automatic_first_draw")

        reset()
        pending = wait_for(lambda s: s["application_ns"] != 0 and s["launch_type"] == "background",
                           "headless process must be classified background")
        time.sleep(1)
        pending = status()
        assert not pending["foreground"] and not pending["first_draw_done"]
        assert not pending["listener_registered"] and pending["first_activity_ns"] == 0
        assert pending["cold_actions"] == []
        save("background_pending", pending, "no_first_draw_no_automatic_action")

        control("finish_background_for_test")
        background = completed("background")
        assert not background["foreground"] and background["first_activity_ns"] == 0
        save("background_explicit_test_endpoint", background, "explicit_test_completion")
        start()
        frozen = wait_for(lambda s: s["foreground"], "post-completion foreground transition")
        assert frozen["launch_type"] == "background"
        assert len(frozen["cold_actions"]) == 1
        assert frozen["cold_actions"][0]["launch_type"] == "background"
        save("background_report_frozen_after_completion", frozen, "explicit_test_completion")

        reset()
        before = wait_for(lambda s: s["launch_type"] == "background", "background before foreground")
        control("repeat_hook")
        repeated = status()
        assert repeated["application_ns"] == before["application_ns"]
        assert repeated["launch_type"] == "background"
        start()
        background = completed("background")
        assert background["application_ns"] == before["application_ns"]
        save("background_repeat_hook_foreground", background, "automatic_first_draw")

        reset()
        wait_for(lambda s: s["launch_type"] == "background", "background-origin gated launch")
        start(gated=True)
        held = wait_for(lambda s: s["foreground"] and s["listener_registered"],
                        "background-origin first draw is held")
        assert held["launch_type"] == "background" and not held["first_draw_done"]
        run("shell", "input", "keyevent", "KEYCODE_HOME")
        stopped = wait_for(lambda s: not s["foreground"], "background-origin launch returns to background")
        assert stopped["launch_type"] == "background" and not stopped["first_draw_done"]
        start(gated=True)
        wait_for(lambda s: s["foreground"], "background-origin launch returns to foreground")
        control("release")
        save("background_origin_foreground_background_foreground", completed("background"),
             "automatic_first_draw")

        reset()
        start(gated=True)
        held = wait_for(lambda s: s["foreground"] and s["listener_registered"],
                        "foreground Activity must register a held first-draw listener")
        assert held["launch_type"] == "foreground" and not held["first_draw_done"]
        assert held["cold_actions"] == []
        run("shell", "input", "keyevent", "KEYCODE_HOME")
        stopped = wait_for(lambda s: not s["foreground"] and s["launch_type"] == MIXED,
                           "background while first draw is pending")
        assert not stopped["first_draw_done"] and stopped["cold_actions"] == []
        time.sleep(1)
        start(gated=True)
        resumed = wait_for(lambda s: s["foreground"], "return to held Activity")
        assert not resumed["first_draw_done"] and resumed["launch_type"] == MIXED
        assert sum(e["event"] == "created" for e in resumed["events"]) == 1
        control("release")
        save("foreground_background_foreground_before_first_draw", completed(MIXED),
             "automatic_first_draw")
    except Exception as exc:
        (args.output / "failure.txt").write_text(str(exc))
        raise
    finally:
        run("shell", "am", "force-stop", PACKAGE)
        (args.output / "summary.json").write_text(json.dumps({"device": args.serial,
            "api": run("shell", "getprop", "ro.build.version.sdk").strip(),
            "results": results}, ensure_ascii=False, indent=2))
    print("All", len(results), "acceptance checks passed; reports:", args.output, flush=True)


if __name__ == "__main__":
    main()
