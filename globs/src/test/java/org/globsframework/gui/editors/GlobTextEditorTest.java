package org.globsframework.gui.editors;

import org.globsframework.metamodel.fields.StringField;
import org.uispec4j.TextBox;

import javax.swing.*;

public class GlobTextEditorTest extends AbstractGlobTextEditorTestCase {
  protected TextBox init(StringField name) {
    JTextField textField =
      GlobTextEditor.init(name, repository, directory).getComponent();
    return new TextBox(textField);
  }
}
