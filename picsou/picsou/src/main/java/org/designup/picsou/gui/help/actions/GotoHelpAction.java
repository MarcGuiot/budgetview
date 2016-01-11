package org.designup.picsou.gui.help.actions;

import org.designup.picsou.gui.browsing.BrowsingAction;
import org.designup.picsou.utils.Lang;
import org.globsframework.utils.directory.Directory;

public class GotoHelpAction extends BrowsingAction {

  private String helpKey;

  public GotoHelpAction(String helpKey, Directory directory) {
    super(Lang.get("gotoSupport"), directory);
    this.helpKey = helpKey;
  }

  protected String getUrl() {
    return Lang.get(helpKey);
  }
}
