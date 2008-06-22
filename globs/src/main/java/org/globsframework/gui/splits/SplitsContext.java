package org.globsframework.gui.splits;

import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.font.FontLocator;
import org.globsframework.gui.splits.styles.StyleService;

import javax.swing.*;
import java.awt.*;

public interface SplitsContext {

  void addComponent(String id, Component component);

  Component findComponent(String id);

  <T extends Component> T findOrCreateComponent(String id, String name, Class<T> componentClass);

  void add(String name, Action action);

  Action getAction(String id);

  ColorService getColorService();

  IconLocator getIconLocator();

  TextLocator getTextLocator();

  FontLocator getFontLocator();

  StyleService getStyleService();

  Class getReferenceClass();
}
