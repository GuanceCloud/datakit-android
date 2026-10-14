#!/usr/bin/env python3
"""Rebuild the small, uninstrumented Java 8 JAR/AAR test inputs (JDK 11+)."""
import io
import os
from pathlib import Path
import subprocess
import tempfile
import zipfile


def archive(entries):
    output = io.BytesIO()
    with zipfile.ZipFile(output, "w", compression=zipfile.ZIP_STORED) as result:
        for name, data in sorted(entries.items()):
            info = zipfile.ZipInfo(name, date_time=(1980, 1, 1, 0, 0, 0))
            info.external_attr = 0o100644 << 16
            result.writestr(info, data)
    return output.getvalue()


def main():
    root = Path(__file__).resolve().parent
    libs = root.parent / "libs"
    libs.mkdir(exist_ok=True)
    javac = str(Path(os.environ["JAVA_HOME"]) / "bin/javac") if os.environ.get("JAVA_HOME") else "javac"
    for kind in ("jar", "aar"):
        with tempfile.TemporaryDirectory() as temporary:
            classes = Path(temporary)
            sources = sorted((root / kind / "src/main/java").rglob("*.java"))
            subprocess.run([javac, "--release", "8", "-g:none", "-d", str(classes),
                            *map(str, sources)], check=True)
            jar = archive({path.relative_to(classes).as_posix(): path.read_bytes()
                           for path in classes.rglob("*.class")})
            artifact = jar if kind == "jar" else archive({
                "classes.jar": jar,
                "AndroidManifest.xml": (root / kind / "src/main/AndroidManifest.xml").read_bytes(),
                "R.txt": b"",
            })
            destination = libs / ("urlconnection-fixture." + kind)
            destination.write_bytes(artifact)
            print(destination.name)


if __name__ == "__main__":
    main()
