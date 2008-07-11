package org.designup.picsou.gui.sandbox;

import org.uispec4j.UISpecTestCase;

import javax.swing.*;
import java.awt.*;

public abstract class ColumnLayoutManagerTest extends UISpecTestCase {

  public void testLayoutPreferseize() throws Exception {
    JPanel mainPanel = new JPanel();
    addPanel(mainPanel, 5, 10);
    addPanel(mainPanel, 10, 10);
    addPanel(mainPanel, 10, 10);
    addPanel(mainPanel, 10, 10);
    addPanel(mainPanel, 4, 10);
    mainPanel.setLayout(new ColumnLayoutManager(3));
    Dimension preferredSize = mainPanel.getPreferredSize();
    checkBetween(15, 25, preferredSize.getHeight());
    checkBetween(25, 35, preferredSize.getWidth());
  }

  private void checkBetween(int min, int max, double value) {
    assertTrue(value + " is more than " + max, value < max);
    assertTrue(value + " is less than " + min, value > min);
  }

  private void addPanel(JPanel mainPanel, int width, int height) {
    JPanel panel = new JPanel();
    panel.setPreferredSize(new Dimension(width, height));
    mainPanel.add(panel);
  }
}
