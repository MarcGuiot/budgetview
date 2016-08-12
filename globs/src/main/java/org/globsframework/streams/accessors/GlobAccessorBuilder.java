package org.globsframework.streams.accessors;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.*;
import org.globsframework.streams.accessors.utils.AbstractGlobAccessor;
import org.globsframework.utils.exceptions.InvalidParameter;
import org.globsframework.utils.exceptions.ItemNotFound;

import java.util.Date;

public class GlobAccessorBuilder {

  private GlobType type;
  private Object[] values;
  private GlobAccessor accessor;

  public GlobAccessorBuilder(GlobType type) {
    values = new Object[type.getFieldCount()];
    this.type = type;
  }

  public GlobAccessor getAccessor() {
    if (accessor == null) {
      accessor = new AbstractGlobAccessor(type) {
        protected Object doGet(Field field) {
          if (!type.equals(field.getGlobType())) {
            throw new InvalidParameter("Field " + field.getName() + " of type + " + field.getGlobType().getName() + " not available in accessor for type " + type.getName());
          }
          return values[field.getIndex()];
        }

        public boolean contains(Field field) {
          return type.equals(field.getGlobType());
        }
      };
    }
    return accessor;
  }

  public GlobAccessorBuilder set(DoubleField field, Double value) throws ItemNotFound {
    return setValue(field, value);
  }

  public GlobAccessorBuilder set(DateField field, Date value) throws ItemNotFound {
    return setValue(field, value);
  }

  public GlobAccessorBuilder set(TimeStampField field, Date value) throws ItemNotFound {
    return setValue(field, value);
  }

  public GlobAccessorBuilder set(IntegerField field, Integer value) throws ItemNotFound {
    return setValue(field, value);
  }

  public GlobAccessorBuilder set(LinkField field, Integer value) throws ItemNotFound {
    return setValue(field, value);
  }

  public GlobAccessorBuilder set(StringField field, String value) throws ItemNotFound {
    return setValue(field, value);
  }

  public GlobAccessorBuilder set(BooleanField field, Boolean value) throws ItemNotFound {
    return setValue(field, value);
  }

  public GlobAccessorBuilder set(LongField field, Long value) throws ItemNotFound {
    return setValue(field, value);
  }

  public GlobAccessorBuilder set(BlobField field, byte[] value) throws ItemNotFound {
    return setValue(field, value);
  }

  public GlobAccessorBuilder clear(Field field) {
    return setValue(field, null);
  }

  public GlobAccessorBuilder setValue(Field field, Object value) throws ItemNotFound {
    if (!type.equals(field.getGlobType())) {
      throw new InvalidParameter("Field " + field.getName() + " of type + " + field.getGlobType().getName() + " not cannot be set on accessor for type " + type.getName());
    }
    values[field.getIndex()] = value;
    return this;
  }

}
