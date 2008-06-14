package org.globsframework.gui.splits.color;

import org.globsframework.gui.splits.exceptions.ResourceLoadingFailed;
import org.globsframework.gui.splits.exceptions.SplitsException;

import java.awt.*;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class ColorService implements ColorSource {

  public static final String DEFAULT_COLOR_SET = "DEFAULT_COLOR_SET";

  private List<ColorChangeListener> listeners = new ArrayList<ColorChangeListener>();
  private ColorSet currentSet;
  private List<ColorSet> colorSets = new ArrayList<ColorSet>();
  private List<ColorCreationListener> colorCreationListeners = new ArrayList<ColorCreationListener>();

  public ColorService() {
    selectCurrentColorSet();
  }

  public ColorService(String name, InputStream stream) {
    colorSets.add(ColorSet.load(name, stream));
    selectCurrentColorSet();
  }

  public ColorService(Class refClass, String... files) throws ResourceLoadingFailed {
    for (String file : files) {
      InputStream stream = refClass.getResourceAsStream(file);
      if (stream == null) {
        throw new ResourceLoadingFailed("Resource file '" + file + "' not found for class: " + refClass.getName());
      }
      colorSets.add(ColorSet.load(extractName(file), stream));
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
    addListener(new ColorChangeListener() {
      public void colorsChanged(ColorSource colorSource) {
        updater.updateColor(get(key));
      }
    });
  }

  public void set(String key, Color color) {
    currentSet.set(key, color);
    notifyListeners();
  }

  /**
   * The <code>toString()</code> method of the given key is used as the actual key.
   */
  public Color get(Object key) {
    if (key == null) {
      throw new SplitsException("null key is not allowed");
    }
    String stringKey = key.toString();
    if (!currentSet.contains(stringKey)) {
      currentSet.set(stringKey, Color.RED);
      for (ColorCreationListener listener : colorCreationListeners) {
        listener.colorCreated(stringKey);
      }
    }
    return currentSet.get(stringKey);
  }

  public void addListener(ColorChangeListener listener) {
    listeners.add(listener);
    listener.colorsChanged(this);
  }

  public void removeListener(ColorChangeListener listener) {
    listeners.remove(listener);
  }

  public void addListener(ColorCreationListener listener) {
    colorCreationListeners.add(listener);
  }

  public void removeListener(ColorCreationListener listener) {
    colorCreationListeners.remove(listener);
  }

  public void autoUpdate(final Container container) {
    addListener(new ColorChangeListener() {
      public void colorsChanged(ColorSource colorSource) {
        container.repaint();
      }
    });
  }

  private void notifyListeners() {
    for (ColorChangeListener listener : new ArrayList<ColorChangeListener>(listeners)) {
      listener.colorsChanged(this);
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
}