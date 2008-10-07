package org.globsframework.gui.editors;

import org.globsframework.metamodel.Field;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public abstract class AbstractGlobTextFieldEditor<PARENT extends AbstractGlobTextFieldEditor>
  extends AbstractGlobTextEditor<JTextField, PARENT> {

  private Action validationAction;

  protected AbstractGlobTextFieldEditor(Field field, GlobRepository repository, Directory directory) {
    super(field, new JTextField(), repository, directory);
  }

  public PARENT setValidationAction(Action action) {
    this.validationAction = action;
    return (PARENT)this;
  }

  protected void registerActions() {
    textComponent.addActionListener(validationAction);
  }
}