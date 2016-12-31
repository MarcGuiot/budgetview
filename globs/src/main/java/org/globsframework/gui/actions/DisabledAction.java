package org.globsframework.gui.actions;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class DisabledAction extends AbstractAction {

  public DisabledAction(String title) {
    super(title);
    setEnabled(false);
  }

  public DisabledAction() {
    setEnabled(false);
  }

  public void actionPerformed(ActionEvent e) {
  }
}
