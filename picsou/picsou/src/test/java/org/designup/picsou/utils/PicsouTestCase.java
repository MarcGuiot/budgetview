package org.designup.picsou.utils;

import org.designup.picsou.model.*;
import org.globsframework.metamodel.GlobModel;
import org.globsframework.model.DummyChangeSetListener;
import org.globsframework.model.GlobChecker;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.GlobRepositoryBuilder;
import org.globsframework.utils.ServicesTestCase;

import java.util.Locale;

public abstract class PicsouTestCase extends ServicesTestCase {
  protected GlobRepository repository;
  protected GlobChecker checker;
  protected GlobModel model;
  protected DummyChangeSetListener listener = new DummyChangeSetListener();

  protected void setUp() throws Exception {
    super.setUp();
    Locale.setDefault(Locale.ENGLISH);
    model = getModel();
    this.directory.add(GlobModel.class, model);
    this.checker = new GlobChecker(model);
    this.repository =
      GlobRepositoryBuilder.init()
        .add(TransactionType.values())
        .add(BudgetArea.values())
        .add(ProfileType.values())
        .add(AccountCardType.values())
        .get();

    Account.createSummary(repository);
    repository.addChangeListener(listener);
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    repository = null;
    directory = null;
    model = null;
    listener = null;
  }

  protected GlobModel getModel() {
    return PicsouModel.get();
  }
}
