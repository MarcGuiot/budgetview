package org.globsframework.sqlstreams;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.fields.*;
import org.globsframework.streams.accessors.*;

import java.util.Date;

public interface SqlCreateBuilder {

  SqlCreateBuilder setAll(GlobAccessor accessor);

  SqlCreateBuilder set(IntegerField field, Integer value);

  SqlCreateBuilder set(BlobField field, byte[] value);

  SqlCreateBuilder set(StringField field, String value);

  SqlCreateBuilder set(BooleanField field, Boolean value);

  SqlCreateBuilder set(LongField field, Long value);

  SqlCreateBuilder set(IntegerField field, IntegerAccessor accessor);

  SqlCreateBuilder set(LongField field, LongAccessor accessor);

  SqlCreateBuilder set(StringField field, StringAccessor accessor);

  SqlCreateBuilder set(TimeStampField field, DateAccessor accessor);

  SqlCreateBuilder set(TimeStampField field, Date date);

  SqlCreateBuilder set(DateField field, Date date);

  SqlCreateBuilder set(DateField field, DateAccessor accessor);

  SqlCreateBuilder set(BlobField field, BlobAccessor accessor);

  SqlCreateBuilder setValue(Field field, Accessor accessor);

  SqlCreateBuilder setValue(Field field, Object value);

  SqlCreateRequest getRequest();

  void run();
}
