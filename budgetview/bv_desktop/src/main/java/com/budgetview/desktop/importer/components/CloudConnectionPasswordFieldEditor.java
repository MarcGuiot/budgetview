package com.budgetview.desktop.importer.components;

import com.budgetview.budgea.model.BudgeaConnectionValue;
import org.globsframework.gui.editors.GlobPasswordEditor;
import org.globsframework.gui.editors.GlobTextEditor;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class CloudConnectionPasswordFieldEditor extends CloudConnectionFieldEditor {
  private GlobPasswordEditor editor;

  public CloudConnectionPasswordFieldEditor(Glob budgeaField, Glob budgeaConnectionValue, GlobRepository repository, Directory directory) {
    super(budgeaField, budgeaConnectionValue, repository, directory);
    editor = GlobPasswordEditor.init(BudgeaConnectionValue.VALUE, repository, directory)
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
