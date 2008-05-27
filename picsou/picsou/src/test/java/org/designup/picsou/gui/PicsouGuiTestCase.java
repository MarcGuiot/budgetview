package org.designup.picsou.gui;

import org.crossbowlabs.globs.gui.SelectionService;
import org.crossbowlabs.globs.metamodel.GlobModel;
import org.crossbowlabs.globs.model.format.DescriptionService;
import org.uispec4j.UISpec4J;
import org.uispec4j.assertion.Assertion;
import org.uispec4j.assertion.UISpecAssert;
import org.designup.picsou.gui.model.PicsouGuiModel;
import org.designup.picsou.gui.utils.PicsouColors;
import org.designup.picsou.gui.utils.PicsouDescriptionService;
import org.designup.picsou.utils.PicsouTestCase;

public abstract class PicsouGuiTestCase extends PicsouTestCase {

  static {
    UISpec4J.init();
  }

  protected void setUp() throws Exception {
    super.setUp();
    directory.add(SelectionService.class, new SelectionService());
    directory.add(DescriptionService.class, new PicsouDescriptionService());
    PicsouColors.registerColorService(directory);
  }

  protected GlobModel getModel() {
    return PicsouGuiModel.get();
  }

  protected void assertTrue(Assertion assertion) {
    UISpecAssert.assertTrue(assertion);
  }

  protected void assertFalse(Assertion assertion) {
    UISpecAssert.assertFalse(assertion);
  }
}
