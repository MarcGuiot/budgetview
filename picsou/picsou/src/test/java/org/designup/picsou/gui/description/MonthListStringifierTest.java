package org.designup.picsou.gui.description;

import junit.framework.TestCase;
import org.designup.picsou.model.Month;
import org.globsframework.model.FieldValue;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.GlobRepositoryBuilder;

public abstract class MonthListStringifierTest extends TestCase {
  private MonthListStringifier stringifier = new MonthListStringifier();

  public void test() throws Exception {
    checkResult("January 2008", 200801);
    checkResult("January - February 2008", 200801, 200802);
    checkResult("January and March 2008", 200801, 200803);
  }

  public void testRanges() throws Exception {
    checkResult("January - February 2008", 200801, 200802);
    checkResult("September 2008 to March 2009", 200809, 200810, 200811, 200812, 200901, 200902, 200903);
  }

  public void testWholeYears() throws Exception {
    checkResult("2008",
                200801, 200802, 200803, 200804, 200805, 200806,
                200807, 200808, 200809, 200810, 200811, 200812);

    checkResult("2008 - 2009",
                200801, 200802, 200803, 200804, 200805, 200806,
                200807, 200808, 200809, 200810, 200811, 200812,
                200901, 200902, 200903, 200904, 200905, 200906,
                200907, 200908, 200909, 200910, 200911, 200912);

    checkResult("2008 to 2010",
                200801, 200802, 200803, 200804, 200805, 200806,
                200807, 200808, 200809, 200810, 200811, 200812,
                200901, 200902, 200903, 200904, 200905, 200906,
                200907, 200908, 200909, 200910, 200911, 200912,
                201001, 201002, 201003, 201004, 201005, 201006,
                201007, 201008, 201009, 201010, 201011, 201012);
  }

  public void testRandomSelection() throws Exception {
    checkResult("Selected months", 200801, 200803, 200804, 200805);
  }

  private void checkResult(String expected, int... monthIds) {
    GlobRepository repository = GlobRepositoryBuilder.init().get();
    for (int monthId : monthIds) {
      repository.create(Month.TYPE, FieldValue.value(Month.ID, monthId));
    }
    assertEquals(expected, stringifier.toString(repository.getAll(Month.TYPE), repository));
  }
}
