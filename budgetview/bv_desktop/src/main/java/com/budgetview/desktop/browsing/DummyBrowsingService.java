package com.budgetview.desktop.browsing;

import com.budgetview.desktop.components.dialogs.CloseDialogAction;
import org.globsframework.gui.splits.utils.GuiUtils;

import javax.swing.*;

public class DummyBrowsingService extends BrowsingService {
  public void launchBrowser(String url) {
    JDialog dialog = new JDialog();

    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

    JLabel label = new JLabel(url);
    label.setName("url");
    panel.add(label);

    panel.add(new JButton(new CloseDialogAction(dialog)));

    dialog.add(panel);
    dialog.pack();
    GuiUtils.showCentered(dialog);
  }
}
