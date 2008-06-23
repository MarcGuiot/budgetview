package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.uispec4j.Key;
import org.uispec4j.Mouse;
import org.uispec4j.Window;

import java.awt.*;

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
    Mouse.doClickInRectangle(dialog.getTextBox("label." + categoryName),
                             new Rectangle(5, 5), false, Key.Modifier.NONE);
  }

  public CategoryChooserChecker checkContains(String[] expectedCategories) {
    for (int i = 0; i < expectedCategories.length; i++) {
      Assert.assertNotNull(window.getTextBox(expectedCategories[i]));
    }
    return this;
  }
}
