/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.webui.commons;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.web.application.Parameter;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.Lifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS
 * Author : Tran Hung Phong
 *          phongth@exoplatform.com
 * Oct 23, 2012
 */
@ComponentConfig(
  lifecycle = Lifecycle.class, 
  template = "classpath:groovy/webui/commons/UISpaceSwitcher.gtmpl",
  events = {@EventConfig(listeners = UISpacesSwitcher.SelectSpaceActionListener.class)}
)
public class UISpacesSwitcher extends UIContainer {
  public static final String SPACE_ID_PARAMETER = "spaceId";
  
  public static final String SELECT_SPACE_ACTION = "SelectSpace";
  
  private EventUIComponent eventComponent;
  
  private String currentSpaceName = StringUtils.EMPTY;
  
  public UISpacesSwitcher() throws Exception {
  }
  
  public void init(EventUIComponent eventComponent) {
    this.eventComponent = eventComponent;
  }
  
  public EventUIComponent getEventComponent() {
    return eventComponent;
  }
  
  public void setCurrentSpaceName(String currentSpaceName) {
    this.currentSpaceName = currentSpaceName;
  }
  
  public String getCurrentSpaceName() {
    return currentSpaceName;
  }
  
  protected String getBaseRestUrl() {
    StringBuilder sb = new StringBuilder();
    sb.append("/").append(PortalContainer.getCurrentPortalContainerName()).append("/");
    sb.append(PortalContainer.getCurrentRestContextName());
    return sb.toString();
  }
  
  protected String createSelectSpaceEvent(String spaceId) throws Exception {
    Parameter parameter = new Parameter(SPACE_ID_PARAMETER, spaceId);
    return event(SELECT_SPACE_ACTION, null, new Parameter[] {parameter});
  }
  
  public static class SelectSpaceActionListener extends EventListener<UISpacesSwitcher> {
    public void execute(Event<UISpacesSwitcher> event) throws Exception {
      WebuiRequestContext context = event.getRequestContext();
      UISpacesSwitcher spaceSwitcher = event.getSource();
      UIPortletApplication root = spaceSwitcher.getAncestorOfType(UIPortletApplication.class);
      EventUIComponent eventComponent = spaceSwitcher.getEventComponent();
      UIComponent uiComponent = null;
      if (eventComponent.getId() != null) {
        uiComponent = (UIComponent) root.findComponentById(eventComponent.getId());
      } else {
        uiComponent = root;
      }
      String eventName = eventComponent.getEventName();
      Event<UIComponent> xEvent = uiComponent.createEvent(eventName, Event.Phase.PROCESS, context);
      if (xEvent != null) {
        xEvent.broadcast();
      }
    }
  }
}