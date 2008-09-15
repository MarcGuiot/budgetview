package org.designup.picsou.functests;

import org.designup.picsou.functests.checkers.CategoryEditionChecker;
import org.designup.picsou.functests.checkers.DeleteCategoryChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.TransactionType;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.interception.WindowHandler;
import org.uispec4j.interception.WindowInterceptor;

public class CategoryEditionTest extends LoggedInFunctionalTestCase {

  public void testPreselectsFirstSubCategory() throws Exception {
    categories.select(MasterCategory.HOUSE);
    CategoryEditionChecker categoryEdition = categories.openEditionDialog();
    categoryEdition.assertMasterSelected(MasterCategory.HOUSE);
    categoryEdition.assertSubSelected("Energy");
  }

  public void testMasterFilterSystemCategoryIds() throws Exception {
    CategoryEditionChecker categoryEdition = categories.openEditionDialog();
    assertFalse(categoryEdition.getMasterList().contains(getCategoryName(MasterCategory.ALL)));
    assertFalse(categoryEdition.getMasterList().contains(getCategoryName(MasterCategory.NONE)));
    assertFalse(categoryEdition.getMasterList().selectionIsEmpty());
  }

  public void testCreateMasterAndSub() throws Exception {
    String newMasterName = "new master Category";
    CategoryEditionChecker categoryEdition = categories.openEditionDialog();
    categoryEdition.createMasterCategory(newMasterName);
    categoryEdition.assertMasterSelected(newMasterName);
    String newSubName = "sub for new master";
    categoryEdition.createSubCategory(newSubName);
    categoryEdition.selectMaster(MasterCategory.HOUSE);
    categoryEdition.assertSubContains("Entretien");
    categoryEdition.selectMaster(newMasterName);
    categoryEdition.assertSubContains(newSubName);
    categoryEdition.validate();
    categories.assertCategoryExists(newMasterName);
    categories.assertCategoryExists(newSubName);
  }

  public void testButtonStatus() throws Exception {
    CategoryEditionChecker categoryEdition = categories.openEditionDialog();

    categoryEdition.selectMaster(MasterCategory.HOUSE);
    UISpecAssert.assertTrue(categoryEdition.getCreateMasterButton().isEnabled());
    UISpecAssert.assertTrue(categoryEdition.getDeleteMasterButton().isEnabled());
    UISpecAssert.assertTrue(categoryEdition.getEditMasterButton().isEnabled());
    UISpecAssert.assertTrue(categoryEdition.getEditSubButton().isEnabled());
    UISpecAssert.assertTrue(categoryEdition.getCreateSubButton().isEnabled());
    UISpecAssert.assertTrue(categoryEdition.getDeleteSubButton().isEnabled());

    categoryEdition.createMasterCategory("new Master");
    UISpecAssert.assertFalse(categoryEdition.getEditSubButton().isEnabled());
    UISpecAssert.assertTrue(categoryEdition.getCreateSubButton().isEnabled());
    UISpecAssert.assertFalse(categoryEdition.getDeleteSubButton().isEnabled());

    categoryEdition.createSubCategory("sub");
    UISpecAssert.assertTrue(categoryEdition.getEditSubButton().isEnabled());
    UISpecAssert.assertTrue(categoryEdition.getCreateSubButton().isEnabled());
    UISpecAssert.assertTrue(categoryEdition.getDeleteSubButton().isEnabled());
  }

  public void testButtonStatusWithEmptyMasterCategory() throws Exception {
    categories.select(MasterCategory.BANK);
    CategoryEditionChecker categoryEdition = categories.openEditionDialog();
    UISpecAssert.assertTrue(categoryEdition.getEditMasterButton().isEnabled());
    UISpecAssert.assertTrue(categoryEdition.getDeleteMasterButton().isEnabled());
    UISpecAssert.assertTrue(categoryEdition.getCreateMasterButton().isEnabled());
    UISpecAssert.assertTrue(categoryEdition.getCreateSubButton().isEnabled());
    UISpecAssert.assertFalse(categoryEdition.getDeleteSubButton().isEnabled());
    UISpecAssert.assertFalse(categoryEdition.getEditSubButton().isEnabled());
  }

  public void testRename() throws Exception {
    CategoryEditionChecker categoryEdition = categories.openEditionDialog();
    categoryEdition.selectMaster(MasterCategory.FOOD);
    categoryEdition.renameMaster(getCategoryName(MasterCategory.FOOD), "Bouffe");
    UISpecAssert.assertThat(categoryEdition.getMasterList().contains("Bouffe"));
  }

