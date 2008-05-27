package org.crossbowlabs.globs.remote;

import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.sqlstreams.CreateBuilder;
import org.crossbowlabs.globs.sqlstreams.SelectBuilder;
import org.crossbowlabs.globs.sqlstreams.SqlRequest;
import org.crossbowlabs.globs.sqlstreams.UpdateBuilder;
import org.crossbowlabs.globs.sqlstreams.constraints.Constraint;

public interface DataAccess {
  SelectBuilder getQueryBuilder(GlobType globType);

  SelectBuilder getQueryBuilder(GlobType globType, Constraint constraint);

  UpdateBuilder getUpdateBuilder(GlobType globType, Constraint constraint);

  SqlRequest getDeleteRequest(GlobType globType);

  SqlRequest getDeleteRequest(GlobType globType, Constraint constraint);

  CreateBuilder getCreateRequest(GlobType globType);


}
