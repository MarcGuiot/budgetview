package org.crossbowlabs.globs.metamodel.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.ElementType;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@java.lang.annotation.Target({ElementType.FIELD})
public @interface ContainmentLink {
}
