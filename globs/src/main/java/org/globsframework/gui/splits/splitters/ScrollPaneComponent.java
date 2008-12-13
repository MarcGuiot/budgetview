package org.globsframework.gui.splits.splitters;

import org.globsframework.gui.splits.SplitProperties;
import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.Splitter;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.color.ColorUpdater;
import org.globsframework.gui.splits.color.Colors;
import org.globsframework.gui.splits.color.utils.BackgroundColorUpdater;
import org.globsframework.gui.splits.exceptions.SplitsException;
import org.globsframework.gui.splits.layout.ComponentStretch;
import org.globsframework.gui.splits.layout.SingleComponentLayout;
import org.globsframework.utils.Strings;
import org.globsframework.utils.exceptions.InvalidParameter;

import javax.swing.*;
import java.awt.*;

public class ScrollPaneComponent extends AbstractSplitter {

  protected ScrollPaneComponent(SplitProperties properties, Splitter[] subSplitters) {
    super(properties, subSplitters);
    if (subSplitters.length != 1) {
      throw new SplitsException("scrollPane must have exactly one subcomponent");
    }
  }

  protected ComponentStretch createRawStretch(SplitsContext context) {
    ComponentStretch subStretch = getSubSplitters()[0].createComponentStretch(context, true);
    JScrollPane scrollPane = findOrCreateComponent(context);
    boolean forceVerticalScroll = Boolean.TRUE.equals(properties.getBoolean("forceVerticalScroll"));
    if (forceVerticalScroll) {
      VerticalScrollPanel panel = new VerticalScrollPanel(subStretch.getComponent());
      scrollPane.setViewportView(panel);
    }
    else {
      scrollPane.setViewportView(subStretch.getComponent());
    }

    ComponentStretch stretch = new ComponentStretch(scrollPane,
                                                    subStretch.getFill(),
                                                    subStretch.getAnchor(),
                                                    subStretch.getWeightX(),
                                                    subStretch.getWeightY());

    String bg = properties.getString("viewportBackground");
    if (Strings.isNotEmpty(bg)) {
      if (Colors.isHexaString(bg)) {
        scrollPane.getViewport().setBackground(Colors.toColor(bg));
      }
      else {
        ColorUpdater updater = new BackgroundColorUpdater(bg, scrollPane.getViewport());
        updater.install(context.getService(ColorService.class));
        context.addDisposable(updater);
      }
    }

    Boolean viewportOpaque = properties.getBoolean("viewportOpaque");
    if (viewportOpaque != null) {
      scrollPane.getViewport().setOpaque(viewportOpaque);
    }

    Integer verticalUnitIncrement = properties.getInt("verticalUnitIncrement");
    if (verticalUnitIncrement != null) {
      scrollPane.getVerticalScrollBar().setUnitIncrement(verticalUnitIncrement);
    }

    Integer horizontalUnitIncrement = properties.getInt("horizontalUnitIncrement");
    if (horizontalUnitIncrement != null) {
      if (forceVerticalScroll) {
        throw new SplitsException("horizontalUnitIncrement cannot be set when forceVerticalScroll is set to true" +
                                  context.dump());
      }
      scrollPane.getHorizontalScrollBar().setUnitIncrement(horizontalUnitIncrement);
    }

    String horizontalPolicy = properties.getString("horizontalScrollbarPolicy");
    if (horizontalPolicy != null) {
      if (forceVerticalScroll) {
        throw new SplitsException("horizontalScrollbarPolicy cannot be set when forceVerticalScroll is set to true" +
                                  context.dump());
      }
      scrollPane.setHorizontalScrollBarPolicy(getHorizontalPolicy(horizontalPolicy));
    }

    String verticalPolicy = properties.getString("verticalScrollbarPolicy");
    if (verticalPolicy != null) {
      scrollPane.setVerticalScrollBarPolicy(getVerticalPolicy(verticalPolicy));
    }

    return stretch;
  }

  protected JScrollPane findOrCreateComponent(SplitsContext context) {
    String ref = properties.getString("ref");
    String componentName = properties.getString("name");
    return context.findOrCreateComponent(ref, componentName, JScrollPane.class, getName());
  }

  public String getName() {
    return "scrollPane";
  }

  protected String[] getExcludedParameters() {
    return new String[]{"viewportBackground", "viewportOpaque",
                        "verticalUnitIncrement", "horizontalUnitIncrement",
                        "verticalScrollbarPolicy", "horizontalScrollbarPolicy",
                        "forceVerticalScroll"};
  }

  private int getHorizontalPolicy(String policy) {
    if (policy.equalsIgnoreCase("always")) {
      return ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS;
    }
    if (policy.equalsIgnoreCase("asNeeded")) {
      return ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;
    }
    if (policy.equalsIgnoreCase("never")) {
      return ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
    }
    throw new InvalidParameter("Invalid horizontal scrollbar policy: " + policy);
  }

  private int getVerticalPolicy(String policy) {
    if (policy.equalsIgnoreCase("always")) {
      return ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS;
    }
    if (policy.equalsIgnoreCase("asNeeded")) {
      return ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;
    }
    if (policy.equalsIgnoreCase("never")) {
      return ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER;
    }
    throw new InvalidParameter("Invalid vertical scrollbar policy: " + policy);
  }

  private static class VerticalScrollPanel extends JPanel implements Scrollable {

    private VerticalScrollPanel(Component innerComponent) {
      super(new SingleComponentLayout());
      add(innerComponent);
      setOpaque(false);
    }

    public Dimension getPreferredScrollableViewportSize() {
      return getPreferredSize();
    }

    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
      return 10;
    }

    public boolean getScrollableTracksViewportHeight() {
      return false;
    }

    public boolean getScrollableTracksViewportWidth() {
      return true;
    }

    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
      return 10;
    }
  }

}
