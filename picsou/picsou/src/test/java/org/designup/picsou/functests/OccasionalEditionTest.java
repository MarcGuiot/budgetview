package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.TransactionType;

public class OccasionalEditionTest extends LoggedInFunctionalTestCase {

  public void testBasicAutomatic() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/29", -19.00, "DVD")
      .load();

    timeline.selectMonth("2008/07");

    views.selectCategorization();
    categorization.setOccasional("DVD", MasterCategory.LEISURES);

    views.selectBudget();
    budgetView.occasional
      .edit()
      .setManual()
      .checkTable(new Object[][]{
        {"2008", "August", "0.00", "19.00"},
        {"2008", "July", "19.00", "19.00"}
      })
      .setAutomatic()
      .validate();

    timeline.selectAll();

    views.selectData();
    transactions
      .initContent()
      .add("01/08/2008", TransactionType.PLANNED, "Planned: Occasional", "", -19.00, "Occasional", MasterCategory.NONE)
      .add("29/07/2008", TransactionType.PRELEVEMENT, "DVD", "", -19.00, "Occasional", MasterCategory.LEISURES)
      .check();

    OfxBuilder.init(this)
      .addTransaction("2008/08/04", -10.00, "DVD")
      .addTransaction("2008/07/28", -20.00, "CINE")
      .load();

    views.selectBudget();
    budgetView.occasional.edit()
      .setManual()
      .checkTable(new Object[][]{
        {"2008", "August", "10.00", "19.00"},
        {"2008", "July", "19.00", "19.00"}
      })
      .setAutomatic()
      .validate();
    views.selectCategorization();
    categorization.setOccasional("CINE", MasterCategory.LEISURES);
    views.selectBudget();
    budgetView.occasional.edit()
      .setManual()
      .checkTable(new Object[][]{
        {"2008", "August", "10.00", "39.00"},
        {"2008", "July", "39.00", "39.00"}
      })
      .setAutomatic()
      .validate();

    views.selectCategorization();
    categorization.setEnvelope("DVD", "Detente", MasterCategory.LEISURES, true);

    views.selectBudget();
    budgetView.occasional.edit()
      .setManual()
      .checkTable(new Object[][]{
        {"2008", "August", "0.00", "20.00"},
        {"2008", "July", "20.00", "20.00"}
      })
      .validate();

    views.selectData();
    timeline.selectAll();
    transactions
      .initContent()
      .add("29/08/2008", TransactionType.PLANNED, "Planned: Detente", "", -9.00, "Detente", MasterCategory.LEISURES)
      .add("04/08/2008", TransactionType.PLANNED, "Planned: Occasional", "", -20.00, "Occasional", MasterCategory.NONE)
      .add("04/08/2008", TransactionType.PRELEVEMENT, "DVD", "", -10.00, "Detente", MasterCategory.LEISURES)
      .add("29/07/2008", TransactionType.PRELEVEMENT, "DVD", "", -19.00, "Detente", MasterCategory.LEISURES)
      .add("28/07/2008", TransactionType.PRELEVEMENT, "CINE", "", -20.00, "Occasional", MasterCategory.LEISURES)
      .check();
  }

  public void testBasicManual() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/29", -19.00, "DVD")
      .load();

    timeline.selectMonth("2008/07");

    views.selectBudget();
    budgetView.occasional.edit()
      .setManual()
      .validate();

    views.selectCategorization();
    categorization.setOccasional("DVD", MasterCategory.LEISURES);

    views.selectBudget();
    budgetView.occasional.edit()
      .checkTable(new Object[][]{
        {"2008", "August", "0.00", "0"},
        {"2008", "July", "19.00", "0"}
      })
      .setAllMonths()
      .setAmount("40")
      .checkTable(new Object[][]{
        {"2008", "August", "0.00", "40.00"},
        {"2008", "July", "19.00", "40.00"}
      })
      .validate();

    views.selectCategorization();
    categorization.setEnvelope("DVD", "Detente", MasterCategory.LEISURES, true);

    views.selectBudget();
    budgetView.occasional.edit()
      .checkTable(new Object[][]{
        {"2008", "August", "0.00", "40.00"},
        {"2008", "July", "0.00", "40.00"}
      })
      .validate();
  }

  public void testOpenEditionSelectsSelectedMonth() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/29", -19.00, "DVD")
      .load();
    views.selectBudget();

    timeline.selectMonth("2008/07");
    budgetView.occasional.edit()
      .setManual()
      .checkMonthsSelected(1)
      .validate();

    timeline.selectMonth("2008/08");
    budgetView.occasional.edit()
      .checkMonthsSelected(0)
      .validate();
  }
}
