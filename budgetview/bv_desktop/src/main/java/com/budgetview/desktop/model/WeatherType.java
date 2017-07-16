package com.budgetview.desktop.model;

import com.budgetview.model.ProfileType;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.impl.ReadOnlyGlob;
import org.globsframework.model.utils.GlobConstantContainer;
import org.globsframework.utils.exceptions.InvalidData;

import static org.globsframework.model.FieldValue.value;

public enum WeatherType implements GlobConstantContainer {
  SUNNY(0),
  CLOUDY(1),
  RAINY(2);

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  private final int id;

  static {
    GlobTypeLoader.init(WeatherType.class, "weatherType");
  }

  WeatherType(int id) {
    this.id = id;
  }

  public ReadOnlyGlob getGlob() {
    return new ReadOnlyGlob(WeatherType.TYPE,
                            value(WeatherType.ID, id));
  }

  public Integer getId() {
    return id;
  }


  public static WeatherType get(Integer id) {
    if (id == null) {
      return null;
    }
    switch (id) {
      case 0:
        return SUNNY;
      case 1:
        return CLOUDY;
      case 2:
        return RAINY;
    }
    throw new InvalidData(id + " not associated to any enum value");
  }

  public boolean worseThan(WeatherType other) {
    return id > other.id;
  }

  public String getName() {
    return name().toLowerCase();
  }
}

