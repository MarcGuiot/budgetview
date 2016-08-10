package org.globsframework.remote;

import org.globsframework.metamodel.DummyObject;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import static org.globsframework.model.FieldValue.value;
import static org.globsframework.model.KeyBuilder.newKey;
import org.globsframework.remote.impl.DefaultCreateRequest;
import org.globsframework.remote.impl.DefaultDeleteRequest;
import org.globsframework.remote.impl.DefaultUpdateRequest;
import org.globsframework.sqlstreams.constraints.Constraints;
import org.globsframework.sqlstreams.drivers.jdbc.GlobsDatabaseTestCase;
import org.globsframework.streams.xml.XmlGlobStreamReader;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;

import java.util.Set;

public class RemoteExecutorTest extends GlobsDatabaseTestCase {

  public void testUpdate() throws Exception {
    populate(sqlConnection,
             XmlGlobStreamReader.parse(directory,
                                       "<dummyObject id='1' name='hello' value='1.1' present='true'/>" +
                                       "<dummyObject id='3' name='word' value='3.3' present='false'/>"));
    GlobRepository repository =
      GlobRepositoryBuilder.init()
        .add(sqlConnection.selectAll(DummyObject.TYPE))
        .get();

    repository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository globRepository) {
        SerializedRemoteAccess access = new SerializedRemoteAccess();
        byte[] bytes = access.apply(changeSet);
        RemoteExecutor executor = new RemoteExecutor(globModel, new RemoteExecutor.RequestBuilder() {
          public RemoteExecutor.UpdateRequest getUpdate(GlobType globType, FieldValues fieldValues) {
            return new DefaultUpdateRequest(sqlConnection, globType, Constraints.fieldsEqual(fieldValues));
          }

          public RemoteExecutor.CreateRequest getCreate(GlobType globType, FieldValues fieldValues) {
            return new DefaultCreateRequest(sqlConnection, globType, fieldValues, false);
          }

          public RemoteExecutor.DeleteRequest getDelete(GlobType globType, FieldValues valuesConstraint) {
            return new DefaultDeleteRequest(sqlConnection, globType, Constraints.fieldsEqual(valuesConstraint));
          }
        });
        executor.execute(SerializedInputOutputFactory.init(bytes));
      }

      public void globsReset(GlobRepository globRepository, Set<GlobType> changedTypes) {
      }
    });

    repository.startChangeSet();

    repository.create(DummyObject.TYPE,
                      value(DummyObject.ID, 2),
                      value(DummyObject.NAME, "a name"),
                      value(DummyObject.PRESENT, Boolean.TRUE));
    repository.delete(newKey(DummyObject.TYPE, 3));
    repository.update(newKey(DummyObject.TYPE, 1), DummyObject.VALUE, 2.2);
    repository.completeChangeSet();
    checkDb(newKey(DummyObject.TYPE, 1), DummyObject.VALUE, 2.2, sqlConnection);
    checkDb(newKey(DummyObject.TYPE, 2), DummyObject.NAME, "a name", sqlConnection);
    assertTrue(sqlConnection.selectAll(DummyObject.TYPE, Constraints.equal(DummyObject.ID, 3)).isEmpty());
  }
}
