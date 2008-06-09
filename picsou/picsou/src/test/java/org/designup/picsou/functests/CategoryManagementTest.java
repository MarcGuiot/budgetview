package org.designup.picsou.functests;

import org.crossbowlabs.globs.utils.Files;
import org.crossbowlabs.globs.utils.TestUtils;
import static org.crossbowlabs.globs.utils.Utils.remove;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.TransactionType;
import org.designup.picsou.utils.Lang;
import org.uispec4j.*;
import org.uispec4j.interception.PopupMenuInterceptor;
import org.uispec4j.interception.WindowHandler;
import org.uispec4j.interception.WindowInterceptor;

public class CategoryManagementTest extends LoggedInFunctionalTestCase {
  public void testFiltering() throws Exception {
    learn("BISTROT ANDRE CARTE 06348905 PAIEMENT CB 1904 015 PARIS", MasterCategory.FOOD);
    learn("STATION BP CARTE 06348905 PAIEMENT CB 1904 PARIS", MasterCategory.TRANSPORTS);
    learn("STATION BP MAIL CARTE 06348905 PAIEMENT CB 1104 PARIS", MasterCategory.TRANSPORTS);
    learn("SARL KALISTEA CARTE 06348905 PAIEMENT CB 1404 PARIS", MasterCategory.FOOD);

    String fileName = TestUtils.getFileName(this, ".qif");

    Files.copyStreamTofile(QifImportTest.class.getResourceAsStream("/testfiles/sg1.qif"),
                           fileName);

    operations.importOfxFile(fileName);

    categories.select(MasterCategory.TRANSPORTS);
    transactions
      .initContent()
      .add("20/04/2006", TransactionType.CREDIT_CARD, "STATION BP CARTE 06348905 PAIEMENT CB 1904 PARIS", "", -17.65, MasterCategory.TRANSPORTS)
      .add("13/04/2006", TransactionType.CREDIT_CARD, "STATION BP MAIL CARTE 06348905 PAIEMENT CB 1104 PARIS", "", -18.70, MasterCategory.TRANSPORTS)
      .check();

    categories.select(MasterCategory.TRANSPORTS, MasterCategory.FOOD);
    transactions
      .initContent()
      .add("20/04/2006", TransactionType.CREDIT_CARD, "BISTROT ANDRE CARTE 06348905 PAIEMENT CB 1904 015 PARIS", "", -49.00, MasterCategory.FOOD)
      .add("20/04/2006", TransactionType.CREDIT_CARD, "STATION BP CARTE 06348905 PAIEMENT CB 1904 PARIS", "", -17.65, MasterCategory.TRANSPORTS)
      .add("19/04/2006", TransactionType.CREDIT_CARD, "SARL KALISTEA CARTE 06348905 PAIEMENT CB 1404 PARIS", "", -14.50, MasterCategory.FOOD)
      .add("13/04/2006", TransactionType.CREDIT_CARD, "STATION BP MAIL CARTE 06348905 PAIEMENT CB 1104 PARIS", "", -18.70, MasterCategory.TRANSPORTS)
      .check();

    categories.select(MasterCategory.FOOD);
    transactions
      .initContent()
      .add("20/04/2006", TransactionType.CREDIT_CARD, "BISTROT ANDRE CARTE 06348905 PAIEMENT CB 1904 015 PARIS", "", -49.00, MasterCategory.FOOD)
      .add("19/04/2006", TransactionType.CREDIT_CARD, "SARL KALISTEA CARTE 06348905 PAIEMENT CB 1404 PARIS", "", -14.50, MasterCategory.FOOD)
      .check();

    categories.select(MasterCategory.ALL);
    transactions
      .initContent()
      .add("20/04/2006", TransactionType.CREDIT_CARD, "BISTROT ANDRE CARTE 06348905 PAIEMENT CB 1904 015 PARIS", "", -49.00, MasterCategory.FOOD)
      .add("20/04/2006", TransactionType.CREDIT_CARD, "STATION BP CARTE 06348905 PAIEMENT CB 1904 PARIS", "", -17.65, MasterCategory.TRANSPORTS)
      .add("19/04/2006", TransactionType.CREDIT_CARD, "SARL KALISTEA CARTE 06348905 PAIEMENT CB 1404 PARIS", "", -14.50, MasterCategory.FOOD)
      .add("13/04/2006", TransactionType.CREDIT_CARD, "STATION BP MAIL CARTE 06348905 PAIEMENT CB 1104 PARIS", "", -18.70, MasterCategory.TRANSPORTS)
      .check();
  }

