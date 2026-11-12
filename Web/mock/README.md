# Dashboard mock server

A small Bun/TypeScript server that mimics the FTC Robot Controller dashboard
backend defined in [`TeamCode/Core.java`](../../TeamCode/src/main/java/org/pionerds/ftc/teamcode/Core.java).

| Service   | Default URL             | Purpose                                 |
| --------- | ----------------------- | --------------------------------------- |
| HTTP      | `http://localhost:8080` | Dashboard assets, health, mock controls |
| WebSocket | `ws://localhost:8081`   | `log` and `position` broadcasts         |

## Usage

Development with the normal Vite dev server (recommended):

```sh
bun run mock
bun run dev
```

Vite proxies `/dashboard/*` to the mock HTTP server, and the dashboard connects
to the mock WebSocket on port 8081 automatically. You do not need to build the
app or use the mock server to serve the dev environment.

Run the mock on its own (serves the production build):

```sh
bun run build
bun run mock
```

Then open <http://localhost:8080/dashboard/>.

Use `bun run mock:watch` to restart the server when it changes.

## Endpoints

| Method | Path                                 | Description                                           |
| ------ | ------------------------------------ | ----------------------------------------------------- |
| `GET`  | `/dashboard/api/get-health`          | Returns `ok` (200) or `unhealthy` (503).              |
| `GET`  | `/dashboard/api/trigger-init`        | Returns `ok`, matching the robot controller.          |
| `GET`  | `/dashboard/api/autonomous/get-path` | Returns the uploaded autonomous path as JSON.         |
| `POST` | `/dashboard/api/autonomous/set-path` | Body `[{ "x": 20, "y": 80, "tolerance": 2 }]`.        |
| `POST` | `/dashboard/api/mock/health`         | Body `{ "healthy": false }` to simulate an outage.    |
| `POST` | `/dashboard/api/mock/log`            | Body `{ "type": "warn", "tag": "Test", "msg": "…" }`. |
| `POST` | `/dashboard/api/mock/position`       | Body `{ "x": 50, "y": 50, "rotation": 1.57 }`.        |

The robot position otherwise follows a looping path, and log messages are emitted
every few seconds.

## WebSocket protocol

The dashboard sends:

```json
{ "namespace": "system", "type": "subscribeToNamespace", "payload": "dashboard" }
```

The mock then broadcasts FTC-style messages:

```json
{ "namespace": "dashboard", "type": "log", "payload": "{\"type\":\"info\",\"tag\":\"Core\",\"msg\":\"…\"}" }
{ "namespace": "dashboard", "type": "position", "payload": "{\"x\":50,\"y\":50,\"rotation\":1.57}" }
```

Payloads are JSON strings, matching `FtcWebSocketMessage` on the robot.

## Configuration

| Variable          | Default | Description                                |
| ----------------- | ------- | ------------------------------------------ |
| `MOCK_HTTP_PORT`  | `8080`  | HTTP port.                                 |
| `MOCK_WS_PORT`    | `8081`  | WebSocket port (must match the frontend).  |
| `MOCK_STATIC_DIR` | `build` | Directory served at `/dashboard/`.         |
| `MOCK_UNHEALTHY`  | unset   | Set to `1` to start with an unhealthy API. |
