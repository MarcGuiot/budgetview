package org.globsframework.gui.editors;

import org.globsframework.metamodel.fields.StringField;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GlobPasswordEditor extends AbstractGlobTextEditor<JPasswordField, GlobPasswordEditor> {
  public static GlobPasswordEditor init(StringField field, GlobRepository repository, Directory directory) {
    return new GlobPasswordEditor(field, repository, directory, new JPasswordField());
  }

  public GlobPasswordEditor(StringField field, GlobRepository repository, Directory directory, JPasswordField component) {
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