  public void testSelectingUnassignedTransactions() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.0, "Station BP", MasterCategory.TRANSPORTS)
      .addTransaction("2006/01/11", -2.0, "Menu K", MasterCategory.NONE)
      .addTransaction("2006/01/12", -3.0, "Dr Lecter", MasterCategory.NONE)
      .addTransaction("2006/01/13", -12.0, "Mac do", MasterCategory.HOUSE, MasterCategory.BANK)
      .load();

    transactions
      .initContent()
      .add("13/01/2006", TransactionType.PRELEVEMENT, "Mac do", "", -12.0, MasterCategory.BANK, MasterCategory.HOUSE)
      .add("12/01/2006", TransactionType.PRELEVEMENT, "Dr Lecter", "", -3.0, MasterCategory.NONE)
      .add("11/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -2.0, MasterCategory.NONE)
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Station BP", "", -1.0, MasterCategory.TRANSPORTS)
      .check();

    categories.select(MasterCategory.NONE);
    transactions
      .initContent()
      .add("13/01/2006", TransactionType.PRELEVEMENT, "Mac do", "", -12.0, MasterCategory.BANK, MasterCategory.HOUSE)
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

    periods.assertCellSelected(1);
    categories.select(MasterCategory.FOOD, MasterCategory.TRANSPORTS);

    periods.selectCell(0);

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
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Chez Lulu", "", -1.0, "Apero")
      .check();

    categories.select(MasterCategory.FOOD);
    transactions
      .initContent()
      .add("15/01/2006", TransactionType.PRELEVEMENT, "MenuK", "", -2.0, MasterCategory.FOOD)
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Chez Lulu", "", -1.0, "Apero")
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
      "Gifts",
      "Groceries",
      "Apero",
      "Courant",
      "Livraison / A emporter",
      "Receptions",
      "Restaurant",
      "Réceptions",
      "Health",
      "Housing",
      "Income",
      "Internal transfers",
      "Leisures",
      "Miscellaneous",
      "Multimedia",
      "Puericulture",
      "Savings",
      "Taxes",
      "Telecommunications",
      "Transports",
      "Assurance",
      "Crédit auto",
      "Entretien/Réparations",
      "Essence",
      "Moto",
      "Parking",
      "Péages",
      "Transports en commun",
      "Voyages",
    };

    categories.assertCategoryNamesEqual(expectedCategories);

    OfxBuilder
      .init(this)
      .addTransaction("2006/01/15", -2.0, "MenuK", MasterCategory.FOOD)
      .load();
    categories.select(MasterCategory.ALL);
    transactions.openCategoryChooserDialog(0).checkContains(remove(expectedCategories, "All categories"));
  }

  public void testCreatingASiblingSubCategory() throws Exception {
    categories.createSubCategory(MasterCategory.FOOD, "Apero");
    categories.select("Apero");
    categories.createSubCategory(MasterCategory.FOOD, "Charcuterie");
    categories.assertCategoryExists("Charcuterie");
    categories.toggleExpanded(MasterCategory.FOOD);
    categories.assertCategoryNotFound("Charcuterie");
  }

  public void testCannotCreateSubCategoryForCertainCategories() throws Exception {
    categories.select(MasterCategory.ALL);
    categories.assertCreationNotAvailable();

    categories.select(MasterCategory.NONE);
    categories.assertCreationNotAvailable();

    categories.select(MasterCategory.INTERNAL);
    categories.assertCreationNotAvailable();
  }

  public void testCategoryNamesMustBeUnique() throws Exception {
    categories.select(MasterCategory.FOOD);
    WindowInterceptor.init(categories.triggerCreate())
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
  }

  public void testCategoryNameMustNotBeEmpty() throws Exception {
    categories.select(MasterCategory.FOOD);
    WindowInterceptor.init(categories.triggerCreate())
      .process(new WindowHandler() {
        public Trigger process(Window window) throws Exception {
          window.getInputTextBox().setText("");
          Button okButton = window.getButton("OK");
          assertFalse(okButton.isEnabled());
          return window.getButton("Close").triggerClick();
        }
      })
      .run();
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
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Chez Lulu", "", -1.0, "Apero")
      .check();

    categories.deleteSelected();

    categories.assertCategoryNotFound("Apero");

    categories.assertSelectionEquals(MasterCategory.FOOD);

    transactions
      .initContent()
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Chez Lulu", "", -1.0, MasterCategory.FOOD)
      .check();
  }

  public void testDeleteSubcategoryUpdatesTransactionsWithMultiAllocations() throws Exception {
    categories.createSubCategory(MasterCategory.FOOD, "Apero");
    categories.createSubCategory(MasterCategory.TRANSPORTS, "Oil");

    OfxBuilder
      .init(this)
      .addCategory(MasterCategory.FOOD, "Apero")
      .addCategory(MasterCategory.TRANSPORTS, "Oil")
      .addTransaction("2006/01/10", -1.0, "Chez Lulu", "Apero", "Oil")
      .load();

    categories.select(MasterCategory.ALL);
    transactions
      .initContent()
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Chez Lulu", "", -1.0, "Apero, Oil")
      .check();

    categories.select("Apero");
    categories.deleteSelected();

    categories.assertCategoryNotFound("Apero");
    categories.assertSelectionEquals(MasterCategory.FOOD);

    categories.select(MasterCategory.ALL);
    transactions
      .initContent()
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Chez Lulu", "", -1.0, MasterCategory.FOOD, "Oil")
      .check();
  }

  public void testDeletingMasterCategoriesIsForbidden() throws Exception {
    categories.select(MasterCategory.ALL);
    categories.assertDeletionNotAvailable();

    categories.select(MasterCategory.NONE);
    categories.assertDeletionNotAvailable();

    categories.select(MasterCategory.FOOD);
    categories.assertDeletionNotAvailable();

    categories.createSubCategory(MasterCategory.FOOD, "Apero");

    categories.select(MasterCategory.FOOD);
    categories.assertDeletionNotAvailable();
  }

  public void testCannotDeleteSystemCategories() throws Exception {
    categories.assertExpanded(MasterCategory.INCOME, false);

    categories.toggleExpanded(MasterCategory.INCOME);
    categories.assertExpanded(MasterCategory.INCOME, true);
    categories.select("Revenus immobiliers");
    categories.assertDeletionNotAvailable();
  }

  public void testRenameSubcategory() throws Exception {
    categories.createSubCategory(MasterCategory.FOOD, "Apero");
    categories.select("Apero");
    WindowInterceptor.init(categories.triggerRename())
      .process(new WindowHandler() {
        public Trigger process(Window window) throws Exception {
          TextBox input = window.getInputTextBox();
          assertTrue(input.textEquals("Apero"));
          input.setText("Pastis");
          return window.getButton("OK").triggerClick();
        }
      })
      .run();
    categories.assertCategoryNotFound("Apero");
    categories.assertCategoryExists("Pastis");
  }

  public void testCanReuseSameName() throws Exception {
    categories.createSubCategory(MasterCategory.FOOD, "Apero");
    categories.select("Apero");
    WindowInterceptor.init(categories.triggerRename())
      .process(new WindowHandler() {
        public Trigger process(Window window) throws Exception {
          TextBox input = window.getInputTextBox();
          input.clear();
          input.appendText("Apero");
          return window.getButton("Close").triggerClick();
        }
      })
      .run();
    categories.assertCategoryExists("Apero");
    categories.assertCategoryNotFound("Pastis");
  }

  public void testCannotReuseAnExistingName() throws Exception {
    categories.createSubCategory(MasterCategory.FOOD, "Apero");
    categories.select("Apero");
    WindowInterceptor.init(categories.triggerRename())
      .process(new WindowHandler() {
        public Trigger process(Window window) throws Exception {
          TextBox input = window.getInputTextBox();
          input.setText(getCategoryName(MasterCategory.BANK));
          Button okButton = window.getButton("OK");
          assertFalse(okButton.isEnabled());
          assertTrue(window.containsLabel(Lang.get("category.name.already.used")));
          return window.getButton("Close").triggerClick();
        }
      })
      .run();
    categories.assertCategoryExists("Apero");
    categories.assertCategoryNotFound("Pastis");
  }

  public void testRenameCancelled() throws Exception {
    categories.createSubCategory(MasterCategory.FOOD, "Apero");
    categories.select("Apero");
    WindowInterceptor.init(categories.triggerRename())
      .process(new WindowHandler() {
        public Trigger process(Window window) throws Exception {
          TextBox input = window.getInputTextBox();
          input.clear();
          input.appendText("Pastis");
          return window.getButton("Close").triggerClick();
        }
      })
      .run();
    categories.assertCategoryExists("Apero");
    categories.assertCategoryNotFound("Pastis");
  }

  public void testRenamingMastersIsForbidden() throws Exception {
    categories.select(MasterCategory.ALL);
    categories.assertRenameNotAvailable();

    categories.select(MasterCategory.NONE);
    categories.assertRenameNotAvailable();

    categories.select(MasterCategory.FOOD);
    categories.assertRenameNotAvailable();
  }

  public void testPopupMenu() throws Exception {
    WindowInterceptor.init(
      PopupMenuInterceptor.run(categories.triggerPopup(MasterCategory.FOOD))
        .getSubMenu("New")
        .triggerClick())
      .process(new WindowHandler() {
        public Trigger process(Window window) throws Exception {
          window.getInputTextBox().setText("Apero");
          return window.getButton("OK").triggerClick();
        }
      })
      .run();

    categories.assertCategoryExists("Apero");

    WindowInterceptor.init(
      PopupMenuInterceptor.run(categories.triggerPopup("Apero"))
        .getSubMenu("Delete")
        .triggerClick())
      .processWithButtonClick("Yes")
      .run();

    categories.assertCategoryNotFound("Apero");
  }

  public void testCategoryExpansion() throws Exception {
    categories.assertExpanded(MasterCategory.FOOD, false);

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
    categories.deleteSelected();
    categories.assertExpansionEnabled(MasterCategory.MISC_SPENDINGS, false);
  }
}
