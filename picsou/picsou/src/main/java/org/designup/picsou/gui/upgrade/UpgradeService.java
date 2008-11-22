package org.designup.picsou.gui.upgrade;

import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.importer.analyzer.TransactionAnalyzer;
import org.designup.picsou.importer.analyzer.TransactionAnalyzerFactory;
import org.designup.picsou.model.*;
import org.globsframework.model.FieldValue;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;

public class UpgradeService {
  private Directory directory;

  public UpgradeService(Directory directory) {
    this.directory = directory;
  }

  public void upgrade(GlobRepository repository, Glob version) {
    repository.startChangeSet();
    try {
      if (version.get(VersionInformation.CURRENT_JAR_VERSION) == 2) {
        PlannedTransactionCorrecter plannedTransactionCorrecter = new PlannedTransactionCorrecter(repository);
        plannedTransactionCorrecter.check();
        plannedTransactionCorrecter.correct();
      }
      if (version.get(VersionInformation.CURRENT_JAR_VERSION) <= 4) {
        repository.update(Series.OCCASIONAL_SERIES,
                          FieldValue.value(Series.PROFILE_TYPE, ProfileType.EVERY_MONTH.getId()),
                          FieldValue.value(Series.DEFAULT_CATEGORY, Category.NONE),
                          FieldValue.value(Series.DAY, 1),
                          FieldValue.value(Series.LABEL, "occasional"));
      }
      repository.update(VersionInformation.KEY, VersionInformation.CURRENT_JAR_VERSION, PicsouApplication.JAR_VERSION);
    }
    finally {
      repository.completeChangeSet();
    }
  }

  public void applyFilter(GlobRepository repository, Glob version) {
    repository.startChangeSet();
    try {
      TransactionAnalyzerFactory analyzerFactory = directory.get(TransactionAnalyzerFactory.class);
      TransactionAnalyzer transactionAnalyzer = analyzerFactory.getAnalyzer();
      GlobList accounts = repository.getAll(Account.TYPE);
      for (Glob account : accounts) {
        if (Account.SUMMARY_ACCOUNT.contains(account.get(Account.ID))) {
          continue;
        }
        Glob bank = Account.getBank(account, repository);
        GlobList transactions = repository.getAll(Transaction.TYPE,
                                                  GlobMatchers.fieldEquals(Transaction.ACCOUNT, bank.get(Bank.ID)));
        transactionAnalyzer.processTransactions(bank.get(Bank.ID), transactions, repository);
      }
      repository.update(VersionInformation.KEY, VersionInformation.CURRENT_BANK_CONFIG_VERSION,
                        version.get(VersionInformation.LATEST_BANK_CONFIG_SOFTWARE_VERSION));
    }
    finally {
      repository.completeChangeSet();
    }
  }
}
