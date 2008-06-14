package org.globsframework.metamodel.utils;

import org.globsframework.utils.exceptions.ItemNotFound;

import java.lang.annotation.Annotation;
import java.util.Map;

public class AnnotatedPropertyHolder<T> extends DefaultPropertyHolder<T> implements Annotable {
  private Map<Class<? extends Annotation>, Annotation> annotations;

  public AnnotatedPropertyHolder(Map<Class<? extends Annotation>, Annotation> annotations) {
    this.annotations = annotations;
  }

  public boolean hasAnnotation(Class<? extends Annotation> annotationClass) {
    return annotations.get(annotationClass) != null;
  }

  public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
    Annotation annotation = annotations.get(annotationClass);
    if (annotation != null) {
      return (A)annotation;
    }
    throw new ItemNotFound("Annotation '" + annotationClass.getName() + "' not registered on field " + this);
  }

  public <A extends Annotation> A findAnnotation(Class<A> annotationClass) {
    return (A)annotations.get(annotationClass);
  }

}
