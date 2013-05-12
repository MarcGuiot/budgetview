package org.designup.picsou.importer;

import org.apache.commons.collections.iterators.ReverseListIterator;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.designup.picsou.bank.BankPluginService;
import org.designup.picsou.gui.accounts.utils.MonthDay;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.gui.importer.utils.NoOperations;
import org.designup.picsou.gui.model.CurrentAccountInfo;
import org.designup.picsou.gui.time.TimeService;
import org.designup.picsou.importer.analyzer.TransactionAnalyzer;
import org.designup.picsou.importer.analyzer.TransactionAnalyzerFactory;
import org.designup.picsou.importer.utils.DateFormatAnalyzer;
import org.designup.picsou.importer.utils.TypedInputStream;
import org.designup.picsou.model.*;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.model.*;
import org.globsframework.model.delta.DefaultChangeSet;
import org.globsframework.model.delta.MutableChangeSet;
import org.globsframework.model.repository.LocalGlobRepository;
import org.globsframework.model.repository.LocalGlobRepositoryBuilder;
import org.globsframework.model.utils.ChangeSetAggregator;
import org.globsframework.model.utils.DefaultChangeSetVisitor;
import org.globsframework.model.utils.GlobFunctor;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.*;
import org.globsframework.utils.collections.MultiMap;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidData;
import org.globsframework.utils.exceptions.InvalidFormat;
import org.globsframework.utils.exceptions.OperationCancelled;
import org.globsframework.utils.exceptions.TruncatedFile;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.globsframework.model.FieldValue.value;

public class ImportSession {
  private GlobRepository referenceRepository;
  private Directory directory;
  private ImportService importService;
  private MutableChangeSet importChangeSet;
  private GlobRepository localRepository;
  private ChangeSetAggregator importChangeSetAggregator;
  private boolean load = false;
  private int lastImportedTransactionsCount = 0;
  private int totalImportedTransactionsCount = 0;
  private GlobList accountIds = new GlobList();
  private int accountCount;
  private ChangeSet changes;
  private Glob realAccount;
  private Boolean importSeries;
  private Key importKey;

  public ImportSession(GlobRepository referenceRepository, Directory directory) {
    this.referenceRepository = referenceRepository;
    this.directory = directory;
    this.importService = directory.get(ImportService.class);
    this.localRepository =
      GlobRepositoryBuilder.init(referenceRepository.getIdGenerator())
        .add(referenceRepository.getAll(AccountUpdateMode.TYPE))
        .get();
  }

  public Set<Key> getNewSeries() {
    if (importSeries != null) {
      return Collections.emptySet();
    }
    return changes.getCreated(ImportedSeries.TYPE);
  }

  public void importSeries(boolean importSeries) {
    this.importSeries = importSeries;
  }

  public GlobRepository getTempRepository() {
    return localRepository;
  }

