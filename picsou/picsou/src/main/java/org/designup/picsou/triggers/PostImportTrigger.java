package org.designup.picsou.triggers;

import org.designup.picsou.model.Account;
import org.designup.picsou.model.ImportToAccount;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.TransactionComparator;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Dates;

import java.util.*;

public class PostImportTrigger extends AbstractChangeSetListener {
  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if(!changeSet.containsChanges(ImportToAccount.TYPE)){
      return;
    }
    Set<Key> created = changeSet.getCreated(ImportToAccount.TYPE);

    // on regroupe tout les imports par comptes (si on plusieurs fichier il y a plusieurs import
    Map<Integer, Integer> accountToImport = new HashMap<Integer, Integer>();
    for (Key key : created) {
      accountToImport.put(key.get(ImportToAccount.ACCOUNT_ID), key.get(ImportToAccount.IMPORT_ID));
    }
    for (Integer accountId : accountToImport.keySet()) {
//      TransactionComparator comparator = TransactionComparator.ASCENDING_ACCOUNT;
      GlobList futureTransaction = repository.getAll(Transaction.TYPE, GlobMatchers.isNull(Transaction.BANK_MONTH));
      Glob account = repository.get(Key.create(Account.TYPE, accountId));
      Date date = account.get(Account.POSITION_DATE);
      int currentMonthId = Month.getMonthId(date);
      int currentDay = Month.getDay(date);
      for (Glob glob : futureTransaction) {
        if (glob.get(Transaction.POSITION_MONTH) < currentMonthId || 
            (glob.get(Transaction.POSITION_MONTH) == currentMonthId && glob.get(Transaction.POSITION_DAY) < currentDay)) {
          repository.update(glob.getKey(), 
                            FieldValue.value(Transaction.POSITION_MONTH, currentMonthId),
                            FieldValue.value(Transaction.POSITION_DAY, currentDay),
                            FieldValue.value(Transaction.BUDGET_MONTH, currentMonthId),
                            FieldValue.value(Transaction.BANK_DAY, currentDay));
        }
      }
    }

//    SortedSet<Glob> trs = repository.getSorted(Transaction.TYPE, comparator, GlobMatchers.ALL);
    
  }
}
