package org.designup.picsou.gui.feedback.actions;

import org.designup.picsou.gui.browsing.BrowsingAction;
import org.designup.picsou.utils.Lang;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class TwitterAction extends BrowsingAction {
  public TwitterAction(Directory directory) {
    super(directory);
    putValue(Action.SHORT_DESCRIPTION, Lang.get("feedback.twitter.tooltip"));
  }

  protected String getUrl() {
    return Lang.get("feedback.twitter.url");
  }
}
