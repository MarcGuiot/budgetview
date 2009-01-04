package org.globsframework.gui.splits;

import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.exceptions.SplitsException;
import org.globsframework.gui.splits.font.FontLocator;
import org.globsframework.gui.splits.impl.DefaultSplitsContext;
import org.globsframework.gui.splits.layout.CardHandler;
import org.globsframework.gui.splits.layout.DefaultCardHandler;
import org.globsframework.gui.splits.repeat.DefaultRepeat;
import org.globsframework.gui.splits.repeat.Repeat;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.splits.splitters.DefaultSplitterFactory;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.gui.splits.xml.SplitsParser;
import org.globsframework.utils.Files;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.GlobsException;
import org.globsframework.utils.exceptions.InvalidData;
import org.globsframework.utils.exceptions.ResourceAccessFailed;

import javax.swing.*;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SplitsBuilder {

  private Map<String, SplitsBuilder> children = new HashMap<String, SplitsBuilder>();
  private DefaultSplitsContext context;
  private Source source;
  private java.util.List<SplitsLoader> loaders = new ArrayList<SplitsLoader>();
  private Directory directory;

  public SplitsBuilder(Directory directory) {
    this.directory = directory;
    this.context = new DefaultSplitsContext(directory);
    addIfMissing(directory, ColorService.class, new ColorService());
    addIfMissing(directory, FontLocator.class, FontLocator.NULL);
    addIfMissing(directory, ImageLocator.class, ImageLocator.NULL);
    addIfMissing(directory, TextLocator.class, TextLocator.NULL);
  }

  public void dispose() {
    context.dispose();
  }

  private <T> void addIfMissing(Directory directory, Class<T> serviceClass, T service) {
    if (directory.find(serviceClass) == null) {
      directory.add(serviceClass, service);
    }
  }

  public static SplitsBuilder init(Directory directory) {
    return new SplitsBuilder(directory);
  }

  public <T extends Component> T add(String name, T component) {
    component.setName(name);
    context.addComponent(name, component);
    return component;
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

  public SplitsBuilder add(String name, HyperlinkListener listener) {
    context.add(name, listener);
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

  public <T> Repeat<T> addRepeat(String name, java.util.List<T> items, RepeatComponentFactory<T> factory) {
    DefaultRepeat<T> repeat = new DefaultRepeat<T>(factory, items);
    context.addRepeat(name, repeat);
    return repeat;
  }

  public SplitsBuilder addLoader(SplitsLoader loader) {
    this.loaders.add(loader);
    return this;
  }

  public SplitsBuilder addDisposable(Disposable disposable) {
    context.addDisposable(disposable);
    return this;
  }

  public SplitsBuilder setSource(Class referenceClass, String resourceName) throws ResourceAccessFailed {
    setSource(referenceClass, resourceName, null);
    return this;
  }

  public SplitsBuilder setSource(Class referenceClass, String resourceName, String encoding) {
    this.context.setReferenceClass(referenceClass);
    this.context.setResourceFile(resourceName);
    this.source = new ResourceFileSource(referenceClass, resourceName, encoding);
    return this;
  }

  public SplitsBuilder setSource(File file) {
    this.context.setResourceFile(file.getPath());
    this.source = new FileSource(file);
    return this;
  }

  public SplitsBuilder setSource(String content) {
    this.source = new StringSource(content);
    return this;
  }

  public Component getComponent(String id) {
    return context.findComponent(id);
  }

  SplitsContext getContext() {
    return context;
  }

  public <T extends Component> T load() {
    context.cleanUp();
    for (Map.Entry<String, SplitsBuilder> entry : children.entrySet()) {
      String name = entry.getKey();
      SplitsBuilder builder = entry.getValue();
      Component component = builder.load();
      component.setName(name);
      context.addOrReplaceComponent(name, component);
    }
    completeBeforeLoad();
    Component component = doLoad();
    completeAfterLoad();
    try {
      return (T)component;
    }
    catch (ClassCastException e) {
      throw new InvalidData("Unexpected result type", e);
    }
  }

  protected void completeBeforeLoad() {
  }

  protected void completeAfterLoad() {
    context.complete();
  }

  public Component doLoad() {
    Reader reader = source.getReader();
    Component component;
    try {
      SplitsParser parser = new SplitsParser(context, new DefaultSplitterFactory());
      component = parser.parse(reader);
    }
    catch (GlobsException e) {
      throw e;
    }
    catch (Exception e) {
      throw new ResourceAccessFailed("Error parsing Splits descriptor: " + e.getMessage() + context.dump(), e);
    }
    for (SplitsLoader loader : loaders) {
      loader.load(component);
    }
    return component;
  }

  public Directory getDirectory() {
    return directory;
  }

  private interface Source {
    Reader getReader();
  }

  private static class ResourceFileSource implements Source {
    private Class referenceClass;
    private String resourceName;
    private String encoding;

    private ResourceFileSource(Class referenceClass, String resourceName, String encoding) {
      this.referenceClass = referenceClass;
      this.resourceName = resourceName;
      this.encoding = encoding;
    }

    public Reader getReader() {
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

  private static class FileSource implements Source {
    private File file;

    private FileSource(File file) {
      this.file = file;
    }

    public Reader getReader() {
      try {
        return new FileReader(file);
      }
      catch (FileNotFoundException e) {
        throw new ResourceAccessFailed("File not found: " + file.getAbsolutePath(), e);
      }
    }
  }

  private static class StringSource implements Source {
    private String content;

    private StringSource(String content) {
      this.content = content;
    }

    public Reader getReader() {
      return new StringReader(content);
    }
  }
}
