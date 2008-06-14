package org.globsframework.wicket.editors;

import org.globsframework.wicket.model.FieldValueModel;
import wicket.markup.html.form.CheckBox;
import wicket.markup.html.panel.Panel;
import wicket.model.Model;

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
