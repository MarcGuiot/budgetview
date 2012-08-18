package org.designup.picsou.functests.utils;

import org.designup.picsou.functests.utils.FunctionalTestCase;
import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.globsframework.utils.Files;

import java.io.File;

public abstract class StartUpFunctionalTestCase extends FunctionalTestCase {
  protected void setUp() throws Exception {
    super.setUp();
    LoggedInFunctionalTestCase.resetWindow();
    System.setProperty(PicsouApplication.LOCAL_PREVAYLER_PATH_PROPERTY, FunctionalTestCase.getUrl());
    Files.deleteSubtree(new File(FunctionalTestCase.getUrl()));
  }
}