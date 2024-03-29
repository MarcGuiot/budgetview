package com.budgetview.io.importer;

import com.budgetview.desktop.accounts.utils.MonthDay;
import com.budgetview.desktop.importer.utils.NoOperations;
import com.budgetview.desktop.time.TimeService;
import com.budgetview.io.importer.analyzer.TransactionAnalyzer;
import com.budgetview.io.importer.analyzer.TransactionAnalyzerFactory;
import com.budgetview.io.importer.utils.DateFormatAnalyzer;
import com.budgetview.io.importer.utils.TypedInputStream;
import com.budgetview.model.*;
import com.budgetview.shared.model.AccountType;
import com.budgetview.shared.model.BudgetArea;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.model.*;
import org.globsframework.model.delta.DefaultChangeSet;
import org.globsframework.model.delta.MutableChangeSet;
import org.globsframework.model.repository.LocalGlobRepository;
import org.globsframework.model.repository.LocalGlobRepositoryBuilder;
import org.globsframework.model.utils.*;
import org.globsframework.utils.Files;
import org.globsframework.utils.Ref;
import org.globsframework.utils.Strings;
import org.globsframework.utils.Utils;
import org.globsframework.utils.collections.MultiMap;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidData;
import org.globsframework.utils.exceptions.InvalidFormat;
import org.globsframework.utils.exceptions.OperationCancelled;
import org.globsframework.utils.exceptions.TruncatedFile;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.globsframework.model.FieldValue.value;
import static org.globsframework.model.utils.GlobMatchers.contained;
import static org.globsframework.model.utils.GlobMatchers.fieldEquals;

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
  private GlobList realAccounts = new GlobList();
  private int accountCount;
  private ChangeSet changes;
  private Glob realAccount;
  private Boolean importSeries;
  private boolean replaceSeries = true;
  private Key importKey;
  private List<GlobMatcher> providerTransactionsToDeleteMatchers = new ArrayList<GlobMatcher>();

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

  public void setImportSeries(boolean importSeries) {
    this.importSeries = importSeries;
  }

  public void setReplaceSeries(boolean replaceSeries) {
    this.replaceSeries = replaceSeries;
  }

  public GlobRepository getTempRepository() {
    return localRepository;
  }

  public List<String> loadFile(final Glob currentRealAccount, final TypedInputStream typedInputStream, Window parentWindow)
    throws IOException, TruncatedFile, NoOperations, InvalidFormat, OperationCancelled {

    this.importSeries = null;
    this.realAccount = currentRealAccount;
    importChangeSet = new DefaultChangeSet();
    importChangeSetAggregator = new ChangeSetAggregator(localRepository, importChangeSet);
    load = true;
    localRepository.reset(GlobList.EMPTY, Transaction.TYPE, ImportedTransaction.TYPE, MonthDay.TYPE, CurrentMonth.TYPE,
                          DeferredCardDate.TYPE, AccountCardType.TYPE, AccountType.TYPE, BudgetArea.TYPE, CsvMapping.TYPE);
    GlobType[] types = {Bank.TYPE, BankEntity.TYPE, Account.TYPE, MonthDay.TYPE, DeferredCardDate.TYPE,
      AccountCardType.TYPE, CurrentMonth.TYPE, Month.TYPE,
      RealAccount.TYPE, Series.TYPE, SubSeries.TYPE, TransactionImport.TYPE, CsvMapping.TYPE};
    localRepository.reset(referenceRepository.getAll(types), types);

    LocalGlobRepository importRepository;
    importRepository = LocalGlobRepositoryBuilder
      .init(referenceRepository)
      .copy(types).get();

    importRepository.startChangeSet();
    final Set<Integer> tmpAccountIds = new HashSet<Integer>();
    importService.run(typedInputStream, referenceRepository, importRepository, directory, parentWindow);
    importRepository.completeChangeSet();
    changes = importRepository.getCurrentChanges();
    changes.safeVisit(RealAccount.TYPE, new DefaultChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        tmpAccountIds.add(key.get(RealAccount.ID));
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        tmpAccountIds.add(key.get(RealAccount.ID));
      }
    });
    changes.safeVisit(ImportedTransaction.TYPE, new DefaultChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        tmpAccountIds.add(values.get(ImportedTransaction.ACCOUNT));
      }
    });
    realAccounts =
      importRepository.getAll(RealAccount.TYPE, contained(RealAccount.ID, tmpAccountIds))
        //=>compte carte (debit differé) en premier pour qu'on puisse choisir le compte en target.
        .sort(RealAccount.CARD_TYPE, RealAccount.NAME, RealAccount.NUMBER);

    // on met en premier un compte qui a des operations sinon, le dateFormat sera demandé pour un compte
    // potentiellement vide

    List<Glob> newRealAccountList = new ArrayList<Glob>();
    for (Iterator it = realAccounts.iterator(); it.hasNext(); ) {
      Glob realAccount = (Glob) it.next();
      if (!importRepository.contains(ImportedTransaction.TYPE,
                                     fieldEquals(ImportedTransaction.ACCOUNT, realAccount.get(RealAccount.ID)))) {
        newRealAccountList.add(realAccount);
        it.remove();
      }
    }
    realAccounts.addAll(newRealAccountList);

    accountCount = realAccounts.size();
    if (currentRealAccount != null) {
      if (realAccounts.size() == 1 && typedInputStream.getType() != BankFileType.OFX && typedInputStream.getType() != BankFileType.JSON) {
        Glob account = realAccounts.remove(0);

        // cas de l'ofx avec un seul compte (donc comme le qif sauf que la position est peut-etre bien renseignée.
        if (Strings.isNotEmpty(account.get(RealAccount.POSITION))) {
          if (Strings.isNullOrEmpty(currentRealAccount.get(RealAccount.POSITION))) {
            localRepository.update(currentRealAccount.getKey(),
                                   RealAccount.POSITION, account.get(RealAccount.POSITION));
          }
        }
        importRepository.delete(account);
        realAccounts.add(currentRealAccount);
        importRepository.getAll(ImportedTransaction.TYPE)
          .safeApply(new GlobFunctor() {
            public void run(Glob glob, GlobRepository repository) throws Exception {
              repository.update(glob.getKey(), ImportedTransaction.ACCOUNT, currentRealAccount.get(RealAccount.ID));
            }
          }, importRepository);
      }
    }

    List<String> dateFormat = getImportedTransactionFormat(importRepository);
    importKey = createCurrentImport(typedInputStream, referenceRepository);
    return dateFormat;
  }

  private Glob readNext() throws NoOperations {
    if (realAccounts.isEmpty()) {
      load = false;
      throw new NoOperations();
    }

    importChangeSet = new DefaultChangeSet();
    importChangeSetAggregator = new ChangeSetAggregator(localRepository, importChangeSet);

    localRepository.startChangeSet();
    Glob currentImportedAccount;
    try {
      currentImportedAccount = realAccounts.remove(0);
      changes.safeVisit(new ForwardChanges(currentImportedAccount.get(RealAccount.ID)));
    }
    finally {
      localRepository.completeChangeSet();
    }

    GlobList importedTransactions = localRepository.getAll(ImportedTransaction.TYPE);

    if (realAccount == null) {
      currentImportedAccount = RealAccount.deduplicate(currentImportedAccount, localRepository);
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

  private List<String> getImportedTransactionFormat(final GlobRepository repository) {
    Set<String> valueSet = repository.getAll(ImportedTransaction.TYPE)
      .getValueSet(ImportedTransaction.BANK_DATE);
    DateFormatAnalyzer dateFormatAnalyzer = new DateFormatAnalyzer(TimeService.getToday());
    return dateFormatAnalyzer.parse(valueSet);
  }

  public Key completeImport(Glob currentlySelectedAccount, String selectedDateFormat) {
    try {
      localRepository.startChangeSet();
      if (!load) {
        return null;
      }
      if (realAccounts.isEmpty()) {
        load = false;
      }

      GlobList allNewTransactions = convertImportedTransaction(selectedDateFormat, currentlySelectedAccount.get(Account.ID));
      boolean shouldImportSeries = shouldImportSeries();
      if (shouldImportSeries) {
        referenceRepository.update(importKey, TransactionImport.IS_WITH_SERIES, shouldImportSeries);
      }
      setCurrentImport(allNewTransactions, localRepository);
      localRepository.deleteAll(ImportedTransaction.TYPE);
    }
    finally {
      localRepository.completeChangeSet();
    }
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
          transactionAnalyzer.processTransactions(bankId, accountIdAndTransactions.getValue(), localRepository);
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
      for (GlobMatcher matcher : providerTransactionsToDeleteMatchers) {
        referenceRepository.delete(Transaction.TYPE, matcher);
      }
      providerTransactionsToDeleteMatchers.clear();
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
        Collections.reverse(importedTransactions);
        iterator = importedTransactions.iterator();
      }
    }

    GlobList createdTransactions = new GlobList();

    int nextId = localRepository.getIdGenerator().getNextId(Transaction.ID, importedTransactions.size() + 10);

    Map<Integer, Integer> linkImportedTransactionToTransaction = new HashMap<Integer, Integer>();
    while (iterator.hasNext()) {
      Glob importedTransaction = iterator.next();
      Date bankDate = parseDate(dateFormat, importedTransaction, ImportedTransaction.BANK_DATE);
      Date userDate = parseDate(dateFormat, importedTransaction, ImportedTransaction.DATE);

      if (importedTransaction.isTrue(ImportedTransaction.DELETED)) {
        addProviderTransactionToDelete(importedTransaction.get(ImportedTransaction.PROVIDER),
                                       importedTransaction.get(ImportedTransaction.PROVIDER_CONNECTION_ID),
                                       importedTransaction.get(ImportedTransaction.PROVIDER_ACCOUNT_ID),
                                       importedTransaction.get(ImportedTransaction.PROVIDER_TRANSACTION_ID));
        continue;
      }

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
        value(Transaction.ORIGINAL_ACCOUNT, accountId),
        value(Transaction.AMOUNT, importedTransaction.get(ImportedTransaction.AMOUNT)),
        value(Transaction.NOTE, importedTransaction.get(ImportedTransaction.NOTE)),
        value(Transaction.BANK_TRANSACTION_TYPE, TransactionAnalyzerFactory.removeBlankAndToUppercase(importedTransaction.get(ImportedTransaction.BANK_TRANSACTION_TYPE))),
        value(Transaction.SPLIT, importedTransaction.get(ImportedTransaction.SPLIT)),
        value(Transaction.SPLIT_SOURCE,
              linkImportedTransactionToTransaction.get(importedTransaction.get(ImportedTransaction.SPLIT_SOURCE))),
        value(Transaction.OFX_CHECK_NUM, TransactionAnalyzerFactory.removeBlankAndToUppercase(importedTransaction.get(ImportedTransaction.OFX_CHECK_NUM))),
        value(Transaction.OFX_MEMO, TransactionAnalyzerFactory.removeBlankAndToUppercase(importedTransaction.get(ImportedTransaction.OFX_MEMO))),
        value(Transaction.LABEL, TransactionAnalyzerFactory.removeBlankAndToUppercase(importedTransaction.get(ImportedTransaction.SIMPLE_LABEL))),
        value(Transaction.ORIGINAL_LABEL, TransactionAnalyzerFactory.removeBlankAndToUppercase(importedTransaction.get(ImportedTransaction.SIMPLE_LABEL))),
        value(Transaction.OFX_NAME, TransactionAnalyzerFactory.removeBlankAndToUppercase(importedTransaction.get(ImportedTransaction.OFX_NAME))),
        value(Transaction.QIF_M, TransactionAnalyzerFactory.removeBlankAndToUppercase(importedTransaction.get(ImportedTransaction.QIF_M))),
        value(Transaction.QIF_P, TransactionAnalyzerFactory.removeBlankAndToUppercase(importedTransaction.get(ImportedTransaction.QIF_P))),
        value(Transaction.SERIES, getSeriesId(importedTransaction)),
        value(Transaction.SUB_SERIES, getSubSeriesId(importedTransaction)),
        value(Transaction.IMPORT_TYPE, importedTransaction.get(ImportedTransaction.IMPORT_TYPE)),
        value(Transaction.PROVIDER, importedTransaction.get(ImportedTransaction.PROVIDER)),
        value(Transaction.PROVIDER_CONNECTION_ID, importedTransaction.get(ImportedTransaction.PROVIDER_CONNECTION_ID)),
        value(Transaction.PROVIDER_ACCOUNT_ID, importedTransaction.get(ImportedTransaction.PROVIDER_ACCOUNT_ID)),
        value(Transaction.PROVIDER_TRANSACTION_ID, importedTransaction.get(ImportedTransaction.PROVIDER_TRANSACTION_ID))
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
      return subSeries != null ? subSeries : ((Integer) Transaction.SUB_SERIES.getDefaultValue());
    }
    else {
      return ((Integer) Transaction.SUB_SERIES.getDefaultValue());
    }
  }

  private Integer getSeriesId(Glob importedTransaction) {
    if (shouldImportThisSeries(importedTransaction)) {
      Glob imported = localRepository.findLinkTarget(importedTransaction, ImportedTransaction.SERIES);
      Integer seriesId = imported.get(ImportedSeries.SERIES);
      return seriesId != null ? seriesId : ((Integer) Transaction.SERIES.getDefaultValue());
    }
    else {
      return ((Integer) Transaction.SERIES.getDefaultValue());
    }
  }

  private boolean shouldImportThisSeries(Glob importedTransaction) {
    Glob importedSeries = localRepository.findLinkTarget(importedTransaction, ImportedTransaction.SERIES);
    return shouldImportSeries() && importedSeries != null && importedSeries.get(ImportedSeries.BUDGET_AREA) != null;
  }

  private boolean shouldImportSeries() {
    return Boolean.TRUE.equals(importSeries);
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
                              value(TransactionImport.SOURCE, file.getFileName()),
                              value(TransactionImport.IS_WITH_SERIES, shouldImportSeries()),
                              value(TransactionImport.REPLACE_SERIES, replaceSeries),
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
      accountNum.set(this.accountCount - this.realAccounts.size());
      return glob;
    }
    catch (NoOperations operations) {
      accountNum.set(0);
      accountCount.set(0);
      return null;
    }
  }

  private class ForwardChanges implements ChangeSetVisitor {
    private final Integer currentAccountId;

    public ForwardChanges(Integer currentAccountId) {
      this.currentAccountId = currentAccountId;
    }

    public void visitCreation(Key key, FieldValues values) throws Exception {
      if (key.getGlobType() == ImportedTransaction.TYPE && Utils.equal(currentAccountId, values.get(ImportedTransaction.ACCOUNT))) {
        localRepository.create(key, values.toArray());
      }
      else if (key.getGlobType() == RealAccount.TYPE && Utils.equal(currentAccountId, key.get(RealAccount.ID))) {
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

  private void addProviderTransactionToDelete(final Integer provider, final Integer providerConnectionId, final Integer providerAccountId, final Integer providerTransactionId) {
    providerTransactionsToDeleteMatchers.add(new GlobMatcher() {
      public boolean matches(Glob transaction, GlobRepository repository) {
        return Utils.equal(provider, transaction.get(Transaction.PROVIDER)) &&
               Utils.equal(providerConnectionId, transaction.get(Transaction.PROVIDER_CONNECTION_ID)) &&
               Utils.equal(providerAccountId, transaction.get(Transaction.PROVIDER_ACCOUNT_ID)) &&
               Utils.equal(providerTransactionId, transaction.get(Transaction.PROVIDER_TRANSACTION_ID));
      }
    });
  }
}
