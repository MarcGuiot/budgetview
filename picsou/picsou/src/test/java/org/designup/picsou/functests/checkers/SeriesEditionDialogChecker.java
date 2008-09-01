package org.designup.picsou.functests.checkers;

import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.Month;
import org.uispec4j.*;
import org.uispec4j.Window;
import static org.uispec4j.assertion.UISpecAssert.assertThat;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.finder.ComponentMatchers;
import org.uispec4j.interception.WindowInterceptor;

import javax.swing.*;
import java.awt.*;

public class SeriesEditionDialogChecker extends DataChecker {
  private Window dialog;
  private Table table;

  public SeriesEditionDialogChecker(Window dialog) {
    this.dialog = dialog;
    this.table = dialog.getTable();
  }

  public SeriesEditionDialogChecker checkName(String seriesName) {
    assertThat(dialog.getInputTextBox("nameField").textEquals(seriesName));
    return this;
  }

  public SeriesEditionDialogChecker setName(String seriesName) {
    dialog.getInputTextBox("nameField").setText(seriesName);
    return this;
  }

  public void checkType(String expectedType) {
    // TODO
  }

  public SeriesEditionDialogChecker checkAmount(String displayedValue) {
    assertThat(dialog.getInputTextBox("amountEditor").textEquals(displayedValue));
    return this;
  }

  public SeriesEditionDialogChecker setAmount(String value) {
    dialog.getInputTextBox("amountEditor").setText(value);
    return this;
  }

  public SeriesEditionDialogChecker setCategory(MasterCategory category) {
    Window chooser = WindowInterceptor.getModalDialog(dialog.getButton("Select").triggerClick());
    CategoryChooserChecker categoryChooser = new CategoryChooserChecker(chooser);
    categoryChooser.selectCategory(getCategoryName(category));
    assertThat(dialog.getTextBox("singleCategoryLabel").textEquals(getCategoryName(category)));
    return this;
  }

  public SeriesEditionDialogChecker selectAllMonths() {
    table.selectRowSpan(0, table.getRowCount() - 1);
    return this;
  }

  public SeriesEditionDialogChecker checkTable(Object[][] content) {
    assertThat(table.contentEquals(content));
    return this;
  }

  public SeriesEditionDialogChecker checkMonthSelected(int index) {
    assertThat(table.rowIsSelected(index));
    return this;
  }

  public SeriesEditionDialogChecker checkMonthsSelected(int... rows) {
    assertThat(table.rowsAreSelected(rows));
    return this;
  }

  public SeriesEditionDialogChecker toggleMonth(String monthLabel) {
    try {
      dialog.getCheckBox(ComponentMatchers.componentLabelFor(monthLabel)).click();
    }
    catch (ItemNotFoundException e) {
      throw new RuntimeException("No component found for: " + monthLabel, e);
    }
    return this;
  }

  public void validate() {
    dialog.getButton("OK").click();
  }

  public Trigger triggerValidate() {
    return dialog.getButton("OK").triggerClick();
  }

  public void cancel() {
    dialog.getButton("Cancel").click();
  }

  public SeriesEditionDialogChecker checkNoSeries() {
    assertThat(dialog.getListBox().isEmpty());
    return this;
  }

  public SeriesEditionDialogChecker checkSeriesList(String... names) {
    assertThat(dialog.getListBox().contentEquals(names));
    return this;
  }

  public SeriesEditionDialogChecker selectSeries(String name) {
    dialog.getListBox().select(name);
    return this;
  }

  public SeriesEditionDialogChecker checkSeriesSelected(String name) {
    dialog.getListBox().select(name);
    return this;
  }

  public SeriesEditionDialogChecker createSeries() {
    dialog.getButton("create").click();
    return this;
  }

  public SeriesEditionDialogChecker deleteSeries() {
    dialog.getButton("delete").click();
    return this;
  }

  public SeriesEditionDialogChecker setStartDate(int monthId) {
    setDate("Start date", monthId);
    return this;
  }

  public SeriesEditionDialogChecker checkNoStartDate() {
    checkDate("Start date", "");
    return this;
  }

  public SeriesEditionDialogChecker checkStartDate(int monthId) {
    checkDate("Start date", monthId);
    return this;
  }

  public SeriesEditionDialogChecker setEndDate(int monthId) {
    setDate("End date", monthId);
    return this;
  }

  public SeriesEditionDialogChecker checkNoEndDate() {
    checkDate("End date", "");
    return this;
  }

  public SeriesEditionDialogChecker checkEndDate(int monthId) {
    checkDate("End date", monthId);
    return this;
  }

  private void setDate(String labelName, int monthId) {
    String text = Month.toMonth(monthId) + "/" + Month.toYear(monthId);
    dialog.getTextBox(ComponentMatchers.componentLabelFor(labelName)).setText(text);
  }

  private void checkDate(String labelName, int monthId) {
    String text = Month.toMonth(monthId) + "/" + Month.toYear(monthId);
    checkDate(labelName, text);
  }

  private void checkDate(String labelName, String text) {
    assertThat(dialog.getTextBox(ComponentMatchers.componentLabelFor(labelName)).textEquals(text));
  }

  public SeriesEditionDialogChecker checkAllMonthsDisabled() {
    for (UIComponent checkBox : dialog.getUIComponents(CheckBox.class)) {
      UISpecAssert.assertFalse(checkBox.isEnabled());
    }
    return this;
  }

  public SeriesEditionDialogChecker checkAllFieldsDisabled() {
    for (Component component : dialog.getSwingComponents(JTextField.class)) {
      TextBox textBox = new TextBox((JTextField)component);
      UISpecAssert.assertFalse(textBox.isEnabled());
    }
    return this;
  }
}
