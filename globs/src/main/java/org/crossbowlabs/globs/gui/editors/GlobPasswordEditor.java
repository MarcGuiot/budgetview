package org.crossbowlabs.globs.gui.editors;

import org.crossbowlabs.globs.metamodel.fields.StringField;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class GlobPasswordEditor extends AbstractGlobTextEditor<JPasswordField> {
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
