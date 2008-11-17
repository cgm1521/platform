/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.applicationregistry.webui.component;

import java.util.Calendar;

import org.exoplatform.application.gadget.Gadget;
import org.exoplatform.application.gadget.GadgetRegistryService;
import org.exoplatform.application.gadget.Source;
import org.exoplatform.application.gadget.SourceStorage;
import org.exoplatform.portal.webui.application.GadgetUtil;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;

/**
 * Created by The eXo Platform SAS
 * Author : Pham Thanh Tung
 *          thanhtungty@gmail.com
 * Jul 29, 2008  
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "system:/groovy/webui/form/UIForm.gtmpl",
    events = {
      @EventConfig(listeners = UIGadgetEditor.SaveActionListener.class),
      @EventConfig(listeners = UIGadgetEditor.CancelActionListener.class)
    }
)
public class UIGadgetEditor extends UIForm {
  
  final static public String FIELD_NAME = "name" ;
  final static public String FIELD_SOURCE = "source" ;
  
  private Source source_;
  private String fullName_;
  
  public UIGadgetEditor() throws Exception {
    addUIFormInput(new UIFormStringInput(FIELD_NAME, null, null)) ;
    addUIFormInput(new UIFormTextAreaInput(FIELD_SOURCE, null, null)) ;
  }
  
  public Source getSource() { return source_; }
  
  public void setSource(Source source) throws Exception {
    source_ = source;
    fullName_ = source_.getName();
    UIFormStringInput uiInputName = getUIStringInput(FIELD_NAME);
    uiInputName.setValue(extractName(fullName_));
    uiInputName.setEditable(false);
    UIFormTextAreaInput uiInputSource = getUIFormTextAreaInput(FIELD_SOURCE);
    uiInputSource.setValue(source_.getTextContent());
  }
  
  public String getSourceFullName() {
    return (fullName_ != null) ? fullName_ : appendTail(getUIStringInput(FIELD_NAME).getValue());
  }
  
  public String getSourceName() {
    return extractName(fullName_);
  }
  
  private String extractName(String fullName) {
    int idx = fullName.indexOf('.');
    return (idx > 0) ? fullName.substring(0, idx) : fullName;
  }
  
  private String appendTail(String name) {
    int idx = name.indexOf('.');
    return (idx > 0) ? name : name + ".xml";
  }
  
  public static class SaveActionListener extends EventListener<UIGadgetEditor> {

    public void execute(Event<UIGadgetEditor> event) throws Exception {
      UIGadgetEditor uiForm = event.getSource() ;
      String name = uiForm.getUIStringInput(UIGadgetEditor.FIELD_NAME).getValue() ;
      String text = uiForm.getUIFormTextAreaInput(UIGadgetEditor.FIELD_SOURCE).getValue() ;
      SourceStorage sourceStorage = uiForm.getApplicationComponent(SourceStorage.class) ;
      String fileName = uiForm.getSourceFullName();
      Source source = new Source(fileName, "application/xml", "UTF-8");
      source.setTextContent(text);
      source.setLastModified(Calendar.getInstance());
      sourceStorage.saveSource(null, source) ;
      GadgetRegistryService service = uiForm.getApplicationComponent(GadgetRegistryService.class) ;
      service.saveGadget(GadgetUtil.toGadget(name, sourceStorage.getSourceURI(fileName), true)) ;
      UIGadgetManagement uiManagement = uiForm.getParent() ;
      uiManagement.initData() ;
      uiManagement.setSelectedGadget(name);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManagement) ;
    }
    
  }
  
  public static class CancelActionListener extends EventListener<UIGadgetEditor> {

    public void execute(Event<UIGadgetEditor> event) throws Exception {
      UIGadgetEditor uiForm = event.getSource() ;
      UIGadgetManagement uiManagement = uiForm.getParent() ;
      Gadget selectedGadget = uiManagement.getSelectedGadget();      
      if(selectedGadget != null) {
        uiManagement.setSelectedGadget(selectedGadget.getName());
      } else uiManagement.reload();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManagement) ;      
    }
    
  }
}