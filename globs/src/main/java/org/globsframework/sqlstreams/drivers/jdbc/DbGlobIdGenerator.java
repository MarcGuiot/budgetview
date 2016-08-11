package org.globsframework.sqlstreams.drivers.jdbc;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.sqlstreams.CreateBuilder;
import org.globsframework.sqlstreams.GlobsDatabase;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.constraints.Constraint;
import org.globsframework.sqlstreams.constraints.Constraints;
import org.globsframework.sqlstreams.exceptions.RollbackFailed;
import org.globsframework.streams.GlobStream;
import org.globsframework.streams.accessors.IntegerAccessor;
import org.globsframework.utils.Ref;

public class DbGlobIdGenerator {
  private GlobType globType;
  private StringField tableNameField;
  private IntegerField idField;
  private GlobsDatabase db;

  public DbGlobIdGenerator(GlobType globType, StringField tableNameField,
                           IntegerField idField, GlobsDatabase db) {
    this.globType = globType;
    this.tableNameField = tableNameField;
    this.idField = idField;
    this.db = db;
  }

  synchronized public int getNextId(String tableName, int idCount) {
    SqlConnection sqlConnection = db.connect();
    while (true) {
      try {
        Ref<IntegerAccessor> idRef = new Ref<IntegerAccessor>();
        Constraint constraint = Constraints.and(getAdditionalConstraint(),
                                                Constraints.equal(tableNameField, tableName));
        GlobStream globStream = sqlConnection.startSelect(globType, constraint)
          .select(idField, idRef).getQuery().getStream();
        int id;
        if (globStream.next()) {
          id = idRef.get().getValue() + idCount;
          sqlConnection.startUpdate(globType, Constraints.equal(tableNameField, tableName))
            .set(idField, id).getRequest().run();
        }
        else {
          id = idCount;
          CreateBuilder builder = sqlConnection.startCreate(globType)
            .setValue(idField, idCount)
            .setValue(tableNameField, tableName);
          addAdditionalInfo(builder);
          builder.run();
        }
        sqlConnection.commitAndClose();
        return id - idCount;
      }
      catch (RollbackFailed e) {
        try {
          Thread.sleep(10);
        }
        catch (InterruptedException e1) {
        }
      }
      finally {
        sqlConnection.rollbackAndClose();
      }
    }
  }

  protected void addAdditionalInfo(CreateBuilder builder) {
  }

  protected Constraint getAdditionalConstraint() {
    return null;
  }
}
