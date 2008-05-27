package org.crossbowlabs.globs.wicket.editors;

import wicket.markup.html.form.AbstractTextComponent;
import wicket.markup.html.panel.Panel;

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
