package org.designup.picsou.gui.description;

import org.designup.picsou.model.Bank;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobStringifier;

import java.util.Comparator;

public class BankStringifier implements GlobStringifier {
  public String toString(Glob bank, GlobRepository repository) {
    if (bank == null) {
      return "";
    }
    return bank.get(Bank.NAME);
  }

  public Comparator<Glob> getComparator(GlobRepository repository) {
    return new BankComparator();
  }
}