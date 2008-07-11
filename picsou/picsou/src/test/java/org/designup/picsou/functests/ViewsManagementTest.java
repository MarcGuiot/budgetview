package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.utils.Lang;
import org.uispec4j.finder.ComponentMatchers;

import javax.swing.*;
import java.awt.*;

public class ViewsManagementTest extends LoggedInFunctionalTestCase {
  public void testHomePage() throws Exception {
    views.selectHome();
    views.assertHomeSelected();
    transactions.assertVisible(false);
  }

  public void testNoData() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2005/03/10", -10, "rent", MasterCategory.HOUSE)
      .addTransaction("2005/01/10", +50, "income")
      .load();

    transactions.assertVisible(true);
    categories.assertVisible(true);

    categories.select(MasterCategory.BANK);
    categories.assertVisible(true);
    transactions.assertVisible(false);
    checkMessage(Lang.get("noData"));

    categories.selectNone();
    categories.assertVisible(true);
    transactions.assertVisible(false);
    checkMessage(Lang.get("noData"));

    categories.select(MasterCategory.HOUSE);
    categories.assertVisible(true);
    transactions.assertVisible(true);

    periods.selectCells("2005/02");
    categories.assertVisible(true);
    transactions.assertVisible(false);

    periods.selectCells("2005/03");
    categories.assertVisible(true);
    transactions.assertVisible(true);
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
