package org.crossbowlabs.globs.sqlstreams.utils;

import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.metamodel.fields.*;
import org.crossbowlabs.globs.sqlstreams.CreateBuilder;
import org.crossbowlabs.globs.sqlstreams.SqlConnection;
import org.crossbowlabs.globs.sqlstreams.SqlRequest;
import org.crossbowlabs.globs.sqlstreams.exceptions.SqlException;
import org.crossbowlabs.globs.streams.accessors.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MultiCreateBuilder implements CreateBuilder {
  private Map<GlobType, CreateBuilder> createBuilders;

  public MultiCreateBuilder(SqlConnection sqlConnection, Collection<GlobType> globTypes) {
    createBuilders = new HashMap<GlobType, CreateBuilder>(globTypes.size());
    for (GlobType globType : globTypes) {
      createBuilders.put(globType, sqlConnection.getCreateBuilder(globType));
    }
  }

  public CreateBuilder set(IntegerField field, Integer value) {
    createBuilders.get(field.getGlobType()).set(field, value);
    return this;
  }

  public CreateBuilder set(BlobField field, byte[] value) {
    createBuilders.get(field.getGlobType()).set(field, value);
    return this;
  }

  public CreateBuilder set(StringField field, String value) {
    createBuilders.get(field.getGlobType()).set(field, value);
    return this;
  }

  public CreateBuilder set(LongField field, Long value) {
    createBuilders.get(field.getGlobType()).set(field, value);
    return this;
  }

  public CreateBuilder set(IntegerField field, IntegerAccessor accessor) {
    createBuilders.get(field.getGlobType()).set(field, accessor);
    return this;
  }

  public CreateBuilder set(LongField field, LongAccessor accessor) {
    createBuilders.get(field.getGlobType()).set(field, accessor);
    return this;
  }

  public CreateBuilder set(StringField field, StringAccessor accessor) {
    createBuilders.get(field.getGlobType()).set(field, accessor);
    return this;
  }

  public CreateBuilder set(TimeStampField field, DateAccessor accessor) {
    createBuilders.get(field.getGlobType()).set(field, accessor);
    return this;
  }

  public CreateBuilder set(BlobField field, BlobAccessor accessor) {
    createBuilders.get(field.getGlobType()).set(field, accessor);
    return this;
  }

  public CreateBuilder setObject(Field field, Accessor accessor) {
    createBuilders.get(field.getGlobType()).setObject(field, accessor);
    return this;
  }

  public CreateBuilder setObject(Field field, Object value) {
    createBuilders.get(field.getGlobType()).setObject(field, value);
    return this;
  }

  public SqlRequest getRequest() {
    return new MultiSqlRequest(createBuilders);
  }

  static private class MultiSqlRequest implements SqlRequest {
    private Collection<SqlRequest> sqlRequests;

    public MultiSqlRequest(Map<GlobType, CreateBuilder> createBuilders) {
      sqlRequests = new ArrayList<SqlRequest>(createBuilders.size());
      for (CreateBuilder builder : createBuilders.values()) {
        sqlRequests.add(builder.getRequest());
      }
    }

    public void run() throws SqlException {
      for (SqlRequest sqlRequest : sqlRequests) {
        sqlRequest.run();
      }
    }
  }
}
