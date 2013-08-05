package org.globsframework.gui.splits.parameters;

import org.globsframework.gui.splits.exceptions.SplitsException;
import org.globsframework.utils.collections.MultiMap;
import org.globsframework.utils.exceptions.InvalidParameter;

import java.util.List;
import java.util.Map;
import java.util.Properties;

public class ConfiguredPropertiesService {

  private Properties currentProperties = new Properties();

  public interface Functor {
    void apply(String value);
  }

  private MultiMap<String, Functor> bindings = new MultiMap<String, Functor>();

  public void apply(Properties properties) {
    currentProperties = properties;
    for (Map.Entry<String, List<Functor>> entry : bindings.entries()) {
      String propertyKey = entry.getKey();
      if (!currentProperties.containsKey(propertyKey)) {
        throw new InvalidParameter("Key '" + propertyKey + "' not found in properties: " + properties.toString());
      }
      String configuredValue = currentProperties.getProperty(propertyKey);
      if (configuredValue == null) {
        throw new InvalidParameter("Null value for key '" + propertyKey + "' found in properties: " + properties.toString());
      }
      for (Functor functor : entry.getValue()) {
        functor.apply(configuredValue);
      }
    }
  }

  public void bind(String propertyKey, Functor functor) {
    String configuredValue = currentProperties.getProperty(propertyKey);
    bindings.put(propertyKey, functor);
    try {
      functor.apply(configuredValue);
    }
    catch (Exception e) {
      throw new SplitsException("Could not bind property: " + propertyKey, e);
    }
  }
}
