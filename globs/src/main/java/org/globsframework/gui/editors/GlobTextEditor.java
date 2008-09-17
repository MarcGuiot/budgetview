package org.globsframework.gui.editors;

import org.globsframework.metamodel.fields.StringField;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GlobTextEditor extends AbstractGlobTextEditor<JTextField, GlobTextEditor> {
  public static GlobTextEditor init(StringField field, GlobRepository repository, Directory directory) {
    return new GlobTextEditor(field, repository, directory, new JTextField());
  }

  public GlobTextEditor(StringField field, GlobRepository repository, Directory directory, JTextField component) {
    super(field, component, repository, directory);
  }

  protected void registerChangeListener() {
    textComponent.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        applyChanges();
      }
    });
  }
}
