package org.designup.picsou.gui.upgrade;

import org.designup.picsou.importer.analyzer.TransactionAnalyzer;
import org.designup.picsou.importer.analyzer.TransactionAnalyzerFactory;
import org.designup.picsou.model.*;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;

public class UpgradeService {
  private Directory directory;

  public UpgradeService(Directory directory) {
    this.directory = directory;
  }

  public void upgradeBankData(GlobRepository repository, Glob version) {
    repository.startChangeSet();
    try {
      GlobList accounts = repository.getAll(Account.TYPE);
      for (Glob account : accounts) {
        if (Account.SUMMARY_ACCOUNT_IDS.contains(account.get(Account.ID))) {
          continue;
        }
        Glob bank = Account.getBank(account, repository);
        Integer bankEntityId = BankEntity.find(account.get(Account.BANK_ENTITY_LABEL), repository);
        if (bankEntityId != null) {
          Integer newBankId = BankEntity.getBank(repository.find(Key.create(BankEntity.TYPE, bankEntityId)),
                                                 repository).get(Bank.ID);
          if (!newBankId.equals(bank.get(Bank.ID))) {
            repository.update(account.getKey(),
                              FieldValue.value(Account.BANK, newBankId),
                              FieldValue.value(Account.BANK_ENTITY, bankEntityId));
          }
        }
        updateOperations(repository, account);
      }
      repository.update(UserVersionInformation.KEY, UserVersionInformation.CURRENT_BANK_CONFIG_VERSION,
                        version.get(AppVersionInformation.LATEST_BANK_CONFIG_SOFTWARE_VERSION));
    }
    finally {
      repository.completeChangeSet();
    }
  }

  public void updateOperations(GlobRepository repository, Glob account) {
    GlobList transactions = repository.getAll(Transaction.TYPE,
                                              GlobMatchers.and(
                                              GlobMatchers.fieldEquals(Transaction.ACCOUNT, account.get(Account.ID)),
                                              GlobMatchers.fieldEquals(Transaction.PLANNED, false),
                                              GlobMatchers.fieldEquals(Transaction.CREATED_BY_SERIES, false)));
    TransactionAnalyzerFactory analyzerFactory = directory.get(TransactionAnalyzerFactory.class);
    TransactionAnalyzer transactionAnalyzer = analyzerFactory.getAnalyzer();
    transactionAnalyzer.processTransactions(account.get(Account.BANK), transactions, repository);
  }
}
