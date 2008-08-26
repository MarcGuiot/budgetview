package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.uispec4j.Window;

public class CategoryChooserChecker {
  private Window window;

  public CategoryChooserChecker(Window window) {
    this.window = window;
  }

  public CategoryChooserChecker selectCategory(String categoryName) {
    selectCategory(window, categoryName);
    return this;
  }

  public static void selectCategory(Window dialog, String categoryName) {
    dialog.getToggleButton(categoryName).click();
    dialog.getButton("ok").click();
  }

  public CategoryChooserChecker checkContains(String[] expectedCategories) {
    for (int i = 0; i < expectedCategories.length; i++) {
      Assert.assertNotNull(window.getTextBox(expectedCategories[i]));
    }
    return this;
  }
}