  public void testDeleteWithTransaction() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.0, "Station BP", "transports.essence")
      .load();

    categories.createSubCategory(MasterCategory.TELECOMS, "Téléphone fixe");

    CategoryEditionChecker categoryEdition = categories.openEditionDialog();
    categoryEdition.selectMaster(MasterCategory.TRANSPORTS);
    categoryEdition.selectSub("Fuel");

    WindowInterceptor.init(categoryEdition.getDeleteSubButton().triggerClick())
      .process(new WindowHandler() {
        public Trigger process(Window window) throws Exception {
          DeleteCategoryChecker confirmationDialog = new DeleteCategoryChecker(window);
          confirmationDialog.checkCategory(getCategoryName(MasterCategory.TRANSPORTS));
          confirmationDialog.selectCategory(getCategoryName(MasterCategory.HOUSE));
          return confirmationDialog.validate();
        }
      }).run();

    categoryEdition.assertMasterSelected(MasterCategory.TRANSPORTS);

    categoryEdition.selectMaster(MasterCategory.TELECOMS);
    categoryEdition.assertSubContains("Téléphone fixe");
    categoryEdition.validate();

    categories.assertCategoryNotFound("Fuel");
    transactions
      .initContent()
      .addOccasional("10/01/2006", TransactionType.PRELEVEMENT, "Station BP", "", -1.0, MasterCategory.HOUSE)
      .check();
  }

  public void testDeleteMasterWithTransactionInSub() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.0, "Station BP", "transports.essence")
      .load();
    CategoryEditionChecker categoryEdition = categories.openEditionDialog();
    categoryEdition.selectMaster(MasterCategory.TRANSPORTS);
    WindowInterceptor.init(categoryEdition.getDeleteMasterButton().triggerClick())
      .process(new WindowHandler() {
        public Trigger process(Window window) throws Exception {
          DeleteCategoryChecker categoryChecker = new DeleteCategoryChecker(window);
          categoryChecker.checkCategory("None");
          UISpecAssert.assertFalse(categoryChecker.getOkButton().isEnabled());
          categoryChecker.selectCategory(getCategoryName(MasterCategory.HOUSE));
          UISpecAssert.assertTrue(categoryChecker.getOkButton().isEnabled());
          categoryChecker.checkCategory(getCategoryName(MasterCategory.HOUSE));
          return categoryChecker.validate();
        }
      }).run();
    categoryEdition.validate();

    categories.assertCategoryNotFound("Essence");
    categories.assertCategoryNotFound(getCategoryName(MasterCategory.TRANSPORTS));
    transactions
      .initContent()
      .addOccasional("10/01/2006", TransactionType.PRELEVEMENT, "Station BP", "", -1.0, MasterCategory.HOUSE)
      .check();
  }

  public void testCannotDeleteCategoryIfUsedInSeries() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/15", -2.0, "Auchan", MasterCategory.HOUSE)
      .load();

    views.selectCategorization();
    categorization.setEnvelope("Auchan", "Groceries", MasterCategory.FOOD, true);

    views.selectData();
    CategoryEditionChecker categoryEdition = categories.openEditionDialog();
    categoryEdition.selectMaster(MasterCategory.FOOD);
    WindowInterceptor.init(categoryEdition.getDeleteMasterButton().triggerClick())
      .process(new WindowHandler() {
        public Trigger process(Window window) throws Exception {
          DeleteCategoryChecker categoryChecker = new DeleteCategoryChecker(window);
          categoryChecker.selectCategory(getCategoryName(MasterCategory.TELECOMS));
          return categoryChecker.validate();
        }
      }).run();
    categoryEdition.validate();

    transactions.checkSeries(0, "Groceries");
  }

  public void testCanReuseNameUsedInOtherMaster() throws Exception {
    categories.openEditionDialog()
      .selectMaster(MasterCategory.HOUSE)
      .createSubCategory("Internet Access")
      .validate();
    categories.assertCategoryExists("Internet Access", 1);

    categories.openEditionDialog()
      .selectMaster(MasterCategory.TELECOMS)
      .createSubCategory("Internet Access")
      .validate();

    categories.assertCategoryExists("Internet Access", 2);
  }

  public void testDeleteAndReopen() throws Exception {
    categories.select(MasterCategory.HOUSE);
    categories.openEditionDialog()
      .deleteMasterCategory()
      .validate();
    CategoryEditionChecker categories = this.categories.openEditionDialog();
    categories.getMasterList().contains();

    categories.validate();
  }

  public void testRenameCategoryUpdateOccational() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/15", -2.0, "Auchan", MasterCategory.HOUSE)
      .load();
    views.selectData();
    categories.openEditionDialog()
      .selectMaster(MasterCategory.FOOD)
      .renameMaster(getCategoryName(MasterCategory.FOOD), "Boire")
      .validate();
    views.selectCategorization();
    categorization
      .selectTableRow(0)
      .selectOccasional()
      .checkOccasionalContainLabel("Boire");
  }

}
