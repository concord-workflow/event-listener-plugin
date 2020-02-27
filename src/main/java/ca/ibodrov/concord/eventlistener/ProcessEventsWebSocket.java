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
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.annotation.WebServlet;

@Named
@WebServlet("/events")
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

    public static class Listener implements WebSocketListener {

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
            this.subscriber = new Subscriber(session);
            subscriberManager.subscribe(subscriber);
        }

        @Override
        public void onWebSocketError(Throwable cause) {
            // ignore
        }
    }
}
