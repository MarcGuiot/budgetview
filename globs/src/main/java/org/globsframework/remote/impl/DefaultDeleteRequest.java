package org.globsframework.remote.impl;

import org.globsframework.metamodel.GlobType;
import org.globsframework.remote.RemoteExecutor;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.constraints.Constraint;

public class DefaultDeleteRequest implements RemoteExecutor.DeleteRequest {
  private SqlConnection sqlConnection;
  private GlobType globType;
  private Constraint constraint;

  public DefaultDeleteRequest(SqlConnection sqlConnection, GlobType globType, Constraint constraint) {
    this.sqlConnection = sqlConnection;
    this.globType = globType;
    this.constraint = constraint;
  }

  public void delete() {
    sqlConnection.startDelete(globType, constraint).run();
  }
}
