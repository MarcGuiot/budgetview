package org.globsframework.gui.editors;

import org.globsframework.metamodel.fields.StringField;
import org.globsframework.model.Glob;
import org.uispec4j.TextBox;

import javax.swing.*;

public class GlobMultiLineTextEditorTest extends AbstractGlobTextEditorTestCase {
  private GlobMultiLineTextEditor editor;

  protected TextBox init(StringField name, String defaultValueForMultivalue, boolean isEditable, boolean sendAtKeyPressed) {
    editor = GlobMultiLineTextEditor.init(name, repository, directory)
      .setMultiSelectionText(defaultValueForMultivalue)
      .setNotifyAtKeyPressed(sendAtKeyPressed)
      .setEditable(isEditable);
    JTextArea field = editor.getComponent();
    return new TextBox(field);
  }

  void forceEdition(Glob glob) {
    editor.forceSelection(glob);
  }

  protected void enterTextAndValidate(TextBox textBox, String text) {
    super.enterTextAndValidate(textBox, text);
    simulateFocusLost(textBox);
  }
}
