package com.budgetview.model.util;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.index.MultiFieldNotUniqueIndex;
import org.globsframework.metamodel.index.MultiFieldUniqueIndex;
import org.globsframework.metamodel.index.NotUniqueIndex;
import org.globsframework.metamodel.index.UniqueIndex;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class TypeLoader {

  // Used instead of GlobTypeLoader to make user that a hardcoded name is provided
  // so that serialization does not get compromised by obfuscated names

  public static TypeLoader init(Class<?> targetClass, String name) {
    return new TypeLoader(GlobTypeLoader.init(targetClass, name));
  }

  private GlobTypeLoader loader;

  private TypeLoader(GlobTypeLoader loader) {
    this.loader = loader;
  }

  public void defineUniqueIndex(UniqueIndex index, Field field) {
    loader.defineUniqueIndex(index, field);
  }

  public void defineNonUniqueIndex(NotUniqueIndex index, Field field) {
    loader.defineNonUniqueIndex(index, field);
  }

  public void defineMultiFieldUniqueIndex(MultiFieldUniqueIndex index, Field... fields) {
    loader.defineMultiFieldUniqueIndex(index, fields);
  }

  public void defineMultiFieldNotUniqueIndex(MultiFieldNotUniqueIndex index, Field... fields) {
    loader.defineMultiFieldNotUniqueIndex(index, fields);
  }
}
