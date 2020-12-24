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
import org.onosproject.net.host.HostEvent;
import org.onosproject.net.host.HostListener;
import org.onosproject.net.host.HostService;
import org.onlab.packet.MacAddress;

/**
 * Skeletal ONOS application component.
 */
@Component(immediate = true)
public class AppComponent {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final HostListener hostListener = new InternalHostListener();

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    private HostService hostService;

    @Activate
    protected void activate() {
        log.info("Starting Yubin APP");
        hostService.addListener(hostListener);
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
                log.info("MAC = "+ mac.toString());
            }
        }
    } // end of class InternalHostListener

}
