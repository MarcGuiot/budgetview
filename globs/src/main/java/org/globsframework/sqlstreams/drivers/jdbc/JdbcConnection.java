package org.globsframework.sqlstreams.drivers.jdbc;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.sqlstreams.*;
import org.globsframework.sqlstreams.constraints.Constraint;
import org.globsframework.sqlstreams.drivers.jdbc.builder.SqlCreateBuilder;
import org.globsframework.sqlstreams.drivers.jdbc.builder.SqlQueryBuilder;
import org.globsframework.sqlstreams.drivers.jdbc.builder.SqlUpdateBuilder;
import org.globsframework.sqlstreams.drivers.jdbc.impl.BlobUpdater;
import org.globsframework.sqlstreams.drivers.jdbc.impl.SqlFieldCreationVisitor;
import org.globsframework.sqlstreams.drivers.jdbc.request.SqlDeleteRequest;
import org.globsframework.sqlstreams.exceptions.ConstraintViolation;
import org.globsframework.sqlstreams.exceptions.RollbackFailed;
import org.globsframework.sqlstreams.exceptions.SqlException;
import org.globsframework.sqlstreams.metadata.MetaData;
import org.globsframework.sqlstreams.utils.StringPrettyWriter;
import org.globsframework.utils.exceptions.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class JdbcConnection implements SqlConnection {
  private Connection connection;
  protected GlobsDatabase db;
  private BlobUpdater blobUpdater;
  private MetaData metaData;

  public JdbcConnection(Connection connection, GlobsDatabase db, BlobUpdater blobUpdater) {
    this.connection = connection;
    this.db = db;
    this.blobUpdater = blobUpdater;
    this.metaData = new MetaData(db, this);
  }

  public SelectBuilder startSelect(GlobType globType) {
    checkConnectionIsNotClosed();
    return new SqlQueryBuilder(connection, globType, null, db, blobUpdater);
  }

  public GlobList selectAll(GlobType globType) {
    return startSelect(globType).selectAll().getList();
  }

  public Glob selectUnique(GlobType globType) throws ItemNotFound, TooManyItems {
    return startSelect(globType).selectAll().getUnique();
  }

  public SelectBuilder startSelect(GlobType globType, Constraint constraint) {
    checkConnectionIsNotClosed();
    return new SqlQueryBuilder(connection, globType, constraint, db, blobUpdater);
  }

  public GlobList selectAll(GlobType globType, Constraint constraint) {
    return startSelect(globType, constraint).selectAll().getQuery().getList();
  }

  public Glob selectUnique(GlobType globType, Constraint constraint) throws ItemNotFound, TooManyItems {
    return startSelect(globType, constraint).getUnique();
  }

  public UpdateBuilder startUpdate(GlobType globType, Constraint constraint) {
    checkConnectionIsNotClosed();
    return new SqlUpdateBuilder(connection, globType, db, constraint, blobUpdater);
  }

  private void checkConnectionIsNotClosed() {
    if (connection == null) {
      throw new UnexpectedApplicationState("Connection was closed");
    }
  }

  interface DbFunctor {
    void doIt() throws SQLException;
  }

  public void commit() throws RollbackFailed {
    checkConnectionIsNotClosed();
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

  public CreateBuilder startCreate(GlobType globType) {
    return new SqlCreateBuilder(connection, globType, db, blobUpdater, this);
  }

  public void createTables(GlobType... globTypes) {
    for (GlobType type : globTypes) {
      createTable(type);
    }
  }

  private void createTable(GlobType globType) {
    if (metaData.tableExists(globType)) {
      return;
    }
    StringPrettyWriter writer = new StringPrettyWriter();
    writer.append("CREATE TABLE ")
      .append(db.getTableName(globType))
      .append(" ( ");
    SqlFieldCreationVisitor creationVisitor = getFieldVisitorCreator(writer);
    int count = 1;
    for (Field field : globType.getFields()) {
      field.safeVisit(creationVisitor.appendComma(count != globType.getFieldCount()));
      count++;
    }
    Field[] keyFields = globType.getKeyFields();
    Field last = keyFields[keyFields.length - 1];
    if (keyFields.length != 0) {
      writer.append(", PRIMARY KEY (");
      for (Field field : keyFields) {
        writer.append(db.getColumnName(field))
          .appendIf(", ", last != field);
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
      throw new OperationFailed("Creation request failed for type '" + globType + "' with statement: " + writer.toString(), e);
    }
  }

  public void emptyTable(GlobType... globTypes) {
    for (GlobType globType : globTypes) {
      emptyTable(globType);
    }
  }

  private void emptyTable(GlobType globType) {
    StringPrettyWriter writer = new StringPrettyWriter();
    writer.append("DELETE FROM ")
      .append(db.getTableName(globType))
      .append(";");
    try {
      PreparedStatement statament = connection.prepareStatement(writer.toString());
      statament.executeUpdate();
      statament.close();
    }
    catch (SQLException e) {
      throw new UnexpectedApplicationState("Unable to empty table: " + writer.toString(), e);
    }
  }

  public void createAll(GlobList all) {
    for (Glob glob : all) {
      create(glob);
    }
  }

  public void create(Glob glob) {
    CreateBuilder createBuilder = startCreate(glob.getType());
    for (Field field : glob.getType().getFields()) {
      createBuilder.setObject(field, glob.getValue(field));
    }
    createBuilder.run();
  }

  abstract protected SqlFieldCreationVisitor getFieldVisitorCreator(StringPrettyWriter prettyWriter);

  public SqlRequest startDelete(GlobType globType) {
    return new SqlDeleteRequest(globType, null, connection, db, blobUpdater);
  }

  public void deleteAll(GlobType globType) {
    startDelete(globType).run();
  }

  public SqlRequest startDelete(GlobType globType, Constraint constraint) {
    return new SqlDeleteRequest(globType, constraint, connection, db, blobUpdater);
  }

  public Connection getInnerConnection() {
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
