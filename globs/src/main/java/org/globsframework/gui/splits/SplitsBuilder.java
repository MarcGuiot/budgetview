package org.globsframework.gui.splits;

import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.exceptions.SplitsException;
import org.globsframework.gui.splits.font.FontLocator;
import org.globsframework.gui.splits.impl.DefaultSplitsContext;
import org.globsframework.gui.splits.impl.DefaultSplitsNode;
import org.globsframework.gui.splits.layout.CardHandler;
import org.globsframework.gui.splits.layout.DefaultCardHandler;
import org.globsframework.gui.splits.layout.DefaultTabHandler;
import org.globsframework.gui.splits.layout.TabHandler;
import org.globsframework.gui.splits.repeat.DefaultRepeat;
import org.globsframework.gui.splits.repeat.Repeat;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.splits.splitters.DefaultSplitterFactory;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.gui.splits.utils.OnLoadListener;
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

public class SplitsBuilder implements PanelBuilder {

  private Map<String, SplitsBuilder> children = new HashMap<String, SplitsBuilder>();
  private DefaultSplitsContext context;
  private Source source;
  private java.util.List<SplitsLoader> loaders = new ArrayList<SplitsLoader>();
  private Directory directory;
  private java.util.List<OnLoadListener> onLoadListeners = new ArrayList<OnLoadListener>();

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

  public <T extends Component> SplitsNode<T> add(String name, T component) {
    component.setName(name);
    DefaultSplitsNode<T> node = new DefaultSplitsNode<T>(component, context);
    context.addComponent(name, (DefaultSplitsNode<Component>)node);
    return node;
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

  public <T extends Action> T add(String name, T action) {
    context.add(name, action);
    return action;
  }

  public SplitsBuilder add(String name, HyperlinkListener listener) {
    context.add(name, listener);
    return this;
  }

  public SplitsBuilder add(String name, SplitsBuilder builder) {
    children.put(name, builder);
    return this;
  }

  public TabHandler addTabHandler(String handlerName) {
    JTabbedPane tabbedPane = new JTabbedPane();
    add(handlerName, tabbedPane);
    return DefaultTabHandler.init(tabbedPane);

  }

  public CardHandler addCardHandler(String handlerName) {
    JPanel panel = new JPanel();
    add(handlerName, panel);
    return DefaultCardHandler.init(panel);
  }

  public <T> Repeat<T> addRepeat(String name, java.util.Collection<T> items, RepeatComponentFactory<T> factory) {
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
    setSource(referenceClass, resourceName, "UTF-8");
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
    SplitsNode node = context.findComponent(id);
    if (node == null) {
      return null;
    }
    return node.getComponent();
  }

  SplitsContext getContext() {
    return context;
  }

  public <T extends Component> T load() {
    return (T)loadWithNode().getComponent();
  }

  public void addOnLoadListener(OnLoadListener listener) {
    onLoadListeners.add(listener);
  }

  public <T extends Component> SplitsNode<T> loadWithNode() {
    context.cleanUp();
    for (Map.Entry<String, SplitsBuilder> entry : children.entrySet()) {
      String name = entry.getKey();
      SplitsBuilder builder = entry.getValue();
      SplitsNode<Component> splitsNode = builder.loadWithNode();
      splitsNode.getComponent().setName(name);
      context.addOrReplaceComponent(name, splitsNode);
    }
    completeBeforeLoad();
    SplitsNode splitComponent = doLoad();
    completeAfterLoad();
    try {
      return (SplitsNode<T>)splitComponent;
    }
    catch (ClassCastException e) {
      throw new InvalidData("Unexpected result type", e);
    }
  }

  protected void completeBeforeLoad() {
  }

  protected void completeAfterLoad() {
    context.complete();
    for (OnLoadListener listener : onLoadListeners) {
      listener.processLoad();
    }
  }

  public SplitsNode doLoad() {
    Reader reader = source.getReader();
    Splitter.SplitComponent component;
    try {
      SplitsParser parser = new SplitsParser(context, new DefaultSplitterFactory());
      component = parser.parse(reader);
    }
    catch (GlobsException e) {
      throw new RuntimeException("Error parsing " + source, e);
    }
    catch (Exception e) {
      throw new ResourceAccessFailed("Error parsing Splits descriptor: " + e.getMessage() + context.dump(), e);
    }
    for (SplitsLoader loader : loaders) {
      loader.load(component.componentConstraints.getComponent(), component.node);
    }
    return component.node;
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

    public String toString() {
      return resourceName;
    }
  }

  private static class FileSource implements Source {
    private File file;

    private FileSource(File file) {
      this.file = file;
    }

    public Reader getReader() {
      try {
        return new InputStreamReader(new FileInputStream(file), "UTF-8");
      }
      catch (FileNotFoundException e) {
        throw new ResourceAccessFailed("File not found: " + file.getAbsolutePath(), e);
      }
      catch (UnsupportedEncodingException e) {
        throw new ResourceAccessFailed("UTF-8", e);
      }
    }

    public String toString() {
      return file.getAbsolutePath();
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

    public String toString() {
      return content;
    }
  }
}
