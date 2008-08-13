package org.designup.picsou.server.serialization;

import org.designup.picsou.model.*;
import org.globsframework.metamodel.GlobModel;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.properties.Property;

public class SerializationManager {
  public static Property<GlobType, PicsouGlobSerializer> SERIALIZATION_PROPERTY;

  public static void init(GlobModel globModel) {
    SERIALIZATION_PROPERTY = globModel.createGlobTypeProperty("serialization");

    Account.TYPE.updateProperty(SERIALIZATION_PROPERTY, new Account.Serializer());
    Bank.TYPE.updateProperty(SERIALIZATION_PROPERTY, new Bank.Serializer());
    BankEntity.TYPE.updateProperty(SERIALIZATION_PROPERTY, new BankEntity.Serializer());
    Category.TYPE.updateProperty(SERIALIZATION_PROPERTY, new Category.Serializer());
    LabelToCategory.TYPE.updateProperty(SERIALIZATION_PROPERTY, new LabelToCategory.Serializer());
    Transaction.TYPE.updateProperty(SERIALIZATION_PROPERTY, new Transaction.Serializer());
    TransactionImport.TYPE.updateProperty(SERIALIZATION_PROPERTY, new TransactionImport.Serializer());
    TransactionToCategory.TYPE.updateProperty(SERIALIZATION_PROPERTY, new TransactionToCategory.Serializer());
    Month.TYPE.updateProperty(SERIALIZATION_PROPERTY, new Month.Serializer());
    Series.TYPE.updateProperty(SERIALIZATION_PROPERTY, new Series.Serializer());
    SeriesBudget.TYPE.updateProperty(SERIALIZATION_PROPERTY, new SeriesBudget.Serializer());
    SeriesToCategory.TYPE.updateProperty(SERIALIZATION_PROPERTY, new SeriesToCategory.Serializer());
    UserPreferences.TYPE.updateProperty(SERIALIZATION_PROPERTY, new UserPreferences.Serializer());
  }
}
