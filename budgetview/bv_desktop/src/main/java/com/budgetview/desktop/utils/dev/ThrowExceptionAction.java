package com.budgetview.desktop.utils.dev;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ThrowExceptionAction extends AbstractAction {
  public static final String LABEL = "Throw exception";

  public ThrowExceptionAction() {
    super(LABEL);
  }

  public void actionPerformed(ActionEvent e) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        throw new RuntimeException("Exception test");
      }
    });
  }
}
