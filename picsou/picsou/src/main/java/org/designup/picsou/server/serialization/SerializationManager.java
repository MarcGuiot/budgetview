package org.designup.picsou.server.serialization;

import org.designup.picsou.model.*;
import org.globsframework.metamodel.GlobModel;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.properties.Property;

public class SerializationManager {
  public static Property<GlobType, PicsouGlobSerializer> SERIALIZATION_PROPERTY;

  public static void init(GlobModel globModel) {
    SERIALIZATION_PROPERTY = globModel.createGlobTypeProperty("serialization");
    Account.TYPE.updateProperty(SERIALIZATION_PROPERTY, new Account.Serialization());
    Bank.TYPE.updateProperty(SERIALIZATION_PROPERTY, new Bank.Serialization());
    BankEntity.TYPE.updateProperty(SERIALIZATION_PROPERTY, new BankEntity.Serialization());
    Category.TYPE.updateProperty(SERIALIZATION_PROPERTY, new Category.Serialization());
    LabelToCategory.TYPE.updateProperty(SERIALIZATION_PROPERTY, new LabelToCategory.Serialization());
    Transaction.TYPE.updateProperty(SERIALIZATION_PROPERTY, new Transaction.Serialization());
    TransactionImport.TYPE.updateProperty(SERIALIZATION_PROPERTY, new TransactionImport.Serialization());
    TransactionToCategory.TYPE.updateProperty(SERIALIZATION_PROPERTY, new TransactionToCategory.Serialization());
  }
}
