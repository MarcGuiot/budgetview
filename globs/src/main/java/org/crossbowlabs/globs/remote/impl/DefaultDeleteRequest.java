package org.crossbowlabs.globs.remote.impl;

import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.remote.RemoteExecutor;
import org.crossbowlabs.globs.sqlstreams.SqlConnection;
import org.crossbowlabs.globs.sqlstreams.constraints.Constraint;

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
    sqlConnection.getDeleteRequest(globType, constraint).run();
  }
}
