package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;

public class SignpostSequenceTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    resetWindow();
    setCurrentDate("2010/05/31");
    setInMemory(false);
    setDeleteLocalPrevayler(true);
    super.setUp();
    setDeleteLocalPrevayler(false);
  }

  public void testImport() throws Exception {

    views.selectHome();
    actions.checkImportSignpostDisplayed("Click here to import your operations");

    actions.openImport().close();
    actions.checkImportSignpostHidden();

    OfxBuilder
      .init(this)
      .addTransaction("2010/05/27", -100, "rent")
      .addTransaction("2006/05/28", +500, "income")
      .load();

    views.checkCategorizationSelected();
    categorization.checkSelectionSignpostDisplayed("Select the operations to categorize");

    categorization.selectTableRow(0);
    categorization.checkSelectionSignpostHidden();

    restartApplication();

    views.selectHome();
    actions.checkImportSignpostHidden();
    
    views.selectCategorization();
    categorization.checkSelectionSignpostHidden();
  }
}
