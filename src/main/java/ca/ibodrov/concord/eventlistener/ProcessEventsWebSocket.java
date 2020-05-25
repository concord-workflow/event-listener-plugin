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

import org.eclipse.jetty.websocket.api.*;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.UUID;

@Named
@WebServlet("/events/*")
public class ProcessEventsWebSocket extends WebSocketServlet {

    private static final Logger log = LoggerFactory.getLogger(ProcessEventsWebSocket.class);

    private final SubscriberManager subscriberManager;

    @Inject
    public ProcessEventsWebSocket(SubscriberManager subscriberManager) {
        this.subscriberManager = subscriberManager;
    }

    @Override
    public void configure(WebSocketServletFactory factory) {
        factory.setCreator((req, resp) -> new Listener(subscriberManager));
        log.info("The /events socket is configured");
    }

    public static class Listener implements WebSocketListener, WebSocketPingPongListener {

        private final SubscriberManager subscriberManager;

        private Subscriber subscriber;

        public Listener(SubscriberManager subscriberManager) {
            this.subscriberManager = subscriberManager;
        }

        @Override
        public void onWebSocketBinary(byte[] payload, int offset, int len) {
            // ignore
        }

        @Override
        public void onWebSocketText(String message) {
            // ignore
        }

        @Override
        public void onWebSocketClose(int statusCode, String reason) {
            if (subscriber == null) {
                return;
            }

            subscriberManager.unsubscribe(subscriber);
        }

        @Override
        public void onWebSocketConnect(Session session) {
            UpgradeRequest req = session.getUpgradeRequest();
            UUID instanceId = extractInstanceId(req.getRequestURI());
            if (instanceId == null) {
                session.close(400, "Invalid request, can't determine the process ID");
            }

            this.subscriber = new Subscriber(session, instanceId);
            subscriberManager.subscribe(subscriber);
        }

        @Override
        public void onWebSocketError(Throwable cause) {
            // ignore
        }

        @Override
        public void onWebSocketPing(ByteBuffer payload) {
            if (subscriber == null) {
                return;
            }

            RemoteEndpoint endpoint = subscriber.getSession().getRemote();
            try {
                endpoint.sendPong(ByteBuffer.wrap("pong".getBytes()));
            } catch (IOException e) {
                log.warn("onWebSocketPing -> error while sending a pong message: {}", e.getMessage());
            }
        }

        @Override
        public void onWebSocketPong(ByteBuffer payload) {
            // nothing to do
        }
    }

    private static UUID extractInstanceId(URI uri) {
        String s = uri.getPath();
        int i = s.lastIndexOf('/');
        if (i < 0 || i + 1 >= s.length()) {
            return null;
        }
        return UUID.fromString(s.substring(i + 1));
    }
}
