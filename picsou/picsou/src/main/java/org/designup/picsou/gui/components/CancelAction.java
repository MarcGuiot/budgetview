package org.designup.picsou.gui.components;

import org.designup.picsou.utils.Lang;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class CancelAction extends AbstractAction {
  private PicsouDialog dialog;

  public CancelAction(PicsouDialog dialog) {
    this(Lang.get("cancel"), dialog);
  }

  public CancelAction(String label, PicsouDialog dialog) {
    super(label);
    this.dialog = dialog;
  }

  public void actionPerformed(ActionEvent e) {
    dialog.setVisible(false);
  }
}