  public List<String> loadFile(final Glob synchronizedAccount, Integer synchroId, PicsouDialog dialog, final TypedInputStream typedInputStream)
    throws IOException, TruncatedFile, NoOperations, InvalidFormat, OperationCancelled {

    this.importSeries = null;
    this.realAccount = synchronizedAccount;
    importChangeSet = new DefaultChangeSet();
    importChangeSetAggregator = new ChangeSetAggregator(localRepository, importChangeSet);
    load = true;
    localRepository.reset(GlobList.EMPTY, Transaction.TYPE, ImportedTransaction.TYPE, MonthDay.TYPE, CurrentMonth.TYPE,
                          DeferredCardDate.TYPE, AccountCardType.TYPE, AccountType.TYPE, BudgetArea.TYPE, CsvMapping.TYPE);
    GlobType[] types = {Bank.TYPE, BankEntity.TYPE, Account.TYPE, MonthDay.TYPE, DeferredCardDate.TYPE,
                        AccountCardType.TYPE, CurrentMonth.TYPE, Month.TYPE, CurrentAccountInfo.TYPE,
                        RealAccount.TYPE, Series.TYPE, SubSeries.TYPE, TransactionImport.TYPE, CsvMapping.TYPE};
    localRepository.reset(referenceRepository.getAll(types), types);

    LocalGlobRepository importRepository;
    importRepository = LocalGlobRepositoryBuilder
      .init(referenceRepository)
      .copy(types).get();

    importRepository.startChangeSet();
    final Set<Integer> tmpAccountIds = new HashSet<Integer>();
    importService.run(typedInputStream, referenceRepository, importRepository, directory, dialog);
    importRepository.completeChangeSet();
    changes = importRepository.getCurrentChanges();
    changes.safeVisit(RealAccount.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        tmpAccountIds.add(key.get(RealAccount.ID));
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
      }
    });
    changes.safeVisit(ImportedTransaction.TYPE, new DefaultChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        tmpAccountIds.add(values.get(ImportedTransaction.ACCOUNT));
      }
    });
    accountIds =
      importRepository.getAll(RealAccount.TYPE, GlobMatchers.contained(RealAccount.ID, tmpAccountIds))
        .sort(RealAccount.NAME).sort(RealAccount.NUMBER);

    // on met en premier un compte qui a des operations sinon, le dateFormat sera demandé pour un compte
    // potentiellement vide

    List<Glob> newList = new ArrayList<Glob>();
    for (Iterator it = accountIds.iterator(); it.hasNext(); ) {
      Glob acc = (Glob)it.next();
      if (!importRepository.contains(ImportedTransaction.TYPE,
                                     GlobMatchers.fieldEquals(ImportedTransaction.ACCOUNT, acc.get(RealAccount.ID)))) {
        newList.add(acc);
        it.remove();
      }
    }
    accountIds.addAll(newList);

    accountCount = accountIds.size();
    if (synchronizedAccount != null) {
      if (accountIds.size() == 1 && typedInputStream.getType() != BankFileType.OFX) {
        if (accountIds.size() == 1) {
          Glob account = accountIds.remove(0);

          //cas de l'ofx avec un seul compte (donc comme le qif sauf que la position est peut-etre
          // bien renseignée.
          if (Strings.isNotEmpty(account.get(RealAccount.POSITION))) {
            if (Strings.isNullOrEmpty(synchronizedAccount.get(RealAccount.POSITION))) {
              localRepository.update(synchronizedAccount.getKey(),
                                     RealAccount.POSITION, account.get(RealAccount.POSITION));
            }
          }
          importRepository.delete(account.getKey());
        }
        accountIds.add(synchronizedAccount);
        importRepository.getAll(ImportedTransaction.TYPE)
          .safeApply(new GlobFunctor() {
            public void run(Glob glob, GlobRepository repository) throws Exception {
              repository.update(glob.getKey(), ImportedTransaction.ACCOUNT, synchronizedAccount.get(RealAccount.ID));
            }
          }, importRepository);
      }
    }
    for (Glob realAccount : accountIds) {
      importRepository.update(realAccount.getKey(), RealAccount.SYNCHO, synchroId);
    }
    List<String> dateFormat = getImportedTransactionFormat(importRepository);

    importKey = createCurrentImport(typedInputStream, referenceRepository);

    return dateFormat;
  }

  private Glob readNext() throws NoOperations {
    if (accountIds.isEmpty()) {
      load = false;
      throw new NoOperations();
    }

    importChangeSet = new DefaultChangeSet();
    importChangeSetAggregator = new ChangeSetAggregator(localRepository, importChangeSet);

    Glob info = localRepository.findOrCreate(Key.create(CurrentAccountInfo.TYPE, 0));
    localRepository.update(info.getKey(), CurrentAccountInfo.BANK, null);

    localRepository.startChangeSet();
    Glob currentImportedAccount;
    try {
      currentImportedAccount = accountIds.remove(0);
      changes.safeVisit(new ForwardChanges(currentImportedAccount.get(RealAccount.ID)));
    }
    finally {
      localRepository.completeChangeSet();
    }

    GlobList importedTransactions = localRepository.getAll(ImportedTransaction.TYPE);

    if (realAccount == null) {
      currentImportedAccount = findExistingRealAccount(currentImportedAccount);
      Integer realAccountId = currentImportedAccount.get(RealAccount.ID);
      for (Glob operation : importedTransactions) {
        localRepository.update(operation.getKey(), ImportedTransaction.ACCOUNT, realAccountId);
      }
    }
    else {
      if (Strings.isNullOrEmpty(realAccount.get(RealAccount.POSITION)) &&
        Strings.isNotEmpty(currentImportedAccount.get(RealAccount.POSITION))) {
        localRepository.update(realAccount.getKey(), RealAccount.POSITION,
                               currentImportedAccount.get(RealAccount.POSITION));
      }
    }

    lastImportedTransactionsCount = importedTransactions.size();
    return currentImportedAccount;
  }

  private Glob findExistingRealAccount(Glob account) {
    GlobList matchingAccounts = new GlobList();
    GlobList globList = localRepository.getAll(RealAccount.TYPE);
    for (Glob glob : globList) {
      if (RealAccount.areStrictlyEquivalent(account, glob)) {
        matchingAccounts.add(glob);
      }
    }
    if (matchingAccounts.isEmpty()) {
      for (Glob glob : globList) {
        if (RealAccount.areEquivalent(account, glob)) {
          matchingAccounts.add(glob);
        }
      }
      if (matchingAccounts.isEmpty()) {
        return localRepository.get(account.getKey());
      }
    }
    if (matchingAccounts.size() == 1) {
      Glob first = matchingAccounts.getFirst();
      localRepository.update(account.getKey(), RealAccount.ACCOUNT, first.get(RealAccount.ACCOUNT));
//      RealAccount.copy(localRepository, matchingAccount.getFirst(), account);
//      localRepository.delete(account.getKey());
    }
    return localRepository.get(account.getKey());
  }

  private List<String> getImportedTransactionFormat(final GlobRepository repository) {
    Set<String> valueSet = repository.getAll(ImportedTransaction.TYPE)
      .getValueSet(ImportedTransaction.BANK_DATE);
    DateFormatAnalyzer dateFormatAnalyzer = new DateFormatAnalyzer(TimeService.getToday());
    return dateFormatAnalyzer.parse(valueSet);
  }

  public Key importTransactions(Glob importedAccount, Glob currentlySelectedAccount, String selectedDateFormat) {
    localRepository.delete(Key.create(CurrentAccountInfo.TYPE, 0));
    if (!load) {
      return null;
    }
    if (accountIds.isEmpty()) {
      load = false;
    }

    BankPluginService bankPluginService = directory.get(BankPluginService.class);
    GlobList transactions = localRepository.getAll(ImportedTransaction.TYPE);
    bankPluginService.apply(currentlySelectedAccount, importedAccount, transactions, referenceRepository,
                            localRepository, importChangeSet);

    GlobList allNewTransactions = convertImportedTransaction(selectedDateFormat, currentlySelectedAccount.get(Account.ID));

    boolean value = shouldImportSeries();
    if (value) {
      referenceRepository.update(importKey, TransactionImport.IS_WITH_SERIES, value);
    }
//    importKey = createCurrentImport(typedStream, localRepository);
    setCurrentImport(allNewTransactions, localRepository);
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
        removeImportedSeries();
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
    totalImportedTransactionsCount += lastImportedTransactionsCount;
    return importKey;
  }

  private void removeImportedSeries() {
    localRepository.delete(ImportedSeries.TYPE, GlobMatchers.ALL);
  }

  public int getTotalImportedTransactionsCount() {
    return totalImportedTransactionsCount;
  }

  private GlobList convertImportedTransaction(String selectedDateFormat, Integer accountId) {
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

    Map<Integer, Integer> linkImportedTransactionToTransaction = new HashMap<Integer, Integer>();
    for (; iterator.hasNext(); ) {
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
        value(Transaction.ACCOUNT, accountId),
        value(Transaction.AMOUNT, importedTransaction.get(ImportedTransaction.AMOUNT)),
        value(Transaction.NOTE, importedTransaction.get(ImportedTransaction.NOTE)),
        value(Transaction.BANK_TRANSACTION_TYPE, TransactionAnalyzerFactory.removeBlankAndToUpercase(importedTransaction.get(ImportedTransaction.BANK_TRANSACTION_TYPE))),
        value(Transaction.SPLIT, importedTransaction.get(ImportedTransaction.SPLIT)),
        value(Transaction.SPLIT_SOURCE,
              linkImportedTransactionToTransaction.get(importedTransaction.get(ImportedTransaction.SPLIT_SOURCE))),
        value(Transaction.OFX_CHECK_NUM, TransactionAnalyzerFactory.removeBlankAndToUpercase(importedTransaction.get(ImportedTransaction.OFX_CHECK_NUM))),
        value(Transaction.OFX_MEMO, TransactionAnalyzerFactory.removeBlankAndToUpercase(importedTransaction.get(ImportedTransaction.OFX_MEMO))),
        value(Transaction.OFX_NAME, TransactionAnalyzerFactory.removeBlankAndToUpercase(importedTransaction.get(ImportedTransaction.OFX_NAME))),
        value(Transaction.QIF_M, TransactionAnalyzerFactory.removeBlankAndToUpercase(importedTransaction.get(ImportedTransaction.QIF_M))),
        value(Transaction.QIF_P, TransactionAnalyzerFactory.removeBlankAndToUpercase(importedTransaction.get(ImportedTransaction.QIF_P))),
        value(Transaction.SERIES, getSeriesId(importedTransaction)),
        value(Transaction.SUB_SERIES, getSubSeriesId(importedTransaction)),
        value(Transaction.IS_OFX, importedTransaction.get(ImportedTransaction.IS_OFX))
      );
      linkImportedTransactionToTransaction.put(importedTransaction.get(ImportedTransaction.ID),
                                               transaction.get(Transaction.ID));
      createdTransactions.add(transaction);
      nextId++;
    }
    return createdTransactions;
  }

  private Integer getSubSeriesId(Glob importedTransaction) {
    if (shouldImportThisSeries(importedTransaction)) {
      Glob series = localRepository.findLinkTarget(importedTransaction, ImportedTransaction.SERIES);
      Integer subSeries = series.get(ImportedSeries.SUB_SERIES);
      return subSeries != null ? subSeries : ((Integer)Transaction.SUB_SERIES.getDefaultValue());
    }
    else {
      return ((Integer)Transaction.SUB_SERIES.getDefaultValue());
    }
  }

  private Integer getSeriesId(Glob importedTransaction) {
    if (shouldImportThisSeries(importedTransaction)) {
      Glob series = localRepository.findLinkTarget(importedTransaction, ImportedTransaction.SERIES);
      Integer seriesId = series.get(ImportedSeries.SERIES);
      return seriesId != null ? seriesId : ((Integer)Transaction.SERIES.getDefaultValue());
    }
    else {
      return ((Integer)Transaction.SERIES.getDefaultValue());
    }
  }

  private boolean shouldImportThisSeries(Glob importedTransaction) {
    Glob series = localRepository.findLinkTarget(importedTransaction, ImportedTransaction.SERIES);
    return shouldImportSeries() && series != null && series.get(ImportedSeries.BUDGET_AREA) != null;
  }

  private boolean shouldImportSeries() {
    return importSeries != null && importSeries;
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
    localRepository.deleteAll(ImportedTransaction.TYPE);
    importChangeSet.safeVisit(new RevertChanges());
    importChangeSetAggregator.dispose();
  }

  private Key createCurrentImport(TypedInputStream file, GlobRepository targetRepository) {
    file.getRepetableStream();
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    ZipOutputStream stream = new ZipOutputStream(byteArrayOutputStream);
    byte[] bytes = null;
    try {
      stream.putNextEntry(new ZipEntry("tmp"));
      Files.copyStream(file.getRepetableStream(), stream);
      bytes = byteArrayOutputStream.toByteArray();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    Glob transactionImport =
      targetRepository.create(TransactionImport.TYPE,
                              value(TransactionImport.IMPORT_DATE, TimeService.getToday()),
                              value(TransactionImport.SOURCE, file.getName()),
                              value(TransactionImport.IS_WITH_SERIES, shouldImportSeries()),
                              value(TransactionImport.FILE_CONTENT, bytes));

    return transactionImport.getKey();
  }

  private void setCurrentImport(GlobList createdTransactions, GlobRepository targetRepository) {
    for (Glob createdTransaction : createdTransactions) {
      targetRepository.setTarget(createdTransaction.getKey(), Transaction.IMPORT, importKey);
    }
    GlobList list = targetRepository.getAll(TransactionImport.TYPE, GlobMatchers.isNotNull(TransactionImport.FILE_CONTENT))
      .sort(TransactionImport.ID);
    int count = 5;
    while (!list.isEmpty() && count != 0) {
      list.remove(list.size() - 1);
      count--;
    }
    for (Glob glob : list) {
      targetRepository.update(glob.getKey(), TransactionImport.FILE_CONTENT, null);
    }
  }

  public Glob gotoNextContent(Ref<Integer> accountNum, Ref<Integer> accountCount) {
    try {
      Glob glob = readNext();
      accountCount.set(this.accountCount);
      accountNum.set(this.accountCount - this.accountIds.size());
      return glob;
    }
    catch (NoOperations operations) {
      accountNum.set(0);
      accountCount.set(0);
      return null;
    }
  }

  private class ForwardChanges implements ChangeSetVisitor {
    private final Integer currentAccoutId;

    public ForwardChanges(Integer currentAccoutId) {
      this.currentAccoutId = currentAccoutId;
    }

    public void visitCreation(Key key, FieldValues values) throws Exception {
      if (key.getGlobType() == ImportedTransaction.TYPE && Utils.equal(currentAccoutId, values.get(ImportedTransaction.ACCOUNT))) {
        localRepository.create(key, values.toArray());
      }
      else if (key.getGlobType() == RealAccount.TYPE && Utils.equal(currentAccoutId, key.get(RealAccount.ID))) {
        localRepository.create(key, values.toArray());
      }
      else if (key.getGlobType() == ImportedSeries.TYPE) {
        localRepository.create(key, values.toArray());
      }
    }

    public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
    }

    public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
    }
  }

  private class RevertChanges implements ChangeSetVisitor {
    public void visitCreation(Key key, FieldValues values) throws Exception {
      localRepository.delete(key);
    }

    public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
      localRepository.update(key, values.getPreviousValues().toArray());
    }

    public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
      localRepository.create(key, previousValues.toArray());
    }
  }
}
