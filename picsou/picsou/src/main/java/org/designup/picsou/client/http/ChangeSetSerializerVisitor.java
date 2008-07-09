package org.designup.picsou.client.http;

import org.designup.picsou.server.model.SerializableGlobType;
import org.designup.picsou.server.serialization.PicsouGlobSerializer;
import org.designup.picsou.server.serialization.SerializationManager;
import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.delta.DefaultDeltaGlob;
import org.globsframework.model.delta.DeltaGlob;
import org.globsframework.model.delta.DeltaState;
import org.globsframework.model.impl.TwoFieldKey;
import org.globsframework.utils.MultiMap;

class ChangeSetSerializerVisitor implements ChangeSetVisitor {
  private PasswordBasedEncryptor passwordBasedEncryptor;
  private GlobRepository repository;
  private MultiMap<String, DeltaGlob> deltaGlobMap;

  public ChangeSetSerializerVisitor(PasswordBasedEncryptor passwordBasedEncryptor, GlobRepository repository) {
    this.passwordBasedEncryptor = passwordBasedEncryptor;
    this.repository = repository;
    deltaGlobMap = new MultiMap<String, DeltaGlob>();
  }

  public MultiMap<String, DeltaGlob> getSerializableGlob() {
    return deltaGlobMap;
  }

  public void visitCreation(Key key, FieldValues values) throws Exception {
    PicsouGlobSerializer serializer = key.getGlobType().getProperty(SerializationManager.SERIALIZATION_PROPERTY, null);
    if (serializer != null) {
      writeData(key, values, DeltaState.CREATED, serializer);
    }
  }

  public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
    PicsouGlobSerializer serializer = key.getGlobType().getProperty(SerializationManager.SERIALIZATION_PROPERTY, null);
    if (serializer != null) {
      writeData(key, values, DeltaState.UPDATED, serializer);
    }
  }

  private void writeData(Key key, FieldValues values, DeltaState state, PicsouGlobSerializer serializer) {
    GlobType globType = key.getGlobType();
    Field keyField = globType.getKeyFields().get(0);
    DefaultDeltaGlob defaultDeltaGlob = new DefaultDeltaGlob(
      new TwoFieldKey(SerializableGlobType.ID, key.getValue(keyField),
                      SerializableGlobType.GLOB_TYPE_NAME, globType.getName()));
    defaultDeltaGlob.setState(state);
    deltaGlobMap.put(globType.getName(), defaultDeltaGlob);
    defaultDeltaGlob.set(SerializableGlobType.VERSION, serializer.getWriteVersion());
    byte[] bytes = serializer.serializeData(repository.get(key));
    defaultDeltaGlob.set(SerializableGlobType.DATA, passwordBasedEncryptor.encrypt(bytes));
  }

  public void visitDeletion(Key key, FieldValues values) throws Exception {
    GlobType globType = key.getGlobType();
    PicsouGlobSerializer serializer = globType.getProperty(SerializationManager.SERIALIZATION_PROPERTY, null);
    if (serializer == null) {
      return;
    }
    Field keyField = globType.getKeyFields().get(0);
    DefaultDeltaGlob defaultDeltaGlob = new DefaultDeltaGlob(
      new TwoFieldKey(SerializableGlobType.ID, key.getValue(keyField),
                      SerializableGlobType.GLOB_TYPE_NAME, globType.getName()));
    defaultDeltaGlob.setState(DeltaState.DELETED);
    deltaGlobMap.put(globType.getName(), defaultDeltaGlob);
  }
}
