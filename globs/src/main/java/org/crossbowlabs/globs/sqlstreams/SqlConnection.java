package org.crossbowlabs.globs.sqlstreams;

import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.model.GlobList;
import org.crossbowlabs.globs.sqlstreams.constraints.Constraint;
import org.crossbowlabs.globs.sqlstreams.exceptions.DbConstraintViolation;
import org.crossbowlabs.globs.sqlstreams.exceptions.RollbackFailed;

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
