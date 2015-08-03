package org.globsframework.gui.editors;

import org.globsframework.metamodel.fields.StringField;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GlobTextEditor extends AbstractGlobTextEditor<JTextField, GlobTextEditor> {

  private GlobTextEditor.ApplyActionListener applyActionListener;

  public static GlobTextEditor init(StringField field, GlobRepository repository, Directory directory) {
    return new GlobTextEditor(field, repository, directory, new JTextField());
  }

  private Action validationAction;

  public GlobTextEditor(StringField field, GlobRepository repository, Directory directory, JTextField component) {
    super(field, component, repository, directory);
  }

  public GlobTextEditor setValidationAction(Action action) {
    if (this.validationAction != null){
      textComponent.removeActionListener(validationAction);
    }
    this.validationAction = action;
    textComponent.addActionListener(validationAction);
    return this;
  }
  
  protected void registerActionListener() {
    if (applyActionListener == null){
      applyActionListener = new ApplyActionListener();
      textComponent.addActionListener(applyActionListener);
    }
  }

  public void dispose() {
    if (validationAction != null){
      textComponent.removeActionListener(validationAction);
    }
    if (applyActionListener != null) {
      textComponent.removeActionListener(applyActionListener);
    }
    super.dispose();
  }

  private class ApplyActionListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      apply();
    }
  }
}
