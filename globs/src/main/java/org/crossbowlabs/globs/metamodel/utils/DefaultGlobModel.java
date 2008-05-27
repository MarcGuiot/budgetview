package org.crossbowlabs.globs.metamodel.utils;

import java.util.*;
import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.metamodel.GlobModel;
import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.metamodel.properties.Property;
import org.crossbowlabs.globs.model.GlobList;
import org.crossbowlabs.globs.utils.exceptions.ItemNotFound;

public class DefaultGlobModel implements GlobModel {
  private Map<String, GlobType> typesByName = new HashMap<String, GlobType>();
  private int objectTypeSpecificCount;
  private int fieldSpecificCount;
  private GlobList constants = new GlobList();
  private GlobModel innerModel;
  private GlobTypeDependencies dependencies;

  public DefaultGlobModel(GlobType... types) {
    this(null, types);
  }

  public DefaultGlobModel(GlobModel innerModel, GlobType... types) {
    add(types);
    this.innerModel = innerModel;
    this.dependencies = new GlobTypeDependencies(getAll());
  }

  public GlobType getType(String name) throws ItemNotFound {
    GlobType globType = typesByName.get(name);
    if (globType != null) {
      return globType;
    }
    if (innerModel != null) {
      return innerModel.getType(name);
    }
    throw new ItemNotFound("No object type found with name: " + name);
  }

  public Collection<GlobType> getAll() {
    Set result = new HashSet();
    result.addAll(typesByName.values());
    if (innerModel != null) {
      result.addAll(innerModel.getAll());
    }
    return result;
  }

  public Iterator<GlobType> iterator() {
    return getAll().iterator();
  }

  public GlobList getConstants() {
    GlobList result = new GlobList();
    result.addAll(constants);
    for (GlobType type : typesByName.values()) {
      result.addAll(type.getConstants());
    }
    if (innerModel != null) {
      result.addAll(innerModel.getConstants());
    }
    return result;
  }

  public GlobTypeDependencies getDependencies() {
    return dependencies;
  }

  public <T> Property<Field, T> createFieldProperty(String name) {
    if (innerModel != null) {
      return innerModel.createFieldProperty(name);
    }
    return new IdProperty<Field, T>(name, fieldSpecificCount++) {
    };
  }

  public <T> Property<GlobType, T> createGlobTypeProperty(String name) {
    if (innerModel != null) {
      return innerModel.createGlobTypeProperty(name);
    }
    return new IdProperty<GlobType, T>(name, objectTypeSpecificCount++) {
    };
  }

  private void add(GlobType... types) {
    for (GlobType type : types) {
      typesByName.put(type.getName(), type);
    }
  }

  public void addConstants(GlobList constants) {
    this.constants.addAll(constants);
  }
}
