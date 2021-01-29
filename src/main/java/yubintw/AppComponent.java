/*
 * Copyright 2020-present Open Networking Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package yubintw;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.onosproject.net.Host;
import org.onosproject.net.HostLocation;
import org.onosproject.net.host.HostEvent;
import org.onosproject.net.host.HostListener;
import org.onosproject.net.host.HostService;
import org.onlab.packet.MacAddress;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Skeletal ONOS application component.
 */
@Component(immediate = true)
public class AppComponent {

    // vCPE-Manager API Endpoint
    private final String VCPE_API_URL = "http://172.17.0.5:8000";

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final HostListener hostListener = new InternalHostListener();

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    private HostService hostService;

    Client client = ClientBuilder.newClient();
    WebTarget wt = client.target(VCPE_API_URL);

    @Activate
    protected void activate() {
        log.info("Starting Yubin APP");
        hostService.addListener(hostListener);

        String response = wt.path("/test").request().get(String.class);
        log.info("API Test: "+ response);

        log.info("Started Yubin APP");
    }

    @Deactivate
    protected void deactivate() {
        hostService.removeListener(hostListener);
        log.info("Stopped Yubin APP");
    }

    /**
     * A listener of host events that provisions two tunnels for each pair of
     * hosts when a new host is discovered.
     */
    private class InternalHostListener implements HostListener {

        @Override
        public void event(HostEvent event) {
            if (event.type() == HostEvent.Type.HOST_ADDED) {
                log.info("Host ADDED event");
            }
            if (event.type() == HostEvent.Type.HOST_MOVED) {
                log.info("Host MOVED event");
            }
            if (event.type() == HostEvent.Type.HOST_REMOVED) {
                log.info("Host REMOVED event");
            }
            if (event.type() == HostEvent.Type.HOST_UPDATED) {
                log.info("Host UPDATED event");
            }

            synchronized (this) {
                // Synchronizing here is an overkill, but safer for demo purposes.
                Host host = event.subject();
                MacAddress mac = host.mac();
                HostLocation location = host.location();
                log.info("MAC = "+ mac.toString());
                log.info("location = "+location.toString());

                // 通知 vCPE Manager
                if (event.type() == HostEvent.Type.HOST_ADDED) {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode jsonNode = mapper.createObjectNode();
                    ((ObjectNode) jsonNode).put("mac", mac.toString());
                    ((ObjectNode) jsonNode).put("location", location.toString());
    
                    Response r = wt.path("/add_update_ue").request(MediaType.APPLICATION_JSON).post(Entity.json(jsonNode.toString()));
                    log.info("add_update_ue");
                    log.info("status code = " + Integer.toString(r.getStatus()));
                }
            }
        }
    } // end of class InternalHostListener
}
