package org.globsframework.gui.editors;

import org.globsframework.metamodel.fields.StringField;
import org.uispec4j.TextBox;

import javax.swing.*;

public class GlobMultiLineTextEditorTest extends AbstractGlobTextEditorTestCase {
  protected TextBox init(StringField name, String defaultValueForMultivalue, boolean isEditable, boolean sendAtKeyPressed) {
    JTextArea textField =
      (JTextArea)GlobMultiLineTextEditor.init(name, repository, directory)
        .setMultiSelectionText(defaultValueForMultivalue)
        .setNotifyAtKeyPressed(sendAtKeyPressed)
        .setEditable(isEditable).getComponent();
    return new TextBox(textField);
  }

  protected void enterTextAndValidate(TextBox textBox, String text) {
    super.enterTextAndValidate(textBox, text);
    simulateFocusLost(textBox);
  }
}
