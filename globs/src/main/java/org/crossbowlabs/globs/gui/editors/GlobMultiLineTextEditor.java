package org.crossbowlabs.globs.gui.editors;

import org.crossbowlabs.globs.metamodel.fields.StringField;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.FocusListener;
import java.awt.event.FocusEvent;

public class GlobMultiLineTextEditor extends AbstractGlobTextEditor<JTextArea> {
  public static GlobMultiLineTextEditor init(StringField field, GlobRepository repository, Directory directory) {
    return new GlobMultiLineTextEditor(field, repository, directory, new JTextArea());
  }

  public GlobMultiLineTextEditor(StringField field, GlobRepository repository, Directory directory, JTextArea component) {
    super(field, component, repository, directory);
  }

  protected void registerChangeListener() {
  }
}
