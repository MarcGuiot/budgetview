package org.globsframework.gui.editors;

import org.globsframework.metamodel.fields.StringField;
import org.globsframework.model.Glob;
import org.uispec4j.TextBox;

import javax.swing.*;

public class GlobPasswordEditorTest extends AbstractGlobTextEditorTestCase {
  private GlobPasswordEditor editor;

  protected TextBox init(StringField name, String defaultValueForMultivalue, boolean isEditable, boolean sendAtKeyPressed) {
    editor = GlobPasswordEditor.init(name, repository, directory)
      .setMultiSelectionText(defaultValueForMultivalue)
      .setNotifyOnKeyPressed(sendAtKeyPressed)
      .setEditable(isEditable);
    JPasswordField textField =
      editor.getComponent();
    return new TextBox(textField);
  }

  void forceEdition(Glob glob) {
    editor.forceSelection(glob);
  }
}
