package org.designup.picsou.importer;

import org.apache.commons.collections.iterators.ReverseListIterator;
import org.designup.picsou.gui.TimeService;
import org.designup.picsou.gui.importer.NoOperations;
import org.designup.picsou.gui.accounts.utils.Day;
import org.designup.picsou.model.DeferredCardPeriod;
import org.designup.picsou.importer.analyzer.TransactionAnalyzer;
import org.designup.picsou.importer.analyzer.TransactionAnalyzerFactory;
import org.designup.picsou.importer.utils.DateFormatAnalyzer;
import org.designup.picsou.importer.utils.TypedInputStream;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.Lang;
import org.designup.picsou.bank.BankPluginService;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.StringField;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.*;
import org.globsframework.model.delta.DefaultChangeSet;
import org.globsframework.model.delta.MutableChangeSet;
import org.globsframework.model.utils.ChangeSetAggregator;
import org.globsframework.utils.MultiMap;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidData;
import org.globsframework.utils.exceptions.TruncatedFile;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ImportSession {
  private GlobRepository referenceRepository;
  private Directory directory;
  private ImportService importService;
  private MutableChangeSet importChangeSet;
  private GlobRepository localRepository;
  private BankFileType fileType;
  private ChangeSetAggregator importChangeSetAggregator;
  private TypedInputStream typedStream;
  private boolean load = false;
  private int lastLoadOperationsCount = 0;
  private int importedOperationsCount = 0;

  public ImportSession(GlobRepository referenceRepository, Directory directory) {
    this.referenceRepository = referenceRepository;
    this.directory = directory;
    this.importService = directory.get(ImportService.class);
    this.localRepository =
      GlobRepositoryBuilder.init(referenceRepository.getIdGenerator())
        .add(referenceRepository.getAll(AccountUpdateMode.TYPE))
        .get();
  }

  public GlobRepository getTempRepository() {
    return localRepository;
  }

  public List<String> loadFile(File file) throws IOException, TruncatedFile, NoOperations {
    localRepository.reset(GlobList.EMPTY, Transaction.TYPE, ImportedTransaction.TYPE, Day.TYPE, CurrentMonth.TYPE,
                          DeferredCardDate.TYPE, DeferredCardPeriod.TYPE, AccountCardType.TYPE, AccountType.TYPE);
    GlobType[] types = {Bank.TYPE, BankEntity.TYPE, Account.TYPE, Day.TYPE, DeferredCardDate.TYPE,
                        DeferredCardPeriod.TYPE, AccountCardType.TYPE, CurrentMonth.TYPE};
    localRepository.reset(referenceRepository.getAll(types), types);

    importChangeSet = new DefaultChangeSet();
    importChangeSetAggregator = new ChangeSetAggregator(localRepository, importChangeSet);

    localRepository.startChangeSet();
    try {
      typedStream = new TypedInputStream(file);
      fileType = typedStream.getType();
      importService.run(typedStream, referenceRepository, localRepository);
    }
    finally {
      localRepository.completeChangeSet();
    }


    BankPluginService bankPluginService = directory.get(BankPluginService.class);
    bankPluginService.apply(referenceRepository, localRepository, importChangeSet);

    load = true;
    GlobList importedOperations = localRepository.getAll(ImportedTransaction.TYPE);
    if (importedOperations.isEmpty()){
      throw new NoOperations();
    }
    lastLoadOperationsCount = importedOperations.size();
    return getImportedTransactionFormat();
  }

  private List<String> getImportedTransactionFormat() {
    Set<String> valueSet = localRepository.getAll(ImportedTransaction.TYPE)
      .getValueSet(ImportedTransaction.BANK_DATE);
    DateFormatAnalyzer dateFormatAnalyzer = new DateFormatAnalyzer(TimeService.getToday());
    return dateFormatAnalyzer.parse(valueSet);
  }

  public Key importTransactions(Key currentlySelectedAccount, String selectedDateFormat) {
    if (!load) {
      return null;
    }
    load = false;

    if (fileType.equals(BankFileType.QIF)) {
      GlobList transactions = localRepository.getAll(ImportedTransaction.TYPE);
      for (Glob transaction : transactions) {
        localRepository.update(transaction.getKey(), ImportedTransaction.ACCOUNT, currentlySelectedAccount.get(Account.ID));
      }
    }

    GlobList allNewTransactions = convertImportedTransaction(selectedDateFormat);

    Key importKey = createImport(typedStream, allNewTransactions, localRepository);
    localRepository.deleteAll(ImportedTransaction.TYPE);
    importChangeSetAggregator.dispose();
    try {
      DefaultChangeSet updateImportChangeSet = new DefaultChangeSet();
      referenceRepository.startChangeSet();
      ChangeSetAggregator updateImportAggregator = new ChangeSetAggregator(localRepository, updateImportChangeSet);
      localRepository.startChangeSet();

      try {
        MultiMap<Integer, Glob> transactionByAccountId = new MultiMap<Integer, Glob>();
        for (Key key : importChangeSet.getCreated(Transaction.TYPE)) {
          Glob transaction = localRepository.get(key);
          transactionByAccountId.put(transaction.get(Transaction.ACCOUNT), transaction);
        }
        for (Map.Entry<Integer, List<Glob>> accountIdAndTransactions : transactionByAccountId.entries()) {
          Glob account = localRepository.get(Key.create(Account.TYPE, accountIdAndTransactions.getKey()));
          Glob bank = localRepository.findLinkTarget(account, Account.BANK);
          Integer bankId = Bank.GENERIC_BANK_ID;
          if (bank != null) {
            bankId = bank.get(Bank.ID);
          }
          TransactionAnalyzer transactionAnalyzer = directory.get(TransactionAnalyzerFactory.class).getAnalyzer();
          transactionAnalyzer.processTransactions(bankId, accountIdAndTransactions.getValue(),
                                                  localRepository);
          localRepository.update(account.getKey(), Account.IS_IMPORTED_ACCOUNT, true);

          TransactionFilter transactionFilter = new TransactionFilter();
          transactionFilter.loadTransactions(referenceRepository, localRepository,
                                             new GlobList(accountIdAndTransactions.getValue()),
                                             currentlySelectedAccount != null ?
                                             currentlySelectedAccount.get(Account.ID) : null);
        }
      }
      finally {
        localRepository.completeChangeSet();
      }
      updateImportAggregator.dispose();
      referenceRepository.apply(importChangeSet);
      referenceRepository.apply(updateImportChangeSet);
    }
    finally {
      referenceRepository.completeChangeSet();
    }
    importedOperationsCount += lastLoadOperationsCount;
    return importKey;
  }

  public int getImportedOperationsCount() {
    return importedOperationsCount;
  }

  private GlobList convertImportedTransaction(String selectedDateFormat) {
    DateFormat dateFormat = new SimpleDateFormat(selectedDateFormat);
    GlobList importedTransactions = localRepository.getAll(ImportedTransaction.TYPE).sort(ImportedTransaction.ID);
    if (importedTransactions.isEmpty()) {
      return GlobList.EMPTY;
    }
    Glob firstTransaction = importedTransactions.getFirst();
    Glob lastTransaction = importedTransactions.getLast();
    Iterator<Glob> iterator = importedTransactions.iterator();
    if (lastTransaction != firstTransaction) {
      Date firstDate = parseDate(dateFormat, firstTransaction, ImportedTransaction.BANK_DATE);
      Date lastDate = parseDate(dateFormat, lastTransaction, ImportedTransaction.BANK_DATE);
      if (lastDate.before(firstDate)) {
        iterator = new ReverseListIterator(importedTransactions);
      }
    }

    GlobList createdTransactions = new GlobList();

    int nextId = localRepository.getIdGenerator().getNextId(Transaction.ID, importedTransactions.size() + 10);

    for (; iterator.hasNext();) {
      Glob importedTransaction = iterator.next();
      Date bankDate = parseDate(dateFormat, importedTransaction, ImportedTransaction.BANK_DATE);
      Date userDate = parseDate(dateFormat, importedTransaction, ImportedTransaction.DATE);

      Glob transaction = localRepository.create(
        Key.create(Transaction.TYPE, nextId),
        value(Transaction.TRANSACTION_TYPE,
              importedTransaction.get(ImportedTransaction.IS_CARD, false) ? TransactionType.getId(TransactionType.CREDIT_CARD) : null),
        value(Transaction.BANK_MONTH, Month.getMonthId(bankDate)),
        value(Transaction.BANK_DAY, Month.getDay(bankDate)),
        value(Transaction.POSITION_MONTH, Month.getMonthId(bankDate)),
        value(Transaction.POSITION_DAY, Month.getDay(bankDate)),
        value(Transaction.MONTH, userDate == null ? null : Month.getMonthId(userDate)),
        value(Transaction.DAY, userDate == null ? null : Month.getDay(userDate)),
        value(Transaction.BUDGET_MONTH, userDate == null ? null : Month.getMonthId(userDate)),
        value(Transaction.BUDGET_DAY, userDate == null ? null : Month.getDay(userDate)),
        value(Transaction.ACCOUNT, importedTransaction.get(ImportedTransaction.ACCOUNT)),
        value(Transaction.AMOUNT, importedTransaction.get(ImportedTransaction.AMOUNT)),
        value(Transaction.NOTE, importedTransaction.get(ImportedTransaction.NOTE)),
        value(Transaction.BANK_TRANSACTION_TYPE, TransactionAnalyzerFactory.removeBlankAndToUpercase(importedTransaction.get(ImportedTransaction.BANK_TRANSACTION_TYPE))),
        value(Transaction.SPLIT, importedTransaction.get(ImportedTransaction.SPLIT)),
        value(Transaction.SPLIT_SOURCE, importedTransaction.get(ImportedTransaction.SPLIT_SOURCE)),
        value(Transaction.OFX_CHECK_NUM, TransactionAnalyzerFactory.removeBlankAndToUpercase(importedTransaction.get(ImportedTransaction.OFX_CHECK_NUM))),
        value(Transaction.OFX_MEMO, TransactionAnalyzerFactory.removeBlankAndToUpercase(importedTransaction.get(ImportedTransaction.OFX_MEMO))),
        value(Transaction.OFX_NAME, TransactionAnalyzerFactory.removeBlankAndToUpercase(importedTransaction.get(ImportedTransaction.OFX_NAME))),
        value(Transaction.QIF_M, TransactionAnalyzerFactory.removeBlankAndToUpercase(importedTransaction.get(ImportedTransaction.QIF_M))),
        value(Transaction.QIF_P, TransactionAnalyzerFactory.removeBlankAndToUpercase(importedTransaction.get(ImportedTransaction.QIF_P))),
        value(Transaction.IS_OFX, importedTransaction.get(ImportedTransaction.IS_OFX))
      );
      createdTransactions.add(transaction);
      nextId++;
    }
    return createdTransactions;
  }

  private Date parseDate(DateFormat dateFormat, Glob glob, StringField dateField) {
    try {
      String stringifiedDate = glob.get(dateField);
      return stringifiedDate == null ? null : dateFormat.parse(stringifiedDate);
    }
    catch (ParseException e) {
      throw new InvalidData("Unable to parse date " + dateField + " in format " + dateFormat, e);
    }
  }

  public void discard() {
    importChangeSetAggregator.dispose();
  }

  public Key createImport(TypedInputStream file, GlobList createdTransactions, GlobRepository targetRepository) {
    Glob transactionImport =
      targetRepository.create(TransactionImport.TYPE,
                              value(TransactionImport.IMPORT_DATE, TimeService.getToday()),
                              value(TransactionImport.SOURCE, file.getName()));

    Key importKey = transactionImport.getKey();

    for (Glob createdTransaction : createdTransactions) {
      targetRepository.setTarget(createdTransaction.getKey(), Transaction.IMPORT, importKey);
    }
    return importKey;
  }

  public boolean isAccountNeeded() {
    return fileType.equals(BankFileType.QIF);
  }
}
