package com.budgetview.server.cloud.model;

import org.globsframework.metamodel.GlobModel;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.utils.GlobModelBuilder;

import java.util.Collection;

public class CloudDatabaseModel {

  private static GlobModel MODEL =
    GlobModelBuilder.init(CloudUser.TYPE,
                          CloudUserDevice.TYPE,
                          CloudInvoiceLog.TYPE,
                          CloudEmailValidation.TYPE,
                          ProviderUpdate.TYPE,
                          ProviderConnection.TYPE,
                          CloudConfig.TYPE)
      .get();

  public static GlobType[] getAllTypes() {
    Collection<GlobType> all = MODEL.getAll();
    return all.toArray(new GlobType[all.size()]);
  }
}
