package org.crossbowlabs.globs.metamodel.utils;

import java.lang.annotation.Annotation;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.metamodel.Link;
import org.crossbowlabs.globs.metamodel.index.*;
import org.crossbowlabs.globs.metamodel.annotations.DefaultBoolean;
import org.crossbowlabs.globs.metamodel.annotations.DefaultDate;
import org.crossbowlabs.globs.metamodel.annotations.DefaultDouble;
import org.crossbowlabs.globs.metamodel.annotations.DefaultInteger;
import org.crossbowlabs.globs.metamodel.annotations.DefaultLong;
import org.crossbowlabs.globs.metamodel.annotations.MaxSize;
import org.crossbowlabs.globs.metamodel.annotations.Target;
import org.crossbowlabs.globs.metamodel.fields.BlobField;
import org.crossbowlabs.globs.metamodel.fields.BooleanField;
import org.crossbowlabs.globs.metamodel.fields.DateField;
import org.crossbowlabs.globs.metamodel.fields.DoubleField;
import org.crossbowlabs.globs.metamodel.fields.FieldValueVisitor;
import org.crossbowlabs.globs.metamodel.fields.FieldVisitor;
import org.crossbowlabs.globs.metamodel.fields.IntegerField;
import org.crossbowlabs.globs.metamodel.fields.LinkField;
import org.crossbowlabs.globs.metamodel.fields.LongField;
import org.crossbowlabs.globs.metamodel.fields.StringField;
import org.crossbowlabs.globs.metamodel.fields.TimeStampField;
import org.crossbowlabs.globs.metamodel.links.DefaultLink;
import org.crossbowlabs.globs.metamodel.links.FieldMappingFunctor;
import org.crossbowlabs.globs.metamodel.links.LinkVisitor;
import org.crossbowlabs.globs.utils.exceptions.InvalidParameter;
import org.crossbowlabs.globs.utils.exceptions.MissingInfo;
import org.crossbowlabs.globs.utils.exceptions.UnexpectedApplicationState;
import org.crossbowlabs.globs.model.Key;
import org.crossbowlabs.globs.model.FieldValues;
import org.crossbowlabs.globs.model.KeyBuilder;
import org.crossbowlabs.globs.model.impl.SingleFieldKey;

class DefaultFieldFactory {
  private MutableGlobType type;

  private static Class[] defaultValuesAnnotations =
        {DefaultInteger.class, DefaultBoolean.class, DefaultDouble.class, DefaultDate.class,
         DefaultLong.class};

  DefaultFieldFactory(MutableGlobType type) {
    this.type = type;
  }

  public Field createField(String name,
                           Class<?> fieldClass,
                           boolean isKeyField,
                           Map<Class<? extends Annotation>, Annotation> annotations) {
    if (StringField.class.isAssignableFrom(fieldClass)) {
      return addString(name, isKeyField, annotations);
    }
    else if (LinkField.class.isAssignableFrom(fieldClass)) {
      return addLink(name, isKeyField, annotations);
    }
    else if (IntegerField.class.isAssignableFrom(fieldClass)) {
      return addInteger(name, isKeyField, annotations);
    }
    else if (LongField.class.isAssignableFrom(fieldClass)) {
      return addLong(name, isKeyField, annotations);
    }
    else if (BooleanField.class.isAssignableFrom(fieldClass)) {
      return addBoolean(name, isKeyField, annotations);
    }
    else if (DateField.class.isAssignableFrom(fieldClass)) {
      return addDate(name, isKeyField, annotations);
    }
    else if (DoubleField.class.isAssignableFrom(fieldClass)) {
      return addDouble(name, isKeyField, annotations);
    }
    else if (TimeStampField.class.isAssignableFrom(fieldClass)) {
      return addTimestamp(name, isKeyField, annotations);
    }
    else if (BlobField.class.isAssignableFrom(fieldClass)) {
      return addBlob(name, isKeyField, annotations);
    }
    throw new UnexpectedApplicationState("unknown field type " + type);
  }

  public Field addLink(String name,
                       boolean isKeyField,
                       Map<Class<? extends Annotation>, Annotation> annotations) {
    return add(new DefaultLinkField(name, type, annotations), isKeyField);
  }

  public IntegerField addInteger(String name,
                                 boolean isKeyField,
                                 Map<Class<? extends Annotation>, Annotation> annotations) {
    return add(new DefaultIntegerField(name, type, annotations), isKeyField);
  }

  public LongField addLong(String name,
                           boolean isKeyField,
                           Map<Class<? extends Annotation>, Annotation> annotations) {
    return add(new DefaultLongField(name, type, annotations), isKeyField);
  }

  public TimeStampField addTimestamp(String name,
                                     boolean isKeyField,
                                     Map<Class<? extends Annotation>, Annotation> annotations) {
    return add(new DefaultTimeStampField(name, type, annotations), isKeyField);
  }

