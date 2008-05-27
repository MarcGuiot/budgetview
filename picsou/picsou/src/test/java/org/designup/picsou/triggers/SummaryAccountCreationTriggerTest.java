package org.designup.picsou.triggers;

import org.crossbowlabs.globs.model.FieldValuesBuilder;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.Key;
import org.crossbowlabs.globs.model.FieldValue;
import org.crossbowlabs.globs.utils.Dates;
import org.designup.picsou.model.Account;
import static org.designup.picsou.model.Account.*;
import org.designup.picsou.utils.PicsouTestCase;

import java.util.Date;

public class SummaryAccountCreationTriggerTest extends PicsouTestCase {
  private Key summaryKey;

  protected void setUp() throws Exception {
    super.setUp();
    repository.addTrigger(new SummaryAccountCreationTrigger());
    summaryKey = Key.create(TYPE, SUMMARY_ACCOUNT_ID);
  }

  public void testCreatesNoSummaryForOnlyOneAccount() throws Exception {
    checker.parse(repository,
                  "<account number='id1' balance='1.0' updateDate='2006/05/24'/>");
    checkSummary(1.0, "2006/05/24");
  }

  public void testCreatesAndUpdatesASummaryForTwoAccounts() throws Exception {
    checker.parse(repository,
                  "<account number='id1' balance='1.0' updateDate='2006/05/24' id='1'/>" +
                  "<account number='id2' balance='2.0' updateDate='2006/05/26' id='2'/>");
    checkSummary(3.0, "2006/05/24");

    Date newDate = Dates.parse("2006/05/30");
    repository.update(Key.create(TYPE, 1),
                      FieldValue.value(BALANCE, 3.0), FieldValue.value(UPDATE_DATE, newDate));
    checkSummary(5.0, "2006/05/26");
  }

  public void testOnlyUpdatesExistingSummaryAccount() throws Exception {
    String input1 =
          "<account number='" + Account.SUMMARY_ACCOUNT_NUMBER + "' balance='1.0' updateDate='2006/01/01' id='-1'/>" +
          "<account number='id1' balance='1.0' updateDate='2006/05/24' id='1'/>" +
          "<account number='id2' balance='2.0' updateDate='2006/05/26' id='2'/>";
    checker.parse(repository, input1);
    checkSummary(3.0, "2006/05/24");

    Date newDate = Dates.parse("2006/05/30");
    repository.update(Key.create(TYPE, 1),
                      FieldValue.value(BALANCE, 3.0), FieldValue.value(UPDATE_DATE, newDate));
    checkSummary(5.0, "2006/05/26");
  }

  private void checkSummary(double balance, String updateDate) {
    Glob summary = repository.get(summaryKey);
    assertEquals(balance, summary.get(BALANCE));
    assertEquals(Dates.parse(updateDate), summary.get(UPDATE_DATE));
  }
}
