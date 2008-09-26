package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.designup.picsou.model.MasterCategory;
import org.uispec4j.Window;
import static org.uispec4j.finder.ComponentMatchers.and;
import static org.uispec4j.finder.ComponentMatchers.fromClass;
import static org.uispec4j.finder.ComponentMatchers.displayedNameIdentity;
import static org.uispec4j.assertion.UISpecAssert.assertFalse;
import static org.uispec4j.assertion.UISpecAssert.assertThat;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.interception.WindowInterceptor;

import javax.swing.*;

public class CategoryChooserChecker extends DataChecker {
  private Window window;

  public CategoryChooserChecker(Window window) {
    this.window = window;
  }

  public CategoryChooserChecker checkTitle(String title) {
    assertThat(window.getTextBox("title").textEquals(title));
    return this;
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
    assertFalse(window.isVisible());
  }

  public static void selectCategory(Window dialog, String categoryName, boolean oneSelection) {
    dialog.getToggleButton(categoryName).click();
    if (!oneSelection) {
      dialog.getButton("ok").click();
    }
    assertFalse(dialog.isVisible());
  }

  public CategoryChooserChecker checkContains(String... expectedCategories) {
    for (String expectedCategory : expectedCategories) {
      Assert.assertNotNull(window.getToggleButton(expectedCategory));
    }
    return this;
  }


  public void checkNotFound(MasterCategory... masters) {
    for (MasterCategory master : masters) {
      UISpecAssert.assertFalse(
        window.containsComponent(and(fromClass(JToggleButton.class),
                                     displayedNameIdentity(getCategoryName(master)))
        )
      );
    }
  }

  public CategoryChooserChecker checkSelected(MasterCategory... category) {
    for (MasterCategory masterCategory : category) {
      assertThat(getCategoryName(masterCategory) + " not selected.", window.getToggleButton(getCategoryName(masterCategory)).isSelected());
    }
    return this;
  }

  public CategoryChooserChecker checkUnselected(MasterCategory... category) {
    for (MasterCategory masterCategory : category) {
      assertFalse(getCategoryName(masterCategory) + " not unselected.", window.getToggleButton(getCategoryName(masterCategory)).isSelected());
    }
    return this;
  }


  public void checkExcluded(String categoryName) {
    UISpecAssert.assertFalse(
      window.containsComponent(and(fromClass(JToggleButton.class),
                                   displayedNameIdentity(categoryName))
      )
    );
  }

  public void cancel() {
    window.getButton("cancel").click();
  }

  public CategoryEditionChecker openCategoryEdition() {
    return new CategoryEditionChecker(WindowInterceptor.getModalDialog(window.getButton("editCategories").triggerClick()));
  }
}
