package org.crossbowlabs.globs.metamodel.fields;

import org.crossbowlabs.globs.metamodel.Link;

/**
 * A {@link Link} whose target field is a single {@link IntegerField}.
 */
public interface LinkField extends IntegerField, Link {
  IntegerField getTargetKeyField();
}
