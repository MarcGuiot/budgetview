package org.globsframework.remote.impl;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.FieldValues;
import org.globsframework.remote.RemoteExecutor;
import org.globsframework.sqlstreams.CreateBuilder;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.SqlRequest;
import org.globsframework.streams.accessors.Accessor;
import org.globsframework.streams.accessors.LongAccessor;
import org.globsframework.streams.accessors.utils.ValueAccessor;

import java.util.HashMap;
import java.util.Map;

public class DefaultCreateRequest implements RemoteExecutor.CreateRequest {
  private Map<Field, ValueAccessor> accessorByField =
    new HashMap<Field, ValueAccessor>();
  protected SqlRequest query;
  protected LongAccessor keyGeneratedAccessor;

  public DefaultCreateRequest(SqlConnection sqlConnection, GlobType globType, FieldValues keyValues, boolean keyGeneratorAccessorWanted) {
    CreateBuilder createBuilder = sqlConnection.startCreate(globType);
    for (Field field : globType.getFields()) {
      ValueAccessor accessor = new ValueAccessor();
      accessorByField.put(field, accessor);
      createBuilder.setValue(field, accessor);
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
    query.execute();
  }
}
