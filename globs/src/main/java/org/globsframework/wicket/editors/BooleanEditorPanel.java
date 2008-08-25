package org.globsframework.wicket.editors;

import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.globsframework.wicket.model.FieldValueModel;

public class BooleanEditorPanel extends Panel {

  public BooleanEditorPanel(String parentId, FieldValueModel model, final String componentId, String label) {
    super(parentId);
    CheckBox component = new CheckBox("editor", model) {
      public String getInputName() {
        return componentId;
      }
    };
    component.setLabel(new Model(label));
    add(component);
  }
}
