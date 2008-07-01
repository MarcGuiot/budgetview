package org.globsframework.gui.splits.splitters;

import org.globsframework.gui.splits.SplitProperties;
import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.Splitter;
import org.globsframework.gui.splits.layout.ComponentStretch;

import javax.swing.*;
import java.awt.*;

public class FrameComponent extends DefaultComponent<JFrame> {

  public FrameComponent(SplitProperties properties, Splitter[] subSplitters) {
    super(JFrame.class, "frame", properties, subSplitters, true);
  }

  protected void postCreateComponent(JFrame component, SplitsContext context) {
    ComponentStretch panelStretch = Sequence.createPanel(getSubSplitters(), Sequence.Direction.VERTICAL, context);
    Component panel = panelStretch.getComponent();
    component.setContentPane((Container)panel);
  }

  protected void complete(Component component) {
    JFrame frame = (JFrame)component;
    frame.pack();
  }
}
