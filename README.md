# Event Listener

An example of a Concord Server plugin. It creates a new WebSocket endpoint
(`/events`), listens for all process events and broadcasts them to all active
subscribers.

## Usage

- compile with `mvn package`;
- copy `target/event-listener-plugin-*.jar` into an `ext` directory somewhere on the filesystem;
- mount the `ext` directory to the Concord Server Docker container:
```
docker run \
...
-v "/path/to/host/ext:/opt/concord/server/ext:ro" \
...
walmartlabs/concord-server
```

If everything is okay you should see something like this in the Server's log:
```
12:21:25.830 [main] [INFO ] [] c.i.c.e.ProcessEventsWebSocket - The /events socket is configured
```

Connect to the endpoint using any WebSocket client (e.g. https://github.com/vi/websocat):
```
$ websocat ws://localhost:8001/events/${processId}
```

Replace `${processId}` with the ID of a process which events you wish to receive.

You can run it several times in different terminal windows to simulate multiple
subscribers.

Start one or more Concord processes. You should see a stream of events in
the `websocat`'s terminal:

```yaml
[{"processKey":{"instanceId":"c028de4b-851a-45f2-b4d4-b0b31f470c4c","createdAt":1582824616684},"eventType":"PROCESS_STATUS","eventDate":null,"data":{"status":"NEW"}}]
[{"processKey":{"instanceId":"c028de4b-851a-45f2-b4d4-b0b31f470c4c","createdAt":1582824616684},"eventType":"PROCESS_STATUS","eventDate":null,"data":{"status":"ENQUEUED"}}]
[{"processKey":{"instanceId":"c028de4b-851a-45f2-b4d4-b0b31f470c4c","createdAt":1582824616684},"eventType":"PROCESS_STATUS","eventDate":null,"data":{"status":"STARTING"}}]
[{"processKey":{"instanceId":"c028de4b-851a-45f2-b4d4-b0b31f470c4c","createdAt":1582824616684},"eventType":"PROCESS_STATUS","eventDate":null,"data":{"status":"RUNNING"}}]
...etc...
```
