package org.globsframework.sqlstreams.drivers.jdbc;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.Key;
import org.globsframework.sqlstreams.SqlRequest;
import org.globsframework.sqlstreams.SqlService;
import org.globsframework.sqlstreams.constraints.Constraint;
import org.globsframework.sqlstreams.constraints.Constraints;
import org.globsframework.sqlstreams.drivers.jdbc.impl.SqlValueFieldVisitor;
import org.globsframework.sqlstreams.drivers.jdbc.impl.ValueConstraintVisitor;
import org.globsframework.sqlstreams.drivers.jdbc.impl.WhereClauseConstraintVisitor;
import org.globsframework.sqlstreams.utils.StringPrettyWriter;
import org.globsframework.streams.accessors.Accessor;
import org.globsframework.utils.exceptions.UnexpectedApplicationState;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

public class SqlUpdateRequest implements SqlRequest {
  private GlobType globType;
  private Constraint constraint;
  private BlobUpdater blobUpdater;
  private Map<Field, Accessor> values;
  private SqlService sqlService;
  private PreparedStatement preparedStatement;
  private SqlValueFieldVisitor sqlValueFieldVisitor;
  private String sqlRequest;

  public SqlUpdateRequest(GlobType globType, Constraint constraint, Map<Field, Accessor> values,
                          Connection connection, SqlService sqlService, BlobUpdater blobUpdater) {
    this.globType = globType;
    this.constraint = constraint;
    this.blobUpdater = blobUpdater;
    this.values = new HashMap<Field, Accessor>(values);
    this.sqlService = sqlService;
    sqlRequest = createRequest();
    try {
      preparedStatement = connection.prepareStatement(sqlRequest);
    }
    catch (SQLException e) {
      throw new UnexpectedApplicationState("For request : " + sqlRequest, e);
    }
    sqlValueFieldVisitor = new SqlValueFieldVisitor(preparedStatement, blobUpdater);
  }

  public void run() {
    int index = 0;
    for (Map.Entry<Field, Accessor> entry : values.entrySet()) {
      sqlValueFieldVisitor.setValue(entry.getValue().getObjectValue(), ++index);
      entry.getKey().safeVisit(sqlValueFieldVisitor);
    }
    constraint.visit(new ValueConstraintVisitor(preparedStatement, index, blobUpdater));
    try {
      preparedStatement.executeUpdate();
    }
    catch (SQLException e) {
      throw new UnexpectedApplicationState("For request : " + sqlRequest, e);
    }
  }

  public void execute(Key key) {
    GlobType globType = key.getGlobType();
    List<Field> list = globType.getKeyFields();
    Constraint constraint = null;
    for (Field field : list) {
      constraint = Constraints.and(constraint, Constraints.equalsObject(field, key.getValue(field)));
    }
    this.constraint = Constraints.and(this.constraint, constraint);
    run();
  }

  private String createRequest() {
    StringPrettyWriter prettyWriter = new StringPrettyWriter();
    prettyWriter.append("UPDATE ")
      .append(sqlService.getTableName(globType))
      .append(" SET ");
    for (Iterator it = values.keySet().iterator(); it.hasNext();) {
      Field field = (Field)it.next();
      prettyWriter
        .append(sqlService.getColumnName(field))
        .append(" = ?").
        appendIf(" , ", it.hasNext());
    }
    prettyWriter.append(" WHERE ");
    Set<GlobType> globTypes = new HashSet<GlobType>();
    globTypes.add(globType);
    constraint.visit(new WhereClauseConstraintVisitor(prettyWriter, sqlService, globTypes));
    if (globTypes.size() > 1) {
      throw new UnexpectedApplicationState("Only the updated table is valide in query " + prettyWriter.toString());
    }

    return prettyWriter.toString();
  }
}
