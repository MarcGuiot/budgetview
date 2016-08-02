package com.budgetview.desktop.components.tips;

import net.java.balloontip.BalloonTip;
import net.java.balloontip.positioners.BalloonTipPositioner;
import net.java.balloontip.styles.BalloonTipStyle;

import javax.swing.*;
import javax.swing.event.AncestorListener;
import java.awt.event.ComponentListener;

public class CustomBalloonTip extends BalloonTip {

  public CustomBalloonTip(JComponent attachedComponent, String text) {
    super(attachedComponent, text);
  }

  public CustomBalloonTip(JComponent attachedComponent, String text, BalloonTipStyle style, boolean useCloseButton) {
    super(attachedComponent, text, style, useCloseButton);
  }

  public CustomBalloonTip(JComponent attachedComponent, String text, BalloonTipStyle style,
                          Orientation orientation, AttachLocation attachLocation,
                          int horizontalOffset,
                          int verticalOffset, boolean useCloseButton) {
    super(attachedComponent, new JLabel(text), style, orientation, attachLocation, horizontalOffset, verticalOffset, useCloseButton);
  }

  public CustomBalloonTip(JComponent attachedComponent, String text, BalloonTipStyle style, BalloonTipPositioner positioner, boolean useCloseButton) {
    super(attachedComponent, new JLabel(text), style, positioner, getDefaultCloseButton());
  }

  public void closeBalloon() {
    super.closeBalloon();
    if (topLevelContainer != null) {
      for (ComponentListener listener : topLevelContainer.getComponentListeners()) {
        if (listener.getClass().getName().startsWith("net.java.balloontip")) {
          topLevelContainer.removeComponentListener(listener);
        }
      }
    }
    if (attachedComponent != null) {
      for (AncestorListener listener : attachedComponent.getAncestorListeners()) {
        if (listener.getClass().getName().startsWith("net.java.balloontip")) {
          attachedComponent.removeAncestorListener(listener);
        }
      }
    }
  }
}
