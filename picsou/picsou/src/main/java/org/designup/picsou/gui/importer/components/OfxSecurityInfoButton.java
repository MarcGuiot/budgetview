package org.designup.picsou.gui.importer.components;

import org.designup.picsou.gui.components.tips.DetailsTip;
import org.designup.picsou.utils.Lang;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class OfxSecurityInfoButton {
  public static JButton create(final Directory directory) {
    final JButton button = new JButton();
    button.setToolTipText(Lang.get("synchro.security.info.tooltip"));
    button.addActionListener(new AbstractAction() {
      public void actionPerformed(ActionEvent actionEvent) {
        DetailsTip tip = new DetailsTip(button, Lang.get("synchro.security.info.message"), directory);
        tip.show();
      }
    });
    return button;
  }
}
