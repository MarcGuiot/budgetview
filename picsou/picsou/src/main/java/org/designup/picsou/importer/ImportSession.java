package org.designup.picsou.importer;

import org.crossbowlabs.globs.metamodel.GlobType;
import static org.crossbowlabs.globs.model.FieldValue.value;
import org.crossbowlabs.globs.model.*;
import org.crossbowlabs.globs.model.delta.DefaultChangeSet;
import org.crossbowlabs.globs.model.delta.MutableChangeSet;
import org.crossbowlabs.globs.model.utils.ChangeSetAggregator;
import org.crossbowlabs.globs.utils.MultiMap;
import org.crossbowlabs.globs.utils.directory.Directory;
import org.crossbowlabs.globs.utils.exceptions.TruncatedFile;
import org.designup.picsou.client.AllocationLearningService;
import org.designup.picsou.importer.analyzer.TransactionAnalyzer;
import org.designup.picsou.importer.analyzer.TransactionAnalyzerFactory;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.Lang;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ImportSession {
  private GlobRepository repository;
  private Directory directory;
  private ImportService importService;
  protected Key importKey;
  protected MutableChangeSet importChangeSet;
  protected GlobRepository localRepository;
  private BankFileType fileType;
  private ChangeSetAggregator importChangeSetAggregator;

  public ImportSession(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.directory = directory;
    this.importService = directory.get(ImportService.class);
    this.localRepository = GlobRepositoryBuilder.init(repository.getIdGenerator()).get();
  }

  public GlobRepository getTempRepository() {
    return localRepository;
  }

  public void loadFile(File file) throws IOException, TruncatedFile {
    localRepository.reset(GlobList.EMPTY,
                          Transaction.TYPE, TransactionToCategory.TYPE, LabelToCategory.TYPE);
    GlobType[] types = {Bank.TYPE, BankEntity.TYPE, Account.TYPE, Category.TYPE};
    localRepository.reset(repository.getAll(types), types);

    importChangeSet = new DefaultChangeSet();
    importChangeSetAggregator = new ChangeSetAggregator(localRepository, importChangeSet);
    localRepository.enterBulkDispatchingMode();

    TypedInputStream typedStream = new TypedInputStream(file);
    fileType = typedStream.getType();
    importKey = importService.run(typedStream, repository, localRepository);
    localRepository.completeBulkDispatchingMode();
  }

  public void importTransactions(Glob currentlySelectedAccount) {
    importChangeSetAggregator.dispose();
    try {
      DefaultChangeSet updateImportChangeSet = new DefaultChangeSet();
      repository.enterBulkDispatchingMode();
      ChangeSetAggregator updateImportAggregator = new ChangeSetAggregator(localRepository, updateImportChangeSet);
      localRepository.enterBulkDispatchingMode();
// TODO:     importChangeSet.getCreated(BankEntity.TYPE);
      if (fileType.equals(BankFileType.QIF)) {
        for (Key key : importChangeSet.getCreated(Transaction.TYPE)) {
          localRepository.update(key, Transaction.ACCOUNT, currentlySelectedAccount.get(Account.ID));
        }
      }

      TransactionAnalyzer transactionAnalyzer = directory.get(TransactionAnalyzerFactory.class).getAnalyzer();
      MultiMap<Integer, Glob> transactionByAccountId = new MultiMap<Integer, Glob>();
      for (Key key : importChangeSet.getCreated(Transaction.TYPE)) {
        Glob transaction = localRepository.get(key);
        transactionByAccountId.put(transaction.get(Transaction.ACCOUNT), transaction);
      }
      for (Map.Entry<Integer, List<Glob>> accountIdAndTransactions : transactionByAccountId.values()) {
        Glob account = localRepository.get(Key.create(Account.TYPE, accountIdAndTransactions.getKey()));
        Glob bankEntity = localRepository.findLinkTarget(account, Account.BANK_ENTITY);
        Glob bank = localRepository.findLinkTarget(bankEntity, BankEntity.BANK);
        Integer id = Bank.UNKNOWN_BANK_ID;
        if (bank != null) {
          id = bank.get(Bank.ID);
        }
        transactionAnalyzer.processTransactions(id, accountIdAndTransactions.getValue(), localRepository);
      }
      localRepository.completeBulkDispatchingMode();
      updateImportAggregator.dispose();
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

  public void discard() {
    importChangeSetAggregator.dispose();    
  }

  public Glob createDefaultAccount() {
    return localRepository.create(Account.TYPE,
                                  value(Account.NAME, Lang.get("account.default.current.name")));
  }
}
