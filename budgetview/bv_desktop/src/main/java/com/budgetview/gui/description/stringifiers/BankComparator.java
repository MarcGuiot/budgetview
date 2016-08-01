package com.budgetview.gui.description.stringifiers;

import com.budgetview.model.Bank;
import org.globsframework.model.Glob;
import org.globsframework.utils.Utils;

import java.util.Comparator;

public class BankComparator implements Comparator<Glob> {

  public int compare(Glob bank1, Glob bank2) {
    if ((bank1 == null)) {
      return -1;
    }
    if (bank2 == null) {
      return 1;
    }
    if (Utils.equal(bank2.get(Bank.ID), Bank.GENERIC_BANK_ID)) {
      return -1;
    }
    if (Utils.equal(bank1.get(Bank.ID), Bank.GENERIC_BANK_ID)) {
      return 1;
    }
    return Utils.compareIgnoreCase(bank1.get(Bank.NAME), bank2.get(Bank.NAME));
  }
}