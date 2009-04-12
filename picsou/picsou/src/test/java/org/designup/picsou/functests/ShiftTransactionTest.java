package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.MasterCategory;

public class ShiftTransactionTest extends LoggedInFunctionalTestCase {

  public void testShift() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/05/01", -10.00, "Non shiftable - First month")
      .addTransaction("2008/06/09", -13.00, "Shiftable to previous")
      .addTransaction("2008/06/10", -15.10, "Non shiftable - middle of month 1")
      .addTransaction("2008/06/15", -17.10, "Non shiftable - middle of month 2")
      .addTransaction("2008/06/20", -11.10, "Non shiftable - middle of month 3")
      .addTransaction("2008/06/21", -77.50, "Shiftable to next")
      .addTransaction("2008/07/25", -27.50, "Non shiftable - Last month")
      .load();

    views.selectCategorization();

    transactionDetails.checkShiftDisabled();

    categorization.selectAllTableRows();
    categorization.selectEnvelopes();
    categorization.selectEnvelopeSeries("An enveloppe", MasterCategory.MISC_SPENDINGS, true);
    categorization.checkTable(new Object[][]{
      {"01/05/2008", "An enveloppe", "NON SHIFTABLE - FIRST MONTH", -10.00},
      {"25/07/2008", "An enveloppe", "NON SHIFTABLE - LAST MONTH", -27.50},
      {"10/06/2008", "An enveloppe", "NON SHIFTABLE - MIDDLE OF MONTH 1", -15.10},
      {"15/06/2008", "An enveloppe", "NON SHIFTABLE - MIDDLE OF MONTH 2", -17.10},
      {"20/06/2008", "An enveloppe", "NON SHIFTABLE - MIDDLE OF MONTH 3", -11.10},
      {"21/06/2008", "An enveloppe", "SHIFTABLE TO NEXT", -77.50},
      {"09/06/2008", "An enveloppe", "SHIFTABLE TO PREVIOUS", -13.00}
    });

    transactionDetails.checkShiftDisabled();

    categorization.selectTableRow("SHIFTABLE TO NEXT");
    transactionDetails.checkShiftEnabled();

    categorization.selectNoTableRow();
    transactionDetails.checkShiftDisabled();

    categorization.selectTableRow("NON SHIFTABLE - MIDDLE OF MONTH 1");
    transactionDetails.checkShiftDisabled();

    categorization.selectTableRow("NON SHIFTABLE - MIDDLE OF MONTH 2");
    transactionDetails.checkShiftDisabled();

    categorization.selectTableRow("NON SHIFTABLE - MIDDLE OF MONTH 3");
    transactionDetails.checkShiftDisabled();

    categorization.selectTableRow("NON SHIFTABLE - LAST MONTH");
    transactionDetails.checkShiftDisabled();

    categorization.selectTableRow("NON SHIFTABLE - FIRST MONTH");
    transactionDetails.checkShiftDisabled();

    categorization.selectTableRow("SHIFTABLE TO NEXT");
    transactionDetails.openShiftDialog()
      .checkContainsText("next month")
      .validate();
    transactionDetails.checkShiftInverted();

    categorization.selectTableRow("SHIFTABLE TO PREVIOUS");
    transactionDetails.openShiftDialog()
      .checkContainsText("previous month")
      .validate();
    transactionDetails.checkShiftInverted();

    categorization.checkTable(new Object[][]{
      {"01/05/2008", "An enveloppe", "NON SHIFTABLE - FIRST MONTH", -10.00},
      {"25/07/2008", "An enveloppe", "NON SHIFTABLE - LAST MONTH", -27.50},
      {"10/06/2008", "An enveloppe", "NON SHIFTABLE - MIDDLE OF MONTH 1", -15.10},
      {"15/06/2008", "An enveloppe", "NON SHIFTABLE - MIDDLE OF MONTH 2", -17.10},
      {"20/06/2008", "An enveloppe", "NON SHIFTABLE - MIDDLE OF MONTH 3", -11.10},
      {"01/07/2008", "An enveloppe", "SHIFTABLE TO NEXT", -77.50},
      {"31/05/2008", "An enveloppe", "SHIFTABLE TO PREVIOUS", -13.00}
    });

    categorization.selectTableRow("SHIFTABLE TO NEXT");
    transactionDetails.checkShiftInverted();
    transactionDetails.unshift();
    transactionDetails.checkShiftEnabled();

    categorization.selectTableRow("SHIFTABLE TO PREVIOUS");
    transactionDetails.checkShiftInverted();
    transactionDetails.unshift();
    transactionDetails.checkShiftEnabled();

    categorization.checkTable(new Object[][]{
      {"01/05/2008", "An enveloppe", "NON SHIFTABLE - FIRST MONTH", -10.00},
      {"25/07/2008", "An enveloppe", "NON SHIFTABLE - LAST MONTH", -27.50},
      {"10/06/2008", "An enveloppe", "NON SHIFTABLE - MIDDLE OF MONTH 1", -15.10},
      {"15/06/2008", "An enveloppe", "NON SHIFTABLE - MIDDLE OF MONTH 2", -17.10},
      {"20/06/2008", "An enveloppe", "NON SHIFTABLE - MIDDLE OF MONTH 3", -11.10},
      {"21/06/2008", "An enveloppe", "SHIFTABLE TO NEXT", -77.50},
      {"09/06/2008", "An enveloppe", "SHIFTABLE TO PREVIOUS", -13.00}
    });
  }

  public void testAmountsAreProperlyUpdatedDuringAShiftAndAnUnshift() throws Exception {
    fail("TBD: verifier budgetView, monthSummaryView, accountView, etc. lors d'un shift/unshift");
  }
}
