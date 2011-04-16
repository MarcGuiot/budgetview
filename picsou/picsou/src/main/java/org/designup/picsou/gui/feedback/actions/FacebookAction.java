package org.designup.picsou.gui.feedback.actions;

import org.designup.picsou.gui.browsing.BrowsingAction;
import org.designup.picsou.utils.Lang;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class FacebookAction extends BrowsingAction {
  public FacebookAction(Directory directory) {
    super(directory);
    putValue(Action.SHORT_DESCRIPTION, Lang.get("feedback.facebook.tooltip"));
  }

  protected String getUrl() {
    return Lang.get("feedback.facebook.url");
  }
}
