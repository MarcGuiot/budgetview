package com.budgetview.server.cloud.model;

import com.budgetview.shared.model.Provider;
import org.globsframework.metamodel.GlobModel;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.utils.GlobModelBuilder;

import java.util.Collection;

public class CloudDatabaseModel {
  public static final int MAJOR_VERSION = 1;
  public static final int MINOR_VERSION = 0;

  private static GlobModel MODEL =
    GlobModelBuilder.init(CloudUser.TYPE,
                          ProviderUpdate.TYPE)
      .get();

  public static GlobType[] getAllTypes() {
    Collection<GlobType> all = MODEL.getAll();
    return all.toArray(new GlobType[all.size()]);
  }
}
