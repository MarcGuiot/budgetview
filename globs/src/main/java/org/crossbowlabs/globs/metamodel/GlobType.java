package org.crossbowlabs.globs.metamodel;

import org.crossbowlabs.globs.metamodel.index.Index;
import org.crossbowlabs.globs.metamodel.properties.PropertyHolder;
import org.crossbowlabs.globs.metamodel.utils.Annotable;
import org.crossbowlabs.globs.model.GlobList;
import org.crossbowlabs.globs.utils.exceptions.ItemNotFound;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.List;

public interface GlobType extends PropertyHolder<GlobType>, Annotable, Serializable {

  String getName();

  Field getField(String name) throws ItemNotFound;

  Field findField(String name);

  boolean hasField(String name);

  Field[] getFields();

  Field getField(int index);

  int getFieldCount();

  List<Field> getKeyFields();

  boolean isKeyField(Field field);

  Link getOutboundLink(String name);

  Link findOutboundLink(String name);

  Link[] getOutboundLinks();

  Link[] getInboundLinks();

  Field[] getFieldsWithAnnotation(Class<? extends Annotation> annotationClass);

  GlobList getConstants();

  Iterable<Index> getIndices();
}