  public DateField addDate(String name,
                           boolean isKeyField,
                           Map<Class<? extends Annotation>, Annotation> annotations) {
    return add(new DefaultDateField(name, type, annotations), isKeyField);
  }

  public DoubleField addDouble(String name,
                               boolean isKeyField,
                               Map<Class<? extends Annotation>, Annotation> annotations) {
    return add(new DefaultDoubleField(name, type, annotations), isKeyField);
  }

  public StringField addString(String name,
                               boolean isKeyField,
                               Map<Class<? extends Annotation>, Annotation> annotations) {
    return add(new DefaultStringField(name, type, annotations), isKeyField);
  }

  public BooleanField addBoolean(String name,
                                 boolean isKeyField,
                                 Map<Class<? extends Annotation>, Annotation> annotations) {
    return add(new DefaultBooleanField(name, type, annotations), isKeyField);
  }

  public BlobField addBlob(String name,
                           boolean isKeyField,
                           Map<Class<? extends Annotation>, Annotation> annotations) {
    return add(new DefaultBlobField(name, type, annotations), isKeyField);
  }

  private <T extends AbstractField> T add(T field, boolean isKeyField) {
    type.addField(field);
    if (isKeyField) {
      type.addKey(field);
    }
    if (field instanceof Link) {
      type.addLink((Link)field);
    }
    field.setKeyField(isKeyField);
    return field;
  }

  public UniqueIndex addUniqueIndex(String name) {
    return new DefaultUniqueIndex(name);
  }

  public NotUniqueIndex addNotUniqueIndex(String name) {
    return new DefaultNotUniqueIndex(name);
  }

  public MultiFieldNotUniqueIndex addMultiFieldNotUniqueIndex(String name){
    return new DefaultMultiFieldNotUniqueIndex(name);
  }

  public MultiFieldUniqueIndex addMultiFieldUniqueIndex(String name) {
    return new DefaultMultiFieldUniqueIndex(name);
  }

  private static class DefaultIntegerField extends AbstractField implements IntegerField {
    private DefaultIntegerField(String name, GlobType globType,
                                Map<Class<? extends Annotation>, Annotation> annotations) {
      super(name, globType, Integer.class, annotations);
      setDefaultValue(computeDefaultValue(this, annotations, DefaultInteger.class));
    }

    public void visit(FieldVisitor visitor) throws Exception {
      visitor.visitInteger(this);
    }

    public void safeVisit(FieldVisitor visitor) {
      try {
        visitor.visitInteger(this);
      }
      catch (RuntimeException e) {
        throw e;
      }
      catch (Exception e) {
        throw new UnexpectedApplicationState(e);
      }
    }

    public void safeVisit(FieldValueVisitor visitor, Object value) {
      try {
        visitor.visitInteger(this, (Integer)value);
      }
      catch (RuntimeException e) {
        throw e;
      }
      catch (Exception e) {
        throw new UnexpectedApplicationState(e);
      }
    }
  }

  private static class DefaultLinkField extends AbstractField implements LinkField {
    private GlobType targetType;
    private IntegerField targetKeyField;

    private DefaultLinkField(String name, GlobType globType,
                             Map<Class<? extends Annotation>, Annotation> annotations) {
      super(name, globType, Integer.class, annotations);

      if (!annotations.containsKey(Target.class)) {
        throw new MissingInfo("Annotation " + Target.class.getName() +
                              " must be specified for LinkField '" + name +
                              "' for type: " + globType.getName());
      }
      Class targetClass = ((Target)annotations.get(Target.class)).value();
      try {
        targetType = GlobTypeUtils.getType(targetClass);
      }
      catch (InvalidParameter e) {
        throw new InvalidParameter("LinkField '" + name + "' in type '" + globType.getName() +
                                   "' cannot reference target class '" + targetClass.getName() + "' because "
                                   +
                                   "it does not define a Glob type");
      }
      List<Field> keyFields = targetType.getKeyFields();
      if (keyFields.size() != 1) {
        throw new InvalidParameter("LinkField '" + name + "' in type '" + globType.getName() +
                                   "' cannot reference target type '" + targetType.getName() +
                                   "' because it uses a composite key");
      }
      Field field = keyFields.get(0);
      if (!(field instanceof IntegerField)) {
        throw new InvalidParameter("LinkField '" + name + "' in type '" + globType.getName() +
                                   "' cannot reference target type '" + targetType.getName() +
                                   "' because it does not use an integer key");
      }
      targetKeyField = (IntegerField)field;
    }

    public IntegerField getTargetKeyField() {
      return targetKeyField;
    }

    public void visit(FieldVisitor visitor) throws Exception {
      visitor.visitLink(this);
    }

