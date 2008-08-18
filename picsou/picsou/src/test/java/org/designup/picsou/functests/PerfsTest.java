package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.MasterCategory;
import org.globsframework.utils.Utils;
import org.uispec4j.utils.Chrono;

public abstract class PerfsTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    super.setUp();
    OfxBuilder builder = OfxBuilder
      .init(this)
      .addBankAccount(30003, 12345, "10101010", 1.23, "2006/01/30");
    for (int m = 0; m < 10; m++) {
      for (int i = 0; i < 200; i++) {
        builder.addTransaction("2006/0" + m + "/10", Utils.randomInt(2000) - 1000, "Blah", getRandomCategory());
      }
    }
    builder.load();

    System.out.println("PerfsTest.test: START");

  }

  // Reference ~= 7400
  public void testPeriodSelections() throws Exception {
    Chrono chrono = Chrono.start();
    for (int i = 0; i < 100; i++) {
      for (int m = 0; m < 10; m++) {
        timeline.selectCell(m);
      }
      System.out.println("Round " + i + " done in " + chrono.getElapsedTime());
    }
    System.out.println("==> " + chrono.getElapsedTime());
  }

  // Reference ~= 13200
  public void testPeriodAndCategoriesSelections() throws Exception {
    Chrono chrono = Chrono.start();
    for (int i = 0; i < 12; i++) {
      for (int m = 0; m < 10; m++) {
        timeline.selectCell(m);
        for (MasterCategory category : MasterCategory.values()) {
          categories.select(category);
        }
      }
      System.out.println("Round " + i + " done in " + chrono.getElapsedTime());
    }
    System.out.println("==> " + chrono.getElapsedTime());
  }

  // Reference ~= 13250
  public void testCategoryAllocation() throws Exception {
    Chrono chrono = Chrono.start();
    for (int i = 0; i < 5; i++) {
      for (int row = 0; row < 5; row++) {
        transactions.assignOccasionalSeries(MasterCategory.FOOD, row);
      }
      for (int row = 0; row < 5; row++) {
        transactions.assignOccasionalSeries(MasterCategory.NONE, row);
      }
      System.out.println("Round " + i + " done in " + chrono.getElapsedTime());
    }
    System.out.println("==> " + chrono.getElapsedTime());
  }

  private MasterCategory getRandomCategory() {
    MasterCategory[] cats = MasterCategory.values();
    int index = 2 + Utils.randomInt((cats.length - 3));
    return cats[index];
  }
}
