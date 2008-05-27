package org.designup.picsou.utils;

import org.crossbowlabs.globs.metamodel.GlobModel;
import org.crossbowlabs.globs.model.DummyChangeSetListener;
import org.crossbowlabs.globs.model.GlobChecker;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.GlobRepositoryBuilder;
import org.crossbowlabs.globs.utils.ServicesTestCase;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.PicsouModel;
import org.designup.picsou.model.TransactionType;

import java.util.Locale;

public abstract class PicsouTestCase extends ServicesTestCase {
  protected GlobRepository repository;
  protected GlobChecker checker;
  protected GlobModel model;
  protected DummyChangeSetListener listener = new DummyChangeSetListener();

  protected void setUp() throws Exception {
    super.setUp();
    Locale.setDefault(Locale.US);
    model = getModel();
    this.directory.add(GlobModel.class, model);
    this.checker = new GlobChecker(model);
    this.repository =
      GlobRepositoryBuilder.init()
        .add(TransactionType.values())
        .add(MasterCategory.createGlobs())
        .get();

    Account.createSummary(repository);
    repository.addChangeListener(listener);
  }

  protected GlobModel getModel() {
    return PicsouModel.get();
  }
}
