package org.globsframework.sqlstreams.drivers.jdbc;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.sqlstreams.SelectQuery;
import org.globsframework.sqlstreams.SqlService;
import org.globsframework.sqlstreams.accessors.SqlAccessor;
import org.globsframework.sqlstreams.constraints.Constraint;
import org.globsframework.sqlstreams.drivers.jdbc.impl.ValueConstraintVisitor;
import org.globsframework.sqlstreams.drivers.jdbc.impl.WhereClauseConstraintVisitor;
import org.globsframework.sqlstreams.utils.StringPrettyWriter;
import org.globsframework.streams.GlobStream;
import org.globsframework.utils.exceptions.ItemNotFound;
import org.globsframework.utils.exceptions.TooManyItems;
import org.globsframework.utils.exceptions.UnexpectedApplicationState;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

public class SqlSelectQuery implements SelectQuery {
  private Set<GlobType> globTypes = new HashSet<GlobType>();
  private Constraint constraint;
  private BlobUpdater blobUpdater;
  private boolean autoClose;
  private Map<Field, SqlAccessor> fieldToAccessorHolder;
  private SqlService sqlService;
  private PreparedStatement preparedStatement;
  private String sql;

  public SqlSelectQuery(Connection connection, Constraint constraint,
                        Map<Field, SqlAccessor> fieldToAccessorHolder, SqlService sqlService,
                        BlobUpdater blobUpdater, boolean autoClose) {
    this.constraint = constraint;
    this.blobUpdater = blobUpdater;
    this.autoClose = autoClose;
    this.fieldToAccessorHolder = new HashMap<Field, SqlAccessor>(fieldToAccessorHolder);
    this.sqlService = sqlService;
    sql = prepareSqlRequest();
    try {
      preparedStatement = connection.prepareStatement(sql);
    }
    catch (SQLException e) {
      throw new UnexpectedApplicationState("for request " + sql, e);
    }
  }

  private String prepareSqlRequest() {
    int index = 0;
    StringPrettyWriter prettyWriter = new StringPrettyWriter();
    prettyWriter.append("select ");
    for (Iterator<Map.Entry<Field, SqlAccessor>> iterator = fieldToAccessorHolder.entrySet().iterator();
         iterator.hasNext();) {
      Map.Entry<Field, SqlAccessor> fieldAndAccessor = iterator.next();
      fieldAndAccessor.getValue().setIndex(++index);
      GlobType globType = fieldAndAccessor.getKey().getGlobType();
      globTypes.add(globType);
      String tableName = sqlService.getTableName(globType);
      prettyWriter.append(tableName)
        .append(".")
        .append(sqlService.getColumnName(fieldAndAccessor.getKey()))
        .appendIf(", ", iterator.hasNext());
    }
    StringPrettyWriter where = null;
    if (constraint != null) {
      where = new StringPrettyWriter();
      where.append(" WHERE ");
      constraint.visit(new WhereClauseConstraintVisitor(where, sqlService, globTypes));
    }
    prettyWriter.append(" from ");
    for (Iterator it = globTypes.iterator(); it.hasNext();) {
      GlobType globType = (GlobType)it.next();
      prettyWriter.append(sqlService.getTableName(globType))
        .appendIf(", ", it.hasNext());
    }
    if (where != null) {
      prettyWriter.append(where.toString());
    }
    return prettyWriter.toString();
  }

  public GlobStream execute() {
    if (preparedStatement == null) {
      throw new UnexpectedApplicationState("Query closed " + sql);
    }
    try {
      if (constraint != null) {
        constraint.visit(new ValueConstraintVisitor(preparedStatement, blobUpdater));
      }
      return new SqlGlobStream(preparedStatement.executeQuery(), fieldToAccessorHolder, this);
    }
    catch (SQLException e) {
      throw new UnexpectedApplicationState("for request : " + sql, e);
    }
  }

  public GlobList executeAsGlobs() {
    GlobStream globStream = execute();
    AccessorGlobBuilder accessorGlobBuilder = AccessorGlobBuilder.init(globStream);
    GlobList result = new GlobList();
    while (globStream.next()) {
      result.addAll(accessorGlobBuilder.getGlobs());
    }
    return result;
  }

  public Glob executeUnique() throws ItemNotFound, TooManyItems {
    GlobList globs = executeAsGlobs();
    if (globs.size() == 1) {
      return globs.get(0);
    }
    if (globs.isEmpty()) {
      throw new ItemNotFound("No result returned for: " + sql);
    }
    throw new TooManyItems("Too many results for: " + sql);
  }

  public void resultSetClose() {
    if (autoClose) {
      close();
    }
  }

  public void close() {
    if (preparedStatement != null) {
      try {
        preparedStatement.close();
        preparedStatement = null;
      }
      catch (SQLException e) {
        throw new UnexpectedApplicationState("PreparedStatement close fail", e);
      }
    }
  }
}
