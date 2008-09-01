package org.designup.picsou.gui.model;

import org.designup.picsou.utils.Lang;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.KeyBuilder;
import org.globsframework.model.impl.ReadOnlyGlob;
import org.globsframework.model.utils.GlobConstantContainer;
import org.globsframework.utils.exceptions.ItemNotFound;

public enum Card implements GlobConstantContainer {
  HOME("HOME", 0, false),
  BUDGET("BUDGET", 1, false),
  DATA("DATA", 2, true),
  REPARTITION("REPARTITION", 3, true),
  EVOLUTION("EVOLUTION", 4, true);

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  private int id;
  private String name;
  public final boolean showCategoryCard;

  static {
    GlobTypeLoader.init(Card.class);
  }

  Card(String name, int id, boolean showCategoryCard) {
    this.name = name;
    this.id = id;
    this.showCategoryCard = showCategoryCard;
  }

  public String getName() {
    return name.toLowerCase();
  }

  public String getLabel() {
    return Lang.get("cards." + getName());
  }

  public ReadOnlyGlob getGlob() {
    return new ReadOnlyGlob(Card.TYPE, value(ID, id));
  }

  public static Card get(int id) {
    switch (id) {
      case 0:
        return HOME;
      case 1:
        return BUDGET;
      case 2:
        return DATA;
      case 3:
        return REPARTITION;
      case 4:
        return EVOLUTION;
    }
    throw new ItemNotFound(id + " is not associated to any Card enum value");
  }

  public org.globsframework.model.Key getKey() {
    return KeyBuilder.newKey(Card.TYPE, id);
  }
}
