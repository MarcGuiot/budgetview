package com.budgetview.session.serialization;

import com.budgetview.model.*;
import com.budgetview.model.deprecated.*;
import com.budgetview.shared.utils.GlobSerializer;
import org.globsframework.metamodel.GlobModel;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.properties.Property;

import java.util.HashSet;
import java.util.Set;

public class SerializationManager {
  public static Property<GlobType, GlobSerializer> SERIALIZATION_PROPERTY;
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
    CsvMapping.TYPE.updateProperty(SERIALIZATION_PROPERTY, new CsvMapping.Serializer());
    Bank.TYPE.updateProperty(SERIALIZATION_PROPERTY, new Bank.Serializer());
    BankEntity.TYPE.updateProperty(SERIALIZATION_PROPERTY, new BankEntity.Serializer());
    AccountPositionError.TYPE.updateProperty(SERIALIZATION_PROPERTY, new AccountPositionError.Serializer());
    Synchro.TYPE.updateProperty(SERIALIZATION_PROPERTY, new Synchro.Serializer());
    Picture.TYPE.updateProperty(SERIALIZATION_PROPERTY, new Picture.Serializer());
    ProjectTransfer.TYPE.updateProperty(SERIALIZATION_PROPERTY, new ProjectTransfer.Serializer());
    ProjectItemAmount.TYPE.updateProperty(SERIALIZATION_PROPERTY, new ProjectItemAmount.Serializer());
    LayoutConfig.TYPE.updateProperty(SERIALIZATION_PROPERTY, new LayoutConfig.Serializer());
    SeriesGroup.TYPE.updateProperty(SERIALIZATION_PROPERTY, new SeriesGroup.Serializer());
    AddOns.TYPE.updateProperty(SERIALIZATION_PROPERTY, new AddOns.Serializer());
    StandardMessage.TYPE.updateProperty(SERIALIZATION_PROPERTY, new StandardMessage.Serializer());
    ProjectAccountGraph.TYPE.updateProperty(SERIALIZATION_PROPERTY, new ProjectAccountGraph.Serializer());
    CloudDesktopUser.TYPE.updateProperty(SERIALIZATION_PROPERTY, new CloudDesktopUser.Serializer());
    CloudProviderConnection.TYPE.updateProperty(SERIALIZATION_PROPERTY, new CloudProviderConnection.Serializer());
  }
}
