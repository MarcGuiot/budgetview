package org.globsframework.gui.editors;

import org.globsframework.metamodel.fields.StringField;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class GlobMultiLineTextEditor extends AbstractGlobTextEditor<JTextArea, GlobMultiLineTextEditor> {
  public static GlobMultiLineTextEditor init(StringField field, GlobRepository repository, Directory directory) {
    return new GlobMultiLineTextEditor(field, repository, directory, new JTextArea());
  }

  public GlobMultiLineTextEditor(StringField field, GlobRepository repository, Directory directory, JTextArea component) {
    super(field, component, repository, directory);
  }

  protected void registerActionListener() {
  }
}
