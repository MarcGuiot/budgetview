package org.designup.picsou.importer.ofx;

import static org.crossbowlabs.globs.model.FieldValue.value;
import org.crossbowlabs.globs.model.*;
import org.crossbowlabs.globs.model.utils.GlobIdGenerator;
import org.crossbowlabs.globs.utils.MultiMap;
import org.crossbowlabs.globs.utils.Strings;
import org.crossbowlabs.globs.utils.exceptions.InvalidFormat;
import org.crossbowlabs.globs.utils.exceptions.TruncatedFile;
import org.crossbowlabs.globs.metamodel.fields.IntegerField;
import org.designup.picsou.importer.AccountFileImporter;
import org.designup.picsou.model.*;
import static org.designup.picsou.model.Transaction.*;
import org.designup.picsou.utils.PicsouUtils;

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
    private ReadOnlyGlobRepository initialRepository;
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


    public Functor(GlobRepository targetRepository, ReadOnlyGlobRepository initialRepository) {
      this.repository = targetRepository;
      this.initialRepository = initialRepository;
      generator = targetRepository.getIdGenerator();
    }

    public void processHeader(String key, String value) {
      if (ofxTagFound) {
        throw new InvalidFormat("Found header properties after <OFX> tag");
      }
    }

    public void enterTag(String tag) {
      if (tag.equals("OFX")) {
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
          repository.create(Transaction.TYPE,
                            value(Transaction.ACCOUNT, currentAccount.get(Account.ID)),
                            value(Transaction.ID, generator.getNextId(Transaction.ID, 1)));
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
          repository.update(transaction.getKey(), Transaction.TRANSACTION_TYPE, TransactionType.CREDIT_CARD.getId());
        }
      }
      if (tag.equals("LEDGERBAL")) {
        updateAccountBalance();
        isInLedgerBal = false;
      }
      if (tag.equalsIgnoreCase("STMTTRN")) {
        processSplitTransactions();
        if (currentCategory != null) {
          categoriesForTransaction.put(currentCategory, null);
        }
        processCategories();
        checkTransaction();
      }
      if (tag.equals("OFX")) {
        fileCompleted = true;
      }

      currentTransactionKey = null;
    }

    private void checkTransaction() {
      Glob glob = repository.get(currentTransactionKey);
      if (glob.get(Transaction.AMOUNT) == null) {
        repository.update(currentTransactionKey, Transaction.AMOUNT, (double)0);
      }
    }

    private void processSplitTransactions() {
      List<Key> keysOfSplitTransactionToUpdate = splitRefToUpdate.get(fitid);
      for (Key splitTransactionKey : keysOfSplitTransactionToUpdate) {
        repository.update(currentTransactionKey, SPLIT, Boolean.TRUE);
        repository.update(splitTransactionKey, SPLIT_SOURCE, currentTransactionKey.get(Transaction.ID));
      }
      splitRefToUpdate.remove(fitid);
    }

    private void processCategories() {
      Set<Integer> categoryIds = new HashSet<Integer>();
      for (Map.Entry<String, List<String>> entry : categoriesForTransaction.values()) {
        String masterName = entry.getKey();
        if (Strings.isNullOrEmpty(masterName)) {
          continue;
        }
        try {
          MasterCategory master = MasterCategory.valueOf(masterName.toUpperCase());
          if (master == MasterCategory.NONE) {
            continue;
          }

          List<String> subcategoryNames = entry.getValue();
          for (String subcategoryName : subcategoryNames) {
            if (Strings.isNullOrEmpty(subcategoryName)) {
              categoryIds.add(master.getId());
            }
            else {
              Glob subCategory = Category.find(subcategoryName, initialRepository);
              if (subCategory == null) {
                subCategory = repository.create(Category.TYPE,
                                                value(Category.MASTER, master.getId()),
                                                value(Category.NAME, subcategoryName),
                                                value(Category.SYSTEM, false));
              }
              categoryIds.add(subCategory.get(Category.ID));
            }
          }

        }
        catch (IllegalArgumentException e) {
          Integer categoryId = Category.findId(masterName, initialRepository);
          if (categoryId != null) {
            categoryIds.add(categoryId);
          }
        }
      }
      if (categoryIds.size() == 1) {
        repository.update(currentTransactionKey, Transaction.CATEGORY, categoryIds.iterator().next());
      }
      else {
        TransactionToCategory.link(repository, currentTransactionKey.get(Transaction.ID),
                                   categoryIds.toArray(new Integer[categoryIds.size()]));
      }
    }

    public void processTag(String tag, String content) {
      if (tag.equalsIgnoreCase("BANKID")) {
        bankEntityId = Integer.valueOf(content);
        repository.findOrCreate(Key.create(BankEntity.TYPE, bankEntityId));
      }
      if (tag.equalsIgnoreCase("DTPOSTED")) {
        updateDate(content, BANK_MONTH, BANK_DAY);
        return;
      }
      if (tag.equalsIgnoreCase("DTUSER")) {
        updateDate(content, MONTH, DAY);
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
      if (tag.equalsIgnoreCase("DISPENSABLE")) {
        if ("true".equalsIgnoreCase(content)) {
          repository.update(currentTransactionKey, DISPENSABLE, true);
        }
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
        repository.update(key, SPLIT, Boolean.TRUE);
        repository.update(currentTransactionKey, SPLIT_SOURCE, key.get(Transaction.ID));
      }
    }

    public void end() {
      if (!ofxTagFound) {
        throw new InvalidFormat("Tag <OFX> not found");
      }
    }

    private void updateName(String content) {
      repository.update(currentTransactionKey, ORIGINAL_LABEL, content);
      repository.update(currentTransactionKey, LABEL, content);
    }

    private void updateDate(String content, IntegerField monthField, IntegerField dayField) {
      Date parsedDate = parseDate(content);
      repository.update(currentTransactionKey, monthField, Month.get(parsedDate));
      repository.update(currentTransactionKey, dayField, Month.getDay(parsedDate));
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
      repository.update(currentTransactionKey, AMOUNT, Double.parseDouble(content));
    }

    private void updateAccount(String accountNumber) {
      currentAccount = repository.findUnique(Account.TYPE, value(Account.NUMBER, accountNumber));
      if (currentAccount == null) {
        currentAccount = repository.create(Account.TYPE,
                                           value(Account.NUMBER, accountNumber),
                                           value(Account.ID, repository.getNextId(Account.ID, 1)),
                                           value(Account.NAME, getName(accountNumber, isCreditCard)),
                                           value(Account.BANK_ENTITY, bankEntityId),
                                           value(Account.IS_CARD_ACCOUNT, isCreditCard));
      }
    }

    private void updateAccountBalance() {
      if (!isInLedgerBal || (currentAccount == null)) {
        return;
      }
      Date previousDate = currentAccount.get(Account.UPDATE_DATE);
      if ((updateDate != null) && ((previousDate == null) || updateDate.after(previousDate))) {
        repository.update(currentAccount.getKey(), Account.UPDATE_DATE, updateDate);
        repository.update(currentAccount.getKey(), Account.BALANCE, balance);
      }

      updateDate = null;
      balance = null;
    }

    private void updateNote(String content) {
      if (!Strings.isNullOrEmpty(content)) {
        repository.update(currentTransactionKey, NOTE, content);
      }
    }

    private String getName(String number, boolean isCard) {
      if (isCard) {
        return "Carte " + PicsouUtils.splitCardNumber(number);
      }
      else {
        return "Compte " + number;
      }
    }
  }
}
