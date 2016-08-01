package com.budgetview.gui.importer.utils;

import com.budgetview.model.ImportedTransaction;
import com.budgetview.model.Transaction;
import com.budgetview.model.Account;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobFunctor;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.collections.MultiMap;
import org.globsframework.utils.Strings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccountFinder implements GlobFunctor {
  private MultiMap<String, Integer> accountsByLabel = new MultiMap<String, Integer>();

  public static Integer findBestAccount(GlobList importedTransactions, GlobRepository repository) {
    AccountFinder accountFinder = new AccountFinder();
    repository.safeApply(Transaction.TYPE,
                         GlobMatchers.isFalse(Transaction.PLANNED), accountFinder);
    return accountFinder.findAccount(importedTransactions);
  }

  public void run(Glob glob, GlobRepository repository) throws Exception {
    String label = getLabel(glob);
    if (!Strings.isNullOrEmpty(label)){
      accountsByLabel.put(label, glob.get(Transaction.ACCOUNT));
    }
  }

  private String getLabel(Glob glob) {
    String label1 = createLabel(glob, Transaction.QIF_M, Transaction.QIF_P);
    if (Strings.isNullOrEmpty(label1)){
      label1 = createLabel(glob, Transaction.OFX_NAME, Transaction.OFX_MEMO);
    }
    return Transaction.anonymise(label1);
  }

  private String getImportedLabel(Glob glob) {
    String label1 = createLabel(glob, ImportedTransaction.QIF_M, ImportedTransaction.QIF_P);
    if (Strings.isNullOrEmpty(label1)){
      label1 = createLabel(glob, ImportedTransaction.OFX_NAME, ImportedTransaction.OFX_MEMO);
    }
    return Transaction.anonymise(label1);
  }

  private String createLabel(Glob glob, final StringField qif_m, final StringField qif_p) {
    String label = "";
    if (Strings.isNotEmpty(glob.get(qif_m))) {
      label += glob.get(qif_m).toUpperCase();
    }
    if (Strings.isNotEmpty(glob.get(qif_p))) {
      label += ":" + glob.get(qif_p).toUpperCase();
    }
    return label;
  }

  public Integer findAccount(GlobList importedTransactions) {
    Map<Integer, Integer> foundAccountsCount = new HashMap<Integer, Integer>();
    for (Glob transaction : importedTransactions) {
      String label = getImportedLabel(transaction);
      List<Integer> accounts = accountsByLabel.get(label);
      for (Integer accountId : accounts) {
        if (!Account.SUMMARY_ACCOUNT_IDS.contains(accountId)){
          Integer count = foundAccountsCount.get(accountId);
          foundAccountsCount.put(accountId, count != null ? count + 1 : 1);
        }
      }
    }
    if (foundAccountsCount.isEmpty()) {
      return null;
    }
    if (foundAccountsCount.size() == 1) {
      return foundAccountsCount.keySet().iterator().next();
    }
    Integer maxCount = 0;
    Integer secondCount = 0;
    Integer accountId = null;
    for (Map.Entry<Integer, Integer> entry : foundAccountsCount.entrySet()) {
      if (entry.getValue() > maxCount) {
        secondCount = maxCount;
        maxCount = entry.getValue();
        accountId = entry.getKey();
      }
      else if (entry.getValue() > secondCount){
        secondCount = entry.getValue();
      }
    }
    if (maxCount > secondCount * 1.5) { // il faut qu'il y est suffisement d'operation pour choisir
      return accountId;
    }
    return null;
  }

}
