package org.designup.picsou.model;

import org.globsframework.metamodel.GlobModel;
import org.globsframework.metamodel.utils.GlobModelBuilder;

public class PicsouModel {
  private static GlobModel model =
    GlobModelBuilder.init(Account.TYPE,
                          Bank.TYPE,
                          BankEntity.TYPE,
                          Category.TYPE,
                          Month.TYPE,
                          Transaction.TYPE,
                          TransactionType.TYPE,
                          TransactionToCategory.TYPE,
                          LabelToCategory.TYPE,
                          TransactionTypeMatcher.TYPE,
                          TransactionImport.TYPE)
      .get();

  public static GlobModel get() {
    return model;
  }
}
