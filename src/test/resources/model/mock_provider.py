"""Run python src/test/resources/model/mock_provider.py, then use http://127.0.0.1:8535/v1.

The model ID selects success, slow-success, slow-error, bad-json, bad-quote,
refusal, truncated, or timeout. Use any nonempty fixture key in the IDE.
Only the request count, selected case, and elapsed time are printed.
"""
import argparse
import json
import threading
import time
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path


DIRECTORY = Path(__file__).resolve().parent
CSV_INPUT = json.loads((DIRECTORY / "csv-input.json").read_text(encoding="utf-8"))
CSV_REPORT = json.loads((DIRECTORY / "csv-report.json").read_text(encoding="utf-8"))
CASES = {"success", "slow-success", "slow-error", "bad-json", "bad-quote", "refusal", "truncated", "timeout"}
COUNTER_LOCK = threading.Lock()
call_count = 0


def fixture_report(issue):
    if issue == CSV_INPUT:
        return CSV_REPORT
    if "导出文件为空" not in issue["title"]:
        raise ValueError("unknown fixture")
    checks = {}
    for check_id in CSV_REPORT["checks"]:
        if check_id == "actual_behavior":
            checks[check_id] = dict(status="clear", reason="标题明确指出导出文件为空。",
                                    evidence=dict(source="title", quote="导出文件为空"), suggestion=None)
        elif check_id in {"reproduction_steps", "expected_result"}:
            checks[check_id] = dict(status="missing", reason="此受控夹具要求再次确认该项。",
                                    evidence=None, suggestion="能否提供最小复现步骤和预期输出？")
        else:
            checks[check_id] = dict(status="not_applicable", reason="这是故障报告。", evidence=None, suggestion=None)
    return dict(schemaVersion="1", kind="bug", kindReason="提交者报告了导出故障。", checks=checks)


class Handler(BaseHTTPRequestHandler):
    def log_message(self, *args):
        pass

    def send_json(self, code, data):
        payload = json.dumps(data, ensure_ascii=False).encode()
        self.send_response(code)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(payload)))
        self.end_headers()
        try:
            self.wfile.write(payload)
        except (BrokenPipeError, ConnectionResetError, ConnectionAbortedError):
            pass

    def do_POST(self):
        global call_count
        if self.path != "/v1/chat/completions":
            self.send_json(404, {})
            return
        start = time.monotonic()
        with COUNTER_LOCK:
            call_count += 1
            index = call_count
        case = "invalid"
        try:
            size = int(self.headers["Content-Length"])
            if not 0 < size <= 1024 * 1024:
                raise ValueError("unexpected payload size")
            request = json.loads(self.rfile.read(size))
            if request["model"] not in CASES:
                raise ValueError("unknown model")
            case = request["model"]
            messages = request["messages"]
            if [m["role"] for m in messages] != ["system", "user"]:
                raise ValueError("unexpected messages")
            issue = json.loads(messages[1]["content"])
            if set(issue) != {"title", "body"} or not all(isinstance(s, str) for s in issue.values()):
                raise ValueError("unexpected issue fields")
            report = fixture_report(issue)
            if case.startswith("slow-"):
                time.sleep(10)
            elif case == "timeout":
                time.sleep(65)
            if case == "slow-error":
                self.send_json(503, {"error": {"message": "controlled failure"}})
                return
            content = json.dumps(report, ensure_ascii=False)
            if case == "bad-quote":
                broken = json.loads(content)
                row = next(row for row in broken["checks"].values() if row["evidence"])
                row["evidence"]["quote"] = "夹具中不存在的引文"
                content = json.dumps(broken, ensure_ascii=False)
            if case == "bad-json":
                content = "{"
            message = dict(role="assistant", content=content)
            if case == "refusal":
                message.update(content=None, refusal="Controlled refusal")
            reason = "length" if case == "truncated" else "stop"
            self.send_json(200, dict(id=f"fixture-{index}", object="chat.completion", created=0,
                                     model=case, choices=[dict(index=0, finish_reason=reason, message=message)]))
        except (ValueError, TypeError, KeyError):
            self.send_json(400, {"error": {"message": "invalid fixture input or model ID"}})
        finally:
            print(f"calls={index} case={case} elapsed_ms={(time.monotonic()-start)*1000:.0f}", flush=True)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--port", type=int, default=8535)
    args = parser.parse_args()
    server = ThreadingHTTPServer(("127.0.0.1", args.port), Handler)
    print(f"Base URL http://127.0.0.1:{server.server_port}/v1", flush=True)
    server.serve_forever()
