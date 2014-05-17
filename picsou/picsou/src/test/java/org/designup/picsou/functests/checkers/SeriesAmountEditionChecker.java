package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import org.designup.picsou.functests.checkers.components.AmountEditorChecker;
import org.uispec4j.ItemNotFoundException;
import org.uispec4j.TextBox;
import org.uispec4j.Window;
import static org.uispec4j.assertion.UISpecAssert.*;

import javax.swing.*;

public class SeriesAmountEditionChecker<T extends SeriesAmountEditionChecker> extends GuiChecker {
  protected final Window dialog;
  private AmountEditorChecker amountEditor;

  SeriesAmountEditionChecker(Window dialog) {
    this.dialog = dialog;
  }

  public T checkAmount(double value) {
    return checkAmount(toString(value));
  }

  public T checkAmount(String displayedValue) {
    assertThat(getAmountTextBox().textEquals(displayedValue));
    return (T)this;
  }

  public T checkAmountIsEmpty() {
    assertThat(getAmountTextBox().textIsEmpty());
    return (T)this;
  }

  public T checkAmountIsSelected() {
    JTextField textEditor = (JTextField)dialog.getInputTextBox("amountEditionField").getAwtComponent();
    Assert.assertEquals(textEditor.getText(), textEditor.getSelectedText());
    return (T)this;
  }

  public TextBox getAmountTextBox() {
    try {
      return dialog.getInputTextBox("amountEditionField");
    }
    catch (ItemNotFoundException e) {
      throw new AssertionFailedError("Amount editor not displayed");
    }
  }

  public T setAmount(double value) {
    if (value < 0) {
      Assert.fail("Use a poitive amount and call selectNegativeAmounts()");
    }
    return setAmount(Double.toString(value));
  }

  public T checkAmountEditionEnabled() {
    checkComponentVisible(dialog, JEditorPane.class, "disabledMessage", false);
    checkComponentVisible(dialog, JTextField.class, "amountEditionField", true);
    return (T)this;
  }

  public T checkAmountEditionDisabled(String message) {
    checkComponentVisible(dialog, JTextField.class, "amountEditionField", false);
    assertThat(dialog.getTextBox("disabledMessage").textEquals(message));
    return (T)this;
  }

  public T setAmount(String value) {
    getAmountTextBox().setText(value, false);
    return (T)this;
  }

  public void setAmountAndValidate(String value) {
    getAmountTextBox().setText(value);
    assertFalse(dialog.isVisible());
  }

  public T selectPositiveAmounts() {
    AmountEditorChecker.init(dialog, "amountEditor").selectPlus();
    return (T)this;
  }

  public T checkPositiveAmountsSelected() {
    AmountEditorChecker.init(dialog, "amountEditor").checkPlusSelected();
    return (T)this;
  }

  public T selectNegativeAmounts() {
    AmountEditorChecker.init(dialog, "amountEditor").selectMinus();
    return (T)this;
  }

  public T checkNegativeAmountsSelected() {
    AmountEditorChecker.init(dialog, "amountEditor").checkMinusSelected();
    return (T)this;
  }

  public T checkAmountTogglesAreNotVisible() {
    AmountEditorChecker.init(dialog, "amountEditor").checkSignSelectorHidden();
    return (T)this;
  }

  public T checkAlignPlannedAndActualEnabled() {
    assertTrue(dialog.getButton("alignValue").isEnabled());
    return (T)this;
  }

  public T checkAlignPlannedAndActualDisabled() {
    assertFalse(dialog.getButton("alignValue").isEnabled());
    return (T)this;
  }

  public T checkActualAmount(String text) {
    assertThat(dialog.getTextBox("actualAmountLabel").textEquals(text));
    return (T)this;
  }

  public T alignPlannedAndActual() {
    dialog.getButton("alignValue").click();
    return (T)this;
  }

  public T checkPropagationEnabled() {
    assertTrue(dialog.getCheckBox("propagate").isSelected());
    return (T)this;
  }

  public T checkPropagationDisabled() {
    assertFalse(dialog.getCheckBox("propagate").isSelected());
    return (T)this;
  }

  public T setPropagationEnabled() {
    dialog.getCheckBox("propagate").select();
    return (T)this;
  }

  public T setPropagationDisabled() {
    dialog.getCheckBox("propagate").unselect();
    return (T)this;
  }
}
