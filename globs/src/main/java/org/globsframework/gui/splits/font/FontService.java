package org.globsframework.gui.splits.font;

import org.globsframework.utils.exceptions.ResourceAccessFailed;
import org.globsframework.utils.exceptions.ItemNotFound;
import org.globsframework.utils.exceptions.InvalidParameter;
import org.globsframework.utils.exceptions.InvalidFormat;
import org.globsframework.utils.Strings;
import org.globsframework.utils.Files;
import org.globsframework.gui.splits.exceptions.SplitsException;

import java.awt.*;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;

public class FontService implements FontLocator {

  private Map<String, Font> fonts = new HashMap<String, Font>();

  public FontService() {

  }

  public FontService(Class refClass, String fileName) throws ResourceAccessFailed, InvalidFormat {
    Properties props = Files.loadProperties(refClass, fileName);
    for (Object key : props.keySet()) {
      String value = props.getProperty((String)key);
      try {
        set((String)key, Fonts.parseFont(value));
      }
      catch (NumberFormatException e) {
        throw new InvalidFormat("Error parsing value '" + value + "' for font: " + key, e);
      }
    }
  }

  public Font get(String key) throws ItemNotFound, InvalidParameter {
    if (Strings.isNullOrEmpty(key)) {
      throw new InvalidParameter("empty keys are not allowed");
    }
    Font font = fonts.get(key);
    if (font == null) {
      throw new ItemNotFound("Font '" + key + "' not found");
    }
    return font;
  }

  public void set(String key, Font font) throws InvalidParameter {
    if (Strings.isNullOrEmpty(key)) {
      throw new InvalidParameter("empty keys are not allowed");
    }
    fonts.put(key, font);
  }
}
