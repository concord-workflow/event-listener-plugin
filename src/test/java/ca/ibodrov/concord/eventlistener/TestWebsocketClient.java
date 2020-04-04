package ca.ibodrov.concord.eventlistener;

/*-
 * *****
 * Concord
 * -----
 * Copyright (C) 2020 Ivan Bodrov
 * -----
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =====
 */

import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import java.net.URI;
import java.util.concurrent.TimeUnit;

public class TestWebsocketClient implements AutoCloseable {

    private WebSocketClient client;

    public TestWebsocket connect(String uri) throws Exception {
        TestWebsocket socket = new TestWebsocket();

        this.client = new WebSocketClient();
        this.client.start();

        ClientUpgradeRequest request = new ClientUpgradeRequest();
        client.connect(socket, new URI(uri), request);

        socket.waitForConnection(1, TimeUnit.MINUTES);
        return socket;
    }

    @Override
    public void close() throws Exception {
        if (client != null) {
            client.stop();
        }
    }
}
