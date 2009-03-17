package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.model.MasterCategory;

public class SavingsViewTest extends LoggedInFunctionalTestCase {
  public void test() throws Exception {
    fail("TBD");
    // filtrage des comptes et series d'epargne
    // affichage des projets futurs seulement
    // projets sur plusieurs mois
    // creation/edition de projets
    // creation/edition de series savings
  }

  public void testHideAccountIfNoSeries() throws Exception {
    savingsAccounts.createSavingsAccount("Epargne", 1000);

    views.selectBudget();
    budgetView.savings.createSeries()
      .setName("Virement CAF")
      .setCategory(MasterCategory.SAVINGS)
      .setToAccount("Epargne")
      .setFromAccount("Main accounts")
      .selectAllMonths()
      .setAmount("300")
      .setDay("5")
      .validate();
    budgetView.savings.checkSeriesPresent("Virement CAF");
  }
  
}
