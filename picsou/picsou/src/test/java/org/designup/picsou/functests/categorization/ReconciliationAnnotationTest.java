package org.designup.picsou.functests.categorization;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;

public class ReconciliationAnnotationTest extends LoggedInFunctionalTestCase {

  public void setUp() throws Exception {
    setCurrentDate("2010/06/25");
    super.setUp();
  }

  public void testStandardUsage() throws Exception {

    reconciliation.checkMenuDisabled();

    OfxBuilder
      .init(this)
      .addTransaction("2010/06/20", 1000.00, "WorldCo")
      .addTransaction("2010/06/20", 100.00, "Auchan")
      .load();

    reconciliation.checkMenuEnabled();
    reconciliation.checkColumnAndMenuHidden();
    categorization.showUncategorizedTransactionsOnly();

    reconciliation.show();
    views.checkCategorizationSelected();
    categorization.checkShowsAllTransactions();
    reconciliation.checkSignpostDisplayed("Click here to mark operations as reconciled");
    reconciliation.checkColumnAndMenuShown();
    categorization.checkTable(new Object[][]{
      {"-", "20/06/2010", "", "AUCHAN", 100.0},
      {"-", "20/06/2010", "", "WORLDCO", 1000.0}
    });

    reconciliation.toggle("WORLDCO");
    reconciliation.checkSignpostHidden();
    categorization.checkTable(new Object[][]{
      {"-", "20/06/2010", "", "AUCHAN", 100.0},
      {"x", "20/06/2010", "", "WORLDCO", 1000.0}
    });
    transactionDetails.checkLabel("WORLDCO");

    reconciliation.hide();
    reconciliation.checkColumnAndMenuHidden();
    categorization.checkTable(new Object[][]{
      {"20/06/2010", "", "AUCHAN", 100.0},
      {"20/06/2010", "", "WORLDCO", 1000.0}
    });

    reconciliation.show();
    reconciliation.checkColumnAndMenuShown();
    categorization.checkTable(new Object[][]{
      {"-", "20/06/2010", "", "AUCHAN", 100.0},
      {"x", "20/06/2010", "", "WORLDCO", 1000.0}
    });

    reconciliation.checkColumnAndMenuShown();
    categorization.checkTable(new Object[][]{
      {"-", "20/06/2010", "", "AUCHAN", 100.0},
      {"x", "20/06/2010", "", "WORLDCO", 1000.0}
    });

    categorization.selectTableRows(0, 1);
    reconciliation.toggle(0);
    categorization.checkTable(new Object[][]{
      {"x", "20/06/2010", "", "AUCHAN", 100.0},
      {"x", "20/06/2010", "", "WORLDCO", 1000.0}
    });
    categorization.checkSelectedTableRow(0);
  }

  public void testPopup() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2010/06/20", 1000.00, "WorldCo")
      .addTransaction("2010/06/15", 150.00, "Auchan")
      .addTransaction("2010/06/20", 200.00, "Fnac")
      .load();

    reconciliation.checkPopupEntryHidden(0);

    reconciliation.show();
    categorization.checkTable(new Object[][]{
      {"-", "15/06/2010", "", "AUCHAN", 150.0},
      {"-", "20/06/2010", "", "FNAC", 200.0},
      {"-", "20/06/2010", "", "WORLDCO", 1000.0}
    });

    reconciliation.checkPopupEntryShown(0);

    categorization.selectTableRows(0, 1);
    categorization.checkSelectedTableRows(0, 1);
    reconciliation.reconcileWithPopup(0);
    categorization.checkTable(new Object[][]{
      {"x", "15/06/2010", "", "AUCHAN", 150.0},
      {"x", "20/06/2010", "", "FNAC", 200.0},
      {"-", "20/06/2010", "", "WORLDCO", 1000.0}
    });
    
