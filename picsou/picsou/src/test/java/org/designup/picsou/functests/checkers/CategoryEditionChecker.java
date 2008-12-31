package org.designup.picsou.functests.checkers;

import org.designup.picsou.model.MasterCategory;
import org.uispec4j.*;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.interception.WindowHandler;
import org.uispec4j.interception.WindowInterceptor;

public class CategoryEditionChecker extends GuiChecker {
  private Window dialog;

  public CategoryEditionChecker(Window dialog) {
    this.dialog = dialog;
  }

  public CategoryEditionChecker createMasterCategory(final String name) {
    WindowInterceptor.init(getCreateMasterCategoryTrigger())
      .process(new WindowHandler() {
        public Trigger process(final Window window) throws Exception {
          return new Trigger() {
            public void run() throws Exception {
              window.getInputTextBox().setText(name);
            }
          };
        }
      })
      .run();
    return this;
  }

  public CategoryEditionChecker createSubCategory(final String name) {
    WindowInterceptor.init(getCreateSubCategoryTrigger())
      .process(new WindowHandler() {
        public Trigger process(final Window window) throws Exception {
          return new Trigger() {
            public void run() throws Exception {
              window.getInputTextBox().setText(name);
            }
          };
        }
      })
      .run();
    return this;
  }

  public CategoryEditionChecker deleteMasterCategory() {
    getDeleteMasterButton().click();
    return this;
  }

  public CategoryEditionChecker deleteSubCategoryWithTransactionUpdate(final String newCategory) {
    deleteCategory(newCategory, getDeleteSubButton().triggerClick());
    return this;
  }

  public CategoryEditionChecker deleteMasterCategoryWithTransactionUpdate(final MasterCategory newCategory) {
    return deleteMasterCategoryWithTransactionUpdate(getCategoryName(newCategory));
  }

  public CategoryEditionChecker deleteMasterCategoryWithTransactionUpdate(final String newCategory) {
    Trigger trigger = getDeleteMasterButton().triggerClick();
    deleteCategory(newCategory, trigger);
    return this;
  }

  private void deleteCategory(final String newCategory, Trigger trigger) {
    WindowInterceptor.init(trigger)
      .process(new WindowHandler() {
        public Trigger process(Window window) throws Exception {
          CategoryDeletionChecker categoryDeletionChecker = new CategoryDeletionChecker(window);
          categoryDeletionChecker.selectCategory(newCategory, null);
          return categoryDeletionChecker.validate();
        }
      }).run();
  }

  public CategoryEditionChecker deleteSubCategory() {
    getDeleteSubButton().click();
    return this;
  }


  public CategoryEditionChecker selectMaster(MasterCategory master) {
    dialog.getListBox("masterCategoryList")
      .select(getCategoryName(master));
    return this;
  }

  public CategoryEditionChecker selectMaster(String master) {
    dialog.getListBox("masterCategoryList")
      .select(master);
    return this;
  }


  public CategoryEditionChecker selectSub(String name) {
    getSubList().select(name);
    return this;
  }

  public void validate() {
    dialog.getButton("OK").click();
  }

  public void cancel() {
    dialog.getButton("Cancel").click();
  }

  public Trigger getCreateSubCategoryTrigger() {
    return getCreateSubButton().triggerClick();
  }

  public Trigger getCreateMasterCategoryTrigger() {
    return getCreateMasterButton().triggerClick();
  }

  public Button getCreateMasterButton() {
    return dialog.getButton("createMasterCategory");
  }

  public Button getDeleteMasterButton() {
    return dialog.getButton("deleteMasterCategory");
  }

  public Button getEditMasterButton() {
    return dialog.getButton("renameMasterCategory");
  }

  public Button getCreateSubButton() {
    return dialog.getButton("createSubCategory");
  }

  public Button getDeleteSubButton() {
    return dialog.getButton("deleteSubCategory");
  }

  public Button getEditSubButton() {
    return dialog.getButton("renameSubCategory");
  }

  public Trigger renameSubCategoryTrigger() {
    return getEditSubButton().triggerClick();
  }

  public void checkSubContains(String name) {
    UISpecAssert.assertThat(getSubList().contains(name));
  }

  public ListBox getSubList() {
    return dialog.getListBox("subCategoryList");
  }

  public ListBox getMasterList() {
    return dialog.getListBox("masterCategoryList");
  }


  public CategoryEditionChecker checkMasterSelected(MasterCategory master) {
    assertMasterSelected(getCategoryName(master));
    return this;
  }

  public CategoryEditionChecker assertMasterSelected(String name) {
    UISpecAssert.assertThat(getMasterList().selectionEquals(name));
    return this;
  }

  public CategoryEditionChecker checkSubSelected(String name) {
    UISpecAssert.assertThat(getSubList().selectionEquals(name));
    return this;
  }

  public CategoryEditionChecker renameMaster(final String previousName, final String name) {
    WindowInterceptor.init(getEditMasterButton().triggerClick())
      .process(new WindowHandler() {
        public Trigger process(final Window window) throws Exception {
          return new Trigger() {
            public void run() throws Exception {
              TextBox box = window.getInputTextBox("input");
              UISpecAssert.assertThat(box.textEquals(previousName));
              box.setText(name);
            }
          };
        }
      }).run();
    return this;
  }

  public void checkClosed() {
    UISpecAssert.assertFalse(dialog.isVisible());
  }

  public CategoryEditionChecker checkCreateMasterEnabled(boolean enabled) {
    UISpecAssert.assertEquals(enabled, getCreateMasterButton().isEnabled());
    return this;
  }

  public CategoryEditionChecker checkCreateSubEnabled(boolean enabled) {
    UISpecAssert.assertEquals(enabled, getCreateSubButton().isEnabled());
    return this;
  }

  public CategoryEditionChecker checkEditMasterEnabled(boolean enabled) {
    UISpecAssert.assertEquals(enabled, getEditMasterButton().isEnabled());
    return this;
  }

  public CategoryEditionChecker checkEditSubEnabled(boolean enabled) {
    UISpecAssert.assertEquals(enabled, getEditSubButton().isEnabled());
    return this;
  }

  public CategoryEditionChecker checkDeleteMasterEnabled(boolean enabled) {
    UISpecAssert.assertEquals(enabled, getDeleteMasterButton().isEnabled());
    return this;
  }

  public CategoryEditionChecker checkDeleteSubEnabled(boolean enabled) {
    UISpecAssert.assertEquals(enabled, getDeleteSubButton().isEnabled());
    return this;
  }

  public CategoryEditionChecker checkMasterNotDisplayed(MasterCategory... masters) {
    ListBox masterList = getMasterList();
    for (MasterCategory master : masters) {
      UISpecAssert.assertFalse(masterList.contains(getCategoryName(master)));
    }
    return this;
  }

  public void checkNoMasterSelected() {
    UISpecAssert.assertTrue(getMasterList().selectionIsEmpty());
  }
}
