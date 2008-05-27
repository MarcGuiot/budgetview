package org.crossbowlabs.splits.color;

import org.crossbowlabs.splits.exceptions.ResourceLoadingFailed;
import org.crossbowlabs.splits.exceptions.SplitsException;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.List;

public class ColorSet {
  private Map<String, Color> colorsByName = new HashMap<String, Color>();
  private String name;

  public ColorSet(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public List<String> getKeys() {
    List<String> keys = new ArrayList<String>();
    keys.addAll(colorsByName.keySet());
    return keys;
  }

  public Color get(String key) {
    return colorsByName.get(key);
  }

  public void set(String key, Color color) {
    if (key == null) {
      throw new SplitsException("null key not allowed");
    }
    if (color == null) {
      throw new SplitsException("null color not allowed for key: " + key);
    }
    colorsByName.put(key, color);
  }

  public boolean contains(String key) {
    return colorsByName.containsKey(key);
  }

  public String toString() {
    return name;
  }

  public void print(PrintStream stream) {
    List<String> keys = new ArrayList(getKeys());
    Collections.sort(keys);

    for (String key : keys) {
      Color color = get(key);
      stream.append(key).append("=").append(Colors.toString(color)).println();
    }
  }

  public static ColorSet load(String name, InputStream stream) throws ResourceLoadingFailed {
    ColorSet colorSet = new ColorSet(name);
    Properties props = new Properties();
    try {
      props.load(stream);
    }
    catch (IOException e) {
      throw new ResourceLoadingFailed("Could not load properties file");
    }
    for (Object key : props.keySet()) {
      String value = props.getProperty((String)key);
      try {
        Color color = Colors.toColor(value);
        colorSet.set((String)key, color);
      }
      catch (NumberFormatException e) {
        throw new SplitsException("Error parsing value '" + value + " for color " + key);
      }
    }
    return colorSet;
  }
}
