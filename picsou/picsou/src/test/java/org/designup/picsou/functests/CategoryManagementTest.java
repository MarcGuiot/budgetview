package org.designup.picsou.functests;

import org.designup.picsou.functests.checkers.CategoryEditionChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.TransactionType;
import org.designup.picsou.utils.Lang;
import static org.globsframework.utils.Utils.remove;
import org.uispec4j.*;
import org.uispec4j.interception.WindowHandler;
import org.uispec4j.interception.WindowInterceptor;

public class CategoryManagementTest extends LoggedInFunctionalTestCase {

  public void testFiltering() throws Exception {

    OfxBuilder
      .init(this)
      .addTransaction("2006/04/20", -49.00, "Menu K", MasterCategory.FOOD)
      .addTransaction("2006/04/20", -17.65, "BP", MasterCategory.TRANSPORTS)
      .addTransaction("2006/04/19", -14.50, "Kalistea", MasterCategory.FOOD)
      .addTransaction("2006/04/13", -18.70, "ELF", MasterCategory.TRANSPORTS)
      .load();

    categories.select(MasterCategory.TRANSPORTS);
    transactions
      .initContent()
      .addOccasional("20/04/2006", TransactionType.PRELEVEMENT, "BP", "", -17.65, MasterCategory.TRANSPORTS)
      .addOccasional("13/04/2006", TransactionType.PRELEVEMENT, "ELF", "", -18.70, MasterCategory.TRANSPORTS)
      .check();

    categories.select(MasterCategory.TRANSPORTS, MasterCategory.FOOD);
    transactions
      .initContent()
      .addOccasional("20/04/2006", TransactionType.PRELEVEMENT, "BP", "", -17.65, MasterCategory.TRANSPORTS)
      .addOccasional("20/04/2006", TransactionType.PRELEVEMENT, "Menu K", "", -49.00, MasterCategory.FOOD)
      .addOccasional("19/04/2006", TransactionType.PRELEVEMENT, "Kalistea", "", -14.50, MasterCategory.FOOD)
      .addOccasional("13/04/2006", TransactionType.PRELEVEMENT, "ELF", "", -18.70, MasterCategory.TRANSPORTS)
      .check();

    categories.select(MasterCategory.FOOD);
    transactions
      .initContent()
      .addOccasional("20/04/2006", TransactionType.PRELEVEMENT, "Menu K", "", -49.00, MasterCategory.FOOD)
      .addOccasional("19/04/2006", TransactionType.PRELEVEMENT, "Kalistea", "", -14.50, MasterCategory.FOOD)
      .check();

    categories.select(MasterCategory.ALL);
    transactions
      .initContent()
      .addOccasional("20/04/2006", TransactionType.PRELEVEMENT, "BP", "", -17.65, MasterCategory.TRANSPORTS)
      .addOccasional("20/04/2006", TransactionType.PRELEVEMENT, "Menu K", "", -49.00, MasterCategory.FOOD)
      .addOccasional("19/04/2006", TransactionType.PRELEVEMENT, "Kalistea", "", -14.50, MasterCategory.FOOD)
      .addOccasional("13/04/2006", TransactionType.PRELEVEMENT, "ELF", "", -18.70, MasterCategory.TRANSPORTS)
      .check();
  }

