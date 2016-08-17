package com.budgetview.desktop.importer.components;

import com.budgetview.budgea.model.BudgeaBankFieldValue;
import com.budgetview.budgea.model.BudgeaConnectionValue;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;

public class CloudConnectionListFieldEditor extends CloudConnectionFieldEditor {
  private JComboBox comboBox;

  public CloudConnectionListFieldEditor(Glob budgeaField, Glob budgeaConnectionValue, GlobRepository repository, Directory directory) {
    super(budgeaField, budgeaConnectionValue, repository, directory);

    Glob[] model = createModel(budgeaField, repository);
    comboBox = new JComboBox(model);
    comboBox.setRenderer(new DefaultListCellRenderer() {
      public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        if (value == null) {
          this.setText("");
        }
        else {
          this.setText(((Glob) value).get(BudgeaBankFieldValue.LABEL));
        }
        return this;
      }
    });
    comboBox.setAction(new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        Glob connectionValue = (Glob)comboBox.getSelectedItem();
        String value = connectionValue != null ? connectionValue.get(BudgeaBankFieldValue.VALUE) : null;
        repository.update(budgeaConnectionValue, BudgeaConnectionValue.VALUE, value);
      }
    });
    comboBox.setSelectedIndex(0);
  }

  public Glob[] createModel(Glob budgeaField, GlobRepository repository) {
    Glob[] values = repository.findLinkedTo(budgeaField, BudgeaBankFieldValue.FIELD).toArray();
    Glob[] model = new Glob[values.length + 1];
    model[0] = null;
    System.arraycopy(values, 0, model, 1, values.length);
    return model;
  }

  public JComboBox getEditor() {
    return comboBox;
  }
}
