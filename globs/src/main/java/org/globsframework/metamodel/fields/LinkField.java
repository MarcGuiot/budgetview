package org.globsframework.metamodel.fields;

import org.globsframework.metamodel.Link;

/**
 * A {@link Link} whose target field is a single {@link IntegerField}.
 */
public interface LinkField extends IntegerField, Link {
  IntegerField getTargetKeyField();
}
