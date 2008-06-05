package org.designup.picsou.model;

import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.metamodel.annotations.Key;
import org.crossbowlabs.globs.metamodel.annotations.NamingField;
import org.crossbowlabs.globs.metamodel.annotations.Target;
import org.crossbowlabs.globs.metamodel.fields.BooleanField;
import org.crossbowlabs.globs.metamodel.fields.IntegerField;
import org.crossbowlabs.globs.metamodel.fields.LinkField;
import org.crossbowlabs.globs.metamodel.fields.StringField;
import org.crossbowlabs.globs.metamodel.utils.GlobTypeLoader;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.ReadOnlyGlobRepository;
import org.crossbowlabs.globs.model.utils.GlobMatchers;

public class Category {

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @NamingField
  public static StringField NAME;

  @Target(Category.class)
  public static LinkField MASTER;

  public static BooleanField SYSTEM;

  public static final Integer ALL = MasterCategory.ALL.getId();
  public static final Integer NONE = MasterCategory.NONE.getId();

  static {
    GlobTypeLoader loader = GlobTypeLoader.init(Category.class);
    loader.addConstants(MasterCategory.createGlobs());
  }

  public static boolean isAll(Glob category) {
    return (category != null) && ALL.equals(category.get(Category.ID));
  }

  public static boolean isNone(Glob category) {
    return (category != null) && NONE.equals(category.get(Category.ID));
  }

  public static Integer findId(String categoryName, ReadOnlyGlobRepository repository) {
    Glob category = find(categoryName, repository);
    if (category == null) {
      return null;
    }
    return category.get(Category.ID);
  }

  public static Glob find(String categoryName, ReadOnlyGlobRepository repository) {
    return repository.findUnique(Category.TYPE, GlobMatchers.fieldEqualsIgnoreCase(Category.NAME, categoryName));
  }

  public static boolean isMaster(Glob category) {
    return category.get(MASTER) == null;
  }

  public static Glob findByName(String name, GlobRepository repository) {
    return repository.findUnique(TYPE, GlobMatchers.fieldEqualsIgnoreCase(NAME, name));
  }

  public static String getName(Integer categoryId, GlobRepository repository) {
    Glob category = repository.get(org.crossbowlabs.globs.model.Key.create(TYPE, categoryId));
    return category.get(NAME);
  }

  public static boolean hasChildren(Integer categoryId, GlobRepository repository) {
    for (Glob category : repository.getAll(Category.TYPE)) {
      if (categoryId.equals(category.get(Category.MASTER))) {
        return true;
      }
    }
    return false;
  }

  public static boolean isSystem(Glob category) {
    return Boolean.TRUE.equals(category.get(Category.SYSTEM));
  }

  public static boolean isReserved(Glob category) {
    return MasterCategory.isReserved(category);
  }
}
