package org.designup.picsou.importer.ofx;

import com.budgetview.shared.utils.Amounts;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.gui.importer.utils.InvalidFileFormat;
import org.designup.picsou.gui.time.TimeService;
import org.designup.picsou.importer.AccountFileImporter;
import org.designup.picsou.importer.utils.DateFormatAnalyzer;
import org.designup.picsou.importer.utils.ImportedTransactionIdGenerator;
import org.designup.picsou.model.*;
import org.globsframework.model.*;
import org.globsframework.model.repository.GlobIdGenerator;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.utils.Strings;
import org.globsframework.utils.Utils;
import org.globsframework.utils.collections.MultiMap;
import org.globsframework.utils.exceptions.TruncatedFile;

import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.globsframework.model.FieldValue.value;

public class OfxImporter implements AccountFileImporter {
  public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");

  public OfxImporter() {
  }

  public GlobList loadTransactions(Reader reader,
                                   GlobRepository initialRepository,
                                   GlobRepository targetRepository, PicsouDialog current) throws TruncatedFile {
    OfxParser parser = new OfxParser();
    try {
      Functor functor = new Functor(targetRepository);
      parser.parse(reader, functor);
      if (!functor.fileCompleted) {
        throw new TruncatedFile();
      }
      return functor.createdTransactions;
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static class Functor implements OfxFunctor {
    private GlobRepository repository;
    private GlobList createdTransactions = new GlobList();
    private GlobList transactionsForAccount = new GlobList();
    private Map<String, Key> fIdToTransaction = new HashMap<String, Key>();
    private MultiMap<String, Key> splitRefToUpdate = new MultiMap<String, Key>();
    private Key currentTransactionKey;
    private Glob currentAccount;
    private boolean isCreditCard = false;
    private boolean isInLedgerBal = false;
    private String bankEntityLabel;
    private String position;
    private Date updateDate;
    private String fitid;
    private boolean ofxTagFound = false;
    private GlobIdGenerator generator;
    private boolean fileCompleted;
    private String name;
    private String memo;
    private String transactionType;
    private String checkNum;
    private boolean forceAccount = false;
    private Integer lastTransactionId;

    public Functor(GlobRepository targetRepository) {
      this.repository = targetRepository;
      generator = new ImportedTransactionIdGenerator(targetRepository.getIdGenerator());
    }

    public void processHeader(String key, String value) {
      if (ofxTagFound) {
        throw new InvalidFileFormat("Found header properties after <OFX> tag (" + key + "=" + value + ")");
      }
    }

    public void enterTag(String tag) {
      if (tag.equals("OFX") || tag.equals("OFC")) {
        if (ofxTagFound) {
          throw new InvalidFileFormat("Found <OFX> tag twice");
        }
        ofxTagFound = true;
      }
      if (!ofxTagFound) {
        throw new InvalidFileFormat("Found tag <" + tag + "> before <OFX>");
      }

      if (tag.equals("BANKMSGSRSV1")) {
        transactionsForAccount.clear();
      }
      else if (tag.equals("CREDITCARDMSGSRSV1")) {
        transactionsForAccount.clear();
      }
      else if (tag.equalsIgnoreCase("CCSTMTRS")) {
        isCreditCard = true;
      }
      else if (tag.equalsIgnoreCase("STMTRS")) {
        isCreditCard = false;
      }
      else if (tag.equalsIgnoreCase("STMTTRN")) {
        int accountId = currentAccount.get(RealAccount.ID);
        lastTransactionId = generator.getNextId(ImportedTransaction.ID, 1);
        Glob transaction =
          repository.create(ImportedTransaction.TYPE,
                            value(ImportedTransaction.ACCOUNT, accountId),
                            value(ImportedTransaction.ID, lastTransactionId),
                            value(ImportedTransaction.IS_OFX, true)
          );
        createdTransactions.add(transaction);
        transactionsForAccount.add(transaction);
        currentTransactionKey = transaction.getKey();
      }
      else if (tag.equals("LEDGERBAL")) {
        isInLedgerBal = true;
      }
    }

    public void leaveTag(String tag) {
      if (tag.equals("BANKMSGSRSV1")) {
        // nothing
      }
      if (tag.equals("CREDITCARDMSGSRSV1")) {
        for (Glob transaction : transactionsForAccount) {
          repository.update(transaction.getKey(), ImportedTransaction.IS_CARD, true);
        }
      }
      if (tag.equals("LEDGERBAL")) {
        updateAccountBalance();
        isInLedgerBal = false;
      }
      if (tag.equalsIgnoreCase("STMTTRN")) {
        updateTransactionLabel();
        processSplitTransactions();
        checkTransaction();
      }
      if (tag.equals("OFX") || tag.equals("OFC")) {
        fileCompleted = true;
      }
      if (tag.equals("STMTTRN")){
        currentTransactionKey = null;
      }
    }

    private void checkTransaction() {
      Glob glob = repository.get(currentTransactionKey);
      if (glob.get(ImportedTransaction.AMOUNT) == null) {
        repository.update(currentTransactionKey, ImportedTransaction.AMOUNT, 0.0);
      }
    }

    private void processSplitTransactions() {
      List<Key> keysOfSplitTransactionToUpdate = splitRefToUpdate.get(fitid);
      for (Key splitTransactionKey : keysOfSplitTransactionToUpdate) {
        repository.update(currentTransactionKey, ImportedTransaction.SPLIT, Boolean.TRUE);
        repository.update(splitTransactionKey, ImportedTransaction.SPLIT_SOURCE, currentTransactionKey.get(ImportedTransaction.ID));
      }
      splitRefToUpdate.remove(fitid);
    }

    public void processTag(String tag, String content) {
      if (tag.equalsIgnoreCase("BANKID")) {
        bankEntityLabel = content;
      }
      if (tag.equalsIgnoreCase("DTPOSTED")) {
        repository.update(currentTransactionKey, ImportedTransaction.BANK_DATE, content.subSequence(0, 8));
        return;
      }
      if (tag.equalsIgnoreCase("DTUSER")) {
        repository.update(currentTransactionKey, ImportedTransaction.DATE, content.subSequence(0, 8));
        return;
      }
      if (tag.equalsIgnoreCase("TRNAMT")) {
        updateAmount(content);
        return;
      }
      if (tag.equalsIgnoreCase("NAME")) {
        updateName(content);
        return;
      }
      if (tag.equalsIgnoreCase("CHKNUM") || tag.equalsIgnoreCase("CHECKNUM")) {
        checkNum = content;
        return;
      }
      if (tag.equalsIgnoreCase("MEMO")) {
        updateMemo(content);
        return;
      }
      if (tag.equalsIgnoreCase("TRNTYPE")) {
        updateTransactionType(content);
        return;
      }
      if (tag.equalsIgnoreCase("ACCTID")) {
        updateAccount(content);
        return;
      }
      if (tag.equalsIgnoreCase("NOTE")) {
        updateNote(content);
        return;
      }
      if (tag.equalsIgnoreCase("BALAMT")) {
        position = content; //Amounts.extractAmount(content);
        return;
      }
      if (tag.equalsIgnoreCase("DTASOF")) {
        updateDate = parseDate(content);
        return;
      }
      if (tag.equalsIgnoreCase("FITID")) {
        fitid = content;
        fIdToTransaction.put(content, currentTransactionKey);
        return;
      }
      if (tag.equalsIgnoreCase("PARENT")) {
        updateParent(content);
        return;
      }
    }

    private void updateParent(String content) {
      Key key = fIdToTransaction.get(content);
      if (key == null) {
        splitRefToUpdate.put(content, currentTransactionKey);
      }
      else {
        repository.update(key, ImportedTransaction.SPLIT, Boolean.TRUE);
        repository.update(currentTransactionKey, ImportedTransaction.SPLIT_SOURCE, key.get(ImportedTransaction.ID));
      }
    }

    public void end() {
      if (!ofxTagFound) {
        throw new InvalidFileFormat("Tag <OFX> not found");
      }
    }

    private void updateName(String content) {
      name = content;
    }

    private void updateMemo(String memo) {
      if (!memo.equals(".")) {
        this.memo = memo;
      }
    }

    private void updateTransactionType(String value) {
      this.transactionType = value;
    }

    private void updateTransactionLabel() {
      repository.update(currentTransactionKey,
                        FieldValue.value(ImportedTransaction.OFX_NAME, name),
                        FieldValue.value(ImportedTransaction.OFX_MEMO, memo),
                        FieldValue.value(ImportedTransaction.OFX_CHECK_NUM, checkNum),
                        FieldValue.value(ImportedTransaction.BANK_TRANSACTION_TYPE, transactionType));
      name = null;
      memo = null;
      checkNum = null;
      transactionType = null;
    }

    private Date parseDate(String content) {
      Date value;
      try {
        // on supprime les 6 zeros si il y en a
        String substring = content.length() >= 14 ? content.substring(0, 8) : content;
        DateFormatAnalyzer formatAnalyzer = new DateFormatAnalyzer(TimeService.getToday());
        List<String> parse = formatAnalyzer.parse(Collections.singleton(substring),
                                                  new ArrayList<String>(Arrays.asList("yy/MM/dd", "dd/MM/yy")));
        SimpleDateFormat format = DATE_FORMAT;
        if (parse.size() == 1) {
          format = new SimpleDateFormat(parse.get(0));
        }
        value = format.parse(substring);
      }
      catch (ParseException e) {
        throw new RuntimeException(e);
      }
      return value;
    }

    private void updateAmount(String content) {
      repository.update(currentTransactionKey, ImportedTransaction.AMOUNT, Amounts.extractAmount(content));
    }

    private void updateAccount(final String accountNumber) {
      if (forceAccount) {
        return;
      }
      Integer bankId = null;
      Integer bankEntityId = BankEntity.find(bankEntityLabel, repository);
      if (bankEntityId != null) {
        bankId = BankEntity.getBank(repository.find(Key.create(BankEntity.TYPE, bankEntityId)), repository).get(Bank.ID);
      }
      else {
        for (Glob account : repository.getAll(Account.TYPE)) {
          if (bankEntityLabel != null && bankEntityLabel.equals(account.get(Account.BANK_ENTITY_LABEL))) {
            bankId = account.get(Account.BANK);
            break;
          }
        }
      }

      String accountName = Account.getName(accountNumber, isCreditCard);
      GlobList similarAccounts = repository.getAll(RealAccount.TYPE, new RealAccountMatcher(accountNumber, bankId));
      if (similarAccounts.size() == 1) {
        accountName = similarAccounts.get(0).get(RealAccount.NAME);
      }

      currentAccount = repository.create(RealAccount.TYPE,
                                         value(RealAccount.NUMBER, Strings.toString(accountNumber)),
                                         value(RealAccount.ID, generator.getNextId(RealAccount.ID, 1)),
                                         value(RealAccount.NAME, accountName),
                                         value(RealAccount.BANK, bankId),
                                         value(RealAccount.BANK_ENTITY_LABEL, bankEntityLabel),
                                         value(RealAccount.BANK_ENTITY, bankEntityId),
                                         value(RealAccount.ACCOUNT_TYPE, isCreditCard ? AccountType.MAIN.getId() : null),
                                         value(RealAccount.CARD_TYPE, isCreditCard ? AccountCardType.UNDEFINED.getId()
                                                                                   : AccountCardType.NOT_A_CARD.getId()));
    }

    private void updateAccountBalance() {
      if (!isInLedgerBal || (currentAccount == null)) {
        return;
      }
      if (updateDate != null) {
        repository.update(currentAccount.getKey(),
                          value(RealAccount.POSITION_DATE, updateDate),
                          value(RealAccount.POSITION, position),
                          value(RealAccount.TRANSACTION_ID, lastTransactionId));
      }
      updateDate = null;
      position = null;
    }

    private void updateNote(String content) {
      if (!Strings.isNullOrEmpty(content)) {
        repository.update(currentTransactionKey, ImportedTransaction.NOTE, content);
      }
    }

    private class RealAccountMatcher implements GlobMatcher {
      private final String accountNumber;
      private final Integer bankId;

      public RealAccountMatcher(String accountNumber, Integer bankId) {
        this.accountNumber = accountNumber;
        this.bankId = bankId;
      }

      public boolean matches(Glob account, GlobRepository repository) {
        if (!Utils.equal(bankId, account.get(RealAccount.BANK))) {
          return false;
        }
        if (!Strings.isNotEmpty(accountNumber) || !accountNumber.equalsIgnoreCase(account.get(RealAccount.NUMBER))) {
          return false;
        }
        return true;
      }
    }
  }
}
