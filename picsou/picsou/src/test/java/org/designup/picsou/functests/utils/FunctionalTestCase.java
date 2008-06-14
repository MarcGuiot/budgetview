package org.designup.picsou.functests.utils;

import org.globsframework.utils.Log;
import org.globsframework.utils.TestUtils;
import org.uispec4j.UISpecTestCase;

import java.util.Locale;

public abstract class FunctionalTestCase extends UISpecTestCase {

  static {
    TestUtils.clearTmpDir();
    Locale.setDefault(Locale.ENGLISH);
  }

  protected void setUp() throws Exception {
    Locale.setDefault(Locale.ENGLISH);
    Log.reset();
  }
}
