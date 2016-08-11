package org.globsframework.sqlstreams.utils;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.*;
import org.globsframework.sqlstreams.CreateBuilder;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.SqlRequest;
import org.globsframework.sqlstreams.exceptions.SqlException;
import org.globsframework.streams.accessors.*;

import java.util.*;

public class MultiCreateBuilder implements CreateBuilder {
  private Map<GlobType, CreateBuilder> createBuilders;

  public MultiCreateBuilder(SqlConnection sqlConnection, Collection<GlobType> globTypes) {
    createBuilders = new HashMap<GlobType, CreateBuilder>(globTypes.size());
    for (GlobType globType : globTypes) {
      createBuilders.put(globType, sqlConnection.startCreate(globType));
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

  public CreateBuilder set(TimeStampField field, Date date) {
    createBuilders.get(field.getGlobType()).set(field, date);
    return this;
  }

  public CreateBuilder set(DateField field, Date date) {
    createBuilders.get(field.getGlobType()).set(field, date);
    return this;
  }

  public CreateBuilder set(DateField field, DateAccessor accessor) {
    createBuilders.get(field.getGlobType()).set(field, accessor);
    return this;
  }

  public CreateBuilder set(BlobField field, BlobAccessor accessor) {
    createBuilders.get(field.getGlobType()).set(field, accessor);
    return this;
  }

  public CreateBuilder setValue(Field field, Accessor accessor) {
    createBuilders.get(field.getGlobType()).setValue(field, accessor);
    return this;
  }

  public CreateBuilder setValue(Field field, Object value) {
    createBuilders.get(field.getGlobType()).setValue(field, value);
    return this;
  }

  public SqlRequest getRequest() {
    return new MultiSqlRequest(createBuilders);
  }

  public void run() throws SqlException {
    getRequest().run();
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

    public void close() {
      for (SqlRequest request : sqlRequests) {
        request.close();
      }
    }
  }
}
