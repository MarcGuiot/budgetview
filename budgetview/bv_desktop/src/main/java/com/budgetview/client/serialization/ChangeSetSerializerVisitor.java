package com.budgetview.client.serialization;

import com.budgetview.session.serialization.SerializedDelta;
import com.budgetview.shared.encryption.PasswordBasedEncryptor;
import com.budgetview.shared.utils.GlobSerializer;
import com.budgetview.session.serialization.SerializedDeltaState;
import com.budgetview.session.serialization.SerializationManager;
import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.model.*;
import org.globsframework.utils.collections.MultiMap;

public class ChangeSetSerializerVisitor implements ChangeSetVisitor {
  private PasswordBasedEncryptor passwordBasedEncryptor;
  private GlobRepository repository;
  private MultiMap<String, SerializedDelta> deltaGlobMap;

  public ChangeSetSerializerVisitor(PasswordBasedEncryptor passwordBasedEncryptor, GlobRepository repository) {
    this.passwordBasedEncryptor = passwordBasedEncryptor;
    this.repository = repository;
    this.deltaGlobMap = new MultiMap<String, SerializedDelta>();
  }

  public MultiMap<String, SerializedDelta> getSerializableGlob() {
    return deltaGlobMap;
  }

  public void visitCreation(Key key, FieldValues values) throws Exception {
    GlobSerializer serializer = key.getGlobType().getProperty(SerializationManager.SERIALIZATION_PROPERTY, null);
    if (serializer != null) {
      writeData(key, SerializedDeltaState.CREATED, serializer);
    }
  }

  public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
    GlobSerializer serializer = key.getGlobType().getProperty(SerializationManager.SERIALIZATION_PROPERTY, null);
    if (serializer != null) {
      writeData(key, SerializedDeltaState.UPDATED, serializer);
    }
  }

  private void writeData(Key key, SerializedDeltaState state, GlobSerializer serializer) {
    Glob values = repository.get(key);
    if (serializer.shouldBeSaved(repository, values)) {
      GlobType globType = key.getGlobType();
      Field keyField = globType.getKeyFields()[0];
      SerializedDelta delta = new SerializedDelta((Integer)key.getValue(keyField));
      delta.setState(state);
      deltaGlobMap.put(globType.getName(), delta);
      delta.setVersion(serializer.getWriteVersion());
      byte[] bytes = serializer.serializeData(values);
      delta.setData(passwordBasedEncryptor.encrypt(bytes));
    }
  }

  public void visitDeletion(Key key, FieldValues values) throws Exception {
    GlobType globType = key.getGlobType();
    GlobSerializer serializer = globType.getProperty(SerializationManager.SERIALIZATION_PROPERTY, null);
    if (serializer == null) {
      return;
    }
    IntegerField keyField = (IntegerField)globType.getKeyFields()[0];
    SerializedDelta value = new SerializedDelta(key.get(keyField));
    value.setState(SerializedDeltaState.DELETED);
    deltaGlobMap.put(globType.getName(), value);
  }
}
