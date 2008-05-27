package org.crossbowlabs.globs.remote;

import org.crossbowlabs.globs.metamodel.DummyObject;
import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.model.*;
import static org.crossbowlabs.globs.model.FieldValue.value;
import static org.crossbowlabs.globs.model.KeyBuilder.newKey;
import org.crossbowlabs.globs.remote.impl.DefaultCreateRequest;
import org.crossbowlabs.globs.remote.impl.DefaultDeleteRequest;
import org.crossbowlabs.globs.remote.impl.DefaultUpdateRequest;
import org.crossbowlabs.globs.sqlstreams.constraints.Constraints;
import org.crossbowlabs.globs.sqlstreams.drivers.jdbc.DbServicesTestCase;
import org.crossbowlabs.globs.streams.xml.XmlGlobStreamReader;
import org.crossbowlabs.globs.utils.serialization.SerializedInputOutputFactory;

import java.util.List;

public class RemoteExecutorTest extends DbServicesTestCase {

  public void testUpdate() throws Exception {
    populate(sqlConnection,
             XmlGlobStreamReader.parse(directory,
                                       "<dummyObject id='1' name='hello' value='1.1' present='true'/>" +
                                       "<dummyObject id='3' name='word' value='3.3' present='false'/>"));
    GlobRepository globRepository = GlobRepositoryBuilder.init().add(sqlConnection.getQueryBuilder(DummyObject.TYPE)
            .selectAll().getQuery().executeAsGlobs()).get();

    globRepository.addChangeListener(new ChangeSetListener() {
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

      public void globsReset(GlobRepository globRepository, List<GlobType> changedTypes) {
      }
    });

    globRepository.enterBulkDispatchingMode();

    globRepository.create(DummyObject.TYPE,
                          value(DummyObject.ID, 2),
                          value(DummyObject.NAME, "a name"),
                          value(DummyObject.PRESENT, Boolean.TRUE));
    globRepository.delete(newKey(DummyObject.TYPE, 3));
    globRepository.update(newKey(DummyObject.TYPE, 1), DummyObject.VALUE, 2.2);
    globRepository.completeBulkDispatchingMode();
    checkDb(newKey(DummyObject.TYPE, 1), DummyObject.VALUE, 2.2, sqlConnection);
    checkDb(newKey(DummyObject.TYPE, 2), DummyObject.NAME, "a name", sqlConnection);
    assertTrue(sqlConnection.getQueryBuilder(DummyObject.TYPE, Constraints.equal(DummyObject.ID, 3)).getQuery().executeAsGlobs().isEmpty());
  }
}
