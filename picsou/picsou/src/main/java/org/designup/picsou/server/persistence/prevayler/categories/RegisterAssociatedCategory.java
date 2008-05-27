package org.designup.picsou.server.persistence.prevayler.categories;

import org.crossbowlabs.globs.utils.directory.Directory;
import org.crossbowlabs.globs.utils.exceptions.InvalidData;
import org.crossbowlabs.globs.utils.serialization.SerializedInput;
import org.crossbowlabs.globs.utils.serialization.SerializedOutput;
import org.designup.picsou.server.persistence.prevayler.CustomSerializable;
import org.designup.picsou.server.persistence.prevayler.CustomSerializableFactory;
import org.prevayler.Transaction;

import java.util.Date;

public class RegisterAssociatedCategory implements Transaction, CustomSerializable {
  private String info;
  private int categoryId;
  private static final byte V1 = 1;
  private static final String REGISTER_CAT_NAME = "RegisterCat";

  public RegisterAssociatedCategory(String info, int categoryId) {
    this.info = info;
    this.categoryId = categoryId;
  }

  public RegisterAssociatedCategory() {
  }

  public void executeOn(Object prevalentSystem, Date executionTime) {
    ((PCategoriesData) prevalentSystem).addCategory(info, categoryId);
  }

  public String getSerializationName() {
    return REGISTER_CAT_NAME;
  }

  public void read(SerializedInput input, Directory directory) {
    byte version = input.readByte();
    if (version != V1) {
      throw new InvalidData("version " + version + " not managed");
    }
    categoryId = input.readNotNullInt();
    info = input.readString();
  }

  public void write(SerializedOutput output, Directory directory) {
    output.writeByte(V1);
    output.write(categoryId);
    output.writeString(info);
  }

  public static CustomSerializableFactory getFactory() {
    return new Factory();
  }

  private static class Factory implements CustomSerializableFactory {

    public String getSerializationName() {
      return REGISTER_CAT_NAME;
    }

    public CustomSerializable create() {
      return new RegisterAssociatedCategory();
    }
  }


}
