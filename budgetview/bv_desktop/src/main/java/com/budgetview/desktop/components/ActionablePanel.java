package com.budgetview.desktop.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ActionablePanel extends JPanel {

  private boolean rolloverInProgress = false;

  public void setActionListener(final ActionListener listener) {

    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

    addMouseListener(new MouseAdapter() {
      public void mouseEntered(MouseEvent e) {
        rolloverInProgress = true;
        repaint();
      }

      public void mouseExited(MouseEvent e) {
        rolloverInProgress = false;
        repaint();
      }

      public void mousePressed(MouseEvent e) {
        if (!e.isConsumed()) {
          listener.actionPerformed(new ActionEvent(ActionablePanel.this, 0, "action"));
          e.consume();
        }
      }
    });
  }

  protected boolean isRolloverInProgress() {
    return rolloverInProgress;
  }
}
