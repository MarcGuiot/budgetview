package org.globsframework.wicket.editors;

import org.apache.wicket.markup.html.form.AbstractTextComponent;
import org.apache.wicket.markup.html.panel.Panel;

public class TextEditorPanel extends Panel {

  public static final String ID = "editor";

  public TextEditorPanel(String parentId,
                         AbstractTextComponent component,
                         boolean required) {
    super(parentId);
    add(component);
    component.setRequired(required);
  }
}
