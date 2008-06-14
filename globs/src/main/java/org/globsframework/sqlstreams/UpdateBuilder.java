package org.globsframework.sqlstreams;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.fields.*;
import org.globsframework.streams.accessors.*;

import java.util.Date;

public interface UpdateBuilder {

  UpdateBuilder updateUntyped(Field field, Object value);

  UpdateBuilder updateUntyped(Field field, Accessor accessor);

  UpdateBuilder update(IntegerField field, IntegerAccessor accessor);

  UpdateBuilder update(IntegerField field, Integer value);

  UpdateBuilder update(LongField field, LongAccessor accessor);

  UpdateBuilder update(LongField field, Long value);

  UpdateBuilder update(DoubleField field, DoubleAccessor accessor);

  UpdateBuilder update(DoubleField field, Double value);

  UpdateBuilder update(DateField field, DateAccessor accessor);

  UpdateBuilder update(DateField field, Date value);

  UpdateBuilder update(TimeStampField field, Date value);

  UpdateBuilder update(TimeStampField field, DateAccessor value);

  UpdateBuilder update(StringField field, StringAccessor accessor);

  UpdateBuilder update(StringField field, String value);

  UpdateBuilder update(BooleanField field, BooleanAccessor accessor);

  UpdateBuilder update(BooleanField field, Boolean value);

  UpdateBuilder update(BlobField field, byte[] value);

  UpdateBuilder update(BlobField field, BlobAccessor accessor);

  UpdateBuilder update(LinkField field, IntegerAccessor accessor);

  UpdateBuilder update(LinkField field, Integer value);

  SqlRequest getRequest();

}
