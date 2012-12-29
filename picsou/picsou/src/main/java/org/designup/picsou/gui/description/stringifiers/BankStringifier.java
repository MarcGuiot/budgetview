package org.designup.picsou.gui.description.stringifiers;

import org.designup.picsou.bank.importer.OtherBank;
import org.designup.picsou.model.Bank;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobStringifier;

import java.util.Comparator;

public class BankStringifier implements GlobStringifier {
  public String toString(Glob bank, GlobRepository repository) {
    if (bank == null) {
      return "";
    }
    if (bank.get(Bank.ID) == OtherBank.BANK_ID){
      return Lang.get("bank.other.name");
    }
    return bank.get(Bank.NAME);
  }

  public Comparator<Glob> getComparator(GlobRepository repository) {
    return new BankComparator();
  }
}