package org.crossbowlabs.globs.remote.impl;

import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.remote.RemoteExecutor;
import org.crossbowlabs.globs.sqlstreams.SqlConnection;
import org.crossbowlabs.globs.sqlstreams.UpdateBuilder;
import org.crossbowlabs.globs.sqlstreams.constraints.Constraint;

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
