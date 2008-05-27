package org.crossbowlabs.globs.remote.impl;

import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.model.FieldValues;
import org.crossbowlabs.globs.remote.RemoteExecutor;
import org.crossbowlabs.globs.sqlstreams.CreateBuilder;
import org.crossbowlabs.globs.sqlstreams.SqlConnection;
import org.crossbowlabs.globs.sqlstreams.SqlRequest;
import org.crossbowlabs.globs.streams.accessors.Accessor;
import org.crossbowlabs.globs.streams.accessors.LongAccessor;
import org.crossbowlabs.globs.streams.accessors.utils.ValueAccessor;

import java.util.HashMap;
import java.util.Map;

public class DefaultCreateRequest implements RemoteExecutor.CreateRequest {
  private Map<Field, ValueAccessor> accessorByField =
    new HashMap<Field, ValueAccessor>();
  protected SqlRequest query;
  protected LongAccessor keyGeneratedAccessor;

  public DefaultCreateRequest(SqlConnection sqlConnection, GlobType globType, FieldValues keyValues, boolean keyGeneratorAccessorWanted) {
    CreateBuilder createBuilder = sqlConnection.getCreateBuilder(globType);
    for (Field field : globType.getFields()) {
      ValueAccessor accessor = new ValueAccessor();
      accessorByField.put(field, accessor);
      createBuilder.setObject(field, accessor);
    }
    keyValues.safeApply(new FieldValues.Functor() {
      public void process(Field field, Object value) throws Exception {
        accessorByField.get(field).setValue(value);
      }
    });
    query = createBuilder.getRequest();
    if (keyGeneratorAccessorWanted) {
//      keyGeneratedAccessor = createBuilder.getKeyGeneratedAccessor();
    }
  }

  public void update(Field field, Object value) {
    accessorByField.get(field).setValue(value);
  }

  public Accessor getValue(Field field) {
    return accessorByField.get(field);
  }

  public void create() {
    query.run();
  }
}
