package org.kie.jbpm.designer.server.services;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.ServletContext;

import org.jboss.errai.bus.server.annotations.Service;
import org.kie.commons.validation.Preconditions;
import org.kie.jbpm.designer.server.MockServletContext;
import org.kie.jbpm.designer.service.JSONService;
import org.kie.jbpm.designer.web.profile.IDiagramProfile;
import org.kie.jbpm.designer.web.profile.impl.JbpmProfileImpl;
import org.uberfire.backend.vfs.Path;
import org.uberfire.backend.vfs.VFSService;

/**
 * [manstis] Default implementation
 */
@Service
@ApplicationScoped
public class DefaultJSONService implements JSONService {

    @Inject
    private VFSService vfs;

    @Override
    public String getJsonModel( final Path path ) {
        Preconditions.checkNotNull( "path", path );

        //Get the XML
        final String bpmn = vfs.readAllString( path );

        //Mock a ServletContext (as the profile definitions are now stored on the classpath)
        final ServletContext context = new MockServletContext();

        //Get the default profile handler (hard coded for now, could be a parameter)
        final IDiagramProfile profile = new JbpmProfileImpl( context );

        //Convert to JSON
        final String json = profile.createUnmarshaller().parseModel( bpmn,
                                                                     profile,
                                                                     "" );
        return json;
    }
}
