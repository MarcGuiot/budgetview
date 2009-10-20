package org.designup.picsou.model;

import org.designup.picsou.server.serialization.SerializationManager;
import org.globsframework.metamodel.GlobModel;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.utils.GlobModelBuilder;

import java.util.*;

public class PicsouModel {
  private static GlobModel MODEL =
    GlobModelBuilder.init(Account.TYPE,
                          AccountType.TYPE,
                          AccountUpdateMode.TYPE,
                          Bank.TYPE,
                          BankEntity.TYPE,
                          Category.TYPE,
                          Month.TYPE,
                          CurrentMonth.TYPE,
                          Transaction.TYPE,
                          TransactionType.TYPE,
                          PreTransactionTypeMatcher.TYPE,
                          TransactionImport.TYPE,
                          UserPreferences.TYPE,
                          Series.TYPE,
                          SubSeries.TYPE,
                          SeriesToCategory.TYPE,
                          SeriesBudget.TYPE,
                          BudgetArea.TYPE,
                          ProfileType.TYPE,
                          AppVersionInformation.TYPE,
                          UserVersionInformation.TYPE,
                          AccountPositionThreshold.TYPE,
                          Notes.TYPE)
      .get();

  static {
    SerializationManager.init(MODEL);
  }

  public static GlobModel get() {
    return MODEL;
  }

  public static Collection<GlobType> getUserSpecificType(){
    Collection<GlobType> globTypeCollection = new ArrayList<GlobType>(MODEL.getAll());
    Set<GlobType> typeNotToRemove = MODEL.getConstants().getTypes();
    typeNotToRemove.addAll(Arrays.asList(PreTransactionTypeMatcher.TYPE, Bank.TYPE, BankEntity.TYPE,
                                         User.TYPE, AppVersionInformation.TYPE));

    globTypeCollection.removeAll(typeNotToRemove);
    return  globTypeCollection;
  }
}
