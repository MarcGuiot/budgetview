package org.designup.picsou.functests.checkers;

import org.designup.picsou.model.MasterCategory;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.interception.WindowInterceptor;

public class SeriesEditionDialogChecker extends DataChecker {
  private Window dialog;

  public SeriesEditionDialogChecker(Window dialog) {
    this.dialog = dialog;
  }

  public void checkName(String seriesName) {
    UISpecAssert.assertThat(dialog.getInputTextBox("nameField").textEquals(seriesName));
  }

  public void setName(String seriesName) {
    dialog.getInputTextBox("nameField").setText(seriesName);
  }

  public void checkType(String expectedType) {
  }

  public void setCategory(MasterCategory category) {
    Window chooser = WindowInterceptor.getModalDialog(dialog.getButton("Select").triggerClick());
    CategoryChooserChecker categoryChooser = new CategoryChooserChecker(chooser);
    categoryChooser.selectCategory(getCategoryName(category));
    UISpecAssert.assertThat(dialog.getTextBox("singleCategoryLabel").textEquals(getCategoryName(category)));
  }

  public void validate() {
    dialog.getButton("OK").click();
  }

  public Trigger doValidate() {
    return dialog.getButton("OK").triggerClick();
  }

  public void cancel() {
    dialog.getButton("Cancel").click();
  }
}
