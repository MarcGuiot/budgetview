package org.designup.picsou.gui.utils.dev;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ThrowExceptionAction extends AbstractAction {
  public ThrowExceptionAction() {
    super("[Throw exception]");
  }

  public void actionPerformed(ActionEvent e) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        throw new RuntimeException("Exception test");
      }
    });
  }
}
