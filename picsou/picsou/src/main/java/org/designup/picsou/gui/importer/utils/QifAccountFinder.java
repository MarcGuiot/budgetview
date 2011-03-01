package org.designup.picsou.gui.importer.utils;

import org.designup.picsou.model.ImportedTransaction;
import org.designup.picsou.model.Transaction;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobFunctor;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.MultiMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QifAccountFinder implements GlobFunctor {
  private MultiMap<String, Integer> accountsByLabel = new MultiMap<String, Integer>();

  public static Integer findQifAccount(GlobList importedTransactions, GlobRepository repository) {
    QifAccountFinder accountFinder = new QifAccountFinder();
    repository.safeApply(Transaction.TYPE, GlobMatchers.ALL, accountFinder);
    return accountFinder.findAccount(importedTransactions);
  }

  public void run(Glob glob, GlobRepository repository) throws Exception {
    String label = Transaction.anonymise(createLabel(glob, Transaction.QIF_M, Transaction.QIF_P));
    accountsByLabel.put(label, glob.get(Transaction.ACCOUNT));
  }

  private String createLabel(Glob glob, final StringField qif_m, final StringField qif_p) {
    String label = "";
    if (glob.get(qif_m) != null) {
      label += glob.get(qif_m).toUpperCase();
    }
    if (glob.get(qif_p) != null) {
      label += ":" + glob.get(qif_p).toUpperCase();
    }
    return label;
  }

  public Integer findAccount(GlobList importedTransactions) {
    Map<Integer, Integer> foundAccountsCount = new HashMap<Integer, Integer>();
    for (Glob transaction : importedTransactions) {
      String label = Transaction.anonymise(createLabel(transaction, ImportedTransaction.QIF_M, ImportedTransaction.QIF_P));
      List<Integer> accounts = accountsByLabel.get(label);
      for (Integer accountId : accounts) {
        Integer count = foundAccountsCount.get(accountId);
        foundAccountsCount.put(accountId, count != null ? count + 1 : 1);
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
