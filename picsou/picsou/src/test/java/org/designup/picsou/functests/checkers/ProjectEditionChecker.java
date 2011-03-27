package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.uispec4j.*;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.interception.WindowInterceptor;

import static org.uispec4j.assertion.UISpecAssert.*;

public class ProjectEditionChecker extends GuiChecker {

  private Window dialog;

  public static ProjectEditionChecker open(Button button) {
    return open(button.triggerClick());
  }

  public static ProjectEditionChecker open(Trigger trigger) {
    return new ProjectEditionChecker(WindowInterceptor.getModalDialog(trigger));
  }

  public ProjectEditionChecker(Window dialog) {
    this.dialog = dialog;
  }

  public ProjectEditionChecker checkTitle(String text) {
    assertThat(dialog.getTextBox("title").textEquals(text));
    return this;
  }

  public ProjectEditionChecker setName(String name) {
    dialog.getTextBox("projectName").setText(name);
    return this;
  }

  public ProjectEditionChecker checkTotalAmount(double amount) {
    assertThat(dialog.getTextBox("totalAmount").textEquals(toString(amount)));
    return this;
  }

  public ProjectEditionChecker checkItems(String expected) {
    Assert.assertEquals(expected.trim(), getContent().trim());
    return this;
  }

  public ProjectEditionChecker setItemName(int index, String name) {
    getItemComponent(index, TextBox.class, "itemLabel").setText(name);
    return this;
  }

  public ProjectEditionChecker setItemDate(int index, int monthId) {
    MonthChooserChecker.selectMonth(getItemComponent(index, Button.class, "month").triggerClick(), monthId);
    return this;
  }

  public ProjectEditionChecker setItemAmount(int index, double amount) {
    if (amount > 0) {
      getItemComponent(index, ToggleButton.class, "positiveAmount").click();
    }
    else {
      getItemComponent(index, ToggleButton.class, "negativeAmount").click();
    }
    getItemComponent(index, TextBox.class, "amountEditor").setText(toString(Math.abs(amount)));
    return this;
  }

  public ProjectEditionChecker deleteLastValueChars(int index, int numberOfChars) {
    TextBox field = getItemComponent(index, TextBox.class, "amountEditor");
    for (int i = 0; i < numberOfChars; i++) {
      field.pressKey(Key.BACKSPACE);
    }
    return this;
  }

  public ProjectEditionChecker setItem(int index, String name, int monthId, double amount) {
    setItemName(index, name);
    setItemDate(index, monthId);
    setItemAmount(index, amount);
    return this;
  }

  public ProjectEditionChecker deleteItem(int index) {
    getItemComponent(index, Button.class, "delete").click();
    return this;
  }

  public ProjectEditionChecker addItem() {
    dialog.getButton("addItem").click();
    return this;
  }

  public ProjectEditionChecker addItem(int index, String label, int month, double amount) {
    addItem();
    setItemName(index, label);
    setItemDate(index, month);
    setItemAmount(index, amount);
    return this;
  }

  private <T extends UIComponent> T getItemComponent(int index, Class<T> uiClass, String uiName) {
    UIComponent[] components = dialog.getPanel("items").getUIComponents(uiClass, uiName);
    if (components.length == 0) {
      UISpecAssert.fail("No project item shown");
    }
    if (index >= components.length) {
      UISpecAssert.fail("Index " + index + " out of bounds. Actual content:\n" + getContent());
    }
    return (T)components[index];
  }

  private String getContent() {
    StringBuilder builder = new StringBuilder();
    Panel itemsPanel = dialog.getPanel("items");
    UIComponent[] labels = itemsPanel.getUIComponents(TextBox.class, "itemLabel");
    UIComponent[] months = itemsPanel.getUIComponents(Button.class, "month");
    UIComponent[] positiveToggles = itemsPanel.getUIComponents(ToggleButton.class, "positiveAmount");
    UIComponent[] negativeToggles = itemsPanel.getUIComponents(ToggleButton.class, "negativeAmount");
    UIComponent[] amounts = itemsPanel.getUIComponents(TextBox.class, "amountEditor");
    for (int i = 0; i < labels.length; i++) {
      if (i > 0) {
        builder.append("\n");
      }
      builder.append(((TextBox)labels[i]).getText());
      builder.append(" | ");
      builder.append(months[i].getLabel());
      builder.append(" | ");
      if (((ToggleButton)positiveToggles[i]).isSelected().isTrue()) {
        builder.append("+");
      }
      String amount = ((TextBox)amounts[i]).getText();
      if (((ToggleButton)negativeToggles[i]).isSelected().isTrue() && !amount.equals("0.00")) {
        builder.append("-");
      }
      builder.append(amount);
    }
    return builder.toString();
  }

  public ProjectEditionChecker checkProjectNameMessage(String expectedMessage) {
    checkErrorTipVisible(dialog, dialog.getTextBox("projectName"), expectedMessage);
    return this;
  }

  public ProjectEditionChecker checkProjectItemMessage(int index, String expectedMessage) {
    checkErrorTipVisible(dialog, getItemComponent(index, TextBox.class, "itemLabel"), expectedMessage);
    return this;
  }

  public ProjectEditionChecker checkNoErrorTipDisplayed() {
    super.checkNoErrorTip(dialog);
    return this;
  }

  public void cancel() {
    dialog.getButton("Cancel").click();
  }

  public ProjectEditionChecker validateAndCheckOpen() {
    dialog.getButton("OK").click();
    assertThat(dialog.isVisible());
    return this;
  }

  public void delete() {
    dialog.getButton("Delete").click();
    assertFalse(dialog.isVisible());
  }

  public void deleteWithConfirmation(String title, String message) {
    ConfirmationDialogChecker.init(dialog.getButton("Delete").triggerClick())
    .checkTitle(title)
    .checkMessageContains(message)
    .validate("Delete project");
    assertFalse(dialog.isVisible());
  }

  public void openDeleteAndNavigate() {
    ConfirmationDialogChecker.init(dialog.getButton("Delete").triggerClick())
    .clickOnHyperlink("see these operations")
    .checkHidden();
    assertFalse(dialog.isVisible());
  }

  public void validate() {
    dialog.getButton("OK").click();
    assertFalse(dialog.isVisible());
  }
}
