package org.globsframework.sqlstreams;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.fields.*;
import org.globsframework.streams.accessors.*;

import java.util.Date;

public interface CreateBuilder {
  CreateBuilder set(IntegerField field, Integer value);

  CreateBuilder set(BlobField field, byte[] value);

  CreateBuilder set(StringField field, String value);

  CreateBuilder set(LongField field, Long value);

  CreateBuilder set(IntegerField field, IntegerAccessor accessor);

  CreateBuilder set(LongField field, LongAccessor accessor);

  CreateBuilder set(StringField field, StringAccessor accessor);

  CreateBuilder set(TimeStampField field, DateAccessor accessor);

  CreateBuilder set(TimeStampField field, Date date);

  CreateBuilder set(DateField field, Date date);

  CreateBuilder set(DateField field, DateAccessor accessor);

  CreateBuilder set(BlobField field, BlobAccessor accessor);

  CreateBuilder setValue(Field field, Accessor accessor);

  CreateBuilder setValue(Field field, Object value);

  SqlRequest getRequest();

  void run();
}
