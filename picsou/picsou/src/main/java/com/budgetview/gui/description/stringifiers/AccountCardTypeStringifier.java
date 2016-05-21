package com.budgetview.gui.description.stringifiers;

import com.budgetview.model.AccountCardType;
import org.globsframework.model.format.utils.AbstractGlobStringifier;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;

public class AccountCardTypeStringifier extends AbstractGlobStringifier {
  public String toString(Glob accountCardType, GlobRepository repository) {
    if (accountCardType == null) {
      return "";
    }
    AccountCardType type = AccountCardType.get(accountCardType.get(AccountCardType.ID));
    return type.getLabel();
  }
}
