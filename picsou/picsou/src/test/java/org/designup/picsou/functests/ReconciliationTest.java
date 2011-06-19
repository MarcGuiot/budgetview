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

    reconciliation.show();
    reconciliation.checkShown();
    categorization.checkTable(new Object[][]{
      {"-", "15/06/2010", "", "AUCHAN", 150.0},
      {"-", "20/06/2010", "", "FNAC", 200.0},
      {"-", "20/06/2010", "", "WORLDCO", 1000.0}
    });

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

    fail("TODO: masquer l'entree dans le filtrage");
  }

  public void testReconciliatingSplitTransactions() throws Exception {
    fail("TODO");
  }

  public void testInitialGuides() throws Exception {
    fail("TODO");
  }

}
