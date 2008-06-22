package org.globsframework.gui.splits;

import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.exceptions.SplitsException;
import org.globsframework.gui.splits.font.FontLocator;
import org.globsframework.gui.splits.impl.DefaultSplitsContext;
import org.globsframework.gui.splits.layout.CardHandler;
import org.globsframework.gui.splits.layout.DefaultCardHandler;
import org.globsframework.gui.splits.xml.SplitsParser;
import org.globsframework.gui.splits.splitters.DefaultSplitterFactory;
import org.globsframework.gui.splits.styles.StyleService;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

public class SplitsBuilder {

  private DefaultSplitsContext context;

  public static SplitsBuilder init(Directory directory) {
    return new SplitsBuilder(directory.get(ColorService.class),
                             directory.find(IconLocator.class),
                             directory.find(TextLocator.class),
                             directory.find(FontLocator.class));
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

  public SplitsBuilder add(String name, Component component) {
    component.setName(name);
    context.addComponent(name, component);
    return this;
  }

  public SplitsBuilder add(Component... components) {
    for (Component component : components) {
      String name = component.getName();
      if (name == null) {
        throw new SplitsException("Component '" + component + "' must have a name");
      }
      add(name, component);
    }
    return this;
  }

  public SplitsBuilder add(String name, Action action) {
    context.add(name, action);
    return this;
  }

  public CardHandler addCardHandler(String handlerName) {
    JPanel panel = new JPanel();
    add(handlerName, panel);
    return DefaultCardHandler.init(panel);
  }

  public Component parse(Class referenceClass, String resourceName) {
    InputStream stream = referenceClass.getResourceAsStream(resourceName);
    if (stream == null) {
      throw new SplitsException("File '" + resourceName + "' not found in classpath");
    }
    try {
      return parse(stream);
    }
    catch (Exception e) {
      throw new SplitsException("Error parsing file '" + resourceName + "' - " + e.getMessage(), e);
    }
  }

  public Component parse(InputStream inputStream) {
    if (inputStream == null) {
      throw new IllegalArgumentException("null inputStream");
    }
    return parse(new InputStreamReader(inputStream));
  }

  public Component parse(InputStream inputStream, String encoding) throws UnsupportedEncodingException {
    return parse(new InputStreamReader(inputStream, encoding));
  }

  public Component parse(Reader reader) throws SplitsException {
    SplitsParser parser = new SplitsParser(context, new DefaultSplitterFactory());
    return parser.parse(reader);
  }

  public Component getComponent(String id) {
    return context.findComponent(id);
  }
}
