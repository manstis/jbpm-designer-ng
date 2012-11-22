package org.kie.jbpm.designer.web.profile.impl;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.codehaus.jackson.JsonParseException;
import org.eclipse.bpmn2.Definitions;
import org.eclipse.emf.ecore.resource.Resource;
import org.kie.jbpm.designer.bpmn2.impl.Bpmn2JsonUnmarshaller;
import org.kie.jbpm.designer.bpmn2.resource.JBPMBpmn2ResourceImpl;
import org.kie.jbpm.designer.epn.impl.EpnJsonMarshaller;
import org.kie.jbpm.designer.epn.impl.EpnJsonUnmarshaller;
import org.kie.jbpm.designer.web.plugin.IDiagramPlugin;
import org.kie.jbpm.designer.web.plugin.impl.PluginServiceImpl;
import org.kie.jbpm.designer.web.profile.IDiagramProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The implementation of the epn profile for Process Designer.
 * @author Tihomir Surdilovic
 */
public class EpnProfileImpl implements IDiagramProfile {

    private static Logger _logger = LoggerFactory.getLogger(EpnProfileImpl.class);
    private Map<String, IDiagramPlugin> _plugins = new LinkedHashMap<String, IDiagramPlugin>();
    
    private String _stencilSet;
    private String _externalLoadHost;
    private String _externalLoadProtocol;
    private String _externalLoadSubdomain;
    private String _usr;
    private String _pwd;
    private String _localHistoryEnabled;
    private String _localHistoryTimeout;
    
    public EpnProfileImpl(ServletContext servletContext) {
        this(servletContext, true);
    }
    
    public EpnProfileImpl(ServletContext servletContext, boolean initializeLocalPlugins) {
        if (initializeLocalPlugins) {
            initializeLocalPlugins(servletContext);
        }
    }
    
    private void initializeLocalPlugins(ServletContext context) {
        Map<String, IDiagramPlugin> registry = PluginServiceImpl.getLocalPluginsRegistry(context);
        //we read the default.xml file and make sense of it.
        InputStream fileStream = null;
        try {
            //[manstis] - Profile definition has been moved to the classpath
            fileStream = EpnProfileImpl.class.getResourceAsStream( new StringBuilder(context.getRealPath("/")).append("/").append("designer").append("/").append("profiles").append("/").append("epn.xml").toString() );
            //try {
            //    fileStream = new FileInputStream(new StringBuilder(context.getRealPath("/")).append("/").append("designer").append("/").append("profiles").append("/").append("epn.xml").toString());
            //} catch (FileNotFoundException e) {
            //    throw new RuntimeException(e);
            //}
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLStreamReader reader = factory.createXMLStreamReader(fileStream, "UTF-8");
            while(reader.hasNext()) {
                if (reader.next() == XMLStreamReader.START_ELEMENT) {
                    if ("profile".equals(reader.getLocalName())) {
                        for (int i = 0 ; i < reader.getAttributeCount() ; i++) {
                            if ("stencilset".equals(reader.getAttributeLocalName(i))) {
                                _stencilSet = reader.getAttributeValue(i);
                            }
                        }
                    } else if ("plugin".equals(reader.getLocalName())) {
                        String name = null;
                        for (int i = 0 ; i < reader.getAttributeCount() ; i++) {
                            if ("name".equals(reader.getAttributeLocalName(i))) {
                                name = reader.getAttributeValue(i);
                            }
                        }
                        _plugins.put(name, registry.get(name));
                    } else if ("externalloadurl".equals(reader.getLocalName())) {
                        for (int i = 0 ; i < reader.getAttributeCount() ; i++) {
                            if ("protocol".equals(reader.getAttributeLocalName(i))) {
                                _externalLoadProtocol = reader.getAttributeValue(i);
                            }
                            if ("host".equals(reader.getAttributeLocalName(i))) {
                                _externalLoadHost = reader.getAttributeValue(i);
                            }
                            if ("subdomain".equals(reader.getAttributeLocalName(i))) {
                                _externalLoadSubdomain = reader.getAttributeValue(i);
                            }
                            if ("usr".equals(reader.getAttributeLocalName(i))) {
                                _usr = reader.getAttributeValue(i);
                            }
                            if ("pwd".equals(reader.getAttributeLocalName(i))) {
                                _pwd = reader.getAttributeValue(i);
                            }
                        }
                    }
                }
            }
        } catch (XMLStreamException e) {
            _logger.error(e.getMessage(), e);
            throw new RuntimeException(e); // stop initialization
        } finally {
            if (fileStream != null) { try { fileStream.close(); } catch(IOException e) {}};
        }
    }
    
