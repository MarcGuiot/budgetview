package org.designup.picsou.model;

import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobBuilder;
import org.globsframework.utils.Utils;
import org.globsframework.utils.exceptions.ItemNotFound;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public enum MasterCategory {
  NONE("NONE", 0),
  ALL("ALL", 1),
  FOOD("FOOD", 2),
  TAXES("TAXES", 3),
  HOUSE("HOUSE", 4),
  HEALTH("HEALTH", 5),
  LEISURES("LEISURES", 6),
  PUERICULTURE("PUERICULTURE", 7),
  TELECOMS("TELECOMS", 8),
  BANK("BANK", 9),
  TRANSPORTS("TRANSPORTS", 10),
  INTERNAL("INTERNAL", 11),
  CLOTHING("CLOTHING", 12),
  SAVINGS("SAVINGS", 13),
  EQUIPMENT("EQUIPMENT", 14),
  BEAUTY("BEAUTY", 15),
  EDUCATION("EDUCATION", 16),
  MISC_SPENDINGS("MISC_SPENDINGS", 18),
  INCOME("INCOME", 19),
  GIFTS("GIFTS", 20);

  private Integer id;
  private String name;
  public static final Set<Integer> RESERVED_CATEGORY_IDS;

  static {
    RESERVED_CATEGORY_IDS =
      new HashSet<Integer>(Arrays.asList(MasterCategory.ALL.getId(), MasterCategory.NONE.getId(), MasterCategory.INTERNAL.getId()));
  }

  MasterCategory(String name, Integer id) {
    this.name = name;
    this.id = id;
  }

  public String getName() {
    return name.toLowerCase();
  }

  public Integer getId() {
    return id;
  }

  public static MasterCategory findMaster(Integer categoryId) throws ItemNotFound {
    if (categoryId == null) {
      return null;
    }
    for (MasterCategory master : MasterCategory.values()) {
      if (Utils.equal(master.id, categoryId)) {
        return master;
      }
    }
    return null;
  }

  public static MasterCategory getMaster(Integer categoryId) throws ItemNotFound {
    MasterCategory master = findMaster(categoryId);
    if (master == null) {
      throw new ItemNotFound("Unknown category " + categoryId);
    }
    return master;
  }

  public Key getKey() {
    return Key.create(Category.TYPE, id);
  }

  public static GlobList createGlobs() {
    GlobList result = new GlobList();
    for (MasterCategory master : values()) {
      result.add(
        GlobBuilder.init(Category.TYPE)
          .set(Category.ID, master.id)
          .set(Category.INNER_NAME, master.getName())
          .get());
    }
    return result;
  }

  public static boolean isReserved(Glob category) {
    return MasterCategory.RESERVED_CATEGORY_IDS.contains(category.get(Category.ID));
  }
}
