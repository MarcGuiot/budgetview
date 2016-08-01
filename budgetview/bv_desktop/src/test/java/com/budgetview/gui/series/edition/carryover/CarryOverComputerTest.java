package com.budgetview.gui.series.edition.carryover;

import junit.framework.TestCase;
import org.globsframework.utils.TestUtils;

import java.util.List;

public class CarryOverComputerTest extends TestCase {

  public void testNothingLeft() throws Exception {
    CarryOverComputer computer = new CarryOverComputer(201104, 200.00, 200.00);
    assertFalse(computer.hasNext());
  }
  
  public void testPositiveRemainder() throws Exception {
    CarryOverComputer computer = new CarryOverComputer(201104, 100.00, 200.00);
    assertFalse(computer.next(0.00, 200.00));
    assertTrue(computer.canCarryOver());
    checkNewPlanned(computer,
                    100.00, 300.00);
  }

  public void testPositiveRemainderWithNegativePlannedAsNextMonth() throws Exception {
    CarryOverComputer computer = new CarryOverComputer(201104, 100.00, 200.00);
    assertFalse(computer.next(0.00, -50.00));
    assertTrue(computer.canCarryOver());
    checkNewPlanned(computer,
                    100.00, 50.00);
  }

  public void testPositiveOverrun() throws Exception {
    CarryOverComputer computer = new CarryOverComputer(201104, 500.00, 200.00);
    assertTrue(computer.next(0.00, 200.00));
    assertFalse(computer.next(0.00, 200.00));
    assertTrue(computer.canCarryOver());
    checkNewPlanned(computer,
                    500.00, 0.00, 100.00);
  }

  public void testPositiveOverrunWithNegativePlannedInNextMonths() throws Exception {
    CarryOverComputer computer = new CarryOverComputer(201104, 500.00, 200.00);
    assertTrue(computer.next(0.00, 0.00));
    assertTrue(computer.next(0.00, 200.00));
    assertTrue(computer.next(0.00, -50.00));
    assertFalse(computer.next(0.00, 200.00));
    assertTrue(computer.canCarryOver());
    checkNewPlanned(computer,
                    500.00, 0.00, 0.00, -50.00, 100.00);
  }

  public void testPositiveOverrunWithActualInNextMonth() throws Exception {
    CarryOverComputer computer = new CarryOverComputer(201104, 500.00, 200.00);
    assertTrue(computer.next(0.00, 0.00));
    assertTrue(computer.next(100.00, 300.00));
    assertTrue(computer.next(0.00, -50.00));
    assertFalse(computer.next(-50.00, 200.00));
    assertTrue(computer.canCarryOver());
    checkNewPlanned(computer,
                    500.00, 0.00, 100.00, -50.00, 100.00);
  }

  public void testCannotCarryOverPositiveOverrun() throws Exception {
    CarryOverComputer computer = new CarryOverComputer(201104, 500.00, 200.00);
    assertTrue(computer.next(0.00, 0.00));
    assertTrue(computer.next(0.00, 0.00));
    assertTrue(computer.next(0.00, 0.00));
    assertFalse(computer.canCarryOver());
    checkNewPlanned(computer,
                    500.00, 0.00, 0.00, 0.00);
  }

  public void testNegativeRemainder() throws Exception {
    CarryOverComputer computer = new CarryOverComputer(201104, -100.00, -200.00);
    assertFalse(computer.next(0.00, -200.00));
    assertTrue(computer.canCarryOver());
    checkNewPlanned(computer,
                    -100.00, -300.00);
  }

  public void testNegativeRemainderWithPositiveValues() throws Exception {
    CarryOverComputer computer = new CarryOverComputer(201104, -100.00, -200.00);
    assertFalse(computer.next(0.00, +50.00));
    assertTrue(computer.canCarryOver());
    checkNewPlanned(computer,
                    -100.00, -50.00);
  }

  public void testNegativeOverrun() throws Exception {
    CarryOverComputer computer = new CarryOverComputer(201104, -300.00, -200.00);
    assertFalse(computer.next(0.00, -200.00));
    assertTrue(computer.canCarryOver());
    checkNewPlanned(computer,
                    -300.00, -100.00);
  }

  public void testNegativeOverrunOverTwoMonths() throws Exception {
    CarryOverComputer computer = new CarryOverComputer(201104, -500.00, -200.00);
    assertTrue(computer.next(0.00, -200.00));
    assertFalse(computer.next(0.00, -200.00));
    assertTrue(computer.canCarryOver());
    checkNewPlanned(computer,
                    -500.00, 0.00, -100.00);
  }

  public void testNegativeOverrunWithPositivePlannedInNextMonths() throws Exception {
    CarryOverComputer computer = new CarryOverComputer(201104, -500.00, -200.00);
    assertTrue(computer.next(0.00, 0.00));
    assertTrue(computer.next(0.00, -200.00));
    assertTrue(computer.next(0.00, +50.00));
    assertFalse(computer.next(0.00, -200.00));
    assertTrue(computer.canCarryOver());
    checkNewPlanned(computer,
                    -500.00, 0.00, 0.00, 50.00, -100.00);
  }

  public void testNegativeOverrunWithActualInNextMonths() throws Exception {
    CarryOverComputer computer = new CarryOverComputer(201104, -500.00, -200.00);
    assertTrue(computer.next(0.00, 0.00));
    assertTrue(computer.next(-100.00, -300.00));
    assertTrue(computer.next(0.00, +50.00));
    assertFalse(computer.next(+50.00, -200.00));
    assertTrue(computer.canCarryOver());
    checkNewPlanned(computer,
                    -500.00, 0.00, -100.00, 50.00, -100.00);
  }

  public void testForceSingleMonthWithNegativeValue() throws Exception {
    CarryOverComputer computer = new CarryOverComputer(201104, -500.00, -200.00);
    computer.forceSingleMonth(0.00, -100.00);
    assertTrue(computer.canCarryOver());
    checkNewPlanned(computer,
                    -300.00, 0.00);
  }

  public void testForceSingleMonthWithActual() throws Exception {
    CarryOverComputer computer = new CarryOverComputer(201104, -500.00, -200.00);
    computer.forceSingleMonth(-50.00, -150.00);
    assertTrue(computer.canCarryOver());
    checkNewPlanned(computer,
                    -300.00, -50.00);
  }

  public void testForceSingleMonthWithPositiveValue() throws Exception {
    CarryOverComputer computer = new CarryOverComputer(201104, 500.00, 200.00);
    computer.forceSingleMonth(0.00, 100.00);
    assertTrue(computer.canCarryOver());
    checkNewPlanned(computer,
                    300.00, 0.00);
  }

  private void checkNewPlanned(CarryOverComputer computer, double... expectedPlanned) {
    List<CarryOver> result = computer.getResult();
    double[] actual = new double[result.size()];
    int i = 0;
    for (CarryOver carryOver : result) {
      actual[i++] = carryOver.getNewPlanned();
    }

    TestUtils.assertEquals(actual, expectedPlanned);
  }
}
