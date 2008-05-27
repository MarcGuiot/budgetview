package org.crossbowlabs.globs.gui.editors;

import org.crossbowlabs.globs.metamodel.fields.StringField;
import org.uispec4j.TextBox;

import javax.swing.*;
import java.awt.event.FocusListener;
import java.awt.event.FocusEvent;

public class GlobMultiLineTextEditorTest extends AbstractGlobTextEditorTestCase {
  protected TextBox init(StringField name) {
    JTextArea textField =
            GlobMultiLineTextEditor.init(name, repository, directory).getComponent();
    return new TextBox(textField);
  }

  protected void enterTextAndValidate(TextBox textBox, String text) {
    super.enterTextAndValidate(textBox, text);
    simulateFocusLost(textBox);
  }
}
