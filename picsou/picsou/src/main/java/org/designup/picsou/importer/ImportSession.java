package org.designup.picsou.importer;

import org.designup.picsou.client.AllocationLearningService;
import org.designup.picsou.importer.analyzer.TransactionAnalyzer;
import org.designup.picsou.importer.analyzer.TransactionAnalyzerFactory;
import org.designup.picsou.importer.utils.DateFormatAnalyzer;
import org.designup.picsou.importer.utils.TypedInputStream;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.Lang;
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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ImportSession {
  private GlobRepository referenceRepository;
  private Directory directory;
  private ImportService importService;
  private MutableChangeSet importChangeSet;
  private GlobRepository localRepository;
  private BankFileType fileType;
  private ChangeSetAggregator importChangeSetAggregator;
  private TypedInputStream typedStream;

  public ImportSession(GlobRepository referenceRepository, Directory directory) {
    this.referenceRepository = referenceRepository;
    this.directory = directory;
    this.importService = directory.get(ImportService.class);
    this.localRepository = GlobRepositoryBuilder.init(referenceRepository.getIdGenerator()).get();
  }

  public GlobRepository getTempRepository() {
    return localRepository;
  }

  public List<String> loadFile(File file) throws IOException, TruncatedFile {
    localRepository.reset(GlobList.EMPTY,
                          Transaction.TYPE, TransactionToCategory.TYPE, LabelToCategory.TYPE,
                          ImportedTransaction.TYPE);
    GlobType[] types = {Bank.TYPE, BankEntity.TYPE, Account.TYPE, Category.TYPE};
    localRepository.reset(referenceRepository.getAll(types), types);

    importChangeSet = new DefaultChangeSet();
    importChangeSetAggregator = new ChangeSetAggregator(localRepository, importChangeSet);
    localRepository.enterBulkDispatchingMode();

    typedStream = new TypedInputStream(file);
    fileType = typedStream.getType();
    importService.run(typedStream, referenceRepository, localRepository);
    localRepository.completeBulkDispatchingMode();
    return getImportedTransactionFormat();
  }

  private List<String> getImportedTransactionFormat() {
    Set<String> valueSet = localRepository.getAll(ImportedTransaction.TYPE)
      .getValueSet(ImportedTransaction.BANK_DATE);
    DateFormatAnalyzer dateFormatAnalyzer = new DateFormatAnalyzer(new Date());
    return dateFormatAnalyzer.parse(valueSet);
  }

  public Key importTransactions(Glob currentlySelectedAccount, String selectedDateFormat) {

    TransactionFilter transactionFilter = new TransactionFilter();
    GlobList newTransactions = transactionFilter.loadTransactions(referenceRepository, localRepository,
                                                                  convertImportedTransaction(selectedDateFormat));

    Key importKey = createImport(typedStream, newTransactions, localRepository);
    localRepository.deleteAll(ImportedTransaction.TYPE);
    importChangeSetAggregator.dispose();
    try {
      DefaultChangeSet updateImportChangeSet = new DefaultChangeSet();
      referenceRepository.enterBulkDispatchingMode();
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
        transactionAnalyzer.processTransactions(id, accountIdAndTransactions.getValue(),
                                                localRepository);
      }
      localRepository.completeBulkDispatchingMode();
      updateImportAggregator.dispose();
      referenceRepository.apply(importChangeSet);
      referenceRepository.apply(updateImportChangeSet);
      AllocationLearningService learningService = directory.get(AllocationLearningService.class);
      for (Map.Entry<Integer, List<Glob>> transactions : transactionByAccountId.values()) {
        learningService.setCategories(transactions.getValue(), referenceRepository);
      }
    }
    finally {
      referenceRepository.completeBulkDispatchingMode();
    }
    return importKey;
  }

  private GlobList convertImportedTransaction(String selectedDateFormat) {
    DateFormat dateFormat = new SimpleDateFormat(selectedDateFormat);
    GlobList importedTransactions = localRepository.getAll(ImportedTransaction.TYPE);
    GlobList createdTransactions = new GlobList();

    for (Glob importedTransaction : importedTransactions) {
      Date bankDate = parseDate(dateFormat, importedTransaction, ImportedTransaction.BANK_DATE);
      Date userDate = parseDate(dateFormat, importedTransaction, ImportedTransaction.DATE);

      Glob transaction = localRepository.create(
        Key.create(Transaction.TYPE, importedTransaction.getValue(ImportedTransaction.ID)),
        value(Transaction.TRANSACTION_TYPE,
              importedTransaction.get(ImportedTransaction.IS_CARD, false) ? TransactionType.getId(TransactionType.CREDIT_CARD) : null),
        value(Transaction.BANK_MONTH, Month.getMonthId(bankDate)),
        value(Transaction.BANK_DAY, Month.getDay(bankDate)),
        value(Transaction.MONTH, userDate == null ? null : Month.getMonthId(userDate)),
        value(Transaction.DAY, userDate == null ? null : Month.getDay(userDate)),
        value(Transaction.LABEL, importedTransaction.get(ImportedTransaction.LABEL)),
        value(Transaction.ACCOUNT, importedTransaction.get(ImportedTransaction.ACCOUNT)),
        value(Transaction.AMOUNT, importedTransaction.get(ImportedTransaction.AMOUNT)),
        value(Transaction.CATEGORY, importedTransaction.get(ImportedTransaction.CATEGORY)),
        value(Transaction.DISPENSABLE, importedTransaction.get(ImportedTransaction.DISPENSABLE)),
        value(Transaction.LABEL_FOR_CATEGORISATION, importedTransaction.get(ImportedTransaction.LABEL_FOR_CATEGORISATION)),
        value(Transaction.NOTE, importedTransaction.get(ImportedTransaction.NOTE)),
        value(Transaction.ORIGINAL_LABEL, importedTransaction.get(ImportedTransaction.ORIGINAL_LABEL)),
        value(Transaction.SPLIT, importedTransaction.get(ImportedTransaction.SPLIT)),
        value(Transaction.SPLIT_SOURCE, importedTransaction.get(ImportedTransaction.SPLIT_SOURCE))
      );
      Integer seriesId = getSeriesId(importedTransaction);
      if (seriesId != null) {
        localRepository.update(transaction.getKey(), Transaction.SERIES, seriesId);
      }
      createdTransactions.add(transaction);

    }
    return createdTransactions;
  }

  private Integer getSeriesId(Glob importedTransaction) {
    Integer categoryId = importedTransaction.get(ImportedTransaction.CATEGORY);
    return categoryId != 0 ? Series.OCCASIONAL_SERIES_ID : null;
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

  public Glob createDefaultAccount() {
    return localRepository.create(Account.TYPE,
                                  value(Account.NAME, Lang.get("account.default.current.name")));
  }

  public Key createImport(TypedInputStream file, GlobList createdTransactions, GlobRepository targetRepository) {
    Glob transactionImport =
      targetRepository.create(TransactionImport.TYPE,
                              value(TransactionImport.IMPORT_DATE, new Date()),
                              value(TransactionImport.SOURCE, file.getName()));

    Key importKey = transactionImport.getKey();

    int lastMonth = 0;
    int lastDay = 0;
    for (Glob createdTransaction : createdTransactions) {
      targetRepository.setTarget(createdTransaction.getKey(), Transaction.IMPORT, importKey);

      Integer transactionMonth = createdTransaction.get(Transaction.BANK_MONTH);
      Integer transactionDay = createdTransaction.get(Transaction.BANK_DAY);
      if (lastMonth < transactionMonth || (lastMonth == transactionMonth && lastDay < transactionDay)) {
        lastMonth = transactionMonth;
        lastDay = transactionDay;
      }
    }
    targetRepository.update(importKey, TransactionImport.LAST_TRANSACTION_DATE, Month.toDate(lastMonth, lastDay));
    return importKey;
  }
}
