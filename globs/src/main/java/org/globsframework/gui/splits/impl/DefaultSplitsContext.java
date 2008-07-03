package org.globsframework.gui.splits.impl;

import org.globsframework.gui.splits.IconLocator;
import org.globsframework.gui.splits.TextLocator;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.font.FontLocator;
import org.globsframework.gui.splits.styles.StyleService;

import java.awt.*;
import java.util.Collections;

public class DefaultSplitsContext extends AbstractSplitsContext {
  private ColorService colorService;
  private IconLocator iconLocator;
  private TextLocator textLocator;
  private FontLocator fontLocator;
  private StyleService styleService;
  private Class referenceClass;

  public DefaultSplitsContext(ColorService colorService, IconLocator iconLocator,
                              TextLocator textLocator, FontLocator fontLocator, StyleService styleService) {
    this.colorService = colorService;
    this.iconLocator = iconLocator;
    this.textLocator = textLocator;
    this.fontLocator = fontLocator;
    this.styleService = styleService;
  }

  public ColorService getColorService() {
    return colorService;
  }

  public IconLocator getIconLocator() {
    return iconLocator;
  }

  public TextLocator getTextLocator() {
    return textLocator;
  }

  public FontLocator getFontLocator() {
    return fontLocator;
  }

  public StyleService getStyleService() {
    return styleService;
  }

  public void setReferenceClass(Class referenceClass) {
    this.referenceClass = referenceClass;
  }

  public Class getReferenceClass() {
    return referenceClass;
  }

  public void cleanUp() {
    Collections.reverse(createdComponents);
    for (Component component : createdComponents) {
      Container parent = component.getParent();
      if (parent != null) {
        parent.remove(component);
      }
    }
    createdComponents.clear();
    super.cleanUp();
  }

}
