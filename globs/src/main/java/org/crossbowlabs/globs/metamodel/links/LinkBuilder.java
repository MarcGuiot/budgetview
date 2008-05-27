package org.crossbowlabs.globs.metamodel.links;

import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.metamodel.fields.*;

public class LinkBuilder {
  private DefaultLink link;

  public static LinkBuilder init(DefaultLink link) {
    return new LinkBuilder(link);
  }

  private LinkBuilder(DefaultLink link) {
    this.link = link;
  }

  public LinkBuilder add(StringField sourceField, StringField targetField) {
    return doAdd(sourceField, targetField);
  }

  public LinkBuilder add(IntegerField sourceField, IntegerField targetField) {
    return doAdd(sourceField, targetField);
  }

  public LinkBuilder add(LongField sourceField, LongField targetField) {
    return doAdd(sourceField, targetField);
  }

  public LinkBuilder add(DateField sourceField, DateField targetField) {
    return doAdd(sourceField, targetField);
  }

  public LinkBuilder add(DoubleField sourceField, DoubleField targetField) {
    return doAdd(sourceField, targetField);
  }

  public LinkBuilder add(BooleanField sourceField, BooleanField targetField) {
    return doAdd(sourceField, targetField);
  }

  private LinkBuilder doAdd(Field sourceField, Field targetField) {
    link.addFieldMapping(sourceField, targetField);
    return this;
  }
}
