package org.globsframework.sqlstreams.drivers.jdbc.request;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.*;
import org.globsframework.model.FieldValues;
import org.globsframework.sqlstreams.GlobsDatabase;
import org.globsframework.sqlstreams.SqlCreateRequest;
import org.globsframework.sqlstreams.drivers.jdbc.JdbcConnection;
import org.globsframework.sqlstreams.drivers.jdbc.impl.BlobUpdater;
import org.globsframework.sqlstreams.drivers.jdbc.impl.GeneratedIds;
import org.globsframework.sqlstreams.drivers.jdbc.impl.SqlValueFieldVisitor;
import org.globsframework.sqlstreams.exceptions.GlobsSQLException;
import org.globsframework.sqlstreams.utils.PrettyWriter;
import org.globsframework.sqlstreams.utils.StringPrettyWriter;
import org.globsframework.streams.accessors.Accessor;
import org.globsframework.utils.collections.Pair;
import org.globsframework.utils.exceptions.UnexpectedApplicationState;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;

public class JdbcCreateRequest implements SqlCreateRequest {
  private PreparedStatement preparedStatement;
  private List<Pair<Field, Accessor>> fields;
  private SqlValueFieldVisitor sqlValueVisitor;
  private GlobType globType;
  private GlobsDatabase db;
  private JdbcConnection jdbcConnection;
  private FieldValues lastGeneratedIds;

  public JdbcCreateRequest(List<Pair<Field, Accessor>> fields,
                           Connection connection,
                           GlobType globType, GlobsDatabase db, BlobUpdater blobUpdater, JdbcConnection jdbcConnection) {
    this.fields = fields;
    this.globType = globType;
    this.db = db;
    this.jdbcConnection = jdbcConnection;
    this.preparedStatement = prepareStatement(fields, connection);
    this.sqlValueVisitor = new SqlValueFieldVisitor(preparedStatement, blobUpdater);
  }

  public PreparedStatement prepareStatement(List<Pair<Field, Accessor>> fields, Connection connection) {
    String sql = prepareRequest(fields, this.globType, new Value() {
      public String get(Pair<Field, Accessor> pair) {
        return "?";
      }
    });

    Field[] keyFields = globType.getKeyFields();
    String[] keyColumnNames = new String[keyFields.length];
    for (int i = 0; i < keyFields.length; i++) {
      keyColumnNames[i] = db.getColumnName(keyFields[i]);
    }
    try {
      return connection.prepareStatement(sql, keyColumnNames);
    }
    catch (SQLException e) {
      throw new UnexpectedApplicationState("In prepareStatement for request : " + sql, e);
    }
  }

  private interface Value {
    String get(Pair<Field, Accessor> pair);
  }

  private String prepareRequest(List<Pair<Field, Accessor>> fields, GlobType globType, Value value) {
    PrettyWriter writer = new StringPrettyWriter();
    writer.append("INSERT INTO ")
      .append(db.getTableName(globType))
      .append(" (");
    int columnCount = 0;
    for (Pair<Field, Accessor> pair : fields) {
      String columnName = db.getColumnName(pair.getFirst());
      writer.appendIf(", ", columnCount > 0);
      columnCount++;
      writer.append(columnName);
    }
    writer.append(") VALUES (");
    for (Iterator<Pair<Field, Accessor>> it = fields.iterator(); it.hasNext(); ) {
      Pair<Field, Accessor> pair = it.next();
      writer.append(value.get(pair)).appendIf(",", it.hasNext());
    }
    writer.append(")");
    return writer.toString();
  }

  public void execute() throws GlobsSQLException {
    try {
      int index = 0;
      for (Pair<Field, Accessor> pair : fields) {
        Object value = pair.getSecond().getObjectValue();
        sqlValueVisitor.setValue(value, ++index);
        pair.getFirst().safeVisit(sqlValueVisitor);
      }
      preparedStatement.executeUpdate();
      lastGeneratedIds = GeneratedIds.convert(preparedStatement.getGeneratedKeys(), globType);
    }
    catch (SQLException e) {
      throw jdbcConnection.getTypedException(getDebugRequest(), e);
    }
  }

  public FieldValues getLastGeneratedIds() {
    return lastGeneratedIds;
  }

  public void close() {
    try {
      preparedStatement.close();
    }
    catch (SQLException e) {
      throw new UnexpectedApplicationState("In close", e);
    }
  }

  private String getDebugRequest() {
    return prepareRequest(fields, globType, new DebugValue());
  }

  private static class DebugValue implements Value, FieldVisitor {
    private Object value;
    private String convertValue;

    public String get(Pair<Field, Accessor> pair) {
      value = pair.getSecond().getObjectValue();
      if (value != null) {
        pair.getFirst().safeVisit(this);
      }
      else {
        convertValue = "'NULL'";
      }
      return convertValue;
    }

    public void visitInteger(IntegerField field) throws Exception {
      convertValue = value.toString();
    }

    public void visitDouble(DoubleField field) throws Exception {
      convertValue = value.toString();
    }

    public void visitString(StringField field) throws Exception {
      convertValue = "'" + value.toString() + "'";
    }

    public void visitDate(DateField field) throws Exception {
      convertValue = "'" + new SimpleDateFormat("yyyyMMdd").format(value) + "'";
    }

    public void visitBoolean(BooleanField field) throws Exception {
      convertValue = value.toString();
    }

    public void visitTimeStamp(TimeStampField field) throws Exception {
      convertValue = "'" + new SimpleDateFormat("yyyyMMdd HH:mm:ss").format(value) + "'";
    }

    public void visitBlob(BlobField field) throws Exception {
      convertValue = "'" + value.toString() + "'";
    }

    public void visitLong(LongField field) throws Exception {
      convertValue = value.toString();
    }

    public void visitLink(LinkField field) throws Exception {
      convertValue = value.toString();
    }
  }
}
