package org.globsframework.wicket.editors;

import wicket.markup.html.form.AbstractTextComponent;
import wicket.markup.html.form.validation.StringValidator;
import wicket.markup.html.panel.Panel;

public class MultiLineTextEditorPanel extends Panel {

  public static final String ID = "editor";

  public MultiLineTextEditorPanel(String parentId,
                                  AbstractTextComponent component,
                                  int maxSize, boolean required) {
    super(parentId);
    add(component);
    component.setRequired(required);
    component.add(StringValidator.maximumLength(maxSize));
  }
}
