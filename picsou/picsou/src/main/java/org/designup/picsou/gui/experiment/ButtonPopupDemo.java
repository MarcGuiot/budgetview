package org.designup.picsou.gui.experiment;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class ButtonPopupDemo {
  private static Component selectedComponent;

  public static void main(String[] args) {
    JFrame frame = new JFrame();

    final JPopupMenu colorMenu = new JPopupMenu("Color");
    colorMenu.add(makeMenuItem("Red"));
    colorMenu.add(makeMenuItem("Green"));
    colorMenu.add(makeMenuItem("Blue"));
    colorMenu.add(new JCheckBoxMenuItem("Blah"));

    MouseListener mouseListener = new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        checkPopup(e);
      }

      public void mouseClicked(MouseEvent e) {
        checkPopup(e);
      }

      public void mouseReleased(MouseEvent e) {
        checkPopup(e);
      }

      private void checkPopup(MouseEvent e) {
        selectedComponent = e.getComponent();
        colorMenu.show(e.getComponent(), 0, selectedComponent.getHeight());
      }
    };

    frame.setLayout(new FlowLayout());
    JButton button = new JButton("Uno");
    button.addMouseListener(mouseListener);
    frame.add(button);
    button = new JButton("Due");
    button.addMouseListener(mouseListener);
    frame.add(button);
    button = new JButton("Tre");
    button.addMouseListener(mouseListener);
    frame.add(button);

    frame.setSize(200, 50);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setVisible(true);
  }

  private static JMenuItem makeMenuItem(final String label) {
    JMenuItem item = new JMenuItem(label);
    item.addActionListener(new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        if (label.equals("Red")) {
          selectedComponent.setForeground(Color.red);
        }
        else if (label.equals("Green")) {
          selectedComponent.setForeground(Color.green);
        }
        else if (label.equals("Blue")) {
          selectedComponent.setForeground(Color.blue);
        }
      }
    });
    return item;
  }
}
