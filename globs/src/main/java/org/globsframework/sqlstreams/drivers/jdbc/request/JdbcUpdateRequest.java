package org.globsframework.sqlstreams.drivers.jdbc.request;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.Key;
import org.globsframework.sqlstreams.GlobsDatabase;
import org.globsframework.sqlstreams.SqlRequest;
import org.globsframework.sqlstreams.constraints.Constraint;
import org.globsframework.sqlstreams.constraints.Where;
import org.globsframework.sqlstreams.drivers.jdbc.impl.BlobUpdater;
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

public class JdbcUpdateRequest implements SqlRequest {
  private GlobType globType;
  private Constraint constraint;
  private BlobUpdater blobUpdater;
  private Map<Field, Accessor> accessors;
  private GlobsDatabase db;
  private PreparedStatement preparedStatement;
  private SqlValueFieldVisitor sqlValueFieldVisitor;
  private String sqlRequest;

  public JdbcUpdateRequest(GlobType globType, Constraint constraint, Map<Field, Accessor> accessors,
                           Connection connection, GlobsDatabase db, BlobUpdater blobUpdater) {
    this.globType = globType;
    this.constraint = constraint;
    createWhereConstraintsIfNeeded(globType, accessors);
    this.blobUpdater = blobUpdater;
    this.accessors = new HashMap<Field, Accessor>(accessors);
    this.db = db;
    this.sqlRequest = createRequest();
    try {
      this.preparedStatement = connection.prepareStatement(sqlRequest);
    }
    catch (SQLException e) {
      throw new UnexpectedApplicationState("For request : " + sqlRequest, e);
    }
    this.sqlValueFieldVisitor = new SqlValueFieldVisitor(preparedStatement, blobUpdater);
  }

  public void createWhereConstraintsIfNeeded(GlobType globType, Map<Field, Accessor> accessors) {
    if (constraint == null) {
      Field[] keyFields = globType.getKeyFields();
      constraint = Where.fieldEqualsValue(keyFields[0], accessors.get(keyFields[0]));
      for (int i = 1; i < keyFields.length; i++) {
        constraint = Where.and(constraint, Where.fieldEqualsValue(keyFields[i], accessors.get(keyFields[i])));
      }
    }
  }

  public void execute() {
    int index = 0;
    for (Map.Entry<Field, Accessor> entry : accessors.entrySet()) {
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

  public void close() {
    try {
      preparedStatement.close();
    }
    catch (SQLException e) {
      throw new UnexpectedApplicationState("In close", e);
    }
  }

  public void execute(Key key) {
    GlobType globType = key.getGlobType();
    Field[] list = globType.getKeyFields();
    Constraint newConstraint = null;
    for (Field field : list) {
      newConstraint = Where.and(newConstraint, Where.fieldEqualsValue(field, key.getValue(field)));
    }
    if (constraint == null) {
      constraint = newConstraint;
    }
    else {
      constraint = Where.and(constraint, newConstraint);
    }
    execute();
  }

  private String createRequest() {
    StringPrettyWriter prettyWriter = new StringPrettyWriter();
    prettyWriter.append("UPDATE ")
      .append(db.getTableName(globType))
      .append(" SET ");
    for (Iterator it = accessors.keySet().iterator(); it.hasNext(); ) {
      Field field = (Field) it.next();
      prettyWriter
        .append(db.getColumnName(field))
        .append(" = ?").
        appendIf(" , ", it.hasNext());
    }
    prettyWriter.append(" WHERE ");
    Set<GlobType> globTypes = new HashSet<GlobType>();
    globTypes.add(globType);
    constraint.visit(new WhereClauseConstraintVisitor(prettyWriter, db, globTypes));
    if (globTypes.size() > 1) {
      throw new UnexpectedApplicationState("More than one globType referenced in query '" + prettyWriter.toString() + "' ==> " + globTypes);
    }

    return prettyWriter.toString();
  }
}
