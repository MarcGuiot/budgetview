package org.crossbowlabs.globs.gui.editors;

import org.crossbowlabs.globs.metamodel.fields.StringField;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class GlobTextEditor extends AbstractGlobTextEditor<JTextField> {
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
