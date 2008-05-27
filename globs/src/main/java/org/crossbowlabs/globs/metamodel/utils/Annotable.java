package org.crossbowlabs.globs.metamodel.utils;

import java.lang.annotation.Annotation;

public interface Annotable {

  boolean hasAnnotation(Class<? extends Annotation> annotationClass);

  <A extends Annotation> A getAnnotation(Class<A> annotationClass);
}
