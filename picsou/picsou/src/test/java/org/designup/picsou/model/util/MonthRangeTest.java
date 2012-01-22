package org.designup.picsou.model.util;

import junit.framework.Assert;
import junit.framework.TestCase;

public class MonthRangeTest extends TestCase {
  public void test() throws Exception {
    MonthRange range1 = new MonthRange(201106, 201206);

    check(new MonthRange(201108, 201206),
          range1.intersection(new MonthRange(201108, 201206)));

    check(new MonthRange(201106, 201206),
          range1.intersection(new MonthRange(201104, 201208)));

    check(new MonthRange(201108, 201204),
          range1.intersection(new MonthRange(201108, 201204)));

    check(new MonthRange(201112, 201112),
          range1.intersection(new MonthRange(201112, 201112)));
  }

  private void check(MonthRange expected, MonthRange actual) {
    if (!expected.equals(actual)) {
      Assert.fail("Actual: " + actual);
    }
  }

}
