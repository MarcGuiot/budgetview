package org.globsframework.gui.splits;

import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.exceptions.SplitsException;
import org.globsframework.gui.splits.font.FontLocator;
import org.globsframework.gui.splits.impl.DefaultSplitsContext;
import org.globsframework.gui.splits.layout.CardHandler;
import org.globsframework.gui.splits.layout.DefaultCardHandler;
import org.globsframework.gui.splits.splitters.DefaultSplitterFactory;
import org.globsframework.gui.splits.styles.StyleService;
import org.globsframework.gui.splits.xml.SplitsParser;
import org.globsframework.utils.Files;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.ResourceAccessFailed;

import javax.swing.*;
import java.awt.*;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SplitsBuilder {

  private Map<String, SplitsBuilder> children = new HashMap<String, SplitsBuilder>();
  private DefaultSplitsContext context;
  private ResourceFileLoader resourceLoader;
  private java.util.List<SplitsLoader> loaders = new ArrayList<SplitsLoader>();

  public static SplitsBuilder init(Directory directory) {
    return new SplitsBuilder(directory);
  }

  public static SplitsBuilder init(ColorService colorService, IconLocator locator) {
    return new SplitsBuilder(colorService, locator);
  }

  public SplitsBuilder(ColorService colorService, IconLocator locator) {
    this(colorService, locator, null, null);
  }

  public SplitsBuilder(ColorService colorService, IconLocator iconLocator, TextLocator textLocator, FontLocator fontLocator) {
    if (iconLocator == null) {
      iconLocator = IconLocator.NULL;
    }
    if (textLocator == null) {
      textLocator = TextLocator.NULL;
    }
    this.context = new DefaultSplitsContext(colorService, iconLocator, textLocator, fontLocator,
                                            new StyleService());
  }

  public SplitsBuilder(Directory directory) {
    this(directory.get(ColorService.class),
         directory.find(IconLocator.class),
         directory.find(TextLocator.class),
         directory.find(FontLocator.class));
  }

  public SplitsBuilder add(String name, Component component) {
    component.setName(name);
    context.addComponent(name, component);
    return this;
  }

  public SplitsBuilder add(Component... components) {
    for (Component component : components) {
      String name = component.getName();
      if (name == null) {
        throw new SplitsException("Component '" + component + "' must have a name" + context.dump());
      }
      add(name, component);
    }
    return this;
  }

  public SplitsBuilder add(String name, Action action) {
    context.add(name, action);
    return this;
  }

  public SplitsBuilder add(String name, SplitsBuilder builder) {
    children.put(name, builder);
    return this;
  }

  public CardHandler addCardHandler(String handlerName) {
    JPanel panel = new JPanel();
    add(handlerName, panel);
    return DefaultCardHandler.init(panel);
  }

  public SplitsBuilder addLoader(SplitsLoader loader) {
    this.loaders.add(loader);
    return this;
  }

  public SplitsBuilder init(Class referenceClass, String resourceName) throws ResourceAccessFailed {
    init(referenceClass, resourceName, null);
    return this;
  }

  public SplitsBuilder init(Class referenceClass, String resourceName, String encoding) {
    this.context.setReferenceClass(referenceClass);
    this.context.setResourceFile(resourceName);
    this.resourceLoader = new ResourceFileLoader(referenceClass, resourceName, encoding);
    return this;
  }

  public Component doParse(Reader reader) throws SplitsException {
    SplitsParser parser = new SplitsParser(context, new DefaultSplitterFactory());
    return parser.parse(reader);
  }

  public Component getComponent(String id) {
    return context.findComponent(id);
  }

  SplitsContext getContext() {
    return context;
  }

  public Component load() {
    for (Map.Entry<String, SplitsBuilder> entry : children.entrySet()) {
      String name = entry.getKey();
      Component component = entry.getValue().load();
      component.setName(name);
      context.addComponent(name, component);
    }
    complete();
    return resourceLoader.run();
  }

  protected void complete() {
  }

  private class ResourceFileLoader {
    private Class referenceClass;
    private String resourceName;
    private String encoding;

    private ResourceFileLoader(Class referenceClass, String resourceName, String encoding) {
      this.referenceClass = referenceClass;
      this.resourceName = resourceName;
      this.encoding = encoding;
    }

    public Component run() {
      Reader reader = getReader();
      Component component;
      try {
        component = doParse(reader);
      }
      catch (Exception e) {
        throw new ResourceAccessFailed("Error parsing file '" + resourceName + "' - " + e.getMessage(), e);
      }
      for (SplitsLoader loader : loaders) {
        loader.load(component);
      }
      return component;
    }

    private Reader getReader() {
      InputStream stream = Files.getStream(referenceClass, resourceName);
      if (Strings.isNotEmpty(encoding)) {
        try {
          return new InputStreamReader(stream, encoding);
        }
        catch (UnsupportedEncodingException e) {
          throw new ResourceAccessFailed("Error for file: " + resourceName, e);
        }
      }
      else {
        return new InputStreamReader(stream);
      }
    }
  }
}
