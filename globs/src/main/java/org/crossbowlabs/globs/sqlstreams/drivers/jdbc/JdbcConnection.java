package org.crossbowlabs.globs.sqlstreams.drivers.jdbc;

import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobList;
import org.crossbowlabs.globs.sqlstreams.*;
import org.crossbowlabs.globs.sqlstreams.constraints.Constraint;
import org.crossbowlabs.globs.sqlstreams.drivers.jdbc.impl.SqlFieldCreationVisitor;
import org.crossbowlabs.globs.sqlstreams.drivers.jdbc.request.SqlCreateBuilder;
import org.crossbowlabs.globs.sqlstreams.drivers.jdbc.request.SqlDeleteBuilder;
import org.crossbowlabs.globs.sqlstreams.drivers.jdbc.request.SqlQueryBuilder;
import org.crossbowlabs.globs.sqlstreams.drivers.jdbc.request.SqlUpdateBuilder;
import org.crossbowlabs.globs.sqlstreams.exceptions.ConstraintViolation;
import org.crossbowlabs.globs.sqlstreams.exceptions.RollbackFailed;
import org.crossbowlabs.globs.sqlstreams.exceptions.SqlException;
import org.crossbowlabs.globs.sqlstreams.metadata.DbChecker;
import org.crossbowlabs.globs.sqlstreams.utils.StringPrettyWriter;
import org.crossbowlabs.globs.utils.exceptions.GlobsException;
import org.crossbowlabs.globs.utils.exceptions.OperationDenied;
import org.crossbowlabs.globs.utils.exceptions.UnexpectedApplicationState;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;

public abstract class JdbcConnection implements SqlConnection {
  private Connection connection;
  protected SqlService sqlService;
  private BlobUpdater blobUpdater;
  private DbChecker checker;

  public JdbcConnection(Connection connection, SqlService sqlService, BlobUpdater blobUpdater) {
    this.connection = connection;
    this.sqlService = sqlService;
    this.blobUpdater = blobUpdater;
    checker = new DbChecker(sqlService, this);
  }

  public SelectBuilder getQueryBuilder(GlobType globType) {
    return new SqlQueryBuilder(connection, globType, null, sqlService, blobUpdater);
  }

  public SelectBuilder getQueryBuilder(GlobType globType, Constraint constraint) {
    return new SqlQueryBuilder(connection, globType, constraint, sqlService, blobUpdater);
  }

  public UpdateBuilder getUpdateBuilder(GlobType globType, Constraint constraint) {
    return new SqlUpdateBuilder(connection, globType, sqlService, constraint, blobUpdater);
  }

  interface DbFunctor {
    void doIt() throws SQLException;
  }

  public void commit() throws RollbackFailed {
    if (connection == null) {
      throw new UnexpectedApplicationState("closed was connection");
    }
    try {
      connection.commit();
    }
    catch (SQLException e) {
      throw getTypedException(null, e);
    }
  }

  public void commitAndClose() {
    applyAndClose(new DbFunctor() {
      public void doIt() throws SQLException {
        connection.commit();
      }
    });
  }

  public void rollbackAndClose() {
    applyAndClose(new DbFunctor() {
      public void doIt() throws SQLException {
        connection.rollback();
      }
    });
  }

  public CreateBuilder getCreateBuilder(GlobType globType) {
    return new SqlCreateBuilder(connection, globType, sqlService, blobUpdater, this);
  }

  public void createTable(GlobType globType) {
    if (checker.tableExists(globType)) {
      return;
    }
    StringPrettyWriter writer = new StringPrettyWriter();
    writer.append("CREATE TABLE ")
      .append(sqlService.getTableName(globType))
      .append(" ( ");
    SqlFieldCreationVisitor creationVisitor = getFieldVisitorCreator(writer);
    int count = 1;
    for (Field field : globType.getFields()) {
      field.safeVisit(creationVisitor.appendComma(count != globType.getFieldCount()));
      count++;
    }
    Iterator<Field> iterator = globType.getKeyFields().iterator();
    if (iterator.hasNext()) {
      writer.append(", PRIMARY KEY (");
      for (; iterator.hasNext();) {
        Field field = iterator.next();
        writer.append(sqlService.getColumnName(field))
          .appendIf(", ", iterator.hasNext());
      }
      writer.append(") ");
    }
    writer.append(");");
    try {
      PreparedStatement statement = connection.prepareStatement(writer.toString());
      statement.executeUpdate();
      statement.close();
    }
    catch (SQLException e) {
      throw new UnexpectedApplicationState("Invalid creation request: " + writer.toString(), e);
    }
  }

  public void emptyTable(GlobType globType) {
    StringPrettyWriter writer = new StringPrettyWriter();
    writer.append("DELETE FROM ")
      .append(sqlService.getTableName(globType))
      .append(";");
    try {
      PreparedStatement statament = connection.prepareStatement(writer.toString());
      statament.executeUpdate();
      statament.close();
    }
    catch (SQLException e) {
      throw new UnexpectedApplicationState("Unable to empty table : " + writer.toString(), e);
    }
  }

  public void showDb() {
  }

  public void populate(GlobList all) {
    for (Glob glob : all) {
      CreateBuilder createBuilder = getCreateBuilder(glob.getType());
      for (Field field : glob.getType().getFields()) {
        createBuilder.setObject(field, glob.getValue(field));
      }
      createBuilder.getRequest().run();
    }
  }

  abstract protected SqlFieldCreationVisitor getFieldVisitorCreator(StringPrettyWriter prettyWriter);

  public SqlRequest getDeleteRequest(GlobType globType) {
    return new SqlDeleteBuilder(globType, null, connection, sqlService, blobUpdater);
  }

  public SqlRequest getDeleteRequest(GlobType globType, Constraint constraint) {
    return new SqlDeleteBuilder(globType, constraint, connection, sqlService, blobUpdater);
  }

  public Connection getConnection() {
    return connection;
  }

  public SqlException getTypedException(String sql, SQLException e) {
    if ("23000".equals(e.getSQLState())) {
      if (sql == null) {
        return new ConstraintViolation(e);
      }
      else {
        return new ConstraintViolation(sql, e);
      }
    }
    return new SqlException(e);
  }

  private void applyAndClose(DbFunctor db) {
    if (connection == null) {
      return;
    }
    GlobsException ex = null;
    try {
      db.doIt();
    }
    catch (SQLException e) {
      ex = getTypedException(null, e);
    }
    finally {
      try {
        connection.close();
      }
      catch (SQLException e) {
        if (ex == null) {
          ex = new OperationDenied(e);
        }
      }
      finally {
        connection = null;
      }
      if (ex != null) {
        throw ex;
      }
    }
  }
}
