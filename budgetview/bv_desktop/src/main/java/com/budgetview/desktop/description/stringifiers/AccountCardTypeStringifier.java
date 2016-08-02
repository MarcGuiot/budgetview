package com.budgetview.desktop.description.stringifiers;

import com.budgetview.model.AccountCardType;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.utils.AbstractGlobStringifier;

public class AccountCardTypeStringifier extends AbstractGlobStringifier {
  public String toString(Glob accountCardType, GlobRepository repository) {
    if (accountCardType == null) {
      return "";
    }
    AccountCardType type = AccountCardType.get(accountCardType.get(AccountCardType.ID));
    return type.getLabel();
  }
}
