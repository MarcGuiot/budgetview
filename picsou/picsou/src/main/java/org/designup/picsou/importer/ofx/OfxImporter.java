package org.designup.picsou.importer.ofx;

import org.designup.picsou.importer.AccountFileImporter;
import org.designup.picsou.importer.utils.ImportedTransactionIdGenerator;
import org.designup.picsou.model.*;
import static org.designup.picsou.model.Category.*;
import org.designup.picsou.utils.Lang;
import org.designup.picsou.utils.PicsouUtils;
import org.globsframework.model.*;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.utils.GlobIdGenerator;
import org.globsframework.utils.MultiMap;
import org.globsframework.utils.Strings;
import org.globsframework.utils.exceptions.InvalidFormat;
import org.globsframework.utils.exceptions.TruncatedFile;

import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class OfxImporter implements AccountFileImporter {
  public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");

  public OfxImporter() {
  }

  public GlobList loadTransactions(Reader reader,
                                   ReadOnlyGlobRepository initialRepository,
                                   GlobRepository targetRepository) throws TruncatedFile {
    OfxParser parser = new OfxParser();
    try {
      Functor functor = new Functor(targetRepository, initialRepository);
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
    private Integer bankEntityId;
    private Double balance;
    private Date updateDate;
    private String fitid;
    private boolean ofxTagFound = false;
    private String currentCategory;
    private MultiMap<String, String> categoriesForTransaction = new MultiMap<String, String>();
    private GlobIdGenerator generator;
    private boolean fileCompleted;
    private String name;
    private String memo;
    private String transactionType;
    private String checkNum;

    public Functor(GlobRepository targetRepository, ReadOnlyGlobRepository initialRepository) {
      this.repository = targetRepository;
      generator = new ImportedTransactionIdGenerator(targetRepository.getIdGenerator());
    }

    public void processHeader(String key, String value) {
      if (ofxTagFound) {
        throw new InvalidFormat("Found header properties after <OFX> tag");
      }
    }

    public void enterTag(String tag) {
      if (tag.equals("OFX") || tag.equals("OFC")) {
        if (ofxTagFound) {
          throw new InvalidFormat("Found <OFX> tag twice");
        }
        ofxTagFound = true;
      }
      if (!ofxTagFound) {
        throw new InvalidFormat("Found tag <" + tag + "> before <OFX>");
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
        Glob transaction =
          repository.create(ImportedTransaction.TYPE,
                            value(ImportedTransaction.ACCOUNT, currentAccount.get(Account.ID)),
                            value(ImportedTransaction.ID, generator.getNextId(ImportedTransaction.ID, 1)),
                            value(ImportedTransaction.IS_OFX, true)
          );
        createdTransactions.add(transaction);
        transactionsForAccount.add(transaction);
        currentTransactionKey = transaction.getKey();
        currentCategory = null;
        categoriesForTransaction.clear();
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
        if (currentCategory != null) {
          categoriesForTransaction.put(currentCategory, null);
        }
        processCategories();
        checkTransaction();
      }
      if (tag.equals("OFX") || tag.equals("OFC")) {
        fileCompleted = true;
      }

      currentTransactionKey = null;
    }

    private void checkTransaction() {
      Glob glob = repository.get(currentTransactionKey);
      if (glob.get(ImportedTransaction.AMOUNT) == null) {
        repository.update(currentTransactionKey, ImportedTransaction.AMOUNT, (double)0);
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

    private void processCategories() {
      Set<Integer> categoryIds = new HashSet<Integer>();
      for (Map.Entry<String, List<String>> entry : categoriesForTransaction.entries()) {
        String masterName = entry.getKey();
        if (Strings.isNullOrEmpty(masterName)) {
          continue;
        }
        Integer masterId = null;
        try {
          masterId = Integer.parseInt(masterName);
        }
        catch (NumberFormatException e) {
          Glob category = repository.findUnique(TYPE, FieldValue.value(Category.NAME, masterName));
          if (category == null) {
            category = repository.create(TYPE, FieldValue.value(Category.NAME, masterName));
          }
          masterId = category.get(Category.ID);
        }
        if (masterId.equals(MasterCategory.NONE.getId())) {
          continue;
        }
        List<String> subcategoryNames = entry.getValue();
        for (String subcategoryName : subcategoryNames) {
          if (Strings.isNullOrEmpty(subcategoryName)) {
            categoryIds.add(masterId);
          }
          else {
            Integer subCategoryId;
            try {
              subCategoryId = Integer.parseInt(subcategoryName);
            }
            catch (NumberFormatException e) {
              Glob subCategory = find(subcategoryName, repository);
              if (subCategory == null) {
                subCategory = repository.create(TYPE,
                                                value(MASTER, masterId),
                                                value(NAME, subcategoryName),
                                                value(SYSTEM, false));
              }
              subCategoryId = subCategory.get(Category.ID);
            }
            categoryIds.add(subCategoryId);
          }
        }
      }
      if (categoryIds.size() == 1) {
        repository.update(currentTransactionKey, ImportedTransaction.CATEGORY, categoryIds.iterator().next());
      }
    }

    public void processTag(String tag, String content) {
      if (tag.equalsIgnoreCase("BANKID")) {
        bankEntityId = Integer.valueOf(content);
        repository.findOrCreate(Key.create(BankEntity.TYPE, bankEntityId));
      }
      if (tag.equalsIgnoreCase("DTPOSTED")) {
        repository.update(currentTransactionKey, ImportedTransaction.BANK_DATE, content);
        return;
      }
      if (tag.equalsIgnoreCase("DTUSER")) {
        repository.update(currentTransactionKey, ImportedTransaction.DATE, content);
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
      if (tag.equalsIgnoreCase("CATEGORY")) {
        if (currentCategory != null) {
          categoriesForTransaction.put(currentCategory, null);
        }
        currentCategory = content;
        return;
      }
      if (tag.equalsIgnoreCase("SUBCATEGORY")) {
        if (currentCategory != null) {
          categoriesForTransaction.put(currentCategory, content);
        }
        currentCategory = null;
        return;
      }
      if (tag.equalsIgnoreCase("BALAMT")) {
        balance = Double.parseDouble(content);
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
        throw new InvalidFormat("Tag <OFX> not found");
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
      repository.update(currentTransactionKey, ImportedTransaction.OFX_NAME, name);
      repository.update(currentTransactionKey, ImportedTransaction.OFX_MEMO, memo);
      repository.update(currentTransactionKey, ImportedTransaction.OFX_CHECK_NUM, checkNum);
      repository.update(currentTransactionKey, ImportedTransaction.BANK_TRANSACTION_TYPE, transactionType);
      name = null;
      memo = null;
      transactionType = null;
    }

    private Date parseDate(String content) {
      Date value;
      try {
        value = DATE_FORMAT.parse(content.substring(0, 8));
      }
      catch (ParseException e) {
        throw new RuntimeException(e);
      }
      return value;
    }

    private void updateAmount(String content) {
      repository.update(currentTransactionKey, ImportedTransaction.AMOUNT, Double.parseDouble(content));
    }

    private void updateAccount(String accountNumber) {
      currentAccount = repository.findUnique(Account.TYPE, value(Account.NUMBER, accountNumber));
      if (currentAccount == null) {
        currentAccount = repository.create(Account.TYPE,
                                           value(Account.NUMBER, accountNumber),
                                           value(Account.ID, generator.getNextId(Account.ID, 1)),
                                           value(Account.NAME, getName(accountNumber, isCreditCard)),
                                           value(Account.BANK_ENTITY, bankEntityId),
                                           value(Account.IS_CARD_ACCOUNT, isCreditCard));
      }
    }

    private void updateAccountBalance() {
      if (!isInLedgerBal || (currentAccount == null)) {
        return;
      }
      Date previousDate = currentAccount.get(Account.BALANCE_DATE);
      if ((updateDate != null) && ((previousDate == null) || updateDate.equals(previousDate) || updateDate.after(previousDate))) {
        repository.update(currentAccount.getKey(), Account.BALANCE_DATE, updateDate);
        repository.update(currentAccount.getKey(), Account.BALANCE, balance);
        repository.update(currentAccount.getKey(), Account.TRANSACTION_ID, null);
      }

      updateDate = null;
      balance = null;
    }

    private void updateNote(String content) {
      if (!Strings.isNullOrEmpty(content)) {
        repository.update(currentTransactionKey, ImportedTransaction.NOTE, content);
      }
    }

    private String getName(String number, boolean isCard) {
      if (isCard) {
        return Lang.get("account.defaultName.card", PicsouUtils.splitCardNumber(number));
      }
      else {
        return Lang.get("account.defaultName.standard", number);
      }
    }

  }

}
