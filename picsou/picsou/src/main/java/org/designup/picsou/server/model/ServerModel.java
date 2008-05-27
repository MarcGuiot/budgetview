package org.designup.picsou.server.model;

import org.crossbowlabs.globs.metamodel.GlobModel;
import org.crossbowlabs.globs.metamodel.utils.DefaultGlobModel;

public class ServerModel {
  private static GlobModel model = new DefaultGlobModel(
    UserCategoryAssociation.TYPE,
    User.TYPE,
    HiddenUser.TYPE,
    ReservedId.TYPE,
    Uncategorised.TYPE,
    Session.TYPE,
    HiddenTransactionToCategory.TYPE,
    HiddenLabelToCategory.TYPE,
    HiddenTransaction.TYPE,
    HiddenAccount.TYPE,
    HiddenBank.TYPE,
    HiddenImport.TYPE,
    HiddenCategory.TYPE
  );

  public static GlobModel get() {
    return model;
  }

  public static void main(String[] args) {
  }
}
