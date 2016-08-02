package com.budgetview.functests.utils;

import com.budgetview.desktop.Application;
import org.globsframework.utils.Files;

import java.io.File;

public abstract class StartUpFunctionalTestCase extends FunctionalTestCase {
  protected void setUp() throws Exception {
    super.setUp();
    LoggedInFunctionalTestCase.resetWindow();
    System.setProperty(Application.LOCAL_PREVAYLER_PATH_PROPERTY, FunctionalTestCase.getUrl());
    Files.deleteWithSubtree(new File(FunctionalTestCase.getUrl()));
  }
}
