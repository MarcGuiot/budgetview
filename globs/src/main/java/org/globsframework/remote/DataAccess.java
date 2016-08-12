package org.globsframework.remote;

import org.globsframework.metamodel.GlobType;
import org.globsframework.sqlstreams.SqlCreateBuilder;
import org.globsframework.sqlstreams.SqlSelectBuilder;
import org.globsframework.sqlstreams.SqlRequest;
import org.globsframework.sqlstreams.SqlUpdateBuilder;
import org.globsframework.sqlstreams.constraints.Constraint;

public interface DataAccess {
  SqlSelectBuilder getQueryBuilder(GlobType globType);

  SqlSelectBuilder getQueryBuilder(GlobType globType, Constraint constraint);

  SqlUpdateBuilder getUpdateBuilder(GlobType globType, Constraint constraint);

  SqlRequest getDeleteRequest(GlobType globType);

  SqlRequest getDeleteRequest(GlobType globType, Constraint constraint);

  SqlCreateBuilder getCreateRequest(GlobType globType);


}
