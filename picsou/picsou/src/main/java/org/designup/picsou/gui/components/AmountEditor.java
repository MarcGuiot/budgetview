package org.designup.picsou.gui.components;

import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.editors.GlobNumericEditor;
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

public class AmountEditor {
  private NumericEditor numericEditor;
  private boolean positiveMode = true;
  private JToggleButton positiveToggle = new JToggleButton(new RadioAction(Lang.get("amount.positive"), true));
  private JToggleButton negativeToggle = new JToggleButton(new RadioAction(Lang.get("amount.negative"), false));
  private boolean updateInProgress = false;
  private boolean preferredPositive;
  private JPanel panel;

  public AmountEditor(DoubleField field, GlobRepository repository, Directory directory,
                      boolean notifyOnKeyPressed, Double valueForNull) {
    numericEditor = new NumericEditor(field, repository, directory);
    numericEditor
      .setAbsoluteValue(true)
      .setValueForNull(valueForNull)
      .setNotifyOnKeyPressed(notifyOnKeyPressed);

    ButtonGroup group = new ButtonGroup();
    group.add(positiveToggle);
    group.add(negativeToggle);

    positiveToggle.doClick(0);

    createPanel(repository, directory);
  }

  public JPanel getPanel() {
    return panel;
  }

  private void createPanel(GlobRepository repository, Directory directory) {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(AmountEditor.class,
                                                      "/layout/general/amountEditor.splits",
                                                      repository, directory);

    builder.add("positiveAmount", positiveToggle);
    builder.add("negativeAmount", negativeToggle);
    builder.add("amountEditor", numericEditor);

    panel = builder.load();
  }

  public AmountEditor update(boolean preferredPositive, boolean hideRadio) {
    try {
      updateInProgress = true;
      this.preferredPositive = preferredPositive;
      update(numericEditor.getConvertedDisplayedValue());
      positiveToggle.setVisible(!hideRadio);
      negativeToggle.setVisible(!hideRadio);
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
    if (positive == positiveToggle.isSelected()) {
      return;
    }
    this.positiveMode = positive;
    if (positive) {
      positiveToggle.doClick(0);
    }
    else {
      negativeToggle.doClick(0);
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

  public void addAction(Action action) {
    numericEditor.getComponent().addActionListener(action);
  }

  public void setEnabled(boolean enabled) {
    numericEditor.setEditable(enabled);
    positiveToggle.setEnabled(enabled);
    negativeToggle.setEnabled(enabled);
  }

  public void setPositiveAmounts() {
    positiveToggle.doClick();
  }

  public void setNegativeAmounts() {
    negativeToggle.doClick();
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
          positiveToggle.setEnabled(textField.isEnabled());
          negativeToggle.setEnabled(textField.isEnabled());
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
        updateRadios(positive);
      }
      finally {
        updateInProgress = false;
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
