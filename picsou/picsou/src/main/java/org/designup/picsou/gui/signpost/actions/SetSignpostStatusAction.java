package org.designup.picsou.gui.signpost.actions;

import org.designup.picsou.model.SignpostStatus;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.model.GlobRepository;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class SetSignpostStatusAction extends AbstractAction {

  private BooleanField field;
  private BooleanField prerequisiteField;
  private GlobRepository repository;

  public SetSignpostStatusAction(BooleanField field, GlobRepository repository) {
    this(field, null, repository);
  }

  public SetSignpostStatusAction(BooleanField field,
                                 BooleanField prerequisiteField,
                                 GlobRepository repository) {
    this.field = field;
    this.prerequisiteField = prerequisiteField;
    this.repository = repository;
  }

  public void actionPerformed(ActionEvent e) {
    if ((prerequisiteField != null) && !SignpostStatus.isCompleted(prerequisiteField, repository)) {
      return;
    }
    SignpostStatus.setCompleted(field, repository);
  }
}
