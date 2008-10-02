package org.globsframework.gui.splits.ui;

import org.globsframework.gui.splits.SplitProperties;
import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.exceptions.SplitsException;
import org.globsframework.gui.splits.utils.PropertySetter;
import org.globsframework.utils.exceptions.InvalidParameter;
import org.globsframework.utils.exceptions.ItemNotFound;

import javax.swing.plaf.ComponentUI;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class UIService {

  private Map<String, UIFactory> factories = new HashMap<String, UIFactory>();
  private Map<String, Class> classes = new HashMap<String, Class>();

  public UIService() {
  }

  public UIService(UIService parent) {
    classes.putAll(parent.classes);
  }

  public ComponentUI getUI(String name, SplitsContext context) {
    UIFactory factory = factories.get(name);
    if (factory == null) {
      throw new ItemNotFound("Unknown UI: " + name);
    }
    return factory.createUI(context);
  }

  public void registerClass(String className, Class uiClass) {
    classes.put(className, uiClass);
  }

  public void registerUI(String name, String className, SplitProperties properties) {
    Class uiClass;
    try {
      if (classes.containsKey(className)) {
        uiClass = classes.get(className);
      }
      else {
        uiClass = Class.forName(className);
      }
    }
    catch (ClassNotFoundException e) {
      throw new ItemNotFound("Cannot find class '" + className + "' for ui: " + name);
    }

    if (!ComponentUI.class.isAssignableFrom(uiClass)) {
      throw new InvalidParameter("Error for UI '" + name + "': class " + className + " must extend a " +
                                 " subclass of " + ComponentUI.class.getName() + " that matches the target component " +
                                 "(for instance LabelUI or ButtonUI)");
    }

    factories.put(name, new UIFactory(uiClass, properties));
  }

  public void addAll(UIService other) {
    factories.putAll(other.factories);
    classes.putAll(other.classes);
  }

  private static class UIFactory {
    private Class uiClass;
    private SplitProperties properties;
    private Constructor constructor;

    public UIFactory(Class uiClass, SplitProperties properties) {
      this.uiClass = uiClass;
      this.properties = properties;

      try {
        constructor = uiClass.getConstructor();
      }
      catch (NoSuchMethodException e) {
        throw new SplitsException("UI class " + uiClass.getName() + " must have a public default constructor");
      }
    }

    public ComponentUI createUI(SplitsContext context) {
      ComponentUI ui = createInstance();
      PropertySetter.process(ui, properties, context);
      return ui;
    }

    private ComponentUI createInstance() {
      try {
        return (ComponentUI)constructor.newInstance();
      }
      catch (InstantiationException e) {
        throw new SplitsException("Cannot instantiate UI component " + uiClass.getName(), e);
      }
      catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      }
      catch (InvocationTargetException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
