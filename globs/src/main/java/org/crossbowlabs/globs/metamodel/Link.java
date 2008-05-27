package org.crossbowlabs.globs.metamodel;

import org.crossbowlabs.globs.metamodel.links.FieldMappingFunctor;
import org.crossbowlabs.globs.metamodel.utils.Annotable;
import org.crossbowlabs.globs.model.FieldValues;
import org.crossbowlabs.globs.model.Key;

import java.io.Serializable;

public interface Link extends Annotable, Serializable {

  GlobType getSourceType();

  GlobType getTargetType();

  String getName();

  boolean isRequired();

  void apply(FieldMappingFunctor functor);

  Key getTargetKey(FieldValues values);
}
