package org.globsframework.gui.splits.impl;

import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.exceptions.SplitsException;
import org.globsframework.gui.splits.repeat.RepeatHandler;
import org.globsframework.utils.exceptions.ItemNotFound;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractSplitsContext implements SplitsContext {
  private Map<String, Component> componentsByName = new HashMap<String, Component>();
  private Map<String, Action> actionsByName = new HashMap<String, Action>();
  protected java.util.List<Component> createdComponents = new ArrayList<Component>();
  private String resourceFile;
  private java.util.List<AutoHideListener> autoHideListeners = new ArrayList<AutoHideListener>();
  private Map<String, RepeatHandler> repeats = new HashMap<String, RepeatHandler>();
  private Map<JLabel, String> labelForAssociations = new HashMap<JLabel, String>();

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

  public <T extends Component> T findOrCreateComponent(String ref, String name, Class<T> componentClass, String splitterName)
    throws SplitsException {

    if (ref != null) {
      if (name != null) {
        throw new SplitsException("Error for tag: " + splitterName + ", ref: " + ref + ", name: " + name +
                                  "- a component referenced with a 'ref' cannot be given a 'name' attribute" +
                                  dump());
      }

      Component component = componentsByName.get(ref);
      if (component == null) {
        throw new SplitsException("Error for tag: " + splitterName + " - no component registered with name '" + ref +
                                  "' - available names: " + componentsByName.keySet() + dump());
      }

      if (!componentClass.isAssignableFrom(component.getClass())) {
        throw new SplitsException("Error for tag: " + splitterName +
                                  " - unexpected type '" + component.getClass().getSimpleName() +
                                  "' for referenced component '" + ref + "' - expected type: " + componentClass.getName()
                                  + dump());
      }
      if (component.getName() == null) {
        component.setName(ref);
      }
      return (T)component;
    }

    try {
      Component newComponent = componentClass.newInstance();
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

  public String dump() {
    StringBuilder builder = new StringBuilder();
    builder
      .append("\n\n")
      .append("Context")
      .append("\n")
      .append("  Class: ").append(getReferenceClass())
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

  public String getResourceFile() {
    return resourceFile;
  }

  public void setResourceFile(String resourceFile) {
    this.resourceFile = resourceFile;
  }

  public void cleanUp() {
    for (AutoHideListener listener : autoHideListeners) {
      listener.dispose(this);
    }
    autoHideListeners.clear();
    labelForAssociations.clear();
  }

  public void dispose() {
    cleanUp();
    for (RepeatHandler repeatHandler : repeats.values()) {
      repeatHandler.dispose();
    }
  }

  public void addAutoHide(Component targetComponent, String sourceComponentName) {
    autoHideListeners.add(new AutoHideListener(targetComponent, sourceComponentName));
  }

  public void addLabelFor(JLabel label, String componentName) {
    labelForAssociations.put(label, componentName);
  }

  public void complete() {
    for (AutoHideListener listener : autoHideListeners) {
      listener.init(this);
    }
    for (Map.Entry<JLabel, String> association : labelForAssociations.entrySet()) {
      JLabel label = association.getKey();
      String ref = association.getValue();
      Component targetComponent = componentsByName.get(ref);
      if (targetComponent == null) {
        throw new SplitsException("Label '"+ label.getText() + "' references an unknown component '" + ref + "'" + dump());
      }
      label.setLabelFor(targetComponent);
    }
  }

  public RepeatHandler getRepeat(String name) {
    return repeats.get(name);
  }

  public void addRepeat(String name, RepeatHandler repeatHandler) {
    repeats.put(name, repeatHandler);
  }

  protected static class AutoHideListener {
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
