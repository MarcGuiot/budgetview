package org.globsframework.sqlstreams;

import org.globsframework.metamodel.Field;
import org.globsframework.model.ChangeSetVisitor;
import org.globsframework.model.FieldValues;
import org.globsframework.model.FieldValuesWithPrevious;
import org.globsframework.model.Key;
import org.globsframework.sqlstreams.constraints.Constraints;

public class InDbChangeSetVisitor implements ChangeSetVisitor {
  private SqlConnection sqlConnection;
  private CreateBuilder createBuilder;
  private FieldValues.Functor functorForCreate = new FieldValues.Functor() {
    public void process(Field field, Object value) throws Exception {
      createBuilder.setObject(field, value);
    }
  };

  private UpdateBuilder updateBuilder;
  private FieldValues.Functor functorForUpdate = new FieldValues.Functor() {
    public void process(Field field, Object value) throws Exception {
      updateBuilder.updateUntyped(field, value);
    }
  };

  public InDbChangeSetVisitor(SqlConnection sqlConnection) {
    this.sqlConnection = sqlConnection;
  }

  public void visitCreation(Key key, FieldValues values) throws Exception {
    createBuilder = sqlConnection.getCreateBuilder(key.getGlobType());
    key.apply(functorForCreate);
    values.apply(functorForCreate);
    createBuilder.getRequest().run();
  }

  public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
    updateBuilder = sqlConnection.getUpdateBuilder(key.getGlobType(),
                                                   Constraints.fieldsEqual(key));
    key.apply(functorForUpdate);
    values.apply(functorForUpdate);
    updateBuilder.getRequest().run();
  }

  public void visitDeletion(Key key, FieldValues values) throws Exception {
    sqlConnection.getDeleteRequest(key.getGlobType(), Constraints.fieldsEqual(key)).run();
  }
}
