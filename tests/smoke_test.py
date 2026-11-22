"""Smoke test of a running Cloud Assess server (lcaac serve).

Sends the requests of samples/*.http and checks the responses:
every result gives the total and the impacts per life cycle step for all indicators,
the life cycle steps add up to the total, and the request meta is passed through.

Usage: python3 tests/smoke_test.py [base_url]   (default: http://localhost:8080)
"""
import json
import sys
import urllib.error
import urllib.request
from pathlib import Path

INDICATORS = ["ADPe", "ADPf", "AP", "GWP", "LU", "ODP", "PM", "POCP", "WU",
              "CTUe", "CTUh_c", "CTUh_nc", "Epf", "Epm", "Ept", "IR"]
STEPS = ["manufacturing", "transport", "use", "end_of_life"]
OPERATIONS = ["compute_resources", "virtual_machines", "storage_resources", "pools"]
REL_TOL = 1e-6
SAMPLES = Path(__file__).resolve().parent.parent / "samples"


def request(method, url, body=None):
    data = None if body is None else json.dumps(body).encode()
    req = urllib.request.Request(url, data=data, method=method, headers={"Content-Type": "application/json"})
    try:
        with urllib.request.urlopen(req, timeout=600) as r:
            return r.status, json.load(r)
    except urllib.error.HTTPError as e:
        return e.code, json.loads(e.read() or b"null")


def parse_http(path):
    """The method, path and JSON body of a single request .http file."""
    head, _, body = path.read_text().partition("\n\n")
    method, url = head.splitlines()[0].split()
    return method, "/" + url.split("/", 3)[3], json.loads(body)


def check_outputs(where, outputs):
    errors = []
    for output in ["total"] + STEPS:
        missing = [i for i in INDICATORS if i not in outputs.get(output, {})]
        if missing:
            errors.append(f"{where}: output '{output}' misses {missing}")
    if errors:
        return errors
    for i in INDICATORS:
        total = outputs["total"][i]["amount"]
        steps = sum(outputs[s][i]["amount"] for s in STEPS)
        if abs(total - steps) > REL_TOL * max(abs(total), 1e-12):
            errors.append(f"{where}: {i} steps sum to {steps}, total is {total}")
    return errors


def check_results(where, body, response):
    """A response with `results` (one per row), or a single result with `outputs`."""
    if "results" in response:
        rows = next(v for v in body.values() if isinstance(v, list))
        if len(response["results"]) != len(rows):
            return [f"{where}: {len(rows)} rows sent, {len(response['results'])} results"]
        errors = []
        for k, (row, result) in enumerate(zip(rows, response["results"])):
            if result["row"].get("meta") != row.get("meta"):
                errors.append(f"{where}[{k}]: meta not passed through")
            errors += check_outputs(f"{where}[{k}]", result["outputs"])
        return errors
    if response["request"].get("meta") != body.get("meta"):
        return [f"{where}: meta not passed through"]
    return check_outputs(where, response["outputs"])


def main(base_url):
    errors = []

    status, health = request("GET", base_url + "/health")
    if (status, health) != (200, {"status": "UP"}):
        errors.append(f"GET /health: {status} {health}")

    status, root = request("GET", base_url + "/")
    if status != 200 or sorted(root.get("operations", [])) != sorted(OPERATIONS):
        errors.append(f"GET /: {status} {root}")

    for sample in sorted(SAMPLES.glob("*.http")):
        method, path, body = parse_http(sample)
        status, response = request(method, base_url + path, body)
        if status != 200:
            errors.append(f"{sample.name}: {method} {path}: {status} {response}")
            continue
        if path.endswith("/batch"):
            found = []
            for k, (b, r) in enumerate(zip(body["requests"], response["results"])):
                found += check_results(f"{sample.name}[{k}]", b, r)
        else:
            found = check_results(sample.name, body, response)
        errors += found
        print(f"{'KO' if found else 'OK'} {sample.name}")

    status, response = request("POST", base_url + "/virtual_machines", {"period": "1 kg"})
    if status != 400:
        errors.append(f"invalid period: expected 400, got {status} {response}")

    for e in errors:
        print(f"ERROR {e}")
    print(f"{'FAILED' if errors else 'PASSED'}: {len(errors)} error(s)")
    return 1 if errors else 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1] if len(sys.argv) > 1 else "http://localhost:8080"))
