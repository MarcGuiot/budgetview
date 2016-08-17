package com.budgetview.desktop.importer.components;

import com.budgetview.budgea.model.BudgeaConnectionValue;
import org.globsframework.gui.editors.GlobTextEditor;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class CloudConnectionNameFieldEditor extends CloudConnectionFieldEditor {
  private GlobTextEditor editor;

  public CloudConnectionNameFieldEditor(Glob budgeaField, Glob budgeaConnectionValue, GlobRepository repository, Directory directory) {
    super(budgeaField, budgeaConnectionValue, repository, directory);

    editor = GlobTextEditor.init(BudgeaConnectionValue.VALUE, repository, directory)
      .forceSelection(budgeaConnectionValue.getKey());
  }

  public JTextField getEditor() {
    return editor.getComponent();
  }

  public void dispose() {
    super.dispose();
    editor.dispose();
    editor = null;
  }
}
