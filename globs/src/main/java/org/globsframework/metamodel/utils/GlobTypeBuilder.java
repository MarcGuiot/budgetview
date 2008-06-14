package org.globsframework.metamodel.utils;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.NamingField;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class GlobTypeBuilder {
  private DefaultGlobType type;
  private DefaultFieldFactory factory;

  public static GlobTypeBuilder init(String typeName) {
    return new GlobTypeBuilder(typeName);
  }

  public GlobTypeBuilder(String typeName) {
    type = new DefaultGlobType(typeName, Collections.EMPTY_MAP);
    factory = new DefaultFieldFactory(type);
  }

  public GlobTypeBuilder addIntegerKey(String fieldName) {
    factory.addInteger(fieldName, true, Collections.EMPTY_MAP);
    return this;
  }

  public GlobTypeBuilder addNamingField(String fieldName) {
    Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
    annotations.put(NamingField.class, new NamingField() {
      public Class<? extends Annotation> annotationType() {
        return NamingField.class;
      }
    });
    factory.addString(fieldName, false, annotations);
    return this;
  }

  public GlobTypeBuilder addStringField(String fieldName) {
    factory.addString(fieldName, false, Collections.EMPTY_MAP);
    return this;
  }

  public GlobTypeBuilder addIntegerField(String fieldName) {
    factory.addInteger(fieldName, false, Collections.EMPTY_MAP);
    return this;
  }

  public GlobTypeBuilder addDoubleField(String fieldName) {
    factory.addDouble(fieldName, false, Collections.EMPTY_MAP);
    return this;
  }

  public GlobTypeBuilder addLongField(String fieldName) {
    factory.addLong(fieldName, false, Collections.EMPTY_MAP);
    return this;
  }

  public GlobTypeBuilder addBooleanField(String fieldName) {
    factory.addBoolean(fieldName, false, Collections.EMPTY_MAP);
    return this;
  }

  public GlobTypeBuilder addBlobField(String fieldName) {
    factory.addBlob(fieldName, false, Collections.EMPTY_MAP);
    return this;
  }

  public GlobTypeBuilder addTimestampField(String fieldName) {
    factory.addTimestamp(fieldName, false, Collections.EMPTY_MAP);
    return this;
  }

  public GlobTypeBuilder addDateField(String fieldName) {
    factory.addDate(fieldName, false, Collections.EMPTY_MAP);
    return this;
  }

  public GlobTypeBuilder addLinkField(String fieldName) {
    factory.addLink(fieldName, false, Collections.EMPTY_MAP);
    return this;
  }

  public GlobType get() {
    type.completeInit();
    return type;
  }
}
