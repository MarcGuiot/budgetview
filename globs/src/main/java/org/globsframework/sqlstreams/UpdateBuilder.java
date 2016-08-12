package org.globsframework.sqlstreams;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.fields.*;
import org.globsframework.streams.accessors.*;

import java.util.Date;

public interface UpdateBuilder {

  UpdateBuilder setAll(GlobAccessor accessor);

  UpdateBuilder setValue(Field field, Object value);

  UpdateBuilder setValue(Field field, Accessor accessor);

  UpdateBuilder set(IntegerField field, IntegerAccessor accessor);

  UpdateBuilder set(IntegerField field, Integer value);

  UpdateBuilder set(LongField field, LongAccessor accessor);

  UpdateBuilder set(LongField field, Long value);

  UpdateBuilder set(DoubleField field, DoubleAccessor accessor);

  UpdateBuilder set(DoubleField field, Double value);

  UpdateBuilder set(DateField field, DateAccessor accessor);

  UpdateBuilder set(DateField field, Date value);

  UpdateBuilder set(TimeStampField field, Date value);

  UpdateBuilder set(TimeStampField field, DateAccessor value);

  UpdateBuilder set(StringField field, StringAccessor accessor);

  UpdateBuilder set(StringField field, String value);

  UpdateBuilder set(BooleanField field, BooleanAccessor accessor);

  UpdateBuilder set(BooleanField field, Boolean value);

  UpdateBuilder set(BlobField field, byte[] value);

  UpdateBuilder set(BlobField field, BlobAccessor accessor);

  UpdateBuilder set(LinkField field, IntegerAccessor accessor);

  UpdateBuilder set(LinkField field, Integer value);

  SqlRequest getRequest();

  void run();

}
