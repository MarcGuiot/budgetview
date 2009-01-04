package org.globsframework.metamodel.utils;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.Link;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Required;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.index.*;
import org.globsframework.metamodel.links.DefaultLink;
import org.globsframework.metamodel.links.LinkBuilder;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.impl.ReadOnlyGlob;
import org.globsframework.model.utils.GlobConstantContainer;
import org.globsframework.utils.Strings;
import org.globsframework.utils.exceptions.InvalidParameter;
import org.globsframework.utils.exceptions.ItemAlreadyExists;
import org.globsframework.utils.exceptions.MissingInfo;
import org.globsframework.utils.exceptions.UnexpectedApplicationState;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

public class GlobTypeLoader {
  private DefaultGlobType type;
  private Map<String, Map<Class<? extends Annotation>, Annotation>> fieldAnnotations =
    new HashMap<String, Map<Class<? extends Annotation>, Annotation>>();
  private DefaultFieldFactory fieldFactory;
  private Class<?> targetClass;
  private String name;

  public static GlobTypeLoader init(Class<?> targetClass) {
    return init(targetClass, null);
  }

  public static GlobTypeLoader init(Class<?> targetClass, String name) {
    GlobTypeLoader loader = new GlobTypeLoader(targetClass, name);
    loader.run();
    loader.type.completeInit();
    return loader;
  }

  public GlobType getType() {
    return type;
  }

  public LinkBuilder defineLink(Link link) {
    return LinkBuilder.init((DefaultLink)link);
  }

  private GlobTypeLoader(Class<?> targetClass, String name) {
    this.targetClass = targetClass;
    this.name = name;
  }

  private void run() {
    checkClassIsNotAlreadyInitialized(targetClass);
    processClass(targetClass);
    processFields(targetClass);
    processLinks(targetClass);
    processConstants(targetClass);
    processIndex(targetClass);
    type.completeInit();
  }

  private void checkClassIsNotAlreadyInitialized(Class targetClass) {
    for (java.lang.reflect.Field classField : targetClass.getFields()) {
      try {
        if (classField.getType().equals(GlobType.class)) {
          if (classField.get(null) != null) {
            throw new UnexpectedApplicationState(targetClass.getName() + " already initialized");
          }
        }
      }
      catch (IllegalAccessException e) {
        throw GlobTypeLoader.getFieldAccessException(targetClass, classField, null, e);
      }
    }
  }

  private void processClass(Class targetClass) {
    Map<Class<? extends Annotation>, Annotation> annotationsByClass = new HashMap<Class<? extends Annotation>, Annotation>();

    for (java.lang.reflect.Field field : targetClass.getFields()) {
      if (field.getType().equals(GlobType.class)) {
        createType(field, annotationsByClass, targetClass);
      }
      else if (isGlobField(field) || isGlobLink(field)) {
        processFieldAnnotations(field);
      }
    }

    if (type == null) {
      throw new MissingInfo("Class " + targetClass.getName() +
                            " must have a TYPE field of class " + GlobType.class.getName());
    }
  }

  private void createType(java.lang.reflect.Field classField,
                          Map<Class<? extends Annotation>, Annotation> annotationsByClass,
                          Class<?> targetClass) {
    if (type != null) {
      throw new ItemAlreadyExists("Class " + targetClass.getName() +
                                  " must have only one TYPE field of class " + GlobType.class.getName());
    }

    this.type = new DefaultGlobType(getTypeName(targetClass), annotationsByClass);
    this.fieldFactory = new DefaultFieldFactory(type);
    GlobTypeLoader.setClassField(classField, type, targetClass);

    for (Annotation annotation : classField.getAnnotations()) {
      annotationsByClass.put(annotation.annotationType(), annotation);
    }
  }

  private void processFieldAnnotations(java.lang.reflect.Field field) {
    Map<Class<? extends Annotation>, Annotation> fieldAnnotationToClass =
      new HashMap<Class<? extends Annotation>, Annotation>(2, 1);
    for (Annotation annotation : field.getAnnotations()) {
      fieldAnnotationToClass.put(annotation.annotationType(), annotation);
    }
    fieldAnnotations.put(field.getName(), fieldAnnotationToClass);
  }

  private void processFields(Class<?> targetClass) {
    for (java.lang.reflect.Field classField : targetClass.getFields()) {
      if (isGlobField(classField)) {
        boolean isKeyField = classField.isAnnotationPresent(Key.class);
        Field field = fieldFactory.createField(getFieldName(classField),
                                               classField.getType(),
                                               isKeyField,
                                               fieldAnnotations.get(classField.getName()));
        setClassField(classField, field, targetClass);
      }
    }
  }

  private void processLinks(Class<?> targetClass) {
    for (java.lang.reflect.Field classField : targetClass.getFields()) {
      if (Link.class.isAssignableFrom(classField.getType()) &&
          !LinkField.class.isAssignableFrom(classField.getType())) {

        boolean required = classField.isAnnotationPresent(Required.class);
        DefaultLink link = new DefaultLink(type, getFieldName(classField),
                                           required,
                                           fieldAnnotations.get(classField.getName()));
        GlobTypeLoader.setClassField(classField, link, targetClass);
      }
    }
  }

