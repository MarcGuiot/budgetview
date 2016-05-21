package com.budgetview.gui;

import com.budgetview.gui.startup.components.OpenRequestManager;
import com.budgetview.gui.utils.ApplicationColors;
import com.budgetview.utils.PicsouTestCase;
import com.budgetview.gui.description.PicsouDescriptionService;
import com.budgetview.gui.model.PicsouGuiModel;
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
    ApplicationColors.registerColorService(directory);
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
