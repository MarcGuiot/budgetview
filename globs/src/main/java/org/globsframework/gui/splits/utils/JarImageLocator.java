package org.globsframework.gui.splits.utils;

import org.globsframework.gui.splits.ImageLocator;
import org.globsframework.gui.splits.exceptions.IconNotFound;

import javax.swing.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class JarImageLocator implements ImageLocator {
  private Class referenceClass;
  private String imagesPath;
  private Map<String, ImageIcon> loadedImages = new HashMap<String, ImageIcon>();

  public JarImageLocator(Class referenceClass, String imagesPath) {
    this.referenceClass = referenceClass;
    this.imagesPath = imagesPath;
  }

  synchronized public ImageIcon get(String fileName) throws IconNotFound {
    ImageIcon imageIcon = loadedImages.get(fileName);
    if (imageIcon == null) {
      String path = imagesPath + "/" + fileName;
      URL iconUrl = referenceClass.getResource(path);
      if (iconUrl != null) {
        imageIcon = new ImageIcon(iconUrl);
        loadedImages.put(fileName, imageIcon);
      }
      else {
        throw new IconNotFound("Cannot findOrCreate icon: " + path + " for class: " + referenceClass);
      }
    }
    return imageIcon;
  }
}
