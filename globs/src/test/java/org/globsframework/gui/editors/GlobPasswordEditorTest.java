package org.globsframework.gui.editors;

import org.globsframework.metamodel.fields.StringField;
import org.uispec4j.TextBox;

import javax.swing.*;

public class GlobPasswordEditorTest extends AbstractGlobTextEditorTestCase {
  protected TextBox init(StringField name, String defaultValueForMultivalue, boolean isEditable, boolean sendAtKeyPressed) {
    JPasswordField textField =
      (JPasswordField)GlobPasswordEditor.init(name, repository, directory)
        .setMultiSelectionText(defaultValueForMultivalue)
        .setNotifyAtKeyPressed(sendAtKeyPressed)
        .setEditable(isEditable).getComponent();
    return new TextBox(textField);
  }
}
