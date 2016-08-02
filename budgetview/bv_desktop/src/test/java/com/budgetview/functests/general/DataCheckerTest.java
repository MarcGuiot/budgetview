package com.budgetview.functests.general;

import com.budgetview.functests.utils.LoggedInFunctionalTestCase;
import com.budgetview.desktop.Application;
import org.junit.Test;

public class DataCheckerTest extends LoggedInFunctionalTestCase {

  @Test
  public void testErrorDialog() throws Exception {
    operations.throwExceptionInApplication()
      .checkMessageContains("No error was found")
      .checkDetailsContain("Exception test") // TODO: mettre le message de l'exception
      .close();
  }

  @Test
  public void testExit() throws Exception {

    assertTrue(Application.EXIT_ON_DATA_ERROR);
    Application.EXIT_ON_DATA_ERROR = false;

    operations.throwExceptionInRepository()
      .checkMessageContains("Something just went wrong with BudgetView")
      .checkDetailsContain("Object user[id=0] already exists")
      .checkCopy()
      .close();

    Thread.sleep(50);
    Application.EXIT_ON_DATA_ERROR = true;
  }
}
