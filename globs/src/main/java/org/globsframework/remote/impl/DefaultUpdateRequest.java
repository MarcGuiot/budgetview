package org.globsframework.remote.impl;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.remote.RemoteExecutor;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.UpdateBuilder;
import org.globsframework.sqlstreams.constraints.Constraint;

public class DefaultUpdateRequest implements RemoteExecutor.UpdateRequest {
  protected UpdateBuilder builder;

  public DefaultUpdateRequest(SqlConnection sqlConnection, GlobType globType, Constraint constraint) {
    builder = sqlConnection.getUpdateBuilder(globType, constraint);
  }

  public void update(Field field, Object value) {
    builder.updateUntyped(field, value);
  }

  public void update() {
    builder.getRequest().run();
  }
}
