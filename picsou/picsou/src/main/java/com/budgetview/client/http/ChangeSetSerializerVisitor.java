package com.budgetview.client.http;

import com.budgetview.server.model.ServerDelta;
import com.budgetview.shared.utils.PicsouGlobSerializer;
import com.budgetview.server.model.ServerState;
import com.budgetview.server.serialization.SerializationManager;
import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.model.*;
import org.globsframework.utils.collections.MultiMap;

class ChangeSetSerializerVisitor implements ChangeSetVisitor {
  private PasswordBasedEncryptor passwordBasedEncryptor;
  private GlobRepository repository;
  private MultiMap<String, ServerDelta> deltaGlobMap;

  public ChangeSetSerializerVisitor(PasswordBasedEncryptor passwordBasedEncryptor, GlobRepository repository) {
    this.passwordBasedEncryptor = passwordBasedEncryptor;
    this.repository = repository;
    deltaGlobMap = new MultiMap<String, ServerDelta>();
  }

  public MultiMap<String, ServerDelta> getSerializableGlob() {
    return deltaGlobMap;
  }

  public void visitCreation(Key key, FieldValues values) throws Exception {
    PicsouGlobSerializer serializer = key.getGlobType().getProperty(SerializationManager.SERIALIZATION_PROPERTY, null);
    if (serializer != null) {
      writeData(key, ServerState.CREATED, serializer);
    }
  }

  public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
    PicsouGlobSerializer serializer = key.getGlobType().getProperty(SerializationManager.SERIALIZATION_PROPERTY, null);
    if (serializer != null) {
      writeData(key, ServerState.UPDATED, serializer);
    }
  }

  private void writeData(Key key, ServerState state, PicsouGlobSerializer serializer) {
    Glob values = repository.get(key);
    if (serializer.shouldBeSaved(repository, values)) {
      GlobType globType = key.getGlobType();
      Field keyField = globType.getKeyFields()[0];
      ServerDelta delta = new ServerDelta((Integer)key.getValue(keyField));
      delta.setState(state);
      deltaGlobMap.put(globType.getName(), delta);
      delta.setVersion(serializer.getWriteVersion());
      byte[] bytes = serializer.serializeData(values);
      delta.setData(passwordBasedEncryptor.encrypt(bytes));
    }
  }

  public void visitDeletion(Key key, FieldValues values) throws Exception {
    GlobType globType = key.getGlobType();
    PicsouGlobSerializer serializer = globType.getProperty(SerializationManager.SERIALIZATION_PROPERTY, null);
    if (serializer == null) {
      return;
    }
    IntegerField keyField = (IntegerField)globType.getKeyFields()[0];
    ServerDelta value = new ServerDelta(key.get(keyField));
    value.setState(ServerState.DELETED);
    deltaGlobMap.put(globType.getName(), value);
  }
}
