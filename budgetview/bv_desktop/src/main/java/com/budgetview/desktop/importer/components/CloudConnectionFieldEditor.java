package com.budgetview.desktop.importer.components;

import com.budgetview.budgea.model.BudgeaBankField;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.gui.views.GlobLabelView;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;

public abstract class CloudConnectionFieldEditor implements Disposable {

  private Glob budgeaField;
  private Glob budgeaFieldValue;
  private GlobRepository repository;
  private Directory directory;
  private GlobLabelView labelView;

  public <T extends Component> CloudConnectionFieldEditor(Glob budgeaField, Glob budgeaFieldValue, GlobRepository repository, Directory directory) {
    this.budgeaField = budgeaField;
    this.budgeaFieldValue = budgeaFieldValue;
    this.repository = repository;
    this.directory = directory;

    labelView = GlobLabelView.init(BudgeaBankField.LABEL, repository, directory)
      .forceSelection(budgeaField.getKey());
  }

  public JLabel getLabel() {
    return labelView.getComponent();
  }

  public abstract JComponent getEditor();

  public void dispose() {
    labelView.dispose();
  }
}