    public void safeVisit(FieldVisitor visitor) {
      try {
        visitor.visitLink(this);
      }
      catch (RuntimeException e) {
        throw e;
      }
      catch (Exception e) {
        throw new UnexpectedApplicationState(e);
      }
    }

    public void safeVisit(FieldValueVisitor visitor, Object value) {
      try {
        visitor.visitInteger(this, (Integer)value);
      }
      catch (RuntimeException e) {
        throw e;
      }
      catch (Exception e) {
        throw new UnexpectedApplicationState(e);
      }
    }

    public GlobType getSourceType() {
      return getGlobType();
    }

    public GlobType getTargetType() {
      return targetType;
    }

    public void apply(FieldMappingFunctor functor) {
      functor.process(this, targetKeyField);
    }

    public void visit(LinkVisitor linkVisitor) {
      linkVisitor.visitLink(this);
    }

    public Key getTargetKey(FieldValues values) {
      Object value = values.getValue(this);
      if (value == null) {
        return null;
      }
      return new SingleFieldKey(targetKeyField, value);
    }

    public String toString() {
      return DefaultLink.toString(getName(), getSourceType(), getTargetType());
    }
  }

  private static class DefaultLongField extends AbstractField implements LongField {
    private DefaultLongField(String name, GlobType globType,
                             Map<Class<? extends Annotation>, Annotation> annotations) {
      super(name, globType, Long.class, annotations);
      setDefaultValue(computeDefaultValue(this, annotations, DefaultLong.class));
    }

    public void visit(FieldVisitor visitor) throws Exception {
      visitor.visitLong(this);
    }

    public void safeVisit(FieldVisitor visitor) {
      try {
        visitor.visitLong(this);
      }
      catch (RuntimeException e) {
        throw e;
      }
      catch (Exception e) {
        throw new UnexpectedApplicationState(e);
      }
    }

    public void safeVisit(FieldValueVisitor visitor, Object value) {
      try {
        visitor.visitLong(this, (Long)value);
      }
      catch (RuntimeException e) {
        throw e;
      }
      catch (Exception e) {
        throw new UnexpectedApplicationState(e);
      }
    }
  }

  private static class DefaultDoubleField extends AbstractField implements DoubleField {
    public DefaultDoubleField(String name, GlobType globType,
                              Map<Class<? extends Annotation>, Annotation> annotations) {
      super(name, globType, Double.class, annotations);
      setDefaultValue(computeDefaultValue(this, annotations, DefaultDouble.class));
    }

    public void visit(FieldVisitor visitor) throws Exception {
      visitor.visitDouble(this);
    }

    public void safeVisit(FieldVisitor visitor) {
      try {
        visitor.visitDouble(this);
      }
      catch (RuntimeException e) {
        throw e;
      }
      catch (Exception e) {
        throw new UnexpectedApplicationState(e);
      }
    }

    public void safeVisit(FieldValueVisitor visitor, Object value) {
      try {
        visitor.visitDouble(this, (Double)value);
      }
      catch (RuntimeException e) {
        throw e;
      }
      catch (Exception e) {
        throw new UnexpectedApplicationState(e);
      }
    }
  }

  private static class DefaultBooleanField extends AbstractField implements BooleanField {
    public DefaultBooleanField(String name, GlobType globType,
                               Map<Class<? extends Annotation>, Annotation> annotations) {
      super(name, globType, Boolean.class, annotations);
      setDefaultValue(computeDefaultValue(this, annotations, DefaultBoolean.class));
    }

    public void visit(FieldVisitor visitor) throws Exception {
      visitor.visitBoolean(this);
    }

    public void safeVisit(FieldVisitor visitor) {
      try {
        visitor.visitBoolean(this);
      }
      catch (RuntimeException e) {
        throw e;
      }
      catch (Exception e) {
        throw new UnexpectedApplicationState(e);
      }
    }

    public void safeVisit(FieldValueVisitor visitor, Object value) {
      try {
        visitor.visitBoolean(this, (Boolean)value);
      }
      catch (RuntimeException e) {
        throw e;
      }
      catch (Exception e) {
        throw new UnexpectedApplicationState(e);
      }
    }
  }

  private static class DefaultStringField extends AbstractField implements StringField {
    private int maxSize;

    public DefaultStringField(String name, GlobType globType,
                              Map<Class<? extends Annotation>, Annotation> annotations) {
      super(name, globType, String.class, annotations);
      if (annotations.containsKey(MaxSize.class)) {
        MaxSize annotationSize = (MaxSize)annotations.get(MaxSize.class);
        maxSize = annotationSize.value();
      }
      else {
        maxSize = 255;
      }
    }

    public void visit(FieldVisitor visitor) throws Exception {
      visitor.visitString(this);
    }

