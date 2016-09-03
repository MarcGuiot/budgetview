package com.budgetview.desktop.description.stringifiers;

import com.budgetview.model.Bank;
import com.budgetview.utils.Lang;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobStringifier;

import java.util.Comparator;

public class BankStringifier implements GlobStringifier {
  public String toString(Glob bank, GlobRepository repository) {
    if (bank == null) {
      return "";
    }
    if (bank.get(Bank.ID) == Bank.GENERIC_BANK_ID){
      return Lang.get("bank.other.name");
    }
    return bank.get(Bank.NAME);
  }

  public Comparator<Glob> getComparator(GlobRepository repository) {
    return new BankComparator();
  }
}