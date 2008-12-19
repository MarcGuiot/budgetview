package org.designup.picsou.functests;

import org.designup.picsou.functests.checkers.CategoryChooserChecker;
import org.designup.picsou.functests.checkers.CategoryDeletionChecker;
import org.designup.picsou.functests.checkers.CategoryEditionChecker;
import org.designup.picsou.functests.checkers.SeriesEditionDialogChecker;
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

  public void testPreselectsFirstMaster() throws Exception {
    categories.openEditionDialog()
      .checkMasterSelected(MasterCategory.BANK)
      .cancel();
  }

  public void testPreselectsFirstSubCategory() throws Exception {
    categories.select(MasterCategory.HOUSE);

    categories.openEditionDialog()
      .checkMasterSelected(MasterCategory.HOUSE)
      .checkSubSelected("Energy")
      .cancel();
  }

  public void testMasterFilterSystemCategoryIds() throws Exception {
    categories.openEditionDialog()
      .checkMasterNotDisplayed(MasterCategory.ALL, MasterCategory.NONE, MasterCategory.INCOME)
      .cancel();
  }

  public void testCreateMasterAndSub() throws Exception {
    String newMasterName = "new master Category";
    CategoryEditionChecker categoryEdition = categories.openEditionDialog();
    categoryEdition.createMasterCategory(newMasterName);
    categoryEdition.assertMasterSelected(newMasterName);
    String newSubName = "sub for new master";
    categoryEdition.createSubCategory(newSubName);
    categoryEdition.selectMaster(MasterCategory.HOUSE);
    categoryEdition.checkSubContains("Entretien");
    categoryEdition.selectMaster(newMasterName);
    categoryEdition.checkSubContains(newSubName);
    categoryEdition.validate();

    categories.checkCategoryExists(newMasterName);
    categories.checkCategoryExists(newSubName);
  }

  public void testButtonStatus() throws Exception {
    CategoryEditionChecker dialog = categories.openEditionDialog();

    dialog.selectMaster(MasterCategory.HOUSE)
      .checkCreateMasterEnabled(true)
      .checkCreateSubEnabled(true)
      .checkEditMasterEnabled(true)
      .checkEditSubEnabled(true)
      .checkDeleteMasterEnabled(true)
      .checkDeleteSubEnabled(true)
      .cancel();

    dialog.createMasterCategory("new Master")
      .checkCreateMasterEnabled(true)
      .checkCreateSubEnabled(true)
      .checkEditMasterEnabled(true)
      .checkEditSubEnabled(false)
      .checkDeleteMasterEnabled(true)
      .checkDeleteSubEnabled(false)
      .cancel();

    dialog.createSubCategory("sub")
      .checkCreateMasterEnabled(true)
      .checkCreateSubEnabled(true)
      .checkEditMasterEnabled(true)
      .checkEditSubEnabled(true)
      .checkDeleteMasterEnabled(true)
      .checkDeleteSubEnabled(true)
      .cancel();
  }

  public void testButtonStatusWithEmptyMasterCategory() throws Exception {
    categories.select(MasterCategory.BANK);
    categories.openEditionDialog()
      .checkCreateMasterEnabled(true)
      .checkCreateSubEnabled(true)
      .checkEditMasterEnabled(true)
      .checkEditSubEnabled(false)
      .checkDeleteMasterEnabled(true)
      .checkDeleteSubEnabled(false)
      .cancel();
  }

  public void testRename() throws Exception {
    CategoryEditionChecker categoryEdition = categories.openEditionDialog();
    categoryEdition.selectMaster(MasterCategory.FOOD);
    categoryEdition.renameMaster(getCategoryName(MasterCategory.FOOD), "Bouffe")
      .validate();
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
          CategoryDeletionChecker confirmationDialog = new CategoryDeletionChecker(window);
          confirmationDialog.checkCategory(getCategoryName(MasterCategory.TRANSPORTS));
          confirmationDialog.selectCategory(getCategoryName(MasterCategory.HOUSE), "Fuel");
          return confirmationDialog.validate();
        }
      }).run();

    categoryEdition.checkMasterSelected(MasterCategory.TRANSPORTS);

    categoryEdition.selectMaster(MasterCategory.TELECOMS);
    categoryEdition.checkSubContains("Téléphone fixe");
    categoryEdition.validate();

    categories.checkCategoryNotFound("Fuel");
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
          CategoryDeletionChecker dialog = new CategoryDeletionChecker(window);
          dialog.checkCategory("None");

          dialog.checkOkEnabled(false);
          dialog.selectCategory(getCategoryName(MasterCategory.HOUSE),
                                getCategoryName(MasterCategory.TRANSPORTS));

          dialog.checkOkEnabled(true);
          dialog.checkCategory(getCategoryName(MasterCategory.HOUSE));
          return dialog.validate();
        }
      }).run();
    categoryEdition.validate();

    categories.checkCategoryNotFound("Essence");
    categories.checkCategoryNotFound(getCategoryName(MasterCategory.TRANSPORTS));
    transactions
      .initContent()
      .addOccasional("10/01/2006", TransactionType.PRELEVEMENT, "Station BP", "", -1.0, MasterCategory.HOUSE)
      .check();
  }

  public void testCannotDeleteCategoryIfUsedInSeries() throws Exception {
    operations.openPreferences().setFutureMonthsCount(2);
    OfxBuilder
      .init(this)
      .addTransaction("2008/03/15", -2.0, "Auchan", MasterCategory.HOUSE)
      .load();

    views.selectCategorization();
    categorization.setEnvelope("Auchan", "course", MasterCategory.FOOD, true);

    views.selectData();
    CategoryEditionChecker categoryEdition = categories.openEditionDialog();
    categoryEdition.selectMaster(MasterCategory.FOOD);
    WindowInterceptor.init(categoryEdition.getDeleteMasterButton().triggerClick())
      .process(new WindowHandler() {
        public Trigger process(Window window) throws Exception {
          CategoryDeletionChecker categoryDeletionChecker = new CategoryDeletionChecker(window);
          categoryDeletionChecker.selectCategory(getCategoryName(MasterCategory.TELECOMS),
                                                 getCategoryName(MasterCategory.FOOD));
          return categoryDeletionChecker.validate();
        }
      }).run();
    categoryEdition.validate();

    transactions.checkSeries(0, "course");
    views.selectCategorization();
    categorization.checkEnvelopeSeriesIsSelected("course", MasterCategory.TELECOMS);
  }

  public void testCannotDeleteUsedCategoriesEvenFromAnotherBudgetArea() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/15", -2.0, "Auchan", MasterCategory.HOUSE)
      .load();

    views.selectCategorization();
    categorization.setEnvelope("Auchan", "Courant", MasterCategory.FOOD, true);

    views.selectBudget();
    SeriesEditionDialogChecker seriesDialog = budgetView.recurring.createSeries();
    CategoryChooserChecker categoryChooser = seriesDialog.openCategory();
    categoryChooser
      .openCategoryEdition()
      .selectMaster(MasterCategory.FOOD)
      .deleteMasterCategoryWithTransactionUpdate(MasterCategory.TRANSPORTS)
      .validate();
    categoryChooser.selectCategory(MasterCategory.TRANSPORTS).validate();
    seriesDialog.setName("New series").validate();

    budgetView.envelopes.editSeries("Courant")
      .checkCategory(MasterCategory.TRANSPORTS)
      .cancel();
  }

  public void testDeleteMasterCategoryUpdatesCategorizationOccasional() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/15", -2.0, "Auchan", MasterCategory.HOUSE)
      .load();

    views.selectCategorization();
    categorization.setOccasional("Auchan", MasterCategory.FOOD);

    views.selectData();
    categories.select(MasterCategory.FOOD);
    categories.deleteSelectedMaster(MasterCategory.TELECOMS);
    views.selectCategorization();
    categorization.checkDoesNotContainOccasional(MasterCategory.FOOD);
    categorization.checkOccasionalSeriesIsSelected(MasterCategory.TELECOMS);
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

  public void testRenameCategoryUpdateOccasional() throws Exception {
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
