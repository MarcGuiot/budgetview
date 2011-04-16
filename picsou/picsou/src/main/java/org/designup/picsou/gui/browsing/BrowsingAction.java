package org.designup.picsou.gui.browsing;

import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public abstract class BrowsingAction extends AbstractAction {
  private BrowsingService browser;

  protected BrowsingAction(Directory directory) {
    this.browser = directory.get(BrowsingService.class);
  }

  protected BrowsingAction(String name, Directory directory) {
    super(name);
    this.browser = directory.get(BrowsingService.class);
  }

  public void actionPerformed(ActionEvent actionEvent) {
    browser.launchBrowser(getUrl());
  }

  protected abstract String getUrl();
}