  private void processConstants(Class<?> targetClass) {
    if (!targetClass.isEnum() && !GlobConstantContainer.class.isAssignableFrom(targetClass)) {
      return;
    }
    if (!targetClass.isEnum()) {
      throw new InvalidParameter("Class " + targetClass.getSimpleName() +
                                 " must be an enum in order to declare constants");
    }
    if (!GlobConstantContainer.class.isAssignableFrom(targetClass)) {
      throw new InvalidParameter("Class " + targetClass.getSimpleName() +
                                 " must implement " + GlobConstantContainer.class.getSimpleName() +
                                 " in order to declare constants");
    }
    for (GlobConstantContainer container : ((Class<GlobConstantContainer>)targetClass).getEnumConstants()) {
      type.addConstant(container.getGlob());
    }
  }

  private void processIndex(Class<?> targetClass) {
    for (java.lang.reflect.Field classField : targetClass.getFields()) {
      if (isUniqueIndexField(classField)) {
        Index index = fieldFactory.addUniqueIndex(classField.getName());
        setClassField(classField, index, targetClass);
        type.addIndex(index);
      }
      if (isNotUniqueIndexField(classField)) {
        Index index = fieldFactory.addNotUniqueIndex(classField.getName());
        setClassField(classField, index, targetClass);
        type.addIndex(index);
      }
      if (isMultiFieldNotUniqueIndexField(classField)) {
        MultiFieldNotUniqueIndex index = fieldFactory.addMultiFieldNotUniqueIndex(classField.getName());
        setClassField(classField, index, targetClass);
        type.addIndex(index);
      }
      if (isMultiFieldUniqueIndexField(classField)) {
        MultiFieldUniqueIndex index = fieldFactory.addMultiFieldUniqueIndex(classField.getName());
        setClassField(classField, index, targetClass);
        type.addIndex(index);
      }
    }
  }

  private boolean isMultiFieldUniqueIndexField(java.lang.reflect.Field field) {
    return MultiFieldUniqueIndex.class.isAssignableFrom(field.getType());
  }

  private boolean isMultiFieldNotUniqueIndexField(java.lang.reflect.Field field) {
    return MultiFieldNotUniqueIndex.class.isAssignableFrom(field.getType());
  }

  private boolean isNotUniqueIndexField(java.lang.reflect.Field field) {
    return NotUniqueIndex.class.isAssignableFrom(field.getType());
  }

  private boolean isUniqueIndexField(java.lang.reflect.Field field) {
    return UniqueIndex.class.isAssignableFrom(field.getType());
  }

  private static void setClassField(java.lang.reflect.Field classField, Object value, Class<?> targetClass) {
    try {
      classField.set(null, value);
    }
    catch (Exception e) {
      throw GlobTypeLoader.getFieldAccessException(targetClass, classField, value, e);
    }
  }

  private static RuntimeException getFieldAccessException(Class<?> targetClass, java.lang.reflect.Field classField, Object value, Exception e) {
    String valueDescription;
    if (value != null) {
      valueDescription = value.toString() + " (class " + value.getClass().getName() + ")";
    }
    else {
      valueDescription = "'null'";
    }
    return new RuntimeException("Unable to initialize field " + targetClass.getName() + "." + classField.getName() +
                                " with value " + valueDescription, e);
  }

  GlobTypeLoader addField(AbstractField field) throws ItemAlreadyExists {
    if (type.hasField(field.getName())) {
      throw new ItemAlreadyExists("Field " + field.getName() +
                                  " declared twice for type " + type.getName());
    }
    type.addField(field);
    return this;
  }

  private String getTypeName(Class<?> aClass) {
    if (name != null) {
      return name;
    }
    else {
      String fullName = aClass.getName();
      int lastSeparatorIndex = Math.max(fullName.lastIndexOf("."), fullName.lastIndexOf("$"));
      return Strings.uncapitalize(fullName.substring(lastSeparatorIndex + 1));
    }
  }

  private String getFieldName(java.lang.reflect.Field field) {
    if (field.getName().length() == 1) {
      return field.getName();
    }
    return Strings.toNiceLowerCase(field.getName());
  }

  private boolean isGlobField(java.lang.reflect.Field field) {
    return Field.class.isAssignableFrom(field.getType());
  }

  private boolean isGlobLink(java.lang.reflect.Field field) {
    return Link.class.isAssignableFrom(field.getType());
  }

  public void addConstants(GlobList globs) {
    for (Glob glob : globs) {
      if (glob instanceof ReadOnlyGlob) {
        type.addConstant((ReadOnlyGlob)glob);
      }
      else {
        type.addConstant(new ReadOnlyGlob(glob.getType(), glob.toArray()));
      }
    }
  }

  public void defineUniqueIndex(UniqueIndex index, Field field) {
    ((DefaultUniqueIndex)type.getIndex(index.getName())).setField(field);
  }

  public void defineNonUniqueIndex(NotUniqueIndex index, Field field) {
    ((DefaultNotUniqueIndex)type.getIndex(index.getName())).setField(field);
  }

  public void defineMultiFieldUniqueIndex(MultiFieldUniqueIndex index, Field... fields) {
    ((DefaultMultiFieldUniqueIndex)type.getMultiFieldIndex(index.getName())).setField(fields);
  }

  public void defineMultiFieldNotUniqueIndex(MultiFieldNotUniqueIndex index, Field... fields) {
    ((DefaultMultiFieldNotUniqueIndex)type.getMultiFieldIndex(index.getName())).setField(fields);
  }
}
