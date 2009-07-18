package org.designup.picsou.gui.components;

import org.designup.picsou.utils.Lang;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class CloseAction extends AbstractAction {
  private PicsouDialog dialog;

  public CloseAction(String key, PicsouDialog dialog) {
    super(Lang.get(key));
    this.dialog = dialog;
  }

  public CloseAction(PicsouDialog dialog) {
    this("close", dialog);
  }

  public void actionPerformed(ActionEvent e) {
    dialog.setVisible(false);
  }
}
