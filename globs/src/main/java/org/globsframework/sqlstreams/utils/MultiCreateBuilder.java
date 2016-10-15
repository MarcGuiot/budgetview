package org.globsframework.sqlstreams.utils;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.sqlstreams.SqlCreateBuilder;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.SqlRequest;
import org.globsframework.sqlstreams.exceptions.GlobsSQLException;

import java.util.*;

public class MultiCreateBuilder {
  private Map<GlobType, SqlCreateBuilder> createBuilders;

  public MultiCreateBuilder(SqlConnection sqlConnection, Collection<GlobType> globTypes) {
    createBuilders = new HashMap<GlobType, SqlCreateBuilder>(globTypes.size());
    for (GlobType globType : globTypes) {
      createBuilders.put(globType, sqlConnection.startCreate(globType));
    }
  }

  public MultiCreateBuilder setValue(Field field, Object value) {
    createBuilders.get(field.getGlobType()).setValue(field, value);
    return this;
  }

  public SqlRequest getRequest() {
    return new MultiSqlRequest(createBuilders);
  }

  public void run() throws GlobsSQLException {
    getRequest().execute();
  }

  static private class MultiSqlRequest implements SqlRequest {
    private Collection<SqlRequest> sqlRequests;

    public MultiSqlRequest(Map<GlobType, SqlCreateBuilder> createBuilders) {
      sqlRequests = new ArrayList<SqlRequest>(createBuilders.size());
      for (SqlCreateBuilder builder : createBuilders.values()) {
        sqlRequests.add(builder.getRequest());
      }
    }

    public int execute() throws GlobsSQLException {
      int result = 0;
      for (SqlRequest sqlRequest : sqlRequests) {
        result += sqlRequest.execute();
      }
      return result;
    }

    public void close() {
      for (SqlRequest request : sqlRequests) {
        request.close();
      }
    }
  }
}
