package org.designup.picsou.functests.checkers;

import org.designup.picsou.model.MasterCategory;
import org.uispec4j.*;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.interception.WindowHandler;
import org.uispec4j.interception.WindowInterceptor;

public class CategoryEditionChecker extends DataChecker {
  private Window dialog;

  public CategoryEditionChecker(Window dialog) {
    this.dialog = dialog;
  }

  public void createMasterCategory(final String name) {
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
  }

  public void createSubCategory(final String name) {
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
  }

  public void deleteMasterCategory(final String name) {
    WindowInterceptor.init(getDeleteMasterButton().triggerClick())
      .process(new WindowHandler() {
        public Trigger process(Window window) throws Exception {
          DeleteCategoryChecker categoryChecker = new DeleteCategoryChecker(window);
          categoryChecker.selectCategory(name);
          return categoryChecker.validate();
        }
      }).run();

  }

  public void deleteSubCategoryWithTransactionUpdate(final String newCategory) {
    WindowInterceptor.init(getDeleteMasterButton().triggerClick())
      .process(new WindowHandler() {
        public Trigger process(Window window) throws Exception {
          DeleteCategoryChecker categoryChecker = new DeleteCategoryChecker(window);
          categoryChecker.selectCategory(newCategory);
          return categoryChecker.validate();
        }
      }).run();

  }

  public void deleteSubCategory() {
    getDeleteSubButton().click();
  }


  public void selectMaster(MasterCategory master) {
    dialog.getListBox("masterCategoryList")
      .select(getCategoryName(master));
  }

  public void selectMaster(String master) {
    dialog.getListBox("masterCategoryList")
      .select(master);
  }


  public void selectSub(String name) {
    getSubList().select(name);
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

  public void assertSubContains(String name) {
    UISpecAssert.assertThat(getSubList().contains(name));
  }

  public ListBox getSubList() {
    return dialog.getListBox("subCategoryList");
  }

  public ListBox getMasterList() {
    return dialog.getListBox("masterCategoryList");
  }


  public void assertMasterSelected(String name) {
    UISpecAssert.assertThat(getMasterList().selectionEquals(name));
  }

  public void assertSubSelected(String name) {
    UISpecAssert.assertThat(getSubList().selectionEquals(name));
  }

  public void renameMaster(final String previousName, final String name) {
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
  }

  public void checkClosed() {
    UISpecAssert.assertFalse(dialog.isVisible());
  }
}
