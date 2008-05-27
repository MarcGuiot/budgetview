package org.designup.picsou.gui.sandbox;

import org.designup.picsou.gui.utils.FadingSwapper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class FadingTests {
  public static void main(String[] args) {
    final JPanel bottom = new JPanel();
    final JPanel above = new JPanel();
    JFrame frame = new JFrame();
    final FadingSwapper swapper = FadingSwapper.init(frame);

    bottom.setLayout(new BorderLayout());
    JLabel label = new JLabel() {
      protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        super.paintComponent(g);
      }
    };
    label.setFont(new Font("Arial", Font.BOLD, 150));
    label.setText("Cool...");
    label.setHorizontalAlignment(JLabel.CENTER);
    bottom.add(label, BorderLayout.CENTER);
    bottom.add(new JButton(new AbstractAction("Fade again!") {
      public void actionPerformed(ActionEvent e) {
        swapper.swapTo(above);
      }
    }), BorderLayout.NORTH);

    above.setLayout(new FlowLayout());
    JButton fadeButton = new JButton(new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        swapper.swapTo(bottom);
      }
    });
    fadeButton.setText("Fade !");
    above.add(fadeButton);

    frame.setContentPane(above);
    frame.setPreferredSize(new Dimension(800, 600));
    frame.pack();
    frame.show();
  }
}
