package org.crossbowlabs.globs.sqlstreams;

import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.metamodel.fields.*;
import org.crossbowlabs.globs.streams.accessors.*;

public interface CreateBuilder {
  CreateBuilder set(IntegerField field, Integer value);

  CreateBuilder set(BlobField field, byte[] value);

  CreateBuilder set(StringField field, String value);

  CreateBuilder set(LongField field, Long value);

  CreateBuilder set(IntegerField field, IntegerAccessor accessor);

  CreateBuilder set(LongField field, LongAccessor accessor);

  CreateBuilder set(StringField field, StringAccessor accessor);

  CreateBuilder set(TimeStampField field, DateAccessor accessor);

  CreateBuilder set(BlobField field, BlobAccessor accessor);

  CreateBuilder setObject(Field field, Accessor accessor);

  CreateBuilder setObject(Field field, Object value);

  SqlRequest getRequest();
}
