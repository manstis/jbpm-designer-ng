package org.kie.jbpm.designer.server.services;

import java.io.StringWriter;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.ServletContext;

import com.google.gwt.user.client.Window;
import org.jboss.drools.impl.DroolsPackageImpl;
import org.jboss.errai.bus.server.annotations.Service;
import org.json.JSONException;
import org.json.JSONObject;
import org.kie.commons.validation.Preconditions;
import org.kie.jbpm.designer.server.MockServletContext;
import org.kie.jbpm.designer.service.DesignerAssetService;
import org.kie.jbpm.designer.web.profile.IDiagramProfile;
import org.kie.jbpm.designer.web.profile.impl.JbpmProfileImpl;
import org.kie.jbpm.designer.web.server.ServletUtil;
import org.uberfire.backend.vfs.Path;
import org.uberfire.backend.vfs.VFSService;

/**
 * [manstis] Default implementation
 */
@Service
@ApplicationScoped
public class DefaultDesignerAssetService implements DesignerAssetService {

    @Inject
    private VFSService vfs;

    @Override
    public String loadJsonModel( final Path path ) {

        Preconditions.checkNotNull( "path",
                                    path );

        //Get the XML
        final String bpmn2 = vfs.readAllString( path );

        //Mock a ServletContext (as the profile definitions are now stored on the classpath)
        final ServletContext context = new MockServletContext();

        //Get the default profile handler (hard coded for now, could be a parameter)
        final IDiagramProfile profile = new JbpmProfileImpl( context );

        //Convert to JSON
        DroolsPackageImpl.init();
        final String json = profile.createUnmarshaller().parseModel( bpmn2,
                                                                     profile,
                                                                     "" );
        return json;
    }

    @Override
    public void saveJsonModel( final Path path,
                               final String jsonModel ) {
        Preconditions.checkNotNull( "path",
                                    path );
        Preconditions.checkNotNull( "jsonModel",
                                    jsonModel );

        //Mock a ServletContext (as the profile definitions are now stored on the classpath)
        final ServletContext context = new MockServletContext();

        //Get the default profile handler (hard coded for now, could be a parameter)
        final IDiagramProfile profile = new JbpmProfileImpl( context );

        //Convert to XML
        final String bpmn2 = profile.createMarshaller().parseModel( jsonModel,
                                                                    "" );

        //Write XML back
        vfs.write( path,
                   bpmn2 );
    }

}
