package org.globsframework.sqlstreams;

import org.globsframework.metamodel.GlobType;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.sqlstreams.constraints.Constraint;
import org.globsframework.sqlstreams.exceptions.DbConstraintViolation;
import org.globsframework.sqlstreams.exceptions.RollbackFailed;
import org.globsframework.utils.exceptions.ItemNotFound;
import org.globsframework.utils.exceptions.TooManyItems;

import java.sql.Connection;

public interface SqlConnection {

  void createTables(GlobType... globType);

  void emptyTable(GlobType... globType);

  SqlSelectBuilder startSelect(GlobType globType);

  GlobList selectAll(GlobType globType);

  Glob selectUnique(GlobType globType) throws ItemNotFound, TooManyItems;

  SqlSelectBuilder startSelect(GlobType globType, Constraint constraint);

  GlobList selectAll(GlobType globType, Constraint constraint);

  Glob selectUnique(GlobType globType, Constraint constraint) throws ItemNotFound, TooManyItems;

  SqlCreateBuilder startCreate(GlobType globType);

  SqlUpdateBuilder startUpdate(GlobType globType);

  SqlUpdateBuilder startUpdate(GlobType globType, Constraint constraint);

  SqlRequest startDelete(GlobType globType);

  SqlRequest startDelete(GlobType globType, Constraint constraint);

  void commit() throws RollbackFailed, DbConstraintViolation;

  void commitAndClose() throws RollbackFailed, DbConstraintViolation;

  void rollbackAndClose();

  Connection getInnerConnection();
}
