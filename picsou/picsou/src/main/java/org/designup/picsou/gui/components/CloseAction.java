package org.designup.picsou.gui.components;

import org.designup.picsou.utils.Lang;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class CloseAction extends AbstractAction {
  private JDialog dialog;

  public CloseAction(String key, JDialog dialog) {
    super(Lang.get(key));
    this.dialog = dialog;
  }

  public CloseAction(JDialog dialog) {
    this("close", dialog);
  }

  public void actionPerformed(ActionEvent e) {
    dialog.setVisible(false);
  }
}
