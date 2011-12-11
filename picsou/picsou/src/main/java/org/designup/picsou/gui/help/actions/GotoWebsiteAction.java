package org.designup.picsou.gui.help.actions;

import org.designup.picsou.gui.browsing.BrowsingAction;
import org.designup.picsou.utils.Lang;
import org.globsframework.utils.directory.Directory;

public class GotoWebsiteAction extends BrowsingAction {

  public GotoWebsiteAction(Directory directory) {
    super(Lang.get("gotoWebsite"), directory);
  }

  public GotoWebsiteAction(String text, Directory directory) {
    super(text, directory);
  }

  protected String getUrl() {
    return Lang.get("site.url");
  }
}
