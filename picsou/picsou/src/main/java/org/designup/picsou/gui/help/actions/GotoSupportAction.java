package org.designup.picsou.gui.help.actions;

import org.designup.picsou.gui.browsing.BrowsingAction;
import org.designup.picsou.gui.browsing.BrowsingService;
import org.designup.picsou.utils.Lang;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class GotoSupportAction extends BrowsingAction {

  public GotoSupportAction(Directory directory) {
    super(Lang.get("gotoSupport"), directory);
  }

  protected String getUrl() {
    return Lang.get("site.support.url");
  }
}
