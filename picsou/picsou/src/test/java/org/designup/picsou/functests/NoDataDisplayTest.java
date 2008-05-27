package org.designup.picsou.functests;

import org.uispec4j.finder.ComponentMatchers;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.utils.Lang;

import javax.swing.*;
import java.awt.*;

public class NoDataDisplayTest extends LoggedInFunctionalTestCase {
  public void testNoData() throws Exception {
    categories.select(MasterCategory.BANK);
    checkMessage(Lang.get("noData"));

    categories.selectNone();
    checkMessage(Lang.get("noData"));
  }

  private void checkMessage(String reference) {
    Component component = mainWindow.getPanel("cardView").findSwingComponent(
      ComponentMatchers.and(
        ComponentMatchers.fromClass(JTextArea.class),
        ComponentMatchers.displayedNameSubstring(reference)
      )
    );
    assertTrue(component != null);
    assertTrue(component.isVisible());
  }
}
