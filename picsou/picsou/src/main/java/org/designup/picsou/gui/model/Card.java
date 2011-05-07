package org.designup.picsou.gui.model;

import org.designup.picsou.utils.Lang;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import static org.globsframework.model.FieldValue.value;

import org.globsframework.model.FieldValues;
import org.globsframework.model.KeyBuilder;
import org.globsframework.model.impl.ReadOnlyGlob;
import org.globsframework.model.utils.GlobConstantContainer;
import org.globsframework.utils.exceptions.ItemNotFound;
import org.globsframework.utils.Strings;

public enum Card implements GlobConstantContainer {
  HOME("home", 0),
  CATEGORIZATION("categorization", 1),
  BUDGET("budget", 2),
  ANALYSIS("analysis", 3),
  DATA("data", 4),
  SAVINGS("savings", 5);

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  private int id;
  private String name;

  static {
    GlobTypeLoader.init(Card.class);
  }

  Card(String name, int id) {
    this.name = name;
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public String getLabel() {
    return Lang.get("cards." + getName());
  }

  public String getDescription() {
    return Lang.get("cards." + getName() + ".description");
  }

  public ReadOnlyGlob getGlob() {
    return new ReadOnlyGlob(Card.TYPE, value(ID, id));
  }

  public static Card get(FieldValues cardValues) {
    return get(cardValues.get(Card.ID));
  }

  public static Card get(int id) {
    switch (id) {
      case 0:
        return HOME;
      case 1:
        return CATEGORIZATION;
      case 2:
        return BUDGET;
      case 3:
        return ANALYSIS;
      case 4:
        return DATA;
      case 5:
        return SAVINGS;
    }
    throw new ItemNotFound(id + " is not associated to any Card enum value");
  }

  public static Card get(String name) {
    return Card.valueOf(Strings.toNiceUpperCase(name));
  }

  public org.globsframework.model.Key getKey() {
    return KeyBuilder.newKey(Card.TYPE, id);
  }

  public int getId() {
    return id;
  }
}
