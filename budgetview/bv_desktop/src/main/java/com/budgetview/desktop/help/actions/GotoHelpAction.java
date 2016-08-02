package com.budgetview.desktop.help.actions;

import com.budgetview.desktop.browsing.BrowsingAction;
import com.budgetview.utils.Lang;
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
