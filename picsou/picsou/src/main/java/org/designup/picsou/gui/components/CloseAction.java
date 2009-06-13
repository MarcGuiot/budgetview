package org.designup.picsou.gui.components;

import org.designup.picsou.utils.Lang;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class CloseAction extends AbstractAction {
  private PicsouDialog dialog;

  public CloseAction(PicsouDialog dialog) {
    super(Lang.get("close"));
    this.dialog = dialog;
  }

  public void actionPerformed(ActionEvent e) {
    dialog.setVisible(false);
  }
}
