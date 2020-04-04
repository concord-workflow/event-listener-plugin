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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.walmartlabs.concord.server.sdk.events.ProcessEvent;
import com.walmartlabs.concord.server.sdk.events.ProcessEventListener;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Named
@Singleton
public class SubscriberManager implements ProcessEventListener {

    private static final Logger log = LoggerFactory.getLogger(SubscriberManager.class);

    private final List<Subscriber> subscribers = new ArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void subscribe(Subscriber s) {
        synchronized (subscribers) {
            subscribers.add(s);
        }

        // TODO send all events happened before the connection was established?
    }

    public void unsubscribe(Subscriber s) {
        synchronized (subscribers) {
            subscribers.remove(s);
        }
    }

    @Override
    public void onEvents(List<ProcessEvent> events) {
        // for large number of subscribers or messages a RW lock, a COW list or
        // another concurrent collection might be better
        synchronized (subscribers) {
            for (Subscriber s : subscribers) {
                List<ProcessEvent> l = events.stream()
                        .filter(e -> e.getProcessKey().getInstanceId().equals(s.getInstanceId()))
                        .collect(Collectors.toList());

                if (l.isEmpty()) {
                    continue;
                }

                String msg;
                try {
                    msg = objectMapper.writeValueAsString(l);
                } catch (IOException e) {
                    log.warn("Serialization error: {}", e.getMessage(), e);
                    return;
                }

                Session session = s.getSession();
                if (session == null) {
                    continue;
                }

                RemoteEndpoint remote = session.getRemote();
                try {
                    remote.sendString(msg);
                } catch (IOException e) {
                    log.warn("Error while sending a message: {}", e.getMessage(), e);
                }
            }
        }
    }
}
