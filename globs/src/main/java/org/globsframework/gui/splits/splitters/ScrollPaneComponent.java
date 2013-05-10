package org.globsframework.gui.splits.splitters;

import org.globsframework.gui.splits.SplitProperties;
import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.Splitter;
import org.globsframework.gui.splits.SplitsNode;
import org.globsframework.gui.splits.ui.UIService;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.color.ColorUpdater;
import org.globsframework.gui.splits.color.Colors;
import org.globsframework.gui.splits.color.utils.BackgroundColorUpdater;
import org.globsframework.gui.splits.exceptions.SplitsException;
import org.globsframework.gui.splits.layout.ComponentConstraints;
import org.globsframework.gui.splits.layout.SingleComponentLayout;
import org.globsframework.utils.Strings;
import org.globsframework.utils.exceptions.InvalidParameter;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.ScrollBarUI;
import java.awt.*;

public class ScrollPaneComponent extends AbstractSplitter {

  protected ScrollPaneComponent(SplitProperties properties, Splitter[] subSplitters) {
    super(properties, subSplitters);
    if (subSplitters.length != 1) {
      throw new SplitsException("scrollPane must have exactly one subcomponent");
    }
  }

  protected SplitComponent createRawStretch(SplitsContext context) {
    ComponentConstraints subConstraints = getSubSplitters()[0].createComponentStretch(context, true).componentConstraints;
    SplitsNode<JScrollPane> splitsNode = findOrCreateComponent(context);
    JScrollPane scrollPane = splitsNode.getComponent();
    boolean forceVerticalScroll = Boolean.TRUE.equals(properties.getBoolean("forceVerticalScroll"));
    if (forceVerticalScroll) {
      VerticalScrollPanel panel = new VerticalScrollPanel(subConstraints.getComponent());
      scrollPane.setViewportView(panel);
    }
    else {
      scrollPane.setViewportView(subConstraints.getComponent());
    }

    ComponentConstraints constraints = new ComponentConstraints(splitsNode.getComponent(),
                                                    subConstraints.getFill(),
                                                    subConstraints.getAnchor(),
                                                    subConstraints.getWeightX(),
                                                    subConstraints.getWeightY());

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

    String uiName = properties.getString("scrollbarUI");
    if (uiName != null) {
      scrollPane.getVerticalScrollBar().setUI(getScrollbarUI(context, uiName));
      scrollPane.getHorizontalScrollBar().setUI(getScrollbarUI(context, uiName));
    }

    return new SplitComponent(constraints, splitsNode);
  }

  private ScrollBarUI getScrollbarUI(SplitsContext context, String uiName) {
    ComponentUI ui = context.getService(UIService.class).getUI(uiName, context);
    if (!ScrollBarUI.class.isInstance(ui)) {
      throw new SplitsException("'"+ uiName +"' should be a subclass of ScrollBarUI" +
                                context.dump());
    }
    return (ScrollBarUI)ui;
  }

  protected void complete(Component component) {
    super.complete(component);
    JScrollPane scroll = (JScrollPane)component;
    GuiUtils.scrollToTop(scroll);
  }

  protected SplitsNode<JScrollPane> findOrCreateComponent(SplitsContext context) {
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
                        "forceVerticalScroll", "scrollbarUI"};
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
