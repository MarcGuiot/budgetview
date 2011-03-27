package org.designup.picsou.gui.actions;

import org.designup.picsou.gui.browsing.BrowsingService;
import org.designup.picsou.utils.Lang;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class GotoSupportAction extends AbstractAction {

  private BrowsingService browser;

  public GotoSupportAction(Directory directory) {
    super(Lang.get("gotoSupport"));
    this.browser = directory.get(BrowsingService.class);
  }

  public void actionPerformed(ActionEvent actionEvent) {
    browser.launchBrowser("http://support.mybudgetview.fr");
  }
}
