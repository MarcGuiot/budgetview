package org.designup.picsou.server.persistence.prevayler.accounts;

import org.designup.picsou.server.persistence.prevayler.CustomSerializable;
import org.designup.picsou.server.persistence.prevayler.CustomSerializableFactory;
import org.globsframework.metamodel.GlobModel;
import org.globsframework.model.delta.DeltaGlob;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidData;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedOutput;
import org.prevayler.Transaction;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ApplyUserDataTransaction implements Transaction, CustomSerializable {
  private static final byte V1 = 1;
  private List<DeltaGlob> deltaGlobs;
  private static final String APPLY_USER_TRANSACTION_NAME = "ApplyUserData";

  public ApplyUserDataTransaction(List<DeltaGlob> deltaGlobs) {
    this.deltaGlobs = deltaGlobs;
  }

  public ApplyUserDataTransaction() {
  }

  public void executeOn(Object prevalentSystem, Date executionTime) {
    UserData userData = ((UserData)prevalentSystem);
    userData.apply(deltaGlobs);
  }

  public String getSerializationName() {
    return APPLY_USER_TRANSACTION_NAME;
  }

  public void read(SerializedInput input, Directory directory) {
    byte version = input.readByte();
    if (version != V1) {
      throw new InvalidData("version " + version + " not supported");
    }
    int length = input.readNotNullInt();
    GlobModel model = directory.get(GlobModel.class);
    deltaGlobs = new ArrayList<DeltaGlob>(length);
    while (length > 0) {
      deltaGlobs.add(input.readDeltaGlob(model));
      length--;
    }
  }

  public void write(SerializedOutput output, Directory directory) {
    output.writeByte(V1);
    output.write(deltaGlobs.size());
    for (DeltaGlob deltaGlob : deltaGlobs) {
      output.writeDeltaGlob(deltaGlob);
    }
  }

  public static CustomSerializableFactory getFactory() {
    return new Factory();
  }

  private static class Factory implements CustomSerializableFactory {

    public String getSerializationName() {
      return APPLY_USER_TRANSACTION_NAME;
    }

    public CustomSerializable create() {
      return new ApplyUserDataTransaction();
    }
  }

}
