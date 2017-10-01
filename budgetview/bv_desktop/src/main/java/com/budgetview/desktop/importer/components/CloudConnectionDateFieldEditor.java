package com.budgetview.desktop.importer.components;

import com.budgetview.budgea.model.BudgeaConnectionValue;
import com.budgetview.model.CurrentMonth;
import com.budgetview.model.Month;
import com.budgetview.utils.Lang;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

import static org.globsframework.model.FieldValue.value;

public class CloudConnectionDateFieldEditor extends CloudConnectionFieldEditor {
  private JPanel panel = new JPanel();
  private JComboBox<Integer> dayCombo = new JComboBox<Integer>();
  private JComboBox<Integer> monthCombo = new JComboBox<Integer>();
  private JComboBox<Integer> yearCombo = new JComboBox<Integer>();

  public CloudConnectionDateFieldEditor(Glob budgeaField, Glob budgeaConnectionValue, GlobRepository repository, Directory directory) {
    super(budgeaField, budgeaConnectionValue, repository, directory);

    initCombo(dayCombo, 1, 31, "dayCombo");
    initCombo(monthCombo, 1, 12, "monthCombo");
    initCombo(yearCombo, Month.toYear(CurrentMonth.getCurrentMonth(repository)), 1900, "yearCombo");

    monthCombo.setRenderer(new DefaultListCellRenderer() {
      public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        if (value == null) {
          this.setText("");
        }
        else {
          this.setText(Lang.get("month." + value + ".long"));
        }
        return this;
      }
    });

    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    panel.add(dayCombo);
    panel.add(new JLabel("/"));
    panel.add(monthCombo);
    panel.add(new JLabel("/"));
    panel.add(yearCombo);
    panel.setOpaque(false);
  }

  public void initCombo(JComboBox combo, int first, int last, String name) {
    combo.setModel(new DefaultComboBoxModel<Integer>(getValues(first, last)));
    combo.addActionListener(new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        updateGlob();
      }
    });
    combo.setName(name);
  }

  private void updateGlob() {
    repository.update(budgeaFieldValue, value(BudgeaConnectionValue.VALUE, getCurrentValue()));
  }

  private String getCurrentValue() {
    Integer day = (Integer) dayCombo.getSelectedItem();
    Integer month = (Integer) monthCombo.getSelectedItem();
    Integer year = (Integer) yearCombo.getSelectedItem();
    if (day == null || month == null || year == null) {
      return null;
    }
    StringBuilder date = new StringBuilder();
    if (day < 10) {
      date.append("0");
    }
    date.append(day).append("/");
    if (month < 10) {
      date.append("0");
    }
    date.append(month).append("/");
    date.append(year);
    return date.toString();
  }

  private Integer[] getValues(int start, int end) {
    Integer[] result = new Integer[Math.abs(end - start) + 2];
    if (start < end) {
      for (int i = 1; i < result.length; i++) {
        result[i] = start + i - 1;
      }
    }
    else {
      for (int i = 1; i < result.length; i++) {
        result[i] = start - i + 1;
      }
    }
    return result;
  }

  public JPanel getEditor() {
    return panel;
  }

  public void dispose() {
    super.dispose();
    panel = null;
  }
}
