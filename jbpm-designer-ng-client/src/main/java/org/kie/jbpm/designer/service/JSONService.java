package org.kie.jbpm.designer.service;

import org.jboss.errai.bus.server.annotations.Remote;
import org.uberfire.backend.vfs.Path;

/**
 * Designer service for JSON<->BPMN2 conversions
 */
@Remote
public interface JSONService {

    public String getJsonModel(final Path path);

}
