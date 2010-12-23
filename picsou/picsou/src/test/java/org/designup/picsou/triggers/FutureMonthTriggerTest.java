package org.designup.picsou.triggers;

import org.designup.picsou.gui.time.TimeService;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.UserPreferences;
import org.designup.picsou.utils.PicsouTestCase;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.Key;
import org.globsframework.utils.Dates;

public class FutureMonthTriggerTest extends PicsouTestCase {

  protected void setUp() throws Exception {
    super.setUp();
    directory.add(new TimeService(Dates.parse("2008/07/09")));
  }

  public void testMoreMonthUserPreferences() throws Exception {
    repository.addTrigger(new MonthTrigger(directory));
    repository.create(Key.create(UserPreferences.TYPE, UserPreferences.SINGLETON_ID),
                      value(UserPreferences.FUTURE_MONTH_COUNT, 5));
    listener.assertLastChangesEqual(Month.TYPE,
                                    "<create id='200807' type='month'/>" +
                                    "<create id='200808' type='month'/>" +
                                    "<create id='200809' type='month'/>" +
                                    "<create id='200810' type='month'/>" +
                                    "<create id='200811' type='month'/>" +
                                    "<create id='200812' type='month'/>");

  }

  public void testLessMonthUserPreferences() throws Exception {
    repository.addTrigger(new MonthTrigger(directory));
    repository.create(Key.create(UserPreferences.TYPE, UserPreferences.SINGLETON_ID),
                      value(UserPreferences.FUTURE_MONTH_COUNT, 5));
    listener.reset();
    repository.update(Key.create(UserPreferences.TYPE, UserPreferences.SINGLETON_ID),
                      value(UserPreferences.FUTURE_MONTH_COUNT, 2));
    listener.assertLastChangesEqual(Month.TYPE,
                                    "<delete id='200810' type='month'/>" +
                                    "<delete id='200811' type='month'/>" +
                                    "<delete id='200812' type='month'/>");
  }

  public void testNoMonth() throws Exception {
    repository.addTrigger(new MonthTrigger(directory));
    repository.create(Key.create(UserPreferences.TYPE, UserPreferences.SINGLETON_ID),
                      value(UserPreferences.FUTURE_MONTH_COUNT, 0));
    listener.assertLastChangesEqual(Month.TYPE, "<create id='200807' type='month'/>");
  }
}
