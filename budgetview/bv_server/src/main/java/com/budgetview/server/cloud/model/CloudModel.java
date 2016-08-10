package com.budgetview.server.cloud.model;

import org.globsframework.metamodel.GlobModel;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.utils.GlobModelBuilder;

import java.util.Collection;

public class CloudModel {
  public static final int MAJOR_VERSION = 1;
  public static final int MINOR_VERSION = 0;

  private static GlobModel MODEL =
    GlobModelBuilder.init(CloudUser.TYPE,
                          ProviderAccount.TYPE,
                          ProviderTransaction.TYPE)
      .get();

  public static GlobModel get() {
    return MODEL;
  }

  public static GlobType[] getAllTypes() {
    Collection<GlobType> all = MODEL.getAll();
    return all.toArray(new GlobType[all.size()]);
  }
}