  public void testSelectingUnassignedTransactions() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.0, "Station BP", MasterCategory.TRANSPORTS)
      .addTransaction("2006/01/11", -2.0, "Menu K", MasterCategory.NONE)
      .addTransaction("2006/01/12", -3.0, "Dr Lecter", MasterCategory.NONE)
      .load();

    transactions
      .initContent()
      .add("12/01/2006", TransactionType.PRELEVEMENT, "Dr Lecter", "", -3.0, MasterCategory.NONE)
      .add("11/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -2.0, MasterCategory.NONE)
      .addOccasional("10/01/2006", TransactionType.PRELEVEMENT, "Station BP", "", -1.0, MasterCategory.TRANSPORTS)
      .check();

    categories.select(MasterCategory.NONE);
    transactions
      .initContent()
      .add("12/01/2006", TransactionType.PRELEVEMENT, "Dr Lecter", "", -3.0, MasterCategory.NONE)
      .add("11/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -2.0, MasterCategory.NONE)
      .check();
  }

  public void testCategorySelectionIsPreservedWhenSelectingAnotherPeriod() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.0, "Station BP")
      .addTransaction("2006/02/10", -2.0, "Unknown")
      .load();

    timeline.checkSelection("2006/01", "2006/02");
    categories.select(MasterCategory.FOOD, MasterCategory.TRANSPORTS);

    timeline.selectMonth("2006/01");

    categories.assertSelectionEquals(MasterCategory.FOOD, MasterCategory.TRANSPORTS);
  }

  public void testCreatingASubCategory() throws Exception {
    categories.createSubCategory(MasterCategory.FOOD, "Apero");
    categories.assertSelectionEquals(MasterCategory.FOOD);

    OfxBuilder
      .init(this)
      .addCategory(MasterCategory.FOOD, "Apero")
      .addTransaction("2006/01/10", -1.0, "Chez Lulu", "Apero")
      .addTransaction("2006/01/15", -2.0, "MenuK", MasterCategory.FOOD)
      .load();

    categories.select("Apero");
    transactions
      .initContent()
      .addOccasional("10/01/2006", TransactionType.PRELEVEMENT, "Chez Lulu", "", -1.0, "Apero")
      .check();

    categories.select(MasterCategory.FOOD);
    transactions
      .initContent()
      .addOccasional("15/01/2006", TransactionType.PRELEVEMENT, "MenuK", "", -2.0, MasterCategory.FOOD)
      .addOccasional("10/01/2006", TransactionType.PRELEVEMENT, "Chez Lulu", "", -1.0, "Apero")
      .check();
  }

  public void testCategoryNames() throws Exception {
    categories.createSubCategory(MasterCategory.FOOD, "Apero");
    categories.createSubCategory(MasterCategory.FOOD, "Receptions");
    categories.createSubCategory(MasterCategory.TRANSPORTS, "Moto");

    String[] expectedCategories = {
      "All categories",
      "Unassigned",
      "Bank",
      "Beauty",
      "Clothing",
      "Education",
      "Equipement",
      "Gifts",
      "Groceries",
      "Apero",
      "Receptions",
      "Health",
      "Housing",
      "Income",
      "Internal transfers",
      "Leisures",
      "Miscellaneous",
      "Puericulture",
      "Savings",
      "Taxes",
      "Telecommunications",
      "Transports",
      "Entretien/Réparations",
      "Fuel",
      "Insurance",
      "Moto",
      "Transports en commun",
      "Vehicle acquisition",
    };

    categories.assertCategoryNamesEqual(expectedCategories);

    OfxBuilder
      .init(this)
      .addTransaction("2006/01/15", -2.0, "MenuK", MasterCategory.FOOD)
      .load();

    views.selectCategorization();
    categorization.selectTableRow(0);
    categorization.checkContainsOccasionalCategories(remove(expectedCategories, "All categories", "Unassigned"));
  }

  public void testCreatingASiblingSubCategory() throws Exception {
    categories.createSubCategory(MasterCategory.FOOD, "Apero");
    categories.select("Apero");
    categories.createSubCategory(MasterCategory.FOOD, "Charcuterie");
    categories.assertCategoryExists("Charcuterie");
    categories.toggleExpanded(MasterCategory.FOOD);
    categories.assertCategoryNotFound("Charcuterie");
  }

  public void testCategoryNamesMustBeUnique() throws Exception {
    categories.select(MasterCategory.FOOD);
    CategoryEditionChecker edition = categories.openEditionDialog();
    WindowInterceptor.init(edition.getCreateMasterCategoryTrigger())
      .process(new WindowHandler() {
        public Trigger process(Window window) throws Exception {
          window.getInputTextBox().setText(getCategoryName(MasterCategory.FOOD));
          Button okButton = window.getButton("OK");
          assertFalse(okButton.isEnabled());
          assertTrue(window.containsLabel(Lang.get("category.name.already.used")));
          return window.getButton("Close").triggerClick();
        }
      })
      .run();
    edition.cancel();
  }

  public void testCategoryNameMustNotBeEmpty() throws Exception {
    categories.select(MasterCategory.FOOD);
    CategoryEditionChecker edition = categories.openEditionDialog();
    WindowInterceptor.init(edition.getCreateSubCategoryTrigger())
      .process(new WindowHandler() {
        public Trigger process(Window window) throws Exception {
          window.getInputTextBox().setText("");
          Button okButton = window.getButton("OK");
          assertFalse(okButton.isEnabled());
          return window.getButton("Close").triggerClick();
        }
      })
      .run();
    edition.cancel();
  }

  public void testDeleteSubcategory() throws Exception {
    categories.createSubCategory(MasterCategory.FOOD, "Apero");
    OfxBuilder
      .init(this)
      .addCategory(MasterCategory.FOOD, "Apero")
      .addTransaction("2006/01/10", -1.0, "Chez Lulu", "Apero")
      .load();

    categories.select("Apero");
    transactions
      .initContent()
      .addOccasional("10/01/2006", TransactionType.PRELEVEMENT, "Chez Lulu", "", -1.0, "Apero")
      .check();

    categories.deleteSubSelected(MasterCategory.FOOD);

    categories.assertCategoryNotFound("Apero");

    categories.assertSelectionEquals(MasterCategory.FOOD);

    transactions
      .initContent()
      .addOccasional("10/01/2006", TransactionType.PRELEVEMENT, "Chez Lulu", "", -1.0, MasterCategory.FOOD)
      .check();
  }

  public void testDeleteSubcategoryUpdatesSeriesToCategory() throws Exception {
    categories.createSubCategory(MasterCategory.FOOD, "Apero");
    categories.createSubCategory(MasterCategory.FOOD, "Courant");

    OfxBuilder
      .init(this)
      .addTransaction("2006/01/11", -1.0, "Auchan")
      .addTransaction("2006/01/10", -1.0, "Chez Lulu")
      .load();

    categories.select(MasterCategory.ALL);

    views.selectCategorization();
    categorization.selectTableRows(0, 1);
    categorization.selectEnvelopes();
    categorization.createEnvelopeSeries()
      .setName("Quotidien")
      .setCategory("Apero", "Courant")
      .validate();
    categorization.selectTableRow(0);
    categorization.selectEnvelopes();
    categorization.selectEnvelopeSeries("Quotidien", "Courant");
    categorization.selectTableRow(1);
    categorization.selectEnvelopes();
    categorization.selectEnvelopeSeries("Quotidien", "Apero");

    views.selectData();
    categories.select("Apero");
    categories.deleteSubSelected("Courant");
    categories.assertSelectionEquals("Courant");
    categories.select(MasterCategory.ALL);
    transactions
      .initContent()
      .add("11/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -1.00, "Quotidien", "Courant")
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Chez Lulu", "", -1.00, "Quotidien", "Courant")
      .check();

    views.selectCategorization();
    categorization.selectTableRows("Auchan", "Chez Lulu")
      .selectTableRows("Auchan")
      .checkContainsEnvelope("Quotidien", "Courant")
      .checkNotContainsCategoryInEnvelope("Quotidien", "Apero")
      .editSeries(false)
      .selectSeries("Quotidien")
      .checkCategory("Courant")
      .validate();

    views.selectData();
    categories.select("Courant");
    categories.deleteSubSelected(MasterCategory.FOOD);
    categories.checkValue(MasterCategory.FOOD, "-2");

    views.selectCategorization();
    categorization.selectTableRows("Auchan", "Chez Lulu")
      .selectTableRows("Auchan")
      .checkContainsEnvelope("Quotidien", MasterCategory.FOOD);

    categorization.editSeries(false)
      .selectSeries("Quotidien")
      .checkCategory(MasterCategory.FOOD)
      .validate();
  }

  public void testRenameSubcategory() throws Exception {
    categories.createSubCategory(MasterCategory.FOOD, "Apero");
    categories.select("Apero");
    CategoryEditionChecker editor = categories.openEditionDialog();
    WindowInterceptor.init(editor.renameSubCategoryTrigger())
      .process(new WindowHandler() {
        public Trigger process(Window window) throws Exception {
          TextBox input = window.getInputTextBox();
          assertTrue(input.textEquals("Apero"));
          input.setText("Pastis");
          return window.getButton("OK").triggerClick();
        }
      })
      .run();
    editor.validate();
    categories.assertCategoryNotFound("Apero");
    categories.assertCategoryExists("Pastis");
  }

  public void testRenameCategoryRenameInCategorization() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/11", -1.0, "Auchan")
      .load();
    views.selectCategorization();
    categorization.setEnvelope("Auchan", "Courant", MasterCategory.FOOD, true);
    views.selectData();
    categories.select(MasterCategory.FOOD);
    categories.openEditionDialog().renameMaster(getCategoryName(MasterCategory.FOOD), "Apero")
      .validate();
    views.selectCategorization();
    categorization.selectEnvelopes();
    categorization.checkContainsLabelInEnvelope("Apero");
  }

  public void testCanReuseSameName() throws Exception {
    categories.createSubCategory(MasterCategory.FOOD, "Apero");
    categories.select("Apero");
    CategoryEditionChecker editor = categories.openEditionDialog();
    WindowInterceptor.init(editor.renameSubCategoryTrigger())
      .process(new WindowHandler() {
        public Trigger process(Window window) throws Exception {
          TextBox input = window.getInputTextBox();
          input.clear();
          input.appendText("Apero");
          return window.getButton("OK").triggerClick();
        }
      })
      .run();
    editor.validate();
    categories.assertCategoryExists("Apero");
  }

  public void testCannotReuseAnExistingSubCategoryNameInTheSameMaster() throws Exception {
    categories.createSubCategory(MasterCategory.FOOD, "Apero");
    categories.createSubCategory(MasterCategory.FOOD, "Restaurant");
    categories.select("Apero");
    CategoryEditionChecker editor = categories.openEditionDialog();

    WindowInterceptor.init(editor.renameSubCategoryTrigger())
      .process(new WindowHandler() {
        public Trigger process(Window window) throws Exception {
          window.getInputTextBox().setText("Restaurant");
          assertFalse(window.getButton("OK").isEnabled());
          assertTrue(window.containsLabel(Lang.get("category.name.already.used")));
          return window.getButton("Close").triggerClick();
        }
      })
      .run();
    editor.validate();
    categories.assertCategoryExists("Apero");
  }

  public void testRenameCancelled() throws Exception {
    categories.createSubCategory(MasterCategory.FOOD, "Apero");
    categories.select("Apero");
    CategoryEditionChecker editor = categories.openEditionDialog();
    WindowInterceptor.init(editor.renameSubCategoryTrigger())
      .process(new WindowHandler() {
        public Trigger process(Window window) throws Exception {
          TextBox input = window.getInputTextBox();
          input.clear();
          input.appendText("Pastis");
          return window.getButton("Close").triggerClick();
        }
      })
      .run();
    editor.validate();
    categories.assertCategoryExists("Apero");
    categories.assertCategoryNotFound("Pastis");
  }

  public void testCategoryExpansion() throws Exception {
    categories.assertExpansionEnabled(MasterCategory.FOOD, false);

    categories.createSubCategory(MasterCategory.FOOD, "Apero");
    categories.assertExpanded(MasterCategory.FOOD, true);
    categories.assertVisible("Apero", true);

    categories.toggleExpanded(MasterCategory.FOOD);
    categories.assertExpanded(MasterCategory.FOOD, false);
    categories.assertVisible("Apero", false);

    categories.toggleExpanded(MasterCategory.FOOD);
    categories.assertExpanded(MasterCategory.FOOD, true);
    categories.assertVisible("Apero", true);
  }

  public void testExpansionWithDoubleClick() throws Exception {
    categories.createSubCategory(MasterCategory.FOOD, "Apero");
    categories.assertExpanded(MasterCategory.FOOD, true);

    categories.doubleClick(MasterCategory.FOOD);
    categories.assertExpanded(MasterCategory.FOOD, false);

    categories.doubleClick(MasterCategory.FOOD);
    categories.assertExpanded(MasterCategory.FOOD, true);
  }

  public void testDoubleClickOnSubcategoryDoesNothing() throws Exception {
    categories.createSubCategory(MasterCategory.FOOD, "Apero");
    categories.assertExpanded(MasterCategory.FOOD, true);

    categories.doubleClick("Apero");
    categories.assertExpanded(MasterCategory.FOOD, true);
  }

  public void testGenericCategoriesCannotBeExpanded() throws Exception {
    categories.assertExpansionEnabled(MasterCategory.ALL, false);
    categories.assertExpansionEnabled(MasterCategory.NONE, false);
  }

  public void testCanShiftSelectFromExpansionColumn() throws Exception {
    Table table = categories.getTable();
    categories.createSubCategory(MasterCategory.MISC_SPENDINGS, "a1");
    categories.createSubCategory(MasterCategory.MISC_SPENDINGS, "a2");
    categories.createSubCategory(MasterCategory.MISC_SPENDINGS, "a3");

    String[] categoryNames = {"a1", "a2", "a3"};
    int[] rows = categories.getIndexes(categoryNames);

    table.clearSelection();
    table.click(rows[0], 0);
    table.click(rows[2], 0, Key.Modifier.SHIFT);

    assertTrue(categories.getTable().rowsAreSelected(rows));
  }

  public void testDeletingAllSubcategoriesDisablesExpansion() throws Exception {
    categories.assertExpansionEnabled(MasterCategory.MISC_SPENDINGS, false);

    categories.createSubCategory(MasterCategory.MISC_SPENDINGS, "Misc");
    categories.assertExpansionEnabled(MasterCategory.MISC_SPENDINGS, true);

    categories.select("Misc");
    categories.deleteSubSelected();
    categories.assertExpansionEnabled(MasterCategory.MISC_SPENDINGS, false);
    categories.assertSelectionEquals(MasterCategory.MISC_SPENDINGS);
  }

  public void testDeleteDifferentSubcategory() throws Exception {
    categories.createSubCategory(MasterCategory.FOOD, "Apero");
    categories.createSubCategory(MasterCategory.TELECOMS, "3G++");
    categories.select(MasterCategory.EDUCATION);
    categories.assertCategoryExists("Apero");
    categories.assertCategoryExists("3G++");
    CategoryEditionChecker edition = categories.openEditionDialog();
    edition.selectMaster(MasterCategory.FOOD);
    edition.selectSub("Apero");
    edition.deleteSubCategory();
    edition.selectMaster(MasterCategory.TELECOMS);
    edition.selectSub("3G++");
    edition.deleteSubCategory();
    edition.validate();

    categories.assertCategoryNotFound("Apero");
    categories.assertCategoryNotFound("3G++");

    categories.assertSelectionEquals(MasterCategory.EDUCATION);
  }

  public void testSelectSubSelectMasterAndSubInCategoryEdition() throws Exception {
    categories.createSubCategory(MasterCategory.FOOD, "Apero");
    categories.select("Apero");
    categories.openEditionDialog()
      .assertMasterSelected(MasterCategory.FOOD)
      .assertSubSelected("Apero");
  }
}
