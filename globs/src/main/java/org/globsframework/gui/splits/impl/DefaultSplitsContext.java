package org.globsframework.gui.splits.impl;

import org.globsframework.gui.splits.IconLocator;
import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.TextLocator;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.exceptions.SplitsException;
import org.globsframework.gui.splits.font.FontLocator;
import org.globsframework.gui.splits.styles.StyleService;
import org.globsframework.utils.exceptions.ItemNotFound;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DefaultSplitsContext implements SplitsContext {
  private Map<String, Component> componentsByName = new HashMap<String, Component>();
  private Map<String, Action> actionsByName = new HashMap<String, Action>();
  private ColorService colorService;
  private IconLocator iconLocator;
  private TextLocator textLocator;
  private FontLocator fontLocator;
  private StyleService styleService;
  private Class referenceClass;
  private String resourceFile;
  private java.util.List<Component> createdComponents = new ArrayList<Component>();
  private java.util.List<AutoHideListener> autoHideListeners = new ArrayList<AutoHideListener>();

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

  public String getResourceFile() {
    return resourceFile;
  }

  public void setResourceFile(String resourceFile) {
    this.resourceFile = resourceFile;
  }

  public void addComponent(String id, Component component) {
    if (componentsByName.containsKey(id)) {
      throw new SplitsException("Component '" + id + "' already declared in the context" +
                                dump());
    }
    addOrReplaceComponent(id, component);
  }

  public void addOrReplaceComponent(String id, Component component) {
    componentsByName.put(id, component);
  }

  public <T extends Component> T findOrCreateComponent(String ref, String name, Class<T> componentClass)
    throws SplitsException {

    if (ref != null) {
      if (name != null) {
        throw new SplitsException("A component referenced with a 'ref' cannot be given a 'name' attribute" +
                                  dump());
      }

      Component component = componentsByName.get(ref);
      if (component != null) {
        return (T)component;
      }
      else {
        throw new SplitsException("No component registered with name '" + ref +
                                  "' - available names: " + componentsByName.keySet() + dump());
      }
    }

    try {
      Constructor constructor = componentClass.getConstructor();
      Component newComponent = (Component)constructor.newInstance();
      createdComponents.add(newComponent);
      if (name != null) {
        newComponent.setName(name);
        componentsByName.put(name, newComponent);
      }
      return (T)newComponent;
    }
    catch (Exception e) {
      throw new SplitsException("Could not invoke empty constructor of class " + componentClass.getName() +
                                dump(), e);
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
      throw new SplitsException("No action registered for id '" + id + "'" + dump());
    }
    return action;
  }

  public void addAutoHide(Component targetComponent, String sourceComponentName) {
    autoHideListeners.add(new AutoHideListener(targetComponent, sourceComponentName));
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
    for (AutoHideListener listener : autoHideListeners) {
      listener.dispose(this);
    }
    autoHideListeners.clear();
  }

  public void complete() {
    for (AutoHideListener listener : autoHideListeners) {
      listener.init(this);
    }
  }

  public String dump() {
    StringBuilder builder = new StringBuilder();
    builder
      .append("\n\n")
      .append("Context")
      .append("\n")
      .append("  Class: ").append(referenceClass)
      .append(" - file: ").append(resourceFile)
      .append("\n");

    builder.append("  Components:").append("\n");
    for (Map.Entry<String, Component> componentEntry : componentsByName.entrySet()) {
      builder.append("    ").append(componentEntry.getKey())
        .append(" => ").append(componentEntry.getValue())
        .append("\n");
    }

    builder.append("  Actions:").append("\n");
    for (Map.Entry<String, Action> actionEntry : actionsByName.entrySet()) {
      builder.append("    ").append(actionEntry.getKey())
        .append(" => ").append(actionEntry.getValue())
        .append("\n");
    }

    return builder.toString();
  }

  private static class AutoHideListener {
    private Component targetComponent;
    private String sourceComponentName;
    protected ComponentAdapter componentListener;

    private AutoHideListener(Component targetComponent, String sourceComponentName) {
      this.targetComponent = targetComponent;
      this.sourceComponentName = sourceComponentName;
    }

    public void init(SplitsContext context) {
      Component sourceComponent = getSourceComponent(context);
      componentListener = createListener();
      sourceComponent.addComponentListener(componentListener);
      targetComponent.setVisible(sourceComponent.isVisible());
    }

    public void dispose(SplitsContext context) {
      if (componentListener != null) {
        Component sourceComponent = getSourceComponent(context);
        sourceComponent.removeComponentListener(componentListener);
      }
    }

    private Component getSourceComponent(SplitsContext context) {
      Component sourceComponent = context.findComponent(sourceComponentName);
      if (sourceComponent == null) {
        throw new ItemNotFound("References autoHideSource component '" + sourceComponentName + "' does not exist");
      }
      return sourceComponent;
    }

    private ComponentAdapter createListener() {
      return new ComponentAdapter() {
        public void componentShown(ComponentEvent e) {
          targetComponent.setVisible(true);
        }

        public void componentHidden(ComponentEvent e) {
          targetComponent.setVisible(false);
        }
      };
    }
  }
}
