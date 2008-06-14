package org.designup.picsou.gui.categories;

import org.designup.picsou.model.Category;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.utils.Utils;

import java.util.Comparator;

public class CategoryComparator implements Comparator<Glob> {
  private static final Integer ALL_CATEGORY_ID = Category.ALL;
  private static final Integer NO_CATEGORY_ID = Category.NONE;

  private GlobRepository repository;
  private GlobStringifier nameStringifier;

  public CategoryComparator(GlobRepository globRepository, GlobStringifier stringifier) {
    this.repository = globRepository;
    this.nameStringifier = stringifier;
  }

  public int compare(Glob category1, Glob category2) {
    if (Utils.equal(category1.get(Category.ID), ALL_CATEGORY_ID)) {
      return -1;
    }
    if (Utils.equal(category2.get(Category.ID), ALL_CATEGORY_ID)) {
      return 1;
    }
    if (Utils.equal(category1.get(Category.ID), NO_CATEGORY_ID)) {
      return -1;
    }
    if (Utils.equal(category2.get(Category.ID), NO_CATEGORY_ID)) {
      return 1;
    }

    String name1 = getName(category1);
    String name2 = getName(category2);

    Glob master1 = getMaster(category1);
    Glob master2 = getMaster(category2);

    boolean category1IsMaster = master1 == null;
    boolean category2IsMaster = master2 == null;

    if (!category1IsMaster && !category2IsMaster && !master1.equals(master2)) {
      return compare(master1, master2);
    }

    if (category1IsMaster && !category2IsMaster) {
      if (category1.equals(master2)) {
        return -1;
      }
      return compare(category1, master2);
    }

    if (!category1IsMaster && category2IsMaster) {
      if (master1.equals(category2)) {
        return 1;
      }
      return compare(master1, category2);
    }

    return name1.compareTo(name2);
  }

  private Glob getMaster(Glob category) {
    Integer masterId = category.get(Category.MASTER);
    if (masterId != null) {
      return repository.get(Key.create(Category.TYPE, masterId));
    }
    return null;
  }

  protected String getName(Glob category) {
    return nameStringifier.toString(category, repository);
  }
}
