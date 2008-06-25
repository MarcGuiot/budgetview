package org.designup.picsou.model;

import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobBuilder;
import org.globsframework.utils.Utils;
import org.globsframework.utils.exceptions.ItemNotFound;

import java.util.Arrays;
import java.util.List;

public enum MasterCategory {
  NONE(0),
  ALL(1),
  FOOD(2),
  TAXES(3),
  HOUSE(4),
  HEALTH(5),
  LEISURES(6),
  PUERICULTURE(7),
  TELECOMS(8),
  BANK(9),
  TRANSPORTS(10),
  INTERNAL(11),
  CLOTHING(12),
  SAVINGS(13),
  MULTIMEDIA(14),
  BEAUTY(15),
  EDUCATION(16),
  MISC_SPENDINGS(18),
  INCOME(19),
  GIFTS(20);

  private Integer id;
  public static final List<Integer> RESERVED_CATEGORY_IDS;

  static {
    RESERVED_CATEGORY_IDS =
      Arrays.asList(MasterCategory.ALL.getId(), MasterCategory.NONE.getId(), MasterCategory.INTERNAL.getId());
  }

  MasterCategory(Integer id) {
    this.id = id;
  }

  public String getName() {
    return name().toLowerCase();
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
          .set(Category.ID, master.getId())
          .set(Category.NAME, master.getName())
          .get());
    }
    return result;
  }

  public static boolean isReserved(Glob category) {
    return MasterCategory.RESERVED_CATEGORY_IDS.contains(category.get(Category.ID));
  }
}
