package org.designup.picsou.gui;

import org.designup.picsou.gui.description.PicsouDescriptionService;
import org.designup.picsou.gui.model.PicsouGuiModel;
import org.designup.picsou.gui.startup.OpenRequestManager;
import org.designup.picsou.gui.utils.PicsouColors;
import org.designup.picsou.utils.PicsouTestCase;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.ui.UIService;
import org.globsframework.metamodel.GlobModel;
import org.globsframework.model.format.DescriptionService;
import org.uispec4j.UISpec4J;
import org.uispec4j.assertion.Assertion;
import org.uispec4j.assertion.UISpecAssert;

public abstract class PicsouGuiTestCase extends PicsouTestCase {

  static {
    UISpec4J.init();
  }

  protected void setUp() throws Exception {
    super.setUp();
    directory.add(SelectionService.class, new SelectionService());
    directory.add(DescriptionService.class, new PicsouDescriptionService());
    directory.add(OpenRequestManager.class, new OpenRequestManager());
    directory.add(new UIService());
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
