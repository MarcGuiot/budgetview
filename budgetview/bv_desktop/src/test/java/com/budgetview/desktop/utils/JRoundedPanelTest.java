package com.budgetview.desktop.utils;

import com.budgetview.desktop.components.JRoundedPanel;
import org.globsframework.gui.splits.utils.GuiUtils;

import javax.swing.*;
import java.awt.*;

public class JRoundedPanelTest {
  public static void main(String[] args) {
    JRoundedPanel panel = new JRoundedPanel(10, 10, 5, 0.7f);
    panel.setTitle("Identification", new Font("Arial", Font.PLAIN, 24));

    JPanel mainPanel = new JPanel();
    mainPanel.setOpaque(false);
    mainPanel.setLayout(new BorderLayout());
    mainPanel.add(panel, BorderLayout.CENTER);
    mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    mainPanel.setPreferredSize(new Dimension(500, 400));
    GuiUtils.showCentered(mainPanel);
  }
}
