package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.designup.picsou.model.MasterCategory;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;

public class CategoryChooserChecker extends DataChecker {
  private Window window;

  public CategoryChooserChecker(Window window) {
    this.window = window;
  }

  public CategoryChooserChecker selectCategory(String categoryName, boolean oneSelection) {
    selectCategory(window, categoryName, oneSelection);
    return this;
  }

  public CategoryChooserChecker selectCategory(String categoryName) {
    window.getToggleButton(categoryName).click();
    return this;
  }

  public void validate() {
    window.getButton("ok").click();
    UISpecAssert.assertFalse(window.isVisible());
  }

  public static void selectCategory(Window dialog, String categoryName, boolean oneSelection) {
    dialog.getToggleButton(categoryName).click();
    if (!oneSelection) {
      dialog.getButton("ok").click();
    }
    UISpecAssert.assertFalse(dialog.isVisible());
  }

  public CategoryChooserChecker checkContains(String[] expectedCategories) {
    for (int i = 0; i < expectedCategories.length; i++) {
      Assert.assertNotNull(window.getTextBox(expectedCategories[i]));
    }
    return this;
  }

  public CategoryChooserChecker checkSelected(MasterCategory... category) {
    for (MasterCategory masterCategory : category) {
      UISpecAssert.assertThat(getCategoryName(masterCategory) + " not selected.", window.getToggleButton(getCategoryName(masterCategory)).isSelected());
    }
    return this;
  }

  public CategoryChooserChecker checkUnSelected(MasterCategory... category) {
    for (MasterCategory masterCategory : category) {
      UISpecAssert.assertFalse(getCategoryName(masterCategory) + " not unselected.", window.getToggleButton(getCategoryName(masterCategory)).isSelected());
    }
    return this;
  }


  public void cancel() {
    window.getButton("cancel").click();
  }
}
