package org.designup.picsou.functests.checkers;

import org.designup.picsou.model.MasterCategory;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.Table;
import org.uispec4j.assertion.UISpecAssert;
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

  public SeriesEditionDialogChecker checkMonthSelected(String month) {
    int index = table.getRowIndex(0, month);
    assertThat(table.rowIsSelected(index));
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
