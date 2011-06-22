package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;

public class ReconciliationTest extends LoggedInFunctionalTestCase {

  public void setUp() throws Exception {
    setCurrentDate("2010/06/20");
    super.setUp();
  }

  public void testStandardUsage() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2010/06/20", 1000.00, "WorldCo")
      .addTransaction("2010/06/20", 100.00, "Auchan")
      .load();

    reconciliation.checkHidden();

    reconciliation.show();
    views.checkCategorizationSelected();
    reconciliation.checkShown();
    categorization.checkTable(new Object[][]{
      {"-", "20/06/2010", "", "AUCHAN", 100.0},
      {"-", "20/06/2010", "", "WORLDCO", 1000.0}
    });

    reconciliation.toggle("WORLDCO");
    categorization.checkTable(new Object[][]{
      {"-", "20/06/2010", "", "AUCHAN", 100.0},
      {"x", "20/06/2010", "", "WORLDCO", 1000.0}
    });
    transactionDetails.checkLabel("WORLDCO");

    reconciliation.hide();
    reconciliation.checkHidden();
    categorization.checkTable(new Object[][]{
      {"20/06/2010", "", "AUCHAN", 100.0},
      {"20/06/2010", "", "WORLDCO", 1000.0}
    });

    reconciliation.show();
    reconciliation.checkShown();
    categorization.checkTable(new Object[][]{
      {"-", "20/06/2010", "", "AUCHAN", 100.0},
      {"x", "20/06/2010", "", "WORLDCO", 1000.0}
    });

    reconciliation.checkShown();
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
    reconciliation.reconcileWithPopup("AUCHAN");
    categorization.checkTable(new Object[][]{
      {"x", "15/06/2010", "", "AUCHAN", 150.0},
      {"x", "20/06/2010", "", "FNAC", 200.0},
      {"-", "20/06/2010", "", "WORLDCO", 1000.0}
    });
    
    reconciliation.unreconcileWithPopup("AUCHAN");
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
    reconciliation.checkShown();
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
  }

  public void testManuallyCategorizedTransactionsAreaAutomaticallyReconciled() throws Exception {
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
      {"!", "15/06/2010", "", "AUCHAN", -50.0},
      {"-", "20/06/2010", "", "FNAC", -200.0},
    });

    reconciliation.checkToggleDisabled(1);
    categorization.checkTable(new Object[][]{
      {"-", "15/06/2010", "", "AUCHAN", -100.0},
      {"!", "15/06/2010", "", "AUCHAN", -50.0},
      {"-", "20/06/2010", "", "FNAC", -200.0},
    });

    categorization.showUnreconciledOnly();
    categorization.setNewVariable(1, "Movies");
    categorization.checkTable(new Object[][]{
      {"-", "15/06/2010", "", "AUCHAN", -100.0},
      {"!", "15/06/2010", "Movies", "AUCHAN", -50.0},
      {"-", "20/06/2010", "", "FNAC", -200.0},
    });

    categorization.showAllTransactions();
    categorization.setNewVariable(0, "Food");
    categorization.checkTable(new Object[][]{
      {"x", "15/06/2010", "Food", "AUCHAN", -100.0},
      {"!", "15/06/2010", "Movies", "AUCHAN", -50.0},
      {"-", "20/06/2010", "", "FNAC", -200.0},
    });

    categorization.showUnreconciledOnly();
    categorization.checkTable(new Object[][]{
      {"-", "20/06/2010", "", "FNAC", -200.0},
    });

    categorization.showAllTransactions();
    categorization.checkTable(new Object[][]{
      {"x", "15/06/2010", "Food", "AUCHAN", -100.0},
      {"!", "15/06/2010", "Movies", "AUCHAN", -50.0},
      {"-", "20/06/2010", "", "FNAC", -200.0},
    });

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
      {"!", "15/06/2010", "Movies", "AUCHAN", -50.0},
      {"-", "20/06/2010", "", "FNAC", -180.0},
      {"!", "20/06/2010", "", "FNAC", -20.0},
    });

    categorization.showUnreconciledOnly();
    categorization.checkTable(new Object[][]{
      {"-", "20/06/2010", "", "FNAC", -180.0},
      {"!", "20/06/2010", "", "FNAC", -20.0},
    });

    reconciliation.toggle(0);
    categorization.checkTable(new Object[][]{
      {"x", "20/06/2010", "", "FNAC", -180.0},
      {"!", "20/06/2010", "", "FNAC", -20.0},
    });

    categorization.setNewVariable(1, "Music");
    categorization.checkTableIsEmpty();
  }

  public void testInitialGuides() throws Exception {
    fail("TODO");
  }

}
