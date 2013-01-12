package org.designup.picsou.server.persistence.prevayler.users;

import org.designup.picsou.server.persistence.prevayler.CustomSerializable;
import org.designup.picsou.server.persistence.prevayler.CustomSerializableFactory;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedOutput;
import org.prevayler.Transaction;

import java.util.Date;

public class SetLang implements Transaction, CustomSerializable {
  private static final String TRANSACTION_LG = "LG";
  private String lang;

  public SetLang(String lang) {
    this.lang = lang;
  }

  public SetLang() {
  }

  public void executeOn(Object prevalentSystem, Date executionTime) {
    ((PRootData)prevalentSystem).setLang(lang);
  }

  public String getSerializationName() {
    return TRANSACTION_LG;
  }

  public void read(SerializedInput input, Directory directory) {
    int version = input.readNotNullInt();
    if (version == 1){
      this.lang = input.readUtf8String();
    }
  }

  public void write(SerializedOutput output, Directory directory) {
    output.write(1);
    output.writeUtf8String(lang);
  }

  public static CustomSerializableFactory getFactory() {
    return new Factory();
  }

  private static class Factory implements CustomSerializableFactory {

    public String getSerializationName() {
      return TRANSACTION_LG;
    }

    public CustomSerializable create() {
      return new SetLang();
    }
  }

}
