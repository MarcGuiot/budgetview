package com.budgetview.functests.utils;

public abstract class RestartTestCase extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    resetWindow();
    setCurrentDate(getCurrentDate());
    setInMemory(false);
    setDeleteLocalPrevayler(true);
    super.setUp();
    setDeleteLocalPrevayler(false);
  }

  protected abstract String getCurrentDate();

  protected void tearDown() throws Exception {
    resetWindow();
    super.tearDown();
  }
}
