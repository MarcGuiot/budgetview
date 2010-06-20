package org.globsframework.gui.splits.utils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AutoDispose {
  public static void registerComboSelection(final JComboBox comboBox,
                                            final Disposable disposable) {
    comboBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (comboBox.getSelectedIndex() > -1) {
          disposable.dispose();
          comboBox.removeActionListener(this);
        }
      }
    });
  }
}
