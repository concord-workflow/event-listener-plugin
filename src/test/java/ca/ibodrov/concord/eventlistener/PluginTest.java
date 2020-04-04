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

import ca.ibodrov.concord.testcontainers.Concord;
import ca.ibodrov.concord.testcontainers.ConcordProcess;
import ca.ibodrov.concord.testcontainers.Payload;
import com.walmartlabs.concord.client.ProcessEntry;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PluginTest {

    @Rule
    public Concord concord = new Concord()
            .localMode(true);

    private TestWebsocketClient websocketClient;

    @Before
    public void setUp() {
        this.websocketClient = new TestWebsocketClient();
    }

    @After
    public void tearDown() throws Exception {
        if (websocketClient != null) {
            websocketClient.close();
        }
    }

    @Test
    public void test() throws Exception {
        String yml = "flows:\n" +
                "  default:\n" +
                "    - ${sleep.ms(5000)}\n" +
                "    - log: Hello!\n";

        ConcordProcess proc = concord.processes().start(new Payload().concordYml(yml));

        String uri = String.format("ws://localhost:%d/events/%s", concord.apiPort(), proc.instanceId());
        TestWebsocket socket = websocketClient.connect(uri);

        ProcessEntry pe = proc.waitForStatus(ProcessEntry.StatusEnum.FINISHED);
        assertEquals(ProcessEntry.StatusEnum.FINISHED, pe.getStatus());

        List<String> inbox = socket.flushInbox();
        assertPattern(inbox, ".*\"status\":\"FINISHED\".*");
    }

    private static void assertPattern(List<String> l, String pattern) {
        assertTrue("Expected to find " + pattern + ", but it wasn't there", l.stream().anyMatch(s -> s.matches(pattern)));
    }
}
