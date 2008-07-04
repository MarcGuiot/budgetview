package org.designup.picsou.functests;

import org.designup.picsou.functests.checkers.CategorizationDialogChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.MasterCategory;

public class CategorizationTest extends LoggedInFunctionalTestCase {
  public void testStandardRecurringTransaction() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -29.90, "Free Telecom")
      .load();

    CategorizationDialogChecker dialog = transactions.categorize(0);
    dialog.checkLabel("Free Telecom");

    dialog.selectRecurring();
    dialog.checkContainsRecurringSeries("Internet", "Rental", "Electricity");
    dialog.selectRecurringSeries("Internet");
    dialog.validate();

    transactionDetails.checkSeries("Internet");
    transactionDetails.checkCategory(MasterCategory.TELECOMS);

    CategorizationDialogChecker reopenedDialog = transactions.categorize(0);
    reopenedDialog.checkRecurringSeriesIsSelected("Internet");
    dialog.selectRecurringSeries("Rental");
    dialog.checkRecurringSeriesIsNotSelected("Internet");
  }

  public void testStandardEnvelopeTransaction() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -29.90, "AUCHAN C'EST BON")
      .load();

    CategorizationDialogChecker dialog = transactions.categorize(0);
    dialog.checkLabel("AUCHAN C'EST BON");

    dialog.selectEnvelopes();
    dialog.checkContainsEnvelope("Groceries", MasterCategory.FOOD, MasterCategory.HOUSE);
    dialog.selectEnvelopeSeries("Groceries", MasterCategory.FOOD);
    dialog.validate();

    transactionDetails.checkSeries("Groceries");
    transactionDetails.checkCategory(MasterCategory.FOOD);

    CategorizationDialogChecker reopenedDialog = transactions.categorize(0);
    reopenedDialog.checkEnveloppeSeriesIsSelected("Groceries", MasterCategory.FOOD);
    dialog.selectEnvelopeSeries("Groceries", MasterCategory.HOUSE);
    dialog.checkEnveloppeSeriesIsSelected("Groceries", MasterCategory.HOUSE);
    dialog.checkEnveloppeSeriesIsNotSelected("Groceries", MasterCategory.FOOD);
  }

  public void testStandardOccasionalTransaction() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -199.90, "LDLC")
      .load();

    CategorizationDialogChecker dialog = transactions.categorize(0);
    dialog.checkLabel("LDLC");

    dialog.selectOccasional();
    dialog.checkContainsOccasional(MasterCategory.MULTIMEDIA, MasterCategory.CLOTHING);
    dialog.selectOccasionalSeries(MasterCategory.MULTIMEDIA);
    dialog.validate();

    transactionDetails.checkSeries("Occasional");
    transactionDetails.checkCategory(MasterCategory.MULTIMEDIA);

    CategorizationDialogChecker reopenedDialog = transactions.categorize(0);
    reopenedDialog.checkOccasionalSeries(MasterCategory.MULTIMEDIA);
  }

  public void testCancel() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -29.90, "Free Telecom")
      .load();

    CategorizationDialogChecker dialog = transactions.categorize(0);
    dialog.checkLabel("Free Telecom");

    dialog.selectRecurring();
    dialog.checkContainsRecurringSeries("Internet", "Rental", "Electricity");
    dialog.selectRecurringSeries("Internet");
    dialog.cancel();

    transactionDetails.checkNoSeries();
  }

  public void testNextPreviousTransaction() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/28", -29.90, "Free Telecom")
      .addTransaction("2008/06/30", -40, "Auchan")
      .load();

    CategorizationDialogChecker dialog = transactions.categorize(0, 1);
    dialog.checkLabel("Free Telecom");

    dialog.selectRecurring();
    dialog.selectRecurringSeries("Internet");
    dialog.checkPreviousIsDisable();
    dialog.selectNext();
    dialog.checkRecurringSeriesIsNotSelected("Internet");
    dialog.checkNextIsDisable();
    dialog.selectEnvelopes();
    dialog.selectEnvelopeSeries("Groceries", MasterCategory.FOOD);
    dialog.selectPrevious();
    dialog.checkPreviousIsDisable();
    dialog.checkRecurringSeriesIsSelected("Internet");
    dialog.selectNext();
    dialog.checkEnveloppeSeriesIsSelected("Groceries", MasterCategory.FOOD);
  }

  public void testSelectRecuringSelectBugdetArea() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/28", -29.90, "Free Telecom")
      .load();

    CategorizationDialogChecker dialog = transactions.categorize(0);

    dialog.selectRecurringSeries("Internet");
    dialog.checkBudgetAreaIsSelected(BudgetArea.RECURRING_EXPENSES);

  }
}
