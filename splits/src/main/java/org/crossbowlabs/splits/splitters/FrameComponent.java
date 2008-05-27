package org.crossbowlabs.splits.splitters;

import org.crossbowlabs.splits.SplitProperties;
import org.crossbowlabs.splits.SplitsContext;
import org.crossbowlabs.splits.Splitter;
import org.crossbowlabs.splits.layout.ComponentStretch;

import javax.swing.*;
import java.awt.*;

public class FrameComponent extends DefaultComponent<JFrame> {

  public FrameComponent(SplitProperties properties, Splitter[] subSplitters, SplitsContext context) {
    super(JFrame.class, "frame", context, properties, subSplitters, true);
  }

  protected void processComponent(JFrame frame, SplitProperties properties, SplitsContext context) {
    ComponentStretch panelStretch = Sequence.createPanel(getSubSplitters(), Sequence.Direction.VERTICAL);
    Component panel = panelStretch.getComponent();
    frame.setContentPane((Container)panel);
  }

  protected void complete(Component component) {
    JFrame frame = (JFrame)component;
    frame.pack();
  }
}
