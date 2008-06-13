package org.crossbowlabs.splits.layout;

import javax.swing.*;
import java.awt.*;

public class SwingStretches {
  public static final double NULL_WEIGHT = 0.1;
  public static final double NORMAL_WEIGHT = 1.0;
  public static final double LARGE_WEIGHT = 10.0;

  public static ComponentStretch get(Component component) {

    if (component instanceof JTable) {
      return new ComponentStretch(component, Fill.BOTH, Anchor.CENTER,
                                  LARGE_WEIGHT, LARGE_WEIGHT);
    }

    if (component instanceof JList) {
      return new ComponentStretch(component, Fill.BOTH, Anchor.CENTER,
                                  NORMAL_WEIGHT, LARGE_WEIGHT);
    }

    if (component instanceof JTree) {
      return new ComponentStretch(component, Fill.BOTH, Anchor.CENTER,
                                  NORMAL_WEIGHT, LARGE_WEIGHT);
    }

    if ((component instanceof JTextField)
        || (component instanceof JCheckBox)) {
      return new ComponentStretch(component, Fill.HORIZONTAL, Anchor.WEST,
                                  NORMAL_WEIGHT, NULL_WEIGHT);
    }

    if ((component instanceof JComboBox)
        || (component instanceof AbstractButton)
        || (component instanceof JSpinner)) {
      return new ComponentStretch(component, Fill.HORIZONTAL, Anchor.CENTER,
                                  NORMAL_WEIGHT, NULL_WEIGHT);
    }

    if ((component instanceof JLabel)) {
      return new ComponentStretch(component, Fill.NONE, Anchor.CENTER,
                                  NULL_WEIGHT, NULL_WEIGHT);
    }

    if (component instanceof JTabbedPane) {
      return new ComponentStretch(component, Fill.BOTH, Anchor.CENTER,
                                  LARGE_WEIGHT, LARGE_WEIGHT);
    }

    return new ComponentStretch(component, Fill.BOTH, Anchor.CENTER,
                                NORMAL_WEIGHT, NORMAL_WEIGHT);
  }
}
