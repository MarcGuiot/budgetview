package org.globsframework.sqlstreams.utils;

import org.globsframework.metamodel.Field;
import org.globsframework.model.ChangeSetVisitor;
import org.globsframework.model.FieldValues;
import org.globsframework.model.FieldValuesWithPrevious;
import org.globsframework.model.Key;
import org.globsframework.sqlstreams.CreateBuilder;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.UpdateBuilder;
import org.globsframework.sqlstreams.constraints.Where;

public class InDbChangeSetVisitor implements ChangeSetVisitor {
  private SqlConnection sqlConnection;
  private CreateBuilder createBuilder;
  private FieldValues.Functor functorForCreate = new FieldValues.Functor() {
    public void process(Field field, Object value) throws Exception {
      createBuilder.setValue(field, value);
    }
  };

  private UpdateBuilder updateBuilder;
  private FieldValues.Functor functorForUpdate = new FieldValues.Functor() {
    public void process(Field field, Object value) throws Exception {
      updateBuilder.setValue(field, value);
    }
  };

  public InDbChangeSetVisitor(SqlConnection sqlConnection) {
    this.sqlConnection = sqlConnection;
  }

  public void visitCreation(Key key, FieldValues values) throws Exception {
    createBuilder = sqlConnection.startCreate(key.getGlobType());
    key.apply(functorForCreate);
    values.apply(functorForCreate);
    createBuilder.getRequest().run();
  }

  public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
    updateBuilder = sqlConnection.startUpdate(key.getGlobType(),
                                              Where.fieldsAreEqual(key));
    key.apply(functorForUpdate);
    values.apply(functorForUpdate);
    updateBuilder.getRequest().run();
  }

  public void visitDeletion(Key key, FieldValues values) throws Exception {
    sqlConnection.startDelete(key.getGlobType(), Where.fieldsAreEqual(key)).run();
  }
}