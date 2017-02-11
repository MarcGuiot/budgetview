package org.globsframework.sqlstreams;

import org.globsframework.metamodel.GlobType;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.sqlstreams.constraints.Constraint;
import org.globsframework.sqlstreams.exceptions.GlobsSQLException;
import org.globsframework.utils.exceptions.ItemNotFound;
import org.globsframework.utils.exceptions.TooManyItems;

import java.sql.Connection;

public interface SqlConnection {

  void createTables(GlobType... globType);

  void emptyTable(GlobType... globType);

  SqlSelectBuilder startSelect(GlobType globType);

  GlobList selectAll(GlobType globType);

  Glob selectUnique(GlobType globType) throws ItemNotFound, TooManyItems;

  SqlSelectBuilder startSelect(GlobType globType, Constraint where);

  GlobList selectAll(GlobType globType, Constraint where);

  Glob selectUnique(GlobType globType, Constraint where) throws ItemNotFound, TooManyItems;

  SqlCreateBuilder startCreate(GlobType globType);

  SqlUpdateBuilder startUpdate(GlobType globType);

  SqlUpdateBuilder startUpdate(GlobType globType, Constraint where);

  SqlRequest startDelete(GlobType globType);

  SqlRequest startDelete(GlobType globType, Constraint where);

  void commit() throws GlobsSQLException;

  void commitAndClose() throws GlobsSQLException;

  void rollbackAndClose() throws GlobsSQLException;

  Connection getInnerConnection();
}
