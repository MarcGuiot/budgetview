package org.globsframework.metamodel;

import org.globsframework.metamodel.links.FieldMappingFunctor;
import org.globsframework.metamodel.utils.Annotable;
import org.globsframework.model.FieldValues;
import org.globsframework.model.Key;

import java.io.Serializable;

public interface Link extends Annotable, Serializable {

  GlobType getSourceType();

  GlobType getTargetType();

  String getName();

  boolean isRequired();

  void apply(FieldMappingFunctor functor);

  Key getTargetKey(FieldValues values);
}
