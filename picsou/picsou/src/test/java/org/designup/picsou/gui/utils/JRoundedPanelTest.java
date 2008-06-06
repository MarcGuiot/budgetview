package org.designup.picsou.gui.utils;

import org.crossbowlabs.splits.utils.GuiUtils;
import org.designup.picsou.gui.components.JRoundedPanel;

import javax.swing.*;
import java.awt.*;

public class JRoundedPanelTest {
  public static void main(String[] args) {
    JRoundedPanel panel = new JRoundedPanel(10, 10, 5, 0.7f);
    panel.setTitle("Identification", new Font("Arial", Font.PLAIN, 24));

//    JPanel loginPanel = new LoginPanel("", new LoginPanel.Launcher() {
//      public void run(GlobRepository globRepository, Directory directory, File initialFile) throws Exception {
//        JOptionPane.showMessageDialog(null, "Ca dechire pas sa race, ca ?");
//      }
//    }).getJPanel();
//    loginPanel.setOpaque(false);
//    panel.add(loginPanel);

    JPanel mainPanel = new JPanel();
    mainPanel.setOpaque(false);
    mainPanel.setLayout(new BorderLayout());
    mainPanel.add(panel, BorderLayout.CENTER);
    mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    mainPanel.setPreferredSize(new Dimension(500, 400));
    GuiUtils.show(mainPanel);
  }
}
