package org.designup.picsou.server.serialization;

import org.designup.picsou.model.*;
import org.designup.picsou.model.DeferredCardPeriod;
import org.globsframework.metamodel.GlobModel;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.properties.Property;

import java.util.Set;
import java.util.HashSet;

public class SerializationManager {
  public static Property<GlobType, PicsouGlobSerializer> SERIALIZATION_PROPERTY;
  public static Set<GlobType> REMOVED_GLOB = new HashSet<GlobType>();

  public static void init(GlobModel globModel) {

    REMOVED_GLOB.add(Bank.TYPE);
    REMOVED_GLOB.add(BankEntity.TYPE);

    SERIALIZATION_PROPERTY = globModel.createGlobTypeProperty("serialization");

    Account.TYPE.updateProperty(SERIALIZATION_PROPERTY, new Account.Serializer());
    DeferredCardPeriod.TYPE.updateProperty(SERIALIZATION_PROPERTY, new DeferredCardPeriod.Serializer());
    DeferredCardDate.TYPE.updateProperty(SERIALIZATION_PROPERTY, new DeferredCardDate.Serializer());
    Category.TYPE.updateProperty(SERIALIZATION_PROPERTY, new Category.Serializer());
    Transaction.TYPE.updateProperty(SERIALIZATION_PROPERTY, new Transaction.Serializer());
    TransactionImport.TYPE.updateProperty(SERIALIZATION_PROPERTY, new TransactionImport.Serializer());
    Month.TYPE.updateProperty(SERIALIZATION_PROPERTY, new Month.Serializer());
    CurrentMonth.TYPE.updateProperty(SERIALIZATION_PROPERTY, new CurrentMonth.Serializer());
    Series.TYPE.updateProperty(SERIALIZATION_PROPERTY, new Series.Serializer());
    SeriesBudget.TYPE.updateProperty(SERIALIZATION_PROPERTY, new SeriesBudget.Serializer());
    SeriesToCategory.TYPE.updateProperty(SERIALIZATION_PROPERTY, new SeriesToCategory.Serializer());
    UserPreferences.TYPE.updateProperty(SERIALIZATION_PROPERTY, new UserPreferences.Serializer());
    UserVersionInformation.TYPE.updateProperty(SERIALIZATION_PROPERTY, new UserVersionInformation.Serializer());
    AccountPositionThreshold.TYPE.updateProperty(SERIALIZATION_PROPERTY, new AccountPositionThreshold.Serializer());
    SubSeries.TYPE.updateProperty(SERIALIZATION_PROPERTY, new SubSeries.Serializer());
    Notes.TYPE.updateProperty(SERIALIZATION_PROPERTY, new Notes.Serializer());
    SignpostStatus.TYPE.updateProperty(SERIALIZATION_PROPERTY, new SignpostStatus.Serializer());
    Project.TYPE.updateProperty(SERIALIZATION_PROPERTY, new Project.Serializer());
    ProjectItem.TYPE.updateProperty(SERIALIZATION_PROPERTY, new ProjectItem.Serializer());
    RealAccount.TYPE.updateProperty(SERIALIZATION_PROPERTY, new RealAccount.Serializer());
  }
}
