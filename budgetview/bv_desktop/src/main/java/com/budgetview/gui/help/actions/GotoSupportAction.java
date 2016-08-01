package com.budgetview.gui.help.actions;

import com.budgetview.gui.browsing.BrowsingAction;
import com.budgetview.utils.Lang;
import org.globsframework.utils.directory.Directory;

public class GotoSupportAction extends BrowsingAction {

  public GotoSupportAction(Directory directory) {
    super(Lang.get("gotoSupport"), directory);
  }

  protected String getUrl() {
    return Lang.get("site.support.url");
  }
}
