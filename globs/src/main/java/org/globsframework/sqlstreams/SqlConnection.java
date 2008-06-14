package org.globsframework.sqlstreams;

import org.globsframework.metamodel.GlobType;
import org.globsframework.model.GlobList;
import org.globsframework.sqlstreams.constraints.Constraint;
import org.globsframework.sqlstreams.exceptions.DbConstraintViolation;
import org.globsframework.sqlstreams.exceptions.RollbackFailed;

import java.sql.Connection;

public interface SqlConnection {

  SelectBuilder getQueryBuilder(GlobType globType);

  SelectBuilder getQueryBuilder(GlobType globType, Constraint constraint);

  CreateBuilder getCreateBuilder(GlobType globType);

  UpdateBuilder getUpdateBuilder(GlobType globType, Constraint constraint);

  SqlRequest getDeleteRequest(GlobType globType);

  SqlRequest getDeleteRequest(GlobType globType, Constraint constraint);

  void commit() throws RollbackFailed, DbConstraintViolation;

  void commitAndClose() throws RollbackFailed, DbConstraintViolation;

  void rollbackAndClose();

  Connection getConnection();

  void createTable(GlobType globType);

  void emptyTable(GlobType globType);

  void showDb();

  void populate(GlobList all);
}
