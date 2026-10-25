#!/usr/bin/env python3
"""Create a fresh loopback-only server; retain logs and fixtures under ignored servers/."""
import argparse
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import socket
import subprocess
import tempfile
import time
import urllib.request

ROOT = Path(__file__).resolve().parent.parent
USER_AGENT = "AntiXrayHeuristics-Compatibility/1.0 (https://github.com/mrfdev/AntiXrayHeuristics)"


def sha256(path):
    digest = hashlib.sha256()
    with path.open("rb") as source:
        for chunk in iter(lambda: source.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def fetch(url):
    request = urllib.request.Request(url, headers={"User-Agent": USER_AGENT})
    with urllib.request.urlopen(request, timeout=60) as response:
        return response.read()


def check_ports(port):
    for offset, kind in [(0, socket.SOCK_STREAM), (1, socket.SOCK_DGRAM), (2, socket.SOCK_STREAM)]:
        with socket.socket(socket.AF_INET, kind) as probe:
            probe.bind(("127.0.0.1", port + offset))


def log_text(path):
    return re.sub(r"\x1b\[[0-?]*[ -/]*[@-~]", "", path.read_text(errors="replace"))


def run(server, java, port, release, paper_api, phase, missing_dependency=False):
    check_ports(port)
    log = server / f"{phase}.log"
    command = [str(java), "-Xms512M", "-Xmx2G", "-XX:ActiveProcessorCount=2",
               "--illegal-final-field-mutation=deny", "-Dfile.encoding=UTF-8", "-Dapple.awt.UIElement=true", "-Daxh.smoke.disposable=true",
               f"-Daxh.smoke.restart={str(phase == 'restart').lower()}", "-jar", "paper.jar", "--nogui"]
    environment = dict(os.environ, JAVA_HOME=str(java.parent.parent),
                       PATH=str(java.parent) + os.pathsep + os.environ.get("PATH", ""))
    with log.open("x") as output:
        process = subprocess.Popen(command, cwd=server, stdin=subprocess.PIPE, stdout=output,
                                   stderr=subprocess.STDOUT, text=True, env=environment)

        def wait_for(markers, offset=0, timeout=240):
            deadline = time.monotonic() + timeout
            while time.monotonic() < deadline:
                content = log_text(log)
                if "COMPATIBILITY_PROBE_FAIL" in content:
                    raise AssertionError(f"Probe failed: {log}")
                if all(marker in content[offset:] for marker in markers):
                    return
                if process.poll() is not None:
                    raise AssertionError(f"Server exited early ({process.returncode}): {log}")
                time.sleep(0.25)
            raise TimeoutError(f"Missing {markers}: {log}")

        def send(value, *markers):
            offset = len(log_text(log))
            process.stdin.write(value + "\n")
            process.stdin.flush()
            wait_for(markers, offset, 30)

        try:
            wait_for(["Done ("])
            if missing_dependency:
                text = log_text(log)
                assert "Unknown/missing dependency plugins: [CoreProtect]" in text, log
                assert "Enabling xrayheuristics" not in text, log
            else:
                wait_for(["COMPATIBILITY_PROBE_PASS"])
                send("version xrayheuristics", f"xrayheuristics version {release}")
                send("version CoreProtect", "CoreProtect version 25.0")
                send("xrayer info", f"Installed version: {release}")
                send("xrayer help", "Config placeholders:")
                send("xrayer debug", "CoreProtect hooked: yes", f"Compiled Paper API: {paper_api}",
                     "Storage type: JSON")
                send("xrayer reload", "reloaded")
                send("xrayer debug", "CoreProtect hooked: yes", "Active sessions: 0")
            process.stdin.write("stop\n")
            process.stdin.flush()
            assert process.wait(timeout=60) == 0, log
        finally:
            if process.poll() is None:
                process.stdin.write("stop\n")
                process.stdin.flush()
                try:
                    process.wait(timeout=45)
                except subprocess.TimeoutExpired:
                    process.terminate()
                    process.wait(timeout=15)

    text = log_text(log)
    assert "All RegionFile I/O tasks to complete" in text, log
    if not missing_dependency:
        assert "Disabling xrayheuristics" in text and "Disabling CoreProtect" in text, log
        errors = [line for line in text.splitlines() if re.search(r"(ERROR|SEVERE)\]|Exception in thread", line)]
        assert not errors, errors
    hashes = {name: sha256(server / "plugins/1MB-XRayHeuristics" / name)
              for name in ["config.yml", "locale.yml", "weights.yml", "data.json"]}
    check_ports(port)
    result = {"phase": phase, "exit_code": process.returncode, "log": str(log), "data_sha256": hashes,
              "probe": [line for line in text.splitlines() if "PROBE" in line],
              "warnings": [line for line in text.splitlines() if "WARN" in line]}
    print(f"PASS: {phase}, clean shutdown and released ports. Log: {log}", flush=True)
    return result


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--paper-jar", type=Path, help="Use a cached JAR after verifying its official checksum")
    parser.add_argument("--coreprotect-jar", type=Path, default=ROOT / "servers/compile-support/CoreProtect-25.0.jar")
    parser.add_argument("--eula-file", type=Path, default=ROOT / "servers/Server-Two-Paper-26.3/eula.txt",
                        help="Existing locally accepted EULA file; this script does not accept a new EULA")
    parser.add_argument("--port", type=int, default=25843)
    args = parser.parse_args()
    properties = dict(line.split("=", 1) for line in (ROOT / "version.properties").read_text().splitlines()
                      if line and not line.startswith("#"))
    assert properties["paperApiChannel"] == "stable", "Only stable releases may be tested here"
    version, build = properties["paperVersion"], properties["paperApiBuild"]
    api = f"{version}.build.{build}-stable"
    release = f"{properties['pluginVersion']}-{properties['buildNumber']}-j{properties['javaTarget']}-{version}"
    java_home = subprocess.check_output(
        ["/bin/bash", str(ROOT / "scripts/java-env.sh"), "--test-home"], text=True).strip()
    java = Path(java_home) / "bin/java"
    runtime = subprocess.check_output([str(java), "-XshowSettings:properties", "-version"],
                                      stderr=subprocess.STDOUT, text=True)
    assert re.search(r"eula\s*=\s*true", args.eula_file.read_text()), "Supply an already accepted local EULA"
    plugin = ROOT / f"build/libs/1MB-XRayHeuristics-v{release}.jar"
    probe = ROOT / "build/smoke/CompatibilityProbe.jar"
    for path in [plugin, probe, args.coreprotect_jar]:
        assert path.is_file(), f"Required artifact missing: {path}; run ./scripts/rebuild.sh smokeTestJar first"
    check_ports(args.port)
    metadata = json.loads(fetch(f"https://fill.papermc.io/v3/projects/paper/versions/{version}/builds/{build}"))
    assert metadata["id"] == int(build) and metadata["channel"] == "STABLE", metadata
    download = metadata["downloads"]["server:default"]
    assert download["checksums"]["sha256"] == properties["paperServerSha256"], "Fill checksum drift"
    (ROOT / "servers").mkdir(exist_ok=True)
    server = Path(tempfile.mkdtemp(prefix=f"paper-{version}-stable-{properties['buildNumber']}-", dir=ROOT / "servers"))
    print(f"Disposable server: {server}\nJava executable: {java}\nRuntime: {runtime.strip()}", flush=True)
    (server / "paper-build.json").write_text(json.dumps(metadata, indent=2) + "\n")
    (server / "runtime.txt").write_text(f"Java executable: {java}\n{runtime}")
    if args.paper_jar:
        shutil.copyfile(args.paper_jar, server / "paper.jar")
    else:
        (server / "paper.jar").write_bytes(fetch(download["url"]))
    assert sha256(server / "paper.jar") == properties["paperServerSha256"], "Paper JAR checksum mismatch"
    (server / "plugins").mkdir()
    for source in [plugin, probe, args.coreprotect_jar]:
        shutil.copyfile(source, server / "plugins" / source.name)
    (server / "eula.txt").write_text("eula=true\n")
    (server / "server.properties").write_text(
        f"server-ip=127.0.0.1\nserver-port={args.port}\nquery.port={args.port + 1}\nrcon.port={args.port + 2}\n"
        "enable-query=false\nenable-rcon=false\nonline-mode=false\nlevel-name=world\n"
        "view-distance=2\nsimulation-distance=2\nspawn-protection=0\nmax-players=1\n"
        "enable-status=false\nwhite-list=true\nenforce-whitelist=true\n")
    (server / "plugins/bStats").mkdir()
    (server / "plugins/bStats/config.yml").write_text("enabled: false\n")
    (server / "config").mkdir()
    (server / "config/paper-global.yml").write_text("spark:\n  enabled: false\n  enable-immediately: false\n")
    results = []
    evidence = {"release": release, "paper_api": api, "java_executable": str(java),
                "runtime": runtime, "server": str(server),
                "paper_sha256": sha256(server / "paper.jar"), "plugin_sha256": sha256(plugin),
                "coreprotect_sha256": sha256(args.coreprotect_jar), "runs": results}
    report = server / "smoke-results.json"
    for phase in ["startup", "restart"]:
        results.append(run(server, java, args.port, release, api, phase))
        report.write_text(json.dumps(evidence, indent=2) + "\n")
    assert results[0]["data_sha256"] == results[1]["data_sha256"], "Plugin data changed after restart"
    disabled = server / "disabled"
    disabled.mkdir()
    for name in [args.coreprotect_jar.name, probe.name]:
        shutil.move(server / "plugins" / name, disabled / name)
    try:
        results.append(run(server, java, args.port, release, api, "missing-coreprotect", True))
    finally:
        for name in [args.coreprotect_jar.name, probe.name]:
            shutil.move(disabled / name, server / "plugins" / name)
    report.write_text(json.dumps(evidence, indent=2) + "\n")
    print(f"PASS: startup, runtime assertions, restart/persistence, and missing dependency. Evidence: {report}")


if __name__ == "__main__":
    main()
