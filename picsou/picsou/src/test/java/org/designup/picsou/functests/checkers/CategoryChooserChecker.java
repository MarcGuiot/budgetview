package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.designup.picsou.model.MasterCategory;
import org.uispec4j.CheckBox;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;
import static org.uispec4j.assertion.UISpecAssert.assertFalse;
import static org.uispec4j.assertion.UISpecAssert.assertThat;
import static org.uispec4j.finder.ComponentMatchers.*;
import org.uispec4j.interception.WindowInterceptor;

import javax.swing.*;

public class CategoryChooserChecker extends GuiChecker {
  private Window window;

  public CategoryChooserChecker(Window window) {
    this.window = window;
  }

  public CategoryChooserChecker checkTitle(String title) {
    assertThat(window.getTextBox("title").textEquals(title));
    return this;
  }

  public CategoryChooserChecker selectCategory(MasterCategory master) {
    return selectCategory(getCategoryName(master));
  }

  public CategoryChooserChecker selectCategory(String categoryName, boolean oneSelection) {
    selectCategory(window, categoryName, oneSelection);
    return this;
  }

  public CategoryChooserChecker selectCategory(String categoryName) {
    CheckBox button = window.getCheckBox(categoryName);
    button.select();
    assertThat(button.isSelected());
    return this;
  }

  public CategoryChooserChecker unselectCategory(String categoryName) {
    CheckBox button = window.getCheckBox(categoryName);
    button.unselect();
    assertFalse(button.isSelected());
    return this;
  }

  public void validate() {
    window.getButton("ok").click();
    assertFalse(window.isVisible());
  }

  public static void selectCategory(Window dialog, String categoryName, boolean oneSelection) {
    dialog.getCheckBox(categoryName).click();
    if (!oneSelection) {
      dialog.getButton("ok").click();
    }
    assertFalse(dialog.isVisible());
  }

  public CategoryChooserChecker checkContains(String... expectedCategories) {
    for (String expectedCategory : expectedCategories) {
      Assert.assertNotNull(window.getCheckBox(expectedCategory));
    }
    return this;
  }


  public void checkNotFound(MasterCategory... masters) {
    for (MasterCategory master : masters) {
      UISpecAssert.assertFalse(
        window.containsComponent(and(fromClass(JCheckBox.class),
                                     displayedNameIdentity(getCategoryName(master)))
        )
      );
    }
  }

  public CategoryChooserChecker checkSelected(MasterCategory... category) {
    for (MasterCategory masterCategory : category) {
      assertThat(getCategoryName(masterCategory) + " not selected.", window.getCheckBox(getCategoryName(masterCategory)).isSelected());
    }
    return this;
  }

  public CategoryChooserChecker checkUnselected(MasterCategory... category) {
    for (MasterCategory masterCategory : category) {
      assertFalse(getCategoryName(masterCategory) + " not unselected.", window.getCheckBox(getCategoryName(masterCategory)).isSelected());
    }
    return this;
  }


  public void checkExcluded(String categoryName) {
    UISpecAssert.assertFalse(
      window.containsComponent(and(fromClass(JCheckBox.class),
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

  public void checkClosed() {
    assertFalse(window.isVisible());
  }
}
