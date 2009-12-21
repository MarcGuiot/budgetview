package org.designup.picsou.gui.browsing;

import org.globsframework.gui.splits.utils.GuiUtils;

import javax.swing.*;

public class DummyBrowsingService extends BrowsingService {
  public void launchBrowser(String url) {
    JDialog dialog = new JDialog();
    JLabel label = new JLabel(url);
    label.setName("url");
    dialog.add(label);
    dialog.pack();
    GuiUtils.showCentered(dialog);
  }
}
