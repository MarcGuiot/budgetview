package org.globsframework.gui.splits.color;

import org.globsframework.utils.Files;
import org.globsframework.utils.exceptions.InvalidParameter;
import org.globsframework.utils.exceptions.ItemNotFound;
import org.globsframework.utils.exceptions.ResourceAccessFailed;

import java.awt.*;
import java.io.PrintStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ColorService implements ColorLocator {

  public static final String DEFAULT_COLOR_SET = "DEFAULT_COLOR_SET";

  private List<ListenerRef> listeners = new ArrayList<ListenerRef>();
  private ColorSet currentSet;
  private List<ColorSet> colorSets = new ArrayList<ColorSet>();
  private List<ColorCreationListener> colorCreationListeners = new ArrayList<ColorCreationListener>();

  public ColorService() {
    selectCurrentColorSet();
  }

  public ColorService(Class refClass, String... files) throws ResourceAccessFailed {
    for (String file : files) {
      colorSets.add(ColorSet.load(extractName(file), Files.loadProperties(refClass, file)));
    }
    selectCurrentColorSet();
  }

  private void selectCurrentColorSet() {
    if (colorSets.isEmpty()) {
      colorSets.add(new ColorSet("default"));
    }
    String defaultColorSet = extractName(System.getProperty(DEFAULT_COLOR_SET, null));
    if (defaultColorSet != null) {
      for (ColorSet colorSet : colorSets) {
        if (defaultColorSet.equals(colorSet.getName())) {
          currentSet = colorSet;
          return;
        }
      }
    }
    currentSet = colorSets.get(0);
  }

  List<ColorSet> getColorSets() {
    return colorSets;
  }

  public void setCurrentSet(ColorSet colorSet) {
    if (!colorSets.contains(colorSet)) {
      colorSets.add(colorSet);
    }
    this.currentSet = colorSet;
    notifyListeners();
  }

  ColorSet getCurrentColorSet() {
    return currentSet;
  }

  String getCurrentColorSetName() {
    return currentSet.getName();
  }

  public List<String> getKeys() {
    return currentSet.getKeys();
  }

  public void install(final String key, final ColorUpdater updater) {
    addListenerRef(new HardListenerRef(new ColorChangeListener() {
      public void colorsChanged(ColorLocator colorLocator) {
        updater.updateColor(get(key));
      }
    }));
  }

  public void uninstall(ColorUpdater updater) {
    listeners.remove(updater);
  }

  public void set(String key, Color color) {
    currentSet.set(key, color);
    notifyListeners();
  }

  public boolean isSet(Object key) {
    String stringKey = getKeyString(key);
    return currentSet.isSet(stringKey);
  }

  public boolean contains(Object key) {
    String stringKey = getKeyString(key);
    return currentSet.contains(stringKey);
  }

  /**
   * The <code>toString()</code> method of the given key is used as the actual key.
   */
  public Color get(Object key) throws ItemNotFound, InvalidParameter {
    String stringKey = getKeyString(key);
    if (!currentSet.contains(stringKey)) {
      currentSet.declareEmptyKey(stringKey);
      for (ColorCreationListener listener : colorCreationListeners) {
        listener.colorCreated(stringKey);
      }
    }
    return currentSet.get(stringKey);
  }

  private String getKeyString(Object key) {
    if (key == null) {
      throw new InvalidParameter("null key is not allowed");
    }
    return key.toString();
  }

  public void addListener(ColorChangeListener listener) {
    addListenerRef(new WeakListenerRef(listener));
  }

  private void addListenerRef(ListenerRef ref) {
    listeners.add(ref);
    ref.get().colorsChanged(this);
  }

  public void removeListener(ColorChangeListener listener) {
    for (Iterator<ListenerRef> iterator = listeners.iterator(); iterator.hasNext();) {
      ListenerRef reference = iterator.next();
      if ((reference.get() == null) || (reference.get() == listener)) {
        iterator.remove();
      }
    }
  }

  public void addListener(ColorCreationListener listener) {
    colorCreationListeners.add(listener);
  }

  public void removeListener(ColorCreationListener listener) {
    colorCreationListeners.remove(listener);
  }

  public void autoUpdate(final Container container) {
    addListenerRef(new HardListenerRef(new ColorChangeListener() {
      public void colorsChanged(ColorLocator colorLocator) {
        container.repaint();
      }
    }));
  }

  private void notifyListeners() {
    List<ListenerRef> copy = new ArrayList<ListenerRef>(listeners);
    List<ListenerRef> toRemove = null;
    for (ListenerRef ref : copy) {
      ColorChangeListener listener = ref.get();
      if (listener != null) {
        listener.colorsChanged(this);
      }
      else {
        if (toRemove == null) {
          toRemove = new ArrayList<ListenerRef>();
        }
        toRemove.add(ref);
      }
    }
    if (toRemove != null) {
      listeners.removeAll(toRemove);
    }
  }

  public void printCurrentSet(PrintStream stream) {
    currentSet.print(stream);
  }

  private String extractName(String file) {
    if (file == null) {
      return null;
    }
    return file.substring(file.indexOf('/') + 1);
  }

  public void removeAllListeners() {
    listeners.clear();
    colorCreationListeners.clear();
    colorSets.clear();
  }

  private interface ListenerRef {
    ColorChangeListener get();
  }

  private class WeakListenerRef implements ListenerRef {
    private WeakReference<ColorChangeListener> ref;

    private WeakListenerRef(ColorChangeListener listener) {
      this.ref = new WeakReference<ColorChangeListener>(listener);
    }

    public ColorChangeListener get() {
      return ref.get();
    }
  }

  private class HardListenerRef implements ListenerRef {
    private ColorChangeListener listener;

    private HardListenerRef(ColorChangeListener listener) {
      this.listener = listener;
    }

    public ColorChangeListener get() {
      return listener;
    }
  }

}