package org.globsframework.gui.splits.splitters;

import org.globsframework.gui.splits.SplitProperties;
import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.Splitter;
import org.globsframework.gui.splits.exceptions.SplitsException;
import org.globsframework.gui.splits.layout.*;
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
  private final Splitter[] subSplitters;
  protected final SplitProperties properties;
  private static final String[] DEFAULT_EXCLUDES =
    {"ref", "styleClass", "autoHideSource", "gridPos", "borderPos",
     "fill", "anchor", "weightX", "weightY",
     "margin", "marginTop", "marginBottom", "marginLeft", "marginRight"};

  protected AbstractSplitter(SplitProperties properties, Splitter[] subSplitters) {
    this.properties = properties;
    this.subSplitters = subSplitters;
  }

  public final SplitComponent createComponentStretch(SplitsContext context, boolean addMargin) {
    SplitComponent splitComponent = createRawStretch(context);
    setGridPos(splitComponent.componentConstraints);
    overrideStretch(splitComponent.componentConstraints);
    Component component = splitComponent.componentConstraints.getComponent();
//    setOpacity(component);
    if (addMargin) {
      addMargin(splitComponent.componentConstraints);
    }
    else {
      splitComponent.componentConstraints.setInsets(getMarginInsets());
    }
    complete(component);
    processAttributes(component, context);
    processAutoHide(component, context);
    processDebug(splitComponent.componentConstraints, context);
    return splitComponent;
  }

  private void processAutoHide(Component component, SplitsContext context) {
    String source = properties.getString("autoHideSource");
    if (Strings.isNullOrEmpty(source)) {
      return;
    }
    context.addAutoHide(component, source);
  }

  private void processDebug(ComponentConstraints constraints, SplitsContext context) {
    if (!GuiUtils.isDebugModeEnabled()) {
      return;
    }

    Component component = constraints.getComponent();
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

  protected abstract SplitComponent createRawStretch(SplitsContext context);

  public Splitter[] getSubSplitters() {
    return subSplitters;
  }

  public SplitProperties getProperties() {
    return properties;
  }

  private void setGridPos(ComponentConstraints constraints) {
    String value = properties.getString("gridPos");
    if (value != null) {
      constraints.setGridPos(SplitsUtils.parseGridPos(value));
    }
  }

  private void overrideStretch(ComponentConstraints constraints) {
    String fill = properties.getString("fill", "defaultFill");
    if (fill != null) {
      constraints.setFill(Utils.toEnum(Fill.class, fill));
    }

    String anchor = properties.getString("anchor", "defaultAnchor");
    if (anchor != null) {
      constraints.setAnchor(Anchor.get(anchor));
    }

    Double weightX = properties.getDouble("weightX");
    if (weightX != null) {
      constraints.setWeightX(weightX);
    }

    Double weightY = properties.getDouble("weightY");
    if (weightY != null) {
      constraints.setWeightY(weightY);
    }
  }

  protected String[] getExcludedParameters() {
    return new String[0];
  }

  private void addMargin(ComponentConstraints innerComponent) {
    Insets insets = getMarginInsets();
    if (insets != null) {
      innerComponent.setComponent(SingleComponentPanels.create(innerComponent.getComponent(), insets));
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

  private void processAttributes(Component component, SplitsContext context) {
    java.util.List<String> toExclude = new ArrayList<String>();
    toExclude.addAll(Arrays.asList(DEFAULT_EXCLUDES));
    toExclude.addAll(Arrays.asList(getExcludedParameters()));
    PropertySetter.process(component, properties, context, toExclude.toArray(new String[toExclude.size()]));
  }

  public String toString() {
    return getName() + " " + properties;
  }
}