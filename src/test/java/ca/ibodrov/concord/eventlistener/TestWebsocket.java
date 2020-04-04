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

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@WebSocket
public class TestWebsocket {

    private final CountDownLatch connectLatch = new CountDownLatch(1);
    private final List<String> inbox = new ArrayList<>();

    public List<String> flushInbox() {
        synchronized (inbox) {
            List<String> l = new ArrayList<>(inbox);
            inbox.clear();
            return l;
        }
    }

    public void waitForConnection(int timeout, TimeUnit unit) throws Exception {
        if (!connectLatch.await(timeout, unit)) {
            throw new IllegalStateException("Timeout waiting for connection");
        }
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        System.out.println("onClose! " + statusCode + " -> " + reason);
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        System.out.println("onConnect! " + session);
        connectLatch.countDown();
    }

    @OnWebSocketMessage
    public void onMessage(String msg) {
        System.out.println("onMessage! " + msg);
        synchronized (inbox) {
            inbox.add(msg);
        }
    }

    @OnWebSocketError
    public void onError(Throwable cause) {
        System.out.print("onError!: " + cause.getMessage());
        cause.printStackTrace(System.out);
    }
}
