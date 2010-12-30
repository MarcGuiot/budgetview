package org.designup.picsou.gui.components;

import org.designup.picsou.utils.Lang;
import org.globsframework.gui.editors.GlobNumericEditor;
import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class AmountEditor {
  private NumericEditor numericEditor;
  private boolean positiveMode = true;
  private JRadioButton positiveRadio = new JRadioButton(new RadioAction(Lang.get("amount.positive"), true));
  private JRadioButton negativeRadio = new JRadioButton(new RadioAction(Lang.get("amount.negative"), false));
  private boolean updateInProgress = false;
  private boolean preferredPositive;

  public AmountEditor(DoubleField field, GlobRepository repository, Directory directory,
                      boolean notifyOnKeyPressed, Double valueForNull) {
    numericEditor = new NumericEditor(field, repository, directory);
    numericEditor
      .setAbsoluteValue(true)
      .setValueForNull(valueForNull)
      .setNotifyOnKeyPressed(notifyOnKeyPressed);

    ButtonGroup group = new ButtonGroup();
    group.add(positiveRadio);
    group.add(negativeRadio);

    positiveRadio.doClick(0);
  }

  public AmountEditor update(boolean preferredPositive, boolean hideRadio) {
    try {
      updateInProgress = true;
      this.preferredPositive = preferredPositive;
      updateRadios(preferredPositive);
      positiveRadio.setVisible(!hideRadio);
      negativeRadio.setVisible(!hideRadio);
    }
    finally {
      updateInProgress = false;
    }
    return this;
  }

  public Double adjustSign(Double value) {
    return positiveMode ? value : -value;
  }

  private void updateRadios(boolean positive) {
    if (positive == positiveRadio.isSelected()) {
      return;
    }
    this.positiveMode = positive;
    if (positive) {
      positiveRadio.doClick(0);
    }
    else {
      negativeRadio.doClick(0);
    }
  }

  public void selectAll() {
    JTextField textField = numericEditor.getComponent();
    if (textField.isVisible()) {
      textField.requestFocusInWindow();
      textField.selectAll();
    }
  }

  public JRadioButton getPositiveRadio() {
    return positiveRadio;
  }

  public JRadioButton getNegativeRadio() {
    return negativeRadio;
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

  public void addAction(Action action) {
    numericEditor.getComponent().addActionListener(action);
  }

  public void setEnabled(boolean enabled) {
    numericEditor.setEditable(enabled);
    positiveRadio.setEnabled(enabled);
    negativeRadio.setEnabled(enabled);
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
          positiveRadio.setEnabled(textField.isEnabled());
          negativeRadio.setEnabled(textField.isEnabled());
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
      if (value != null) {
        double amount = (Double)value;
        boolean positive = (amount > 0) || ((amount == 0) && preferredPositive);
        updateInProgress = true;
        try {
          updateRadios(positive);
        }
        finally {
          updateInProgress = false;
        }
      }
    }
  }

  private class RadioAction extends AbstractAction {

    private boolean positive;

    private RadioAction(String name, boolean positive) {
      super(name);
      this.positive = positive;
    }

    public void actionPerformed(ActionEvent e) {
      positiveMode = positive;
      if (!updateInProgress) {
        numericEditor.apply();
      }
    }
  }
}
