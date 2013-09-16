package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.designup.picsou.functests.checkers.components.AmountEditorChecker;
import org.designup.picsou.functests.checkers.components.MonthSliderChecker;
import org.globsframework.utils.TablePrinter;
import org.uispec4j.Panel;
import org.uispec4j.Table;
import org.uispec4j.TextBox;
import org.uispec4j.UIComponent;

import javax.swing.*;

import static org.uispec4j.assertion.UISpecAssert.*;

public abstract class ProjectItemEditionChecker<T extends ProjectItemEditionChecker> extends GuiChecker {
  protected Panel panel;
  protected Panel enclosingPanel;

  public ProjectItemEditionChecker(Panel enclosingPanel) {
    this.enclosingPanel = enclosingPanel;
    this.panel = enclosingPanel.getPanel("projectItemEditionPanel");
  }

  public T setLabel(String name) {
    TextBox textBox = panel.getInputTextBox("nameField");
    textBox.setText(name, false);
    textBox.focusLost();
    return (T)this;
  }

  public T checkLabel(String label) {
    assertThat(panel.getInputTextBox("nameField").textEquals(label));
    return (T)this;
  }

  public T setMonth(int monthId) {
    MonthSliderChecker.init(panel, "monthEditor").setMonth(monthId);
    return (T)this;
  }

  public T checkMonth(String text) {
    MonthSliderChecker.init(panel, "monthEditor").checkText(text);
    return (T)this;
  }

  public T switchToSeveralMonths() {
    panel.getButton("switchToSeveralMonths").click();
    return (T)this;
  }

  public T setMonthCount(int numberOfMonths) {
    TextBox textBox = panel.getInputTextBox("monthCountEditor");
    textBox.setText(Integer.toString(numberOfMonths), false);
    textBox.focusLost();
    return (T)this;
  }

  public T enterMonthCount(String chars) {
    TextBox textBox = panel.getInputTextBox("monthCountEditor");
    for (char c : chars.toCharArray()) {
      textBox.setText(Character.toString(c), false);
    }
    return (T)this;
  }

  public T checkMonthCount(int numberOfMonths) {
    assertThat(panel.getInputTextBox("monthCountEditor").textEquals(Integer.toString(numberOfMonths)));
    return (T)this;
  }

  public T validateMonthCount() {
    panel.getInputTextBox("monthCountEditor").focusLost();
    return (T)this;
  }

  public T switchToMonthEditor() {
    panel.getButton("switchToMonthEditor").click();
    return (T)this;
  }

  public T checkShowsMonthEditor() {
    checkComponentVisible(panel, JPanel.class, "monthEditorPanel", true);
    return (T)this;
  }

  public T revertToSingleAmount() {
    panel.getButton("revertToSingleAmount").click();
    return (T)this;
  }

  public T checkShowsSingleMonth() {
    checkComponentVisible(panel, JPanel.class, "singleMonthPanel", true);
    return (T)this;
  }

  public T checkShowsSingleAmount() {
    checkComponentVisible(panel, JPanel.class, "singleAmountPanel", true);
    return (T)this;
  }

  public T selectRow(int row) {
    Table table = panel.getTable("monthAmountsTable");
    table.selectRow(row);
    return (T)this;
  }

  public T selectRows(int... rows) {
    Table table = panel.getTable("monthAmountsTable");
    table.selectRows(rows);
    return (T)this;
  }

  public T checkMonthAmount(double amount) {
    AmountEditorChecker.init(panel, "monthAmountEditor").checkAmount(amount);
    return (T)this;
  }

  public T setMonthAmount(Integer row, double amount) {
    selectRow(row);
    setMonthAmount(amount);
    return (T)this;
  }

  public abstract T setMonthAmount(double amount);

  public T checkMonthAmounts(String expected) {
    Table table = panel.getTable("monthAmountsTable");
    TablePrinter printer = new TablePrinter();
    for (int row = 0; row < table.getRowCount(); row++) {
      String month = table.getContentAt(row, 0).toString();
      String amount = table.getContentAt(row, 1).toString();
      printer.addRow(month, amount);
    }
    Assert.assertEquals(expected.trim(), printer.toString().trim());
    return (T)this;
  }

  public T setTableMonthCount(int count) {
    panel.getInputTextBox("tableMonthCountEditor").setText(Integer.toString(count));
    return (T)this;
  }

  public T checkTableMonthCount(int count) {
    assertThat(panel.getInputTextBox("tableMonthCountEditor").textEquals(Integer.toString(count)));
    return (T)this;
  }

  public void validate() {
    panel.getButton("validate").click();
    checkNoTipVisible(enclosingPanel);
    assertFalse(enclosingPanel.containsSwingComponent(JPanel.class, "projectItemEditionPanel"));
  }

  public T validateAndCheckNameTip(String message) {
    doValidateAndCheckError(message, panel.getInputTextBox("nameField"));
    return (T)this;
  }

  public T validateAndCheckMonthCountTip(String message) {
    doValidateAndCheckError(message, panel.getInputTextBox("monthCountEditor"));
    return (T)this;
  }

  protected void doValidateAndCheckError(String message, UIComponent component) {
    panel.getButton("validate").click();
    assertTrue(enclosingPanel.containsSwingComponent(JPanel.class, "projectItemEditionPanel"));
    checkTipVisible(enclosingPanel, component, message);
  }

  public T checkNoTipShown() {
    checkNoTipVisible(enclosingPanel);
    return (T)this;
  }

  public void cancel() {
    panel.getButton("cancel").click();
  }
}
