package org.globsframework.gui.splits.layout;

import javax.swing.*;
import java.awt.*;

public class GridBagBuilder {

  private static final Insets NULL_INSETS = new Insets(0, 0, 0, 0);

  private JPanel panel;
  private GridBagConstraints constraints = new GridBagConstraints();
  private Insets defaultInsets = null;

  public static GridBagBuilder init(JPanel panel) {
    return new GridBagBuilder(panel);
  }

  public static GridBagBuilder init() {
    return new GridBagBuilder(new JPanel());
  }

  public GridBagBuilder(JPanel panel) {
    this.panel = panel;
    panel.setLayout(new GridBagLayout());
  }

  public GridBagBuilder setDefaultInsets(int top, int left, int bottom, int right) {
    this.defaultInsets = new Insets(top, left, bottom, right);
    return this;
  }

  public GridBagBuilder add(ComponentConstraints constraints,
                            int gridx, int gridy,
                            int gridwidth, int gridheight) {
    add(constraints.getComponent(),
        gridx, gridy,
        gridwidth, gridheight,
        constraints.getWeightX(), constraints.getWeightY(),
        constraints.getFill(), constraints.getAnchor(),
        constraints.getInsets());
    return this;
  }

  public GridBagBuilder add(Component component,
                            int gridx, int gridy,
                            int gridwidth, int gridheight,
                            Insets insets) {
    ComponentConstraints constraints = SwingStretches.get(component);
    constraints.setInsets(insets);
    return add(constraints, gridx, gridy, gridwidth, gridheight);
  }

  public GridBagBuilder add(Component component,
                            int gridx, int gridy) {
    return add(component, gridx, gridy, 1, 1, defaultInsets);
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
    else {
      constraints.insets = NULL_INSETS;
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
