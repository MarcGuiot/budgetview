package org.globsframework.gui.splits.layout;

import org.globsframework.gui.splits.SplitProperties;
import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.exceptions.SplitsException;
import org.globsframework.gui.splits.impl.DefaultSplitProperties;
import org.globsframework.gui.splits.utils.PropertySetter;
import org.globsframework.utils.ClassUtils;
import org.globsframework.utils.exceptions.InvalidParameter;
import org.globsframework.utils.exceptions.ItemNotFound;

import java.awt.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class LayoutService {
  private Map<String, LayoutFactory> factories = new HashMap<String, LayoutFactory>();

  public LayoutService() {
  }

  public LayoutService(LayoutService parent) {
    factories.putAll(parent.factories);
  }

  public LayoutManager getLayout(String name, SplitsContext context) {
    LayoutFactory factory = factories.get(name);
    if (factory == null) {
      Object instance = null;
      try {
        instance = ClassUtils.createFromClassName(name);
      }
      catch (ClassNotFoundException e) {
        throw new ItemNotFound("Unknown layout: " + name + " - make sure that it is registered or that it refers to a public static class reference");
      }
      if (!(instance instanceof LayoutManager)) {
        throw new InvalidParameter("Class '" + name + "' should implement LayoutManager");
      }
      return (LayoutManager)instance;
    }
    return factory.createLayout(context);
  }

  public void registerClass(String className, Class layoutManagerClass) {
    factories.put(className, new LayoutFactory(layoutManagerClass, new DefaultSplitProperties()));
  }

  public void addAll(LayoutService other) {
    factories.putAll(other.factories);
  }

  private static class LayoutFactory {
    private Class uiClass;
    private SplitProperties properties;
    private Constructor constructor;

    public LayoutFactory(Class layoutManagerClass, SplitProperties properties) {
      this.uiClass = layoutManagerClass;
      this.properties = properties;

      try {
        constructor = layoutManagerClass.getConstructor();
      }
      catch (NoSuchMethodException e) {
        throw new SplitsException("Layout class " + layoutManagerClass.getName() + " must have a public default constructor");
      }
    }

    public LayoutManager createLayout(SplitsContext context) {
      LayoutManager layoutManager = createInstance();
      PropertySetter.process(layoutManager, properties, context);
      return layoutManager;
    }

    private LayoutManager createInstance() {
      try {
        return (LayoutManager)constructor.newInstance();
      }
      catch (InstantiationException e) {
        throw new SplitsException("Cannot instantiate Layout component " + uiClass.getName(), e);
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
