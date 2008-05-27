package org.crossbowlabs.globs.sqlstreams.drivers.jdbc;

import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobList;
import org.crossbowlabs.globs.sqlstreams.SelectQuery;
import org.crossbowlabs.globs.sqlstreams.SqlService;
import org.crossbowlabs.globs.sqlstreams.accessors.SqlAccessor;
import org.crossbowlabs.globs.sqlstreams.constraints.Constraint;
import org.crossbowlabs.globs.sqlstreams.drivers.jdbc.impl.ValueConstraintVisitor;
import org.crossbowlabs.globs.sqlstreams.drivers.jdbc.impl.WhereClauseConstraintVisitor;
import org.crossbowlabs.globs.sqlstreams.utils.StringPrettyWriter;
import org.crossbowlabs.globs.streams.GlobStream;
import org.crossbowlabs.globs.utils.exceptions.ItemNotFound;
import org.crossbowlabs.globs.utils.exceptions.TooManyItems;
import org.crossbowlabs.globs.utils.exceptions.UnexpectedApplicationState;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

public class SqlSelectQuery implements SelectQuery {
  private Set<GlobType> globTypes = new HashSet<GlobType>();
  private Constraint constraint;
  private BlobUpdater blobUpdater;
  private Map<Field, SqlAccessor> fieldToAccessorHolder;
  private SqlService sqlService;
  private PreparedStatement preparedStatement;
  private String sql;

  public SqlSelectQuery(Connection connection, Constraint constraint,
                        Map<Field, SqlAccessor> fieldToAccessorHolder, SqlService sqlService,
                        BlobUpdater blobUpdater) {
    this.constraint = constraint;
    this.blobUpdater = blobUpdater;
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
    try {
      if (constraint != null) {
        constraint.visit(new ValueConstraintVisitor(preparedStatement, blobUpdater));
      }
      return new SqlGlobStream(preparedStatement.executeQuery(), fieldToAccessorHolder);
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
}
