package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import org.uispec4j.ItemNotFoundException;
import org.uispec4j.TextBox;
import org.uispec4j.Window;
import static org.uispec4j.assertion.UISpecAssert.*;

import javax.swing.*;

public class SeriesAmountEditionChecker<T extends SeriesAmountEditionChecker> extends GuiChecker {
  protected final Window dialog;

  public SeriesAmountEditionChecker(Window dialog) {
    this.dialog = dialog;
  }

  public T checkAmount(String displayedValue) {
    assertThat(getAmountTextBox().textEquals(displayedValue));
    return (T)this;
  }

  public T checkAmountIsEmpty() {
    assertThat(getAmountTextBox().textIsEmpty());
    return (T)this;
  }

  public T checkAmountIsDisabled() {
    assertFalse(getAmountTextBox().isEnabled());
    assertFalse(dialog.getRadioButton("positiveAmounts").isEnabled());
    assertFalse(dialog.getRadioButton("negativeAmounts").isEnabled());
    return (T)this;
  }

  public T checkAmountIsSelected() {
    JTextField textEditor = (JTextField)dialog.getInputTextBox("amountEditor").getAwtComponent();
    Assert.assertEquals(textEditor.getText(), textEditor.getSelectedText());
    return (T)this;
  }

  public TextBox getAmountTextBox() {
    try {
      return dialog.getInputTextBox("amountEditor");
    }
    catch (ItemNotFoundException e) {
      throw new AssertionFailedError("Amount editor not displayed");
    }
  }

  public T setAmount(double value) {
    return setAmount(Double.toString(value));
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
    dialog.getRadioButton("positiveAmounts").click();
    return (T)this;
  }

  public T checkPositiveAmountsSelected() {
    assertThat(dialog.getRadioButton("positiveAmounts").isSelected());
    return (T)this;
  }

  public T selectNegativeAmounts() {
    dialog.getRadioButton("negativeAmounts").click();
    return (T)this;
  }

  public T checkNegativeAmountsSelected() {
    assertThat(dialog.getRadioButton("negativeAmounts").isSelected());
    return (T)this;
  }

  public T checkAmountsRadioAreNotVisible() {
    assertFalse(dialog.getRadioButton("negativeAmounts").isVisible());
    assertFalse(dialog.getRadioButton("positiveAmounts").isVisible());
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
}