    public void safeVisit(FieldVisitor visitor) {
      try {
        visitor.visitString(this);
      }
      catch (RuntimeException e) {
        throw e;
      }
      catch (Exception e) {
        throw new UnexpectedApplicationState(e);
      }
    }

    public void safeVisit(FieldValueVisitor visitor, Object value) {
      try {
        visitor.visitString(this, (String)value);
      }
      catch (RuntimeException e) {
        throw e;
      }
      catch (Exception e) {
        throw new UnexpectedApplicationState(e);
      }
    }

    public int getMaxSize() {
      return maxSize;
    }
  }

  private static class DefaultDateField extends AbstractField implements DateField {
    public DefaultDateField(String name, GlobType globType,
                            Map<Class<? extends Annotation>, Annotation> annotations) {
      super(name, globType, Date.class, annotations);
      setDefaultValue(computeDefaultValue(this, annotations, DefaultDate.class));
    }

    public void visit(FieldVisitor visitor) throws Exception {
      visitor.visitDate(this);
    }

    public void safeVisit(FieldVisitor visitor) {
      try {
        visitor.visitDate(this);
      }
      catch (RuntimeException e) {
        throw e;
      }
      catch (Exception e) {
        throw new UnexpectedApplicationState(e);
      }
    }

    public void safeVisit(FieldValueVisitor visitor, Object value) {
      try {
        visitor.visitDate(this, (Date)value);
      }
      catch (RuntimeException e) {
        throw e;
      }
      catch (Exception e) {
        throw new UnexpectedApplicationState(e);
      }
    }
  }

  private static class DefaultTimeStampField extends AbstractField implements TimeStampField {
    public DefaultTimeStampField(String name, GlobType globType,
                                 Map<Class<? extends Annotation>, Annotation> annotations) {
      super(name, globType, Date.class, annotations);
      setDefaultValue(computeDefaultValue(this, annotations, DefaultDate.class));
    }

    public void visit(FieldVisitor visitor) throws Exception {
      visitor.visitTimeStamp(this);
    }

    public void safeVisit(FieldVisitor visitor) {
      try {
        visitor.visitTimeStamp(this);
      }
      catch (RuntimeException e) {
        throw e;
      }
      catch (Exception e) {
        throw new UnexpectedApplicationState(e);
      }
    }

    public void safeVisit(FieldValueVisitor visitor, Object value) {
      try {
        visitor.visitTimeStamp(this, (Date)value);
      }
      catch (RuntimeException e) {
        throw e;
      }
      catch (Exception e) {
        throw new UnexpectedApplicationState(e);
      }
    }
  }

  private static class DefaultBlobField extends AbstractField implements BlobField {
    private int maxSize;

    public DefaultBlobField(String name, GlobType globType,
                            Map<Class<? extends Annotation>, Annotation> annotations) {
      super(name, globType, byte[].class, annotations);
      if (annotations.containsKey(MaxSize.class)) {
        MaxSize annotationSize = (MaxSize)annotations.get(MaxSize.class);
        maxSize = annotationSize.value();
      }
      else {
        maxSize = 40;
      }
    }

    public void visit(FieldVisitor visitor) throws Exception {
      visitor.visitBlob(this);
    }

    public void safeVisit(FieldVisitor visitor) {
      try {
        visitor.visitBlob(this);
      }
      catch (RuntimeException e) {
        throw e;
      }
      catch (Exception e) {
        throw new UnexpectedApplicationState(e);
      }
    }

    public void safeVisit(FieldValueVisitor visitor, Object value) {
      try {
        visitor.visitBlob(this, (byte[])value);
      }
      catch (RuntimeException e) {
        throw e;
      }
      catch (Exception e) {
        throw new UnexpectedApplicationState(e);
      }
    }

    public int getMaxSize() {
      return maxSize;
    }
  }

  private static Object computeDefaultValue(Field field,
                                            Map<Class<? extends Annotation>, Annotation> annotations,
                                            Class<? extends Annotation> targetAnnotationClass) {
    for (Class annotationClass : defaultValuesAnnotations) {
      Annotation annotation = annotations.get(annotationClass);
      if (annotation == null) {
        continue;
      }
      if (!annotationClass.equals(targetAnnotationClass)) {
        throw new InvalidParameter("Field " + field.getGlobType().getName() + "." + field.getName() +
                                   " should declare a default value with annotation @" +
                                   targetAnnotationClass.getSimpleName() +
                                   " instead of @" + annotationClass.getSimpleName());
      }
      if (targetAnnotationClass.equals(DefaultDate.class)) {
        return new Date();
      }
      try {
        return annotationClass.getMethod("value").invoke(annotation);
      }
      catch (Exception e) {
        throw new InvalidParameter("Cannot determine default value for field: " + field, e);
      }
    }
    return null;
  }
}
