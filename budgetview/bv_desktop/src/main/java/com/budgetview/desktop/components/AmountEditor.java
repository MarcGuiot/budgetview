package com.budgetview.desktop.components;

import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.editors.GlobNumericEditor;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class AmountEditor implements Disposable {
  private NumericEditor numericEditor;
  private boolean positiveMode = true;
  private JToggleButton signToggle = new JToggleButton(new ToggleAction()); // Selected = Plus
  private boolean updateInProgress = false;
  private boolean preferredPositive;
  private JPanel panel;
  private GlobRepository repository;
  private Directory directory;

  public AmountEditor(DoubleField field, GlobRepository repository, Directory directory,
                      boolean notifyOnKeyPressed, Double valueForNull) {
    this.repository = repository;
    this.directory = directory;
    numericEditor = new NumericEditor(field, repository, directory);
    numericEditor
      .setPositiveNumbersOnly(true)
      .setValueForNull(valueForNull)
      .setNotifyOnKeyPressed(notifyOnKeyPressed);
  }

  public JPanel getPanel() {
    if (panel == null){
      createPanel(repository, directory);
    }
    return panel;
  }

  private void createPanel(GlobRepository repository, Directory directory) {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(AmountEditor.class,
                                                      "/layout/general/amountEditor.splits",
                                                      repository, directory);

    builder.add("signToggle", signToggle);
    builder.add("amountEditionField", numericEditor);
    signToggle.setSelected(false);
    positiveMode = false;

    panel = builder.load();
  }

  public AmountEditor update(boolean preferredPositive, boolean hideRadio) {
    try {
      updateInProgress = true;
      this.preferredPositive = preferredPositive;
      update(numericEditor.getConvertedDisplayedValue());
      signToggle.setVisible(!hideRadio);
    }
    finally {
      updateInProgress = false;
    }
    return this;
  }

  public Double adjustSign(Double value) {
    return positiveMode ? value : -value;
  }

  private void updateToggle(boolean positive) {
    this.positiveMode = positive;
    if (positive != signToggle.isSelected()) {
      this.signToggle.doClick(0);
    }
  }

  public void selectAll() {
    JTextField textField = numericEditor.getComponent();
    if (textField.isVisible()) {
      GuiUtils.selectAndRequestFocus(textField);
      textField.requestFocusInWindow();
      textField.selectAll();
    }
  }

  public GlobNumericEditor getNumericEditor() {
    return numericEditor;
  }

  public Double getValue() {
    return numericEditor.getConvertedDisplayedValue();
  }

  public AmountEditor forceSelection(Key key) {
    numericEditor.forceSelection(key);
    return this;
  }

  public AmountEditor addAction(Action action) {
    numericEditor.setValidationAction(action);
    return this;
  }

  public void setEnabled(boolean enabled) {
    numericEditor.setEditable(enabled);
    signToggle.setEnabled(enabled);
  }

  public void setNegativeAmounts() {
    if (signToggle.isSelected()) {
      signToggle.doClick();
    }
  }

  public void dispose() {
    numericEditor.dispose();
  }

  private class NumericEditor extends GlobNumericEditor {
    private NumericEditor(Field field, GlobRepository repository, Directory directory) {
      super(field, repository, directory);
    }

    protected void registerActionListener() {
      super.registerActionListener();
      final JTextField textField = numericEditor.getComponent();
      textField.addPropertyChangeListener("enabled", new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
          signToggle.setEnabled(textField.isEnabled());
        }
      });
    }

    protected Double getConvertedDisplayedValue() {
      Double value = (Double)super.getConvertedDisplayedValue();
      if (value == null) {
        return null;
      }
      return adjustSign(value);
    }

    protected void setDisplayedValue(Object value) {
      super.setDisplayedValue(value);
      update((Double)value);
    }
  }

  private void update(Double amount) {
    if (amount != null) {
      boolean positive = (amount > 0) || ((amount == 0) && preferredPositive);
      updateInProgress = true;
      try {
        updateToggle(positive);
      }
      finally {
        updateInProgress = false;
      }
    }
  }

  private class ToggleAction extends AbstractAction {
    public void actionPerformed(ActionEvent e) {
      positiveMode = signToggle.isSelected();
      if (!updateInProgress) {
        numericEditor.apply();
      }
    }
  }
}
