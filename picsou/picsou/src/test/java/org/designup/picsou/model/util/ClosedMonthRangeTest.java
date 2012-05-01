package org.designup.picsou.model.util;

import junit.framework.Assert;
import junit.framework.TestCase;

public class ClosedMonthRangeTest extends TestCase {
  public void test() throws Exception {
    ClosedMonthRange range1 = new ClosedMonthRange(201106, 201206);

    check(new ClosedMonthRange(201108, 201206),
          range1.intersection(new ClosedMonthRange(201108, 201206)));

    check(new ClosedMonthRange(201106, 201206),
          range1.intersection(new ClosedMonthRange(201104, 201208)));

    check(new ClosedMonthRange(201108, 201204),
          range1.intersection(new ClosedMonthRange(201108, 201204)));

    check(new ClosedMonthRange(201112, 201112),
          range1.intersection(new ClosedMonthRange(201112, 201112)));
  }

  private void check(ClosedMonthRange expected, ClosedMonthRange actual) {
    if (!expected.equals(actual)) {
      Assert.fail("Actual: " + actual);
    }
  }

}