    public String getName() {
        return "epn";
    }

    public String getTitle() {
        return "EPN Designer";
    }

    public String getStencilSet() {
        return _stencilSet;
    }

    public Collection<String> getStencilSetExtensions() {
        return Collections.emptyList();
    }

    public String getSerializedModelExtension() {
        return "epn";
    }

    public String getStencilSetURL() {
        return "/designer/stencilsets/epn/epn.json";
    }

    public String getStencilSetNamespaceURL() {
        return "http://b3mn.org/stencilset/epn#";
    }

    public String getStencilSetExtensionURL() {
        return "http://oryx-editor.org/stencilsets/extensions/epn#";
    }

    public Collection<String> getPlugins() {
        return Collections.unmodifiableCollection(_plugins.keySet());
    }

    public IDiagramMarshaller createMarshaller() {
        return new IDiagramMarshaller() {
            public String parseModel(String jsonModel, String preProcessingData) {
                EpnJsonUnmarshaller unmarshaller = new EpnJsonUnmarshaller();
                Object def; //TODO will be replaced with the epn ecore model class (definitions)
                try {
                    def = unmarshaller.unmarshall(jsonModel);
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    //TODO do something now with the model (save it!);
                    return outputStream.toString();
                } catch (JsonParseException e) {
                    _logger.error(e.getMessage(), e);
                } catch (IOException e) {
                    _logger.error(e.getMessage(), e);
                }

                return "";
            }
            
            public Definitions getDefinitions(String jsonModel,
					String preProcessingData) {
				try {
					Bpmn2JsonUnmarshaller unmarshaller = new Bpmn2JsonUnmarshaller();
					JBPMBpmn2ResourceImpl res = (JBPMBpmn2ResourceImpl) unmarshaller.unmarshall(jsonModel, preProcessingData);
					return (Definitions) res.getContents().get(0);
				} catch (JsonParseException e) {
					_logger.error(e.getMessage(), e);
				} catch (IOException e) {
					_logger.error(e.getMessage(), e);
				}
				return null;
			}
            
            public Resource getResource(String jsonModel, String preProcessingData) {
				try {
					Bpmn2JsonUnmarshaller unmarshaller = new Bpmn2JsonUnmarshaller();
					return (JBPMBpmn2ResourceImpl) unmarshaller.unmarshall(jsonModel, preProcessingData);
				} catch (JsonParseException e) {
					_logger.error(e.getMessage(), e);
				} catch (IOException e) {
					_logger.error(e.getMessage(), e);
				}
				return null;
			}
        };
    }

    public IDiagramUnmarshaller createUnmarshaller() {
        return new IDiagramUnmarshaller() {
            public String parseModel(String xmlModel, IDiagramProfile profile, String preProcessingData) {
                EpnJsonMarshaller marshaller = new EpnJsonMarshaller();
                marshaller.setProfile(profile);
                try {
                    return marshaller.marshall(""); // TODO FIX THIS!
                } catch (Exception e) {
                    _logger.error(e.getMessage(), e);
                }
                return "";
            }
        };
    }

    public String getExternalLoadURLProtocol() {
        return _externalLoadProtocol;
    }

    public String getExternalLoadURLHostname() {
        return _externalLoadHost;
    }

    public String getExternalLoadURLSubdomain() {
        return _externalLoadSubdomain;
    }

    public String getUsr() {
        return _usr;
    }

    public String getPwd() {
        return _pwd;
    }

    public String getLocalHistoryEnabled() {
        return _localHistoryEnabled;
    }

    public String getLocalHistoryTimeout() {
        return _localHistoryTimeout;
    }
}
