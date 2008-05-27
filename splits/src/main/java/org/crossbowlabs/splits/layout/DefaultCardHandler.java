package org.crossbowlabs.splits.layout;

import javax.swing.*;
import java.awt.*;

public class DefaultCardHandler implements CardHandler {
  private JPanel panel;
  private CardLayout layout;

  public static CardHandler init(JPanel panel) {
    CardLayout layout = new CardLayout();
    panel.setLayout(layout);
    return new DefaultCardHandler(panel, layout);
  }

  private DefaultCardHandler(JPanel panel, CardLayout layout) {
    this.panel = panel;
    this.layout = layout;
  }

  public void show(String cardName) {
    layout.show(panel, cardName);
  }
}
