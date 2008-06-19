package org.globsframework.gui.splits.impl;

import org.globsframework.gui.splits.IconLocator;
import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.TextLocator;
import org.globsframework.gui.splits.font.FontLocator;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.exceptions.SplitsException;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public class DefaultSplitsContext implements SplitsContext {
  private Map<String, Component> componentsByName = new HashMap<String, Component>();
  private Map<String, Action> actionsByName = new HashMap<String, Action>();
  private ColorService colorService;
  private IconLocator iconLocator;
  private TextLocator textLocator;
  private FontLocator fontLocator;

  public DefaultSplitsContext(ColorService colorService, IconLocator iconLocator,
                              TextLocator textLocator, FontLocator fontLocator) {
    this.colorService = colorService;
    this.iconLocator = iconLocator;
    this.textLocator = textLocator;
    this.fontLocator = fontLocator;
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

  public void addComponent(String id, Component component) {
    if (componentsByName.containsKey(id)) {
      throw new SplitsException("Component '" + id + "' already declared in the context");
    }
    componentsByName.put(id, component);
  }

  public <T extends Component> T findOrCreateComponent(String ref, String name, Class<T> componentClass)
    throws SplitsException {

    if (ref != null) {
      if (name != null) {
        throw new SplitsException("A component referenced with a 'ref' cannot be given a 'name' attribute");
      }

      Component component = componentsByName.get(ref);
      if (component != null) {
        return (T)component;
      }
      else {
        throw new SplitsException("No component registered with name '" + ref +
                                  "' - available names: " + componentsByName.keySet());
      }
    }

    try {
      Constructor constructor = componentClass.getConstructor();
      Component newComponent = (Component)constructor.newInstance();
      if (name != null) {
        newComponent.setName(name);
        componentsByName.put(name, newComponent);
      }
      return (T)newComponent;
    }
    catch (Exception e) {
      throw new SplitsException("Could not invoke empty constructor of class " + componentClass.getName(), e);
    }
  }

  public Component findComponent(String id) {
    return componentsByName.get(id);
  }

  public void add(String name, Action action) {
    actionsByName.put(name, action);
  }

  public Action getAction(String id) {
    Action action = actionsByName.get(id);
    if (action == null) {
      throw new SplitsException("No action registered for id '" + id + "'");
    }
    return action;
  }
}
