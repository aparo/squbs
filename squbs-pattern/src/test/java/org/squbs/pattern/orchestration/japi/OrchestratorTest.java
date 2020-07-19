/*
 *  Copyright 2017 PayPal
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.squbs.pattern.orchestration.japi;

import akka.actor.ActorRef;
import akka.actor.Props;
import com.typesafe.config.ConfigFactory;
import org.junit.After;
import org.junit.Test;
import org.squbs.testkit.japi.CustomTestKit;
import org.squbs.testkit.japi.DebugTimingTestKit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.squbs.pattern.orchestration.japi.Messages.*;

public class OrchestratorTest extends CustomTestKit {

    public OrchestratorTest() {
        super(ConfigFactory.parseString("squbs.external-config-dir = orchestratorTestConfig"));
    }

    @After
    public void tearDown() {
        shutdown();
    }

    @Test
    public void testResultAfterFinish() {
        new DebugTimingTestKit(system()) {{
            ActorRef orchestrator = getSystem().actorOf(Props.create(TestOrchestrator.class));
            orchestrator.tell(new TestRequest("test"), getRef());

            // Check for the submitted message
            SubmittedOrchestration submitted = expectMsgClass(SubmittedOrchestration.class);
            long submitTime = submitted.timeNs / 1000L;
            System.out.println("Submission took " + submitTime + " microseconds.");
            assertTrue(submitTime / 1000L < 230000L);
            assertEquals("test", submitted.message);

            // Check for the finished message
            FinishedOrchestration finished = expectMsgClass(FinishedOrchestration.class);
            long finishTime = finished.timeNs / 1000L;
            System.out.println("Orchestration took " + finishTime + " microseconds.");
            assertTrue(finishTime > 30000L); // 3 orchestrations with 10 millisecond delay each
            assertEquals("test", finished.message);
            assertEquals(6L, finished.lastId);
        }};
    }
}
