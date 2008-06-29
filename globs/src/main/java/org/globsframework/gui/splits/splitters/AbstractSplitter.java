package org.globsframework.gui.splits.splitters;

import org.globsframework.gui.splits.SplitProperties;
import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.Splitter;
import org.globsframework.gui.splits.exceptions.SplitsException;
import org.globsframework.gui.splits.layout.Anchor;
import org.globsframework.gui.splits.layout.ComponentStretch;
import org.globsframework.gui.splits.layout.Fill;
import org.globsframework.gui.splits.layout.GridBagBuilder;
import org.globsframework.gui.splits.utils.DoubleOperation;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.gui.splits.utils.PropertySetter;
import org.globsframework.gui.splits.utils.SplitsUtils;
import org.globsframework.utils.Strings;
import org.globsframework.utils.Utils;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public abstract class AbstractSplitter implements Splitter {
  protected ComponentStretch stretch;
  private final Splitter[] subSplitters;
  private final SplitProperties properties;
  private final SplitsContext context;
  private static final String[] DEFAULT_EXCLUDES =
    {"ref", "styleClass", "autoHideSource", "gridPos", "opaque", "borderPos",
     "fill", "anchor", "weightX", "weightY",
     "margin", "marginTop", "marginBottom", "marginLeft", "marginRight"};

  protected AbstractSplitter(SplitProperties properties, Splitter[] subSplitters, SplitsContext context) {
    this.properties = properties;
    this.subSplitters = subSplitters;
    this.context = context;
  }

  public final ComponentStretch getComponentStretch(boolean addMargin) {
    if (stretch != null) {
      return stretch;
    }

    stretch = createRawStretch();
    setGridPos(stretch);
    overrideStretch(stretch);
    Component component = stretch.getComponent();
    setOpacity(component);
    if (addMargin) {
      addMargin(stretch);
    }
    complete(component);
    processAttributes(component);
    processAutoHide(component);
    processDebug(stretch);
    return stretch;
  }

  private void processAutoHide(Component component) {
    String source = properties.getString("autoHideSource");
    if (Strings.isNullOrEmpty(source)) {
      return;
    }
    context.addAutoHide(component, source);
  }

  private void processDebug(ComponentStretch stretch) {
    if (!GuiUtils.isDebugModeEnabled()) {
      return;
    }

    Component component = stretch.getComponent();
    if (!(component instanceof JComponent)) {
      return;
    }

    JComponent jComponent = (JComponent)component;

    StringBuilder builder = new StringBuilder();
    builder.append("<html>");
    builder.append("<p> <b>").append(getName());
    String componentName = component.getName();
    if ((componentName != null) && (componentName.length() > 0)) {
      builder.append(" <font color='red'>").append(componentName).append("</font>");
    }
    builder.append("</b></p>");
    java.util.List<String> propertyNames = new ArrayList<String>();
    propertyNames.addAll(Arrays.asList(properties.getPropertyNames()));
    propertyNames.remove("ref");
    Collections.sort(propertyNames);
    if (propertyNames.size() > 0) {
      builder.append("<hr><table border='0'>");
      for (String property : propertyNames) {
        builder
          .append("<tr>")
          .append("<td><b>")
          .append(property)
          .append("</b></td><td>")
          .append(properties.getString(property))
          .append("</tr>");
      }
      builder.append("</table>");
    }
    builder.append("</html>");

    jComponent.setToolTipText(builder.toString());

    jComponent.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.RED));
  }

  protected void complete(Component component) {
  }

  protected abstract ComponentStretch createRawStretch();

  public Splitter[] getSubSplitters() {
    return subSplitters;
  }

  public SplitProperties getProperties() {
    return properties;
  }

  public SplitsContext getContext() {
    return context;
  }

  private void setGridPos(ComponentStretch stretch) {
    String value = properties.getString("gridPos");
    if (value != null) {
      stretch.setGridPos(SplitsUtils.parseGridPos(value));
    }
  }

  private void overrideStretch(ComponentStretch stretch) {
    String fill = properties.getString("fill", "defaultFill");
    if (fill != null) {
      stretch.setFill(Utils.toEnum(Fill.class, fill));
    }

    String anchor = properties.getString("anchor", "defaultAnchor");
    if (anchor != null) {
      stretch.setAnchor(Anchor.get(anchor));
    }

    Double weightX = properties.getDouble("weightX");
    if (weightX != null) {
      stretch.setWeightX(weightX);
    }

    Double weightY = properties.getDouble("weightY");
    if (weightY != null) {
      stretch.setWeightY(weightY);
    }
  }

  protected String[] getExcludedParameters() {
    return new String[0];
  }

  private void addMargin(ComponentStretch innerComponent) {
    Insets insets = getMarginInsets();
    if (insets != null) {
      innerComponent.setComponent(GridBagBuilder.createSingleCell(innerComponent.getComponent(), insets));
    }
  }

  public Insets getMarginInsets() {
    Integer margin = properties.getInt("margin", "defaultMargin");
    Integer marginTop = properties.getInt("marginTop", "defaultMarginTop");
    Integer marginBottom = properties.getInt("marginBottom", "defaultMarginBottom");
    Integer marginLeft = properties.getInt("marginLeft", "defaultMarginLeft");
    Integer marginRight = properties.getInt("marginRight", "defaultMarginRight");
    if ((margin == null)
        && (marginTop == null) && (marginBottom == null)
        && (marginLeft == null) && (marginRight == null)) {
      return null;
    }

    return new Insets(toInt(marginTop, margin),
                      toInt(marginLeft, margin),
                      toInt(marginBottom, margin),
                      toInt(marginRight, margin));
  }

  private void setOpacity(Component component) {
    Boolean opaque = properties.getBoolean("opaque");
    if (opaque == null) {
      return;
    }

    if (!(component instanceof JComponent)) {
      throw new SplitsException("Opacity cannot be set on " + component.getClass().getSimpleName());
    }

    JComponent jComponent = (JComponent)component;
    jComponent.setOpaque(opaque);
  }

  private int toInt(Integer value, Integer defaultValue) {
    if (value != null) {
      return value;
    }
    return defaultValue != null ? defaultValue : 0;
  }

  private void processAttributes(Component component) {
    java.util.List<String> toExclude = new ArrayList<String>();
    toExclude.addAll(Arrays.asList(DEFAULT_EXCLUDES));
    toExclude.addAll(Arrays.asList(getExcludedParameters()));
    PropertySetter.process(component, properties, context, toExclude.toArray(new String[toExclude.size()]));
  }

  protected ComponentStretch createContainerStretch(Component container, DoubleOperation operation) {
    double weightX = 0;
    double weightY = 0;
    for (Splitter splitter : getSubSplitters()) {
      ComponentStretch stretch = splitter.getComponentStretch(false);
      weightX = operation.get(stretch.getWeightX(), weightX);
      weightY = operation.get(stretch.getWeightY(), weightY);
    }
    return new ComponentStretch(container, Fill.BOTH, Anchor.CENTER, weightX, weightY);
  }

  public String toString() {
    return getName() + " " + properties;
  }
}