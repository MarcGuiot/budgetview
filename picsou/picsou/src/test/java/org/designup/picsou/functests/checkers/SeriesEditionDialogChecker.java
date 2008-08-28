package org.designup.picsou.functests.checkers;

import org.designup.picsou.model.MasterCategory;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.Table;
import static org.uispec4j.assertion.UISpecAssert.assertThat;
import org.uispec4j.interception.WindowInterceptor;

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

  public void validate() {
    dialog.getButton("OK").click();
  }

  public Trigger triggerValidate() {
    return dialog.getButton("OK").triggerClick();
  }

  public void cancel() {
    dialog.getButton("Cancel").click();
  }
}
