package com.budgetview.gui.components.dialogs;

import com.budgetview.utils.Lang;

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