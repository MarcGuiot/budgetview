package org.globsframework.gui.editors;

import org.globsframework.metamodel.fields.StringField;
import org.globsframework.model.Glob;
import org.globsframework.model.Key;
import org.uispec4j.TextBox;

import javax.swing.*;

public class GlobTextEditorTest extends AbstractGlobTextEditorTestCase {
  private GlobTextEditor editor;

  protected TextBox init(StringField name, String defaultValueForMultivalue, boolean isEditable, boolean sendAtKeyPressed) {
    editor = GlobTextEditor.init(name, repository, directory)
      .setMultiSelectionText(defaultValueForMultivalue)
      .setNotifyOnKeyPressed(sendAtKeyPressed)
      .setEditable(isEditable);
    JTextField textField =
      editor.getComponent();
    return new TextBox(textField);
  }

  void forceEdition(Key key) {
    editor.forceSelection(key);
  }
}
