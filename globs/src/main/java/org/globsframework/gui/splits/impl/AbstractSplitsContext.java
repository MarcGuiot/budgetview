package org.globsframework.gui.splits.impl;

import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.SplitsNode;
import org.globsframework.gui.splits.exceptions.SplitsException;
import org.globsframework.gui.splits.repeat.RepeatHandler;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.gui.splits.utils.OnLoadListener;
import org.globsframework.utils.exceptions.ItemNotFound;

import javax.swing.*;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.*;

public abstract class AbstractSplitsContext implements SplitsContext {
  private Map<String, SplitsNode<Component>> componentsByName = new HashMap<String, SplitsNode<Component>>();
  private Map<String, Action> actionsByName = new HashMap<String, Action>();
  protected java.util.List<Component> createdComponents = new ArrayList<Component>();
  private String resourceFile;
  private java.util.List<AutoHideListener> autoHideListeners = new ArrayList<AutoHideListener>();
  private Map<String, RepeatHandler> repeats = new HashMap<String, RepeatHandler>();
  private Map<JLabel, String> labelForAssociations = new HashMap<JLabel, String>();
  private Map<Disposable, Disposable> disposables = new HashMap<Disposable, Disposable>();
  private Map<String, HyperlinkListener> hyperlinkListenersByName = new HashMap<String, HyperlinkListener>();
  private java.util.List<OnLoadListener> onLoadListeners;

  public void addComponent(String id, SplitsNode<Component> component) {
    if (componentsByName.containsKey(id)) {
      throw new SplitsException("Component '" + id + "' already declared in the context" +
                                dump());
    }
    addOrReplaceComponent(id, component);
  }

  public void addOrReplaceComponent(String id, SplitsNode<Component> component) {
    componentsByName.put(id, component);
  }

  public <T extends Component> SplitsNode<T> findOrCreateComponent(String ref, String name, Class<T> componentClass, String splitterName)
    throws SplitsException {

    if (ref != null) {
      if (name != null) {
        throw new SplitsException("Error for tag: " + splitterName + ", ref: " + ref + ", name: " + name +
                                  "- a component referenced with a 'ref' cannot be given a 'name' attribute" +
                                  dump());
      }

      SplitsNode<Component> component = componentsByName.get(ref);
      if (component == null) {
        throw new SplitsException("Error for tag: " + splitterName + " - no component registered with ref='" + ref +
                                  "' - available names: " + componentsByName.keySet() + dump());
      }

      if (!componentClass.isAssignableFrom(component.getComponent().getClass())) {
        throw new SplitsException("Error for tag: " + splitterName +
                                  " - unexpected type '" + component.getComponent().getClass().getSimpleName() +
                                  "' for referenced component '" + ref + "' - expected type: " + componentClass.getName()
                                  + dump());
      }
      if (component.getComponent().getName() == null) {
        component.getComponent().setName(ref);
      }
      return (SplitsNode<T>)component;
    }

    try {
      T newComponent = componentClass.newInstance();
      DefaultSplitsNode<T> defaultNode = new DefaultSplitsNode<T>(newComponent, this);
      createdComponents.add(newComponent);
      if (name != null) {
        newComponent.setName(name);
        componentsByName.put(name, (SplitsNode<Component>)defaultNode);
      }
      return defaultNode;
    }
    catch (Exception e) {
      throw new SplitsException("Could not invoke empty constructor of class " + componentClass.getName() +
                                dump(), e);
    }
  }

  public SplitsNode findComponent(String id) {
    return componentsByName.get(id);
  }

  public void add(String id, Action action) {
    actionsByName.put(id, action);
  }

  public Action getAction(String id) {
    Action action = actionsByName.get(id);
    if (action == null) {
      throw new SplitsException("No action registered for id '" + id + "'" + dump());
    }
    return action;
  }

  public void add(String name, HyperlinkListener listener) {
    hyperlinkListenersByName.put(name, listener);
  }

  public HyperlinkListener getHyperlinkListener(String name) {
    HyperlinkListener listener = hyperlinkListenersByName.get(name);
    if (listener == null) {
      throw new SplitsException("No hyperlinkListener registered for name '" + name + "'" + dump());
    }
    return listener;
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
    for (Map.Entry<String, SplitsNode<Component>> componentEntry : componentsByName.entrySet()) {
      builder.append("    ").append(componentEntry.getKey())
        .append(" => ").append(componentEntry.getValue().getComponent())
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
    for (Disposable listener : disposables.values()) {
      listener.dispose();
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
    for (SplitsNode<Component> node : componentsByName.values()) {
      node.reapplyStyle();
    }
    for (Map.Entry<JLabel, String> association : labelForAssociations.entrySet()) {
      JLabel label = association.getKey();
      String ref = association.getValue();
      SplitsNode<Component> targetComponent = componentsByName.get(ref);
      if (targetComponent == null) {
        throw new SplitsException("Label '" + label.getText() + "' references an unknown component '" + ref + "'" +
                                  dump());
      }
      label.setLabelFor(targetComponent.getComponent());
    }
    if (onLoadListeners != null) {
      for (OnLoadListener listener : onLoadListeners) {
        listener.processLoad();
      }
    }
  }

  public RepeatHandler getRepeat(String name) {
    return repeats.get(name);
  }

  public void addRepeat(String name, RepeatHandler repeatHandler) {
    repeats.put(name, repeatHandler);
  }

  public void addDisposable(Disposable listener) {
    Disposable previous = this.disposables.put(listener, listener);
    if (previous != null){
      previous.dispose();
    }
  }

  public void addOnLoadListener(OnLoadListener listener) {
    if (onLoadListeners == null) {
      onLoadListeners = new ArrayList<OnLoadListener>();
    }
    onLoadListeners.add(listener);
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
      setVisible(sourceComponent.isVisible());
    }

    private void setVisible(boolean visible) {
      targetComponent.setVisible(visible);
      if (targetComponent instanceof JComponent) {
        GuiUtils.revalidate((JComponent)targetComponent);
      }

    }

    public void dispose(SplitsContext context) {
      if (componentListener != null) {
        Component sourceComponent = getSourceComponent(context);
        sourceComponent.removeComponentListener(componentListener);
      }
    }

    private Component getSourceComponent(SplitsContext context) {
      SplitsNode splitsNode = context.findComponent(sourceComponentName);
      if (splitsNode == null) {
        throw new ItemNotFound("References autoHideSource component '" + sourceComponentName + "' does not exist");
      }
      return splitsNode.getComponent();
    }

    private ComponentAdapter createListener() {
      return new ComponentAdapter() {
        public void componentShown(ComponentEvent e) {
          setVisible(true);
        }

        public void componentHidden(ComponentEvent e) {
          setVisible(false);
        }
      };
    }
  }
}
