package org.designup.picsou.gui.components;

import org.designup.picsou.utils.Lang;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class CloseDialogAction extends AbstractAction {
  private JDialog dialog;

  public CloseDialogAction(String key, JDialog dialog) {
    super(Lang.get(key));
    this.dialog = dialog;
  }

  public CloseDialogAction(JDialog dialog) {
    this("close", dialog);
  }

  public void actionPerformed(ActionEvent e) {
    dialog.setVisible(false);
  }
}
