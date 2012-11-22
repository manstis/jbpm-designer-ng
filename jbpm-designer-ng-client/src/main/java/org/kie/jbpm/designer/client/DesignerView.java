/*
 * Copyright 2012 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.jbpm.designer.client;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class DesignerView
        extends Composite
        implements DesignerPresenter.View,
                   RequiresResize {

    @Inject
    private Bootstrap bootstrap;

    private DesignerPresenter presenter;

    //The HTML elementID of the Oryx container - this needs to be unique to support multiple instances
    private final String panelId = "designer-panel";

    private VerticalPanel vp = new VerticalPanel();

    @PostConstruct
    public void init() {
        initWidget( vp );
        bootstrap.init();
    }

    @Override
    public void init( final DesignerPresenter presenter ) {
        this.presenter = presenter;
    }

    @Override
    public void setJsonModel( final String jsonModel ) {

        //Bootstrap controls (hack for now to ensure start-up scripts are loaded and ran)
        final HorizontalPanel buttonsContainer = new HorizontalPanel();
        final Button bootstrapButton = new Button( "Bootstrap" );
        bootstrapButton.addClickHandler( new ClickHandler() {
            @Override
            public void onClick( ClickEvent event ) {
                bootstrap();
            }
        } );
        buttonsContainer.add( bootstrapButton );

        final Button loadProcessButton = new Button( "Load process" );
        loadProcessButton.addClickHandler( new ClickHandler() {
            @Override
            public void onClick( ClickEvent event ) {
                loadProcess( panelId,
                             jsonModel );
            }
        } );
        buttonsContainer.add( loadProcessButton );
        vp.add( buttonsContainer );

        //Otyx container
        final HTML html = new HTML();
        final DivElement designerDiv = DivElement.as( DOM.createDiv() );
        designerDiv.setId( panelId );
        html.getElement().insertFirst( designerDiv );
        vp.add( html );

    }

    private native void bootstrap()  /*-{
        $wnd.bootstrap();
    }-*/;

    private native void loadProcess( final String panelId,
                                     final String jsonModel )  /*-{
        $wnd.Kickstart.load();
        $wnd.loadProcess(panelId, jsonModel);
    }-*/;

    @Override
    public void onResize() {
        final Widget w = getParent();
        final int width = w.getOffsetWidth();
        vp.setWidth( width + "px" );
    }
}
