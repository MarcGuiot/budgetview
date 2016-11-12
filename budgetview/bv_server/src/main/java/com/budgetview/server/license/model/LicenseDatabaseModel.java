package com.budgetview.server.license.model;

import com.budgetview.server.cloud.model.CloudUser;
import com.budgetview.server.cloud.model.CloudUserDevice;
import com.budgetview.server.cloud.model.ProviderUpdate;
import com.budgetview.server.license.mail.Mailer;
import org.globsframework.metamodel.GlobModel;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.utils.GlobModelBuilder;

import java.util.Collection;

public class LicenseDatabaseModel {

  private static GlobModel MODEL =
    GlobModelBuilder.init(License.TYPE,
                          MailError.TYPE,
                          RepoInfo.TYPE,
                          SoftwareInfo.TYPE)
      .get();

  public static GlobType[] getAllTypes() {
    Collection<GlobType> all = MODEL.getAll();
    return all.toArray(new GlobType[all.size()]);
  }
}
