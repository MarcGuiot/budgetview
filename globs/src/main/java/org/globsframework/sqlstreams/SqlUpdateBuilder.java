package org.globsframework.sqlstreams;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.fields.*;
import org.globsframework.streams.accessors.*;

import java.util.Date;

public interface SqlUpdateBuilder {

  SqlUpdateBuilder setAll(GlobAccessor accessor);

  SqlUpdateBuilder setValue(Field field, Object value);

  SqlUpdateBuilder setValue(Field field, Accessor accessor);

  SqlUpdateBuilder set(IntegerField field, IntegerAccessor accessor);

  SqlUpdateBuilder set(IntegerField field, Integer value);

  SqlUpdateBuilder set(LongField field, LongAccessor accessor);

  SqlUpdateBuilder set(LongField field, Long value);

  SqlUpdateBuilder set(DoubleField field, DoubleAccessor accessor);

  SqlUpdateBuilder set(DoubleField field, Double value);

  SqlUpdateBuilder set(DateField field, DateAccessor accessor);

  SqlUpdateBuilder set(DateField field, Date value);

  SqlUpdateBuilder set(TimeStampField field, Date value);

  SqlUpdateBuilder set(TimeStampField field, DateAccessor value);

  SqlUpdateBuilder set(StringField field, StringAccessor accessor);

  SqlUpdateBuilder set(StringField field, String value);

  SqlUpdateBuilder set(BooleanField field, BooleanAccessor accessor);

  SqlUpdateBuilder set(BooleanField field, Boolean value);

  SqlUpdateBuilder set(BlobField field, byte[] value);

  SqlUpdateBuilder set(BlobField field, BlobAccessor accessor);

  SqlUpdateBuilder set(LinkField field, IntegerAccessor accessor);

  SqlUpdateBuilder set(LinkField field, Integer value);

  SqlRequest getRequest();

  void run();

}
