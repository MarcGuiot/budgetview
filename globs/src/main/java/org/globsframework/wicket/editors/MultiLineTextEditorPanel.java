package org.globsframework.wicket.editors;

import org.apache.wicket.markup.html.form.AbstractTextComponent;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.validation.validator.StringValidator;

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
