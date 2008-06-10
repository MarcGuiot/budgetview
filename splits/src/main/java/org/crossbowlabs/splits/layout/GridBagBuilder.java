package org.crossbowlabs.splits.layout;

import javax.swing.*;
import java.awt.*;

public class GridBagBuilder {
  private JPanel panel;
  private GridBagConstraints constraints = new GridBagConstraints();

  public static GridBagBuilder init(JPanel panel) {
    return new GridBagBuilder(panel);
  }

  public static GridBagBuilder init() {
    return new GridBagBuilder(new JPanel());
  }

  public static Container createSingleCell(Component component, Insets insets) {
    return setSingleCell(new JPanel(), component, insets);
  }

  public static JPanel setSingleCell(JPanel containingPanel, Component component) {
    return setSingleCell(containingPanel, component, new Insets(0, 0, 0, 0));
  }

  public static JPanel setSingleCell(JPanel containingPanel, Component component, Insets insets) {
    containingPanel.removeAll();
    return init(containingPanel)
      .add(component,
           0, 0, 1, 1, 1.0, 1.0,
           Fill.BOTH, Anchor.CENTER,
           insets)
      .setOpaque(false)
      .getPanel();
  }

  public GridBagBuilder(JPanel panel) {
    this.panel = panel;
    panel.setLayout(new GridBagLayout());
  }

  public GridBagBuilder add(Component component,
                  int gridx, int gridy,
                  int gridwidth, int gridheight,
                  Insets insets) {
    ComponentStretch stretch = SwingStretches.get(component);
    add(component,
        gridx, gridy,
        gridwidth, gridheight,
        stretch.getWeightX(), stretch.getWeightY(),
        stretch.getFill(), stretch.getAnchor(),
        insets);
    return this;
  }

  public GridBagBuilder add(Component component,
                            int gridx, int gridy,
                            int gridwidth, int gridheight,
                            double weightx, double weighty,
                            Fill fill, Anchor anchor) {
    return add(component, gridx, gridy, gridwidth, gridheight, weightx, weighty, fill, anchor, null);
  }

  public GridBagBuilder add(Component component,
                            int gridx, int gridy,
                            int gridwidth, int gridheight,
                            double weightx, double weighty,
                            Fill fill, Anchor anchor,
                            Insets insets) {
    constraints.gridx = gridx;
    constraints.gridy = gridy;
    constraints.gridwidth = gridwidth;
    constraints.gridheight = gridheight;
    constraints.fill = fill.getValue();
    constraints.anchor = anchor.getValue();
    constraints.weightx = weightx;
    constraints.weighty = weighty;
    if (insets != null) {
      constraints.insets = insets;
    }

    panel.add(component, constraints);
    return this;
  }

  public GridBagBuilder setOpaque(boolean opaque) {
    panel.setOpaque(opaque);
    return this;
  }

  public GridBagBuilder setBackground(Color color) {
    panel.setBackground(color);
    return this;
  }

  public JPanel getPanel() {
    return panel;
  }
}
