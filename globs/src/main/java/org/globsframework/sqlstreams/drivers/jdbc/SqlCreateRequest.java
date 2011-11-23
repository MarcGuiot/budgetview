package org.globsframework.sqlstreams.drivers.jdbc;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.*;
import org.globsframework.sqlstreams.SqlRequest;
import org.globsframework.sqlstreams.SqlService;
import org.globsframework.sqlstreams.accessors.GeneratedKeyAccessor;
import org.globsframework.sqlstreams.drivers.jdbc.impl.SqlValueFieldVisitor;
import org.globsframework.sqlstreams.utils.PrettyWriter;
import org.globsframework.sqlstreams.utils.StringPrettyWriter;
import org.globsframework.streams.accessors.Accessor;
import org.globsframework.utils.collections.Pair;
import org.globsframework.utils.exceptions.UnexpectedApplicationState;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;

public class SqlCreateRequest implements SqlRequest {
  private PreparedStatement preparedStatement;
  private List<Pair<Field, Accessor>> fields;
  private SqlValueFieldVisitor sqlValueVisitor;
  private GeneratedKeyAccessor generatedKeyAccessor;
  private GlobType globType;
  private SqlService sqlService;
  private JdbcConnection jdbcConnection;

  public SqlCreateRequest(List<Pair<Field, Accessor>> fields, GeneratedKeyAccessor generatedKeyAccessor,
                          Connection connection,
                          GlobType globType, SqlService sqlService, BlobUpdater blobUpdater, JdbcConnection jdbcConnection) {
    this.generatedKeyAccessor = generatedKeyAccessor;
    this.fields = fields;
    this.globType = globType;
    this.sqlService = sqlService;
    this.jdbcConnection = jdbcConnection;
    String sql = prepareRequest(fields, this.globType, new Value() {
      public String get(Pair<Field, Accessor> pair) {
        return "?";
      }
    });
    try {
      preparedStatement = connection.prepareStatement(sql);
    }
    catch (SQLException e) {
      throw new UnexpectedApplicationState("In prepareStatement for request : " + sql, e);
    }
    this.sqlValueVisitor = new SqlValueFieldVisitor(preparedStatement, blobUpdater);
  }

  interface Value {
    String get(Pair<Field, Accessor> pair);
  }

  private String prepareRequest(List<Pair<Field, Accessor>> fields, GlobType globType, Value value) {
    PrettyWriter writer = new StringPrettyWriter();
    writer.append("INSERT INTO ")
      .append(sqlService.getTableName(globType))
      .append(" (");
    int columnCount = 0;
    for (Pair<Field, Accessor> pair : fields) {
      String columnName = sqlService.getColumnName(pair.getFirst());
      writer.appendIf(", ", columnCount > 0);
      columnCount++;
      writer.append(columnName);
    }
    writer.append(") VALUES (");
    for (Iterator<Pair<Field, Accessor>> it = fields.iterator(); it.hasNext();) {
      Pair<Field, Accessor> pair = it.next();
      writer.append(value.get(pair)).appendIf(",", it.hasNext());
    }
    writer.append(")");
    return writer.toString();
  }

  public void run() {
    try {
      int index = 0;
      for (Pair<Field, Accessor> pair : fields) {
        Object value = pair.getSecond().getObjectValue();
        sqlValueVisitor.setValue(value, ++index);
        pair.getFirst().safeVisit(sqlValueVisitor);
      }
      preparedStatement.executeUpdate();
      if (generatedKeyAccessor != null) {
        generatedKeyAccessor.setResult(preparedStatement.getGeneratedKeys());
      }
//      Log.write(getDebugRequest());
    }
    catch (SQLException e) {
      throw jdbcConnection.getTypedException(getDebugRequest(), e);
    }
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
