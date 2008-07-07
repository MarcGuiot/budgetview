package org.designup.picsou.functests.checkers;

import org.uispec4j.Window;
import org.uispec4j.interception.WindowInterceptor;
import org.designup.picsou.model.MasterCategory;

public class SeriesCreationDialogChecker extends DataChecker {
  private Window dialog;

  public SeriesCreationDialogChecker(Window dialog) {
    this.dialog = dialog;
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
  }

  public void validate() {
    dialog.getButton("OK").click();
  }
}
