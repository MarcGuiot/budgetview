package org.globsframework.gui.splits.splitters;

import org.globsframework.gui.splits.Splitter;
import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.SplitProperties;
import org.globsframework.gui.splits.SplitsNode;
import org.globsframework.gui.splits.impl.DefaultSplitsNode;
import org.globsframework.gui.splits.layout.ComponentConstraints;
import org.globsframework.gui.splits.layout.Fill;
import org.globsframework.gui.splits.layout.Anchor;

import javax.swing.*;
import java.awt.*;

public class Header implements Splitter {
  private SplitProperties properties;
  private Splitter[] subSplitters;

  protected Header(SplitProperties properties, Splitter[] subSplitters) {
    this.properties = properties;
    this.subSplitters = subSplitters;
  }

  public Splitter[] getHeaderSplitters() {
    return subSplitters;
  }

  public String getName() {
    return "header";
  }

  public SplitComponent createComponentStretch(SplitsContext context, boolean addMargin) {
    final JPanel panel = new JPanel();
    final ComponentConstraints constraints = new ComponentConstraints(panel, Fill.BOTH, Anchor.CENTER, 0, 0);
    SplitsNode<JPanel> node = new DefaultSplitsNode<JPanel>(panel, context);
    return new SplitComponent(constraints, node);
  }

  public Insets getMarginInsets() {
    return null;
  }

  public SplitProperties getProperties() {
    return properties;
  }
}
