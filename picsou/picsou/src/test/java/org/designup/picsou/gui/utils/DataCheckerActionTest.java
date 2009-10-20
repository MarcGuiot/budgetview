package org.designup.picsou.gui.utils;

import junit.framework.TestCase;
import org.globsframework.model.impl.DefaultGlobRepository;
import org.globsframework.model.impl.DefaultGlobIdGenerator;
import org.globsframework.model.Key;
import org.globsframework.model.FieldValue;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.TestUtils;
import org.globsframework.utils.Dates;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.SeriesBudget;
import org.designup.picsou.gui.TimeService;

public class DataCheckerActionTest extends TestCase {
  private DefaultGlobRepository repository;
  private DefaultDirectory directory;

  protected void setUp() throws Exception {
    super.setUp();
    TimeService.setCurrentDate(Dates.parse("2009/10/10"));
    repository = new DefaultGlobRepository(new DefaultGlobIdGenerator());
    directory = new DefaultDirectory();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    repository = null;
    directory = null;
  }

  public void testMissingMonth() throws Exception {
    checkMissingMonth(new int[]{200911},200910, 200912, 201001, 201002);
  }

  private void checkMissingMonth(int missingMonth[], int... currentMonth) {
    createMonth(currentMonth);
    DataCheckerAction action = new DataCheckerAction(repository, directory);
    StringBuilder builder = new StringBuilder();
    assertTrue(action.doCheck(builder));
    String s = builder.toString();
    for (int month : missingMonth) {
      assertTrue(Integer.toString(month), s.contains("Missing month " + month));
    }
    for (int i : currentMonth) {
      assertFalse(Integer.toString(i), s.contains("Missing month " + i));
    }
    builder = new StringBuilder();
    boolean check = action.doCheck(builder);
    assertFalse(builder.toString(), check);
  }

  private void createMonth(final int ...months) {
    for (int month : months) {
      repository.create(Key.create(Month.TYPE, month));
    }
  }

  public void testErrorOnSeriesBudget() throws Exception {
    createMonth(200906,200907,200908,200909,200910,200911,200912,201001,201002);
    repository.create(Series.TYPE, FieldValue.value(Series.NAME, "series name"),
                      FieldValue.value(Series.BUDGET_AREA, 0),
                      FieldValue.value(Series.FIRST_MONTH, 200906),
                      FieldValue.value(Series.LAST_MONTH, 201001),
                      FieldValue.value(Series.DAY, 1));
    doCheck("Missing SeriesBudget ");
  }

  public void testMissingSeries() throws Exception {
    createMonth(200906, 200907, 200908, 200909, 200910);
    repository.create(SeriesBudget.TYPE, FieldValue.value(SeriesBudget.ID, 123),
                      FieldValue.value(SeriesBudget.MONTH, 200907),
                      FieldValue.value(SeriesBudget.DAY, 1),
                      FieldValue.value(SeriesBudget.SERIES, 1233),
                      FieldValue.value(SeriesBudget.AMOUNT, 9.));
    doCheck("Missing series 123");
  }

  private void doCheck(final String expectedError) {
    DataCheckerAction checkerAction = new DataCheckerAction(repository, directory);
    StringBuilder builder = new StringBuilder();
    assertTrue(checkerAction.doCheck(builder));
    String s = builder.toString();
    assertTrue(s, s.contains(expectedError));
    assertFalse(checkerAction.doCheck(new StringBuilder()));
  }
}
