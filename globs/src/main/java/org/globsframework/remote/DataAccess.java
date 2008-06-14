package org.globsframework.remote;

import org.globsframework.metamodel.GlobType;
import org.globsframework.sqlstreams.CreateBuilder;
import org.globsframework.sqlstreams.SelectBuilder;
import org.globsframework.sqlstreams.SqlRequest;
import org.globsframework.sqlstreams.UpdateBuilder;
import org.globsframework.sqlstreams.constraints.Constraint;

public interface DataAccess {
  SelectBuilder getQueryBuilder(GlobType globType);

  SelectBuilder getQueryBuilder(GlobType globType, Constraint constraint);

  UpdateBuilder getUpdateBuilder(GlobType globType, Constraint constraint);

  SqlRequest getDeleteRequest(GlobType globType);

  SqlRequest getDeleteRequest(GlobType globType, Constraint constraint);

  CreateBuilder getCreateRequest(GlobType globType);


}
