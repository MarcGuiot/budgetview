package org.designup.picsou.importer;

import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.model.*;
import static org.crossbowlabs.globs.model.FieldValue.value;
import org.crossbowlabs.globs.model.utils.ChangeSetAggregator;
import org.crossbowlabs.globs.utils.Log;
import org.crossbowlabs.globs.utils.MultiMap;
import org.crossbowlabs.globs.utils.exceptions.TruncatedFile;
import org.crossbowlabs.globs.utils.directory.Directory;
import org.designup.picsou.client.AllocationLearningService;
import org.designup.picsou.importer.analyzer.TransactionAnalyzer;
import org.designup.picsou.importer.analyzer.TransactionAnalyzerFactory;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.Lang;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ImportSession {

  private static final String DEFAULT_ACCOUNT_ID = "0";
  private static final String DEFAULT_ACCOUNT_NAME = "Compte principal";
  public static final int DEFAULT_BANK_ID = 99999;
  private static final int DEFAULT_BRANCH_ID = 10000;

  public static final int DEFAULT_BANK_ENTITY_ID = 30003;
  private GlobRepository repository;
  private Directory directory;
  private ImportService importService;
  protected Key importKey;
  protected ChangeSet importChangeSet;
  protected GlobRepository targetRepository;
  private BankFileType fileType;

  public ImportSession(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.directory = directory;
    this.importService = directory.get(ImportService.class);
    this.targetRepository = GlobRepositoryBuilder.init(repository.getIdGenerator()).get();
  }

  public GlobRepository getTempRepository() {
    return targetRepository;
  }

  public void loadFile(File file) throws IOException, TruncatedFile {
    targetRepository.reset(GlobList.EMPTY,
                           Transaction.TYPE, TransactionToCategory.TYPE, LabelToCategory.TYPE);
    GlobType[] types = {Bank.TYPE, BankEntity.TYPE, Account.TYPE, Category.TYPE};
    targetRepository.reset(repository.getAll(types), types);

    ChangeSetAggregator importChangeSetAggregator = new ChangeSetAggregator(targetRepository);
    targetRepository.enterBulkDispatchingMode();

    TypedInputStream typedStream = new TypedInputStream(file);
    fileType = typedStream.getType();
    importKey = importService.run(typedStream, repository, targetRepository);
    targetRepository.completeBulkDispatchingMode();
    importChangeSet = importChangeSetAggregator.dispose();
  }

  public void importTransactions() {
    try {
      repository.enterBulkDispatchingMode();
      ChangeSetAggregator updateImportAggregator = new ChangeSetAggregator(targetRepository);
      targetRepository.enterBulkDispatchingMode();
// TODO:     importChangeSet.getCreated(BankEntity.TYPE);
      if (fileType.equals(BankFileType.QIF)) {
        Integer accountId = createDefaultAccountIfNeeded(targetRepository).get(Account.ID);
        for (Key key : importChangeSet.getCreated(Transaction.TYPE)) {
          targetRepository.update(key, Transaction.ACCOUNT, accountId);
        }
      }

      TransactionAnalyzer transactionAnalyzer = directory.get(TransactionAnalyzerFactory.class).getAnalyzer();
      MultiMap<Integer, Glob> transactionByAccountId = new MultiMap<Integer, Glob>();
      for (Key key : importChangeSet.getCreated(Transaction.TYPE)) {
        Glob transaction = targetRepository.get(key);
        transactionByAccountId.put(transaction.get(Transaction.ACCOUNT), transaction);
      }
      for (Map.Entry<Integer, List<Glob>> accountIdAndTransactions : transactionByAccountId.values()) {
        Glob account = targetRepository.get(Key.create(Account.TYPE, accountIdAndTransactions.getKey()));
        Glob bankEntity = targetRepository.findLinkTarget(account, Account.BANK_ENTITY);
        Glob bank = targetRepository.findLinkTarget(bankEntity, BankEntity.BANK);
        Integer id = Bank.UNKNOWN_BANK_ID;
        if (bank != null) {
          id = bank.get(Bank.ID);
        }
        transactionAnalyzer.processTransactions(id, accountIdAndTransactions.getValue(), targetRepository);
      }
      targetRepository.completeBulkDispatchingMode();
      ChangeSet updateImportChangeSet = updateImportAggregator.dispose();
      repository.apply(importChangeSet);
      repository.apply(updateImportChangeSet);
      AllocationLearningService learningService = directory.get(AllocationLearningService.class);
      for (Map.Entry<Integer, List<Glob>> transactions : transactionByAccountId.values()) {
        learningService.setCategories(transactions.getValue(), repository);
      }
    }
    finally {
      repository.completeBulkDispatchingMode();
    }
  }

  public static Glob createDefaultAccountIfNeeded(GlobRepository globRepository) {
    Glob account = globRepository.findUnique(Account.TYPE, value(Account.BANK_ENTITY, DEFAULT_BANK_ENTITY_ID));
    if (account == null) {
      return globRepository.create(Key.create(Account.TYPE, 0),
                                   value(Account.NUMBER, DEFAULT_ACCOUNT_ID),
                                   value(Account.NAME, DEFAULT_ACCOUNT_NAME),
                                   value(Account.BRANCH_ID, DEFAULT_BRANCH_ID),
                                   value(Account.BANK_ENTITY, DEFAULT_BANK_ENTITY_ID));
    }
    return account;
  }
}
