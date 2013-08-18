package org.globsframework.gui.splits.color;

import org.globsframework.utils.Files;
import org.globsframework.utils.exceptions.InvalidParameter;
import org.globsframework.utils.exceptions.ItemNotFound;
import org.globsframework.utils.exceptions.ResourceAccessFailed;

import java.awt.*;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ColorService implements ColorLocator {

  public static final String DEFAULT_COLOR_SET = "DEFAULT_COLOR_SET";

  private Set<ColorChangeListener> listeners = new HashSet<ColorChangeListener>();
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

  public void setCurrentSet(String name) {
    for (ColorSet colorSet : colorSets) {
      if (colorSet.getName().equals(name)) {
        setCurrentSet(colorSet);
        return;
      }
    }
    throw new ItemNotFound("No color set found with name: " + name);
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
    if (key == null) {
      return null;
    }
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
    listeners.add(listener);
    listener.colorsChanged(this);
  }

  public void removeListener(ColorChangeListener listener) {
    listeners.remove(listener);
  }

  public void addCreationListener(ColorCreationListener listener) {
    colorCreationListeners.add(listener);
  }

  public void removeCreationListener(ColorCreationListener listener) {
    colorCreationListeners.remove(listener);
  }

  public void autoUpdate(final Container container) {
    addListener(new ColorChangeListener() {
      public void colorsChanged(ColorLocator colorLocator) {
        container.repaint();
      }
    });
  }

  private void notifyListeners() {
    List<ColorChangeListener> copy = new ArrayList<ColorChangeListener>(listeners);
    for (ColorChangeListener listener : copy) {
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

  public void removeAllListeners() {
    listeners.clear();
    colorCreationListeners.clear();
    colorSets.clear();
  }
}