    reconciliation.unreconcileWithPopup(0);
    categorization.checkTable(new Object[][]{
      {"-", "15/06/2010", "", "AUCHAN", 150.0},
      {"-", "20/06/2010", "", "FNAC", 200.0},
      {"-", "20/06/2010", "", "WORLDCO", 1000.0}
    });

    reconciliation.hide();
    reconciliation.checkPopupEntryHidden(0);
  }

  public void testFiltering() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2010/06/20", 1000.00, "WorldCo")
      .addTransaction("2010/06/15", 150.00, "Auchan")
      .addTransaction("2010/06/20", 200.00, "Fnac")
      .load();

    categorization.checkFilteringModes("All operations",
                                       "Selected months",
                                       "Last imported file",
                                       "Uncategorized operations",
                                       "Uncategorized operations for the selected months");
    categorization.showUncategorizedTransactionsOnly();

    reconciliation.show();
    reconciliation.checkColumnAndMenuShown();
    categorization.checkTable(new Object[][]{
      {"-", "15/06/2010", "", "AUCHAN", 150.0},
      {"-", "20/06/2010", "", "FNAC", 200.0},
      {"-", "20/06/2010", "", "WORLDCO", 1000.0}
    });

    categorization.checkShowsAllTransactions();
    categorization.checkFilteringModes("All operations",
                                       "Selected months",
                                       "Last imported file",
                                       "Uncategorized operations",
                                       "Uncategorized operations for the selected months",
                                       "Unreconciled operations");

    categorization.showUnreconciledOnly();
    categorization.checkTable(new Object[][]{
      {"-", "15/06/2010", "", "AUCHAN", 150.0},
      {"-", "20/06/2010", "", "FNAC", 200.0},
      {"-", "20/06/2010", "", "WORLDCO", 1000.0}
    });

    reconciliation.toggle("WORLDCO");
    categorization.checkTable(new Object[][]{
      {"-", "15/06/2010", "", "AUCHAN", 150.0},
      {"-", "20/06/2010", "", "FNAC", 200.0},
      {"x", "20/06/2010", "", "WORLDCO", 1000.0}
    });

    reconciliation.toggle("FNAC");
    categorization.checkTable(new Object[][]{
      {"-", "15/06/2010", "", "AUCHAN", 150.0},
      {"x", "20/06/2010", "", "FNAC", 200.0}
    });

    reconciliation.hide();
    categorization.checkShowsAllTransactions();
    categorization.checkFilteringModes("All operations",
                                       "Selected months",
                                       "Last imported file",
                                       "Uncategorized operations",
                                       "Uncategorized operations for the selected months");
    categorization.checkTable(new Object[][]{
      {"15/06/2010", "", "AUCHAN", 150.0},
      {"20/06/2010", "", "FNAC", 200.0},
      {"20/06/2010", "", "WORLDCO", 1000.0}
    });
    
    categorization.showUncategorizedTransactionsOnly();
    reconciliation.show();
    categorization.checkTable(new Object[][]{
      {"-", "15/06/2010", "", "AUCHAN", 150.0},
      {"x", "20/06/2010", "", "FNAC", 200.0},
      {"x", "20/06/2010", "", "WORLDCO", 1000.0}
    });
  }

  public void testMultiSelection() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2010/06/20", 1000.00, "WorldCo")
      .addTransaction("2010/06/15", -150.00, "Auchan")
      .addTransaction("2010/06/20", -200.00, "Fnac")
      .addTransaction("2010/06/20", -50.00, "McDo")
      .load();

    reconciliation.show();
    categorization.checkTable(new Object[][]{
      {"-", "15/06/2010", "", "AUCHAN", -150.0},
      {"-", "20/06/2010", "", "FNAC", -200.0},
      {"-", "20/06/2010", "", "MCDO", -50.0},
      {"-", "20/06/2010", "", "WORLDCO", 1000.0},
    });

    reconciliation.toggle(1);
    categorization.selectTableRows(1, 2);
    categorization.checkSelectedTableRows(1, 2);
    
    reconciliation.reconcileWithPopup(1);
    categorization.checkTable(new Object[][]{
      {"-", "15/06/2010", "", "AUCHAN", -150.0},
      {"x", "20/06/2010", "", "FNAC", -200.0},
      {"x", "20/06/2010", "", "MCDO", -50.0},
      {"-", "20/06/2010", "", "WORLDCO", 1000.0},
    });
    categorization.checkSelectedTableRows(1, 2);
  }

  public void testReconciliatingSplitTransactions() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2010/06/15", -150.00, "Auchan")
      .addTransaction("2010/06/20", -200.00, "Fnac")
      .load();

    reconciliation.show();

    categorization.selectTableRow(0);
    transactionDetails.openSplitDialog()
      .checkTable(new Object[][]{
        {"", "Auchan", -150.00, ""},
      })
      .enterAmount("50.00")
      .enterNote("DVD")
      .validateAndClose();

    categorization.checkTable(new Object[][]{
      {"-", "15/06/2010", "", "AUCHAN", -100.0},
      {"/-/", "15/06/2010", "", "AUCHAN", -50.0},
      {"-", "20/06/2010", "", "FNAC", -200.0},
    });

    reconciliation.checkToggleDisabled(1);
    categorization.checkTable(new Object[][]{
      {"-", "15/06/2010", "", "AUCHAN", -100.0},
      {"/-/", "15/06/2010", "", "AUCHAN", -50.0},
      {"-", "20/06/2010", "", "FNAC", -200.0},
    });
    reconciliation.checkToggleTooltip(0, "Click to set this operation as reconciled");
    reconciliation.checkToggleTooltip(1, "You must reconcile the source operation");

    categorization.showUnreconciledOnly();
    categorization.setNewVariable(1, "Movies");
    categorization.checkTable(new Object[][]{
      {"-", "15/06/2010", "", "AUCHAN", -100.0},
      {"/-/", "15/06/2010", "Movies", "AUCHAN", -50.0},
      {"-", "20/06/2010", "", "FNAC", -200.0},
    });

    categorization.showAllTransactions();
    categorization.setNewVariable(0, "Food");
    categorization.checkTable(new Object[][]{
      {"x", "15/06/2010", "Food", "AUCHAN", -100.0},
      {"/x/", "15/06/2010", "Movies", "AUCHAN", -50.0},
      {"-", "20/06/2010", "", "FNAC", -200.0},
    });
    reconciliation.checkToggleTooltip(0, "Click to set this operation as unreconciled");
    reconciliation.checkToggleTooltip(1, "You must unreconcile the source operation");

    categorization.showUnreconciledOnly();
    categorization.checkTable(new Object[][]{
      {"-", "20/06/2010", "", "FNAC", -200.0},
    });

    categorization.showAllTransactions();
    categorization.checkTable(new Object[][]{
      {"x", "15/06/2010", "Food", "AUCHAN", -100.0},
      {"/x/", "15/06/2010", "Movies", "AUCHAN", -50.0},
      {"-", "20/06/2010", "", "FNAC", -200.0},
    });
    reconciliation.checkToggleTooltip(0, "Click to set this operation as unreconciled");
    reconciliation.checkToggleTooltip(1, "You must unreconcile the source operation");

    categorization.selectTableRow(2);
    transactionDetails.openSplitDialog()
      .checkTable(new Object[][]{
        {"", "FNAC", -200.00, ""},
      })
      .enterAmount("20.00")
      .enterNote("CD")
      .validateAndClose();
    categorization.checkTable(new Object[][]{
      {"x", "15/06/2010", "Food", "AUCHAN", -100.0},
      {"/x/", "15/06/2010", "Movies", "AUCHAN", -50.0},
      {"-", "20/06/2010", "", "FNAC", -180.0},
      {"/-/", "20/06/2010", "", "FNAC", -20.0},
    });

    categorization.showUnreconciledOnly();
    categorization.checkTable(new Object[][]{
      {"-", "20/06/2010", "", "FNAC", -180.0},
      {"/-/", "20/06/2010", "", "FNAC", -20.0},
    });

    reconciliation.toggle(0);
    categorization.checkTable(new Object[][]{
      {"x", "20/06/2010", "", "FNAC", -180.0},
      {"/x/", "20/06/2010", "", "FNAC", -20.0},
    });

    categorization.setNewVariable(1, "Music");
    categorization.checkTableIsEmpty();
  }

  public void testManuallyCategorizedTransactionsAreAutomaticallyReconciled() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2010/06/20", 1000.00, "WorldCo")
      .addTransaction("2010/06/15", 150.00, "Auchan")
      .addTransaction("2010/06/20", 200.00, "Fnac")
      .load();

    reconciliation.show();
    categorization.checkTable(new Object[][]{
      {"-", "15/06/2010", "", "AUCHAN", 150.0},
      {"-", "20/06/2010", "", "FNAC", 200.0},
      {"-", "20/06/2010", "", "WORLDCO", 1000.0}
    });

    categorization.setNewRecurring(1, "Leisures");
    categorization.checkTable(new Object[][]{
      {"-", "15/06/2010", "", "AUCHAN", 150.0},
      {"x", "20/06/2010", "Leisures", "FNAC", 200.0},
      {"-", "20/06/2010", "", "WORLDCO", 1000.0}
    });

    OfxBuilder
      .init(this)
      .addTransaction("2010/06/25", 15.00, "Auchan")
      .addTransaction("2010/06/25", 20.00, "Fnac")
      .load();
    categorization.checkTable(new Object[][]{
      {"-", "15/06/2010", "", "AUCHAN", 150.0},
      {"-", "25/06/2010", "", "AUCHAN", 15.0},
      {"x", "20/06/2010", "Leisures", "FNAC", 200.0},
      {"-", "25/06/2010", "Leisures", "FNAC", 20.0},
      {"-", "20/06/2010", "", "WORLDCO", 1000.0}
    });

    categorization.setRecurring(3, "Leisures");
    categorization.checkTable(new Object[][]{
      {"-", "15/06/2010", "", "AUCHAN", 150.0},
      {"-", "25/06/2010", "", "AUCHAN", 15.0},
      {"x", "20/06/2010", "Leisures", "FNAC", 200.0},
      {"x", "25/06/2010", "Leisures", "FNAC", 20.0},
      {"-", "20/06/2010", "", "WORLDCO", 1000.0}
    });
  }

  public void testManuallyCreatedTransactionsAreNotAutomaticallyReconciled() throws Exception {

    mainAccounts.createNewAccount()
      .setName("Cash")
      .setAccountNumber("012345")
      .selectBank("CIC")
      .validate();

    transactionCreation.show()
      .setLabel("FNAC").setAmount(-50.00).setDay(25)
      .create();
    reconciliation.show();
    categorization.checkTable(new Object[][]{
      {"-", "25/06/2010", "", "FNAC", -50.0}
    });

    categorization.setNewVariable(0, "Leisures");
    categorization.checkTable(new Object[][]{
      {"-", "25/06/2010", "Leisures", "FNAC", -50.0}
    });

    categorization.setNewVariable(0, "Groceries", 100.00);
    categorization.checkTable(new Object[][]{
      {"-", "25/06/2010", "Groceries", "FNAC", -50.0}
    });

    categorization.setVariable(0, "Leisures");
    categorization.checkTable(new Object[][]{
      {"-", "25/06/2010", "Leisures", "FNAC", -50.0}
    });

    reconciliation.toggle("FNAC");
    categorization.checkTable(new Object[][]{
      {"x", "25/06/2010", "Leisures", "FNAC", -50.0}
    });
  }
}
