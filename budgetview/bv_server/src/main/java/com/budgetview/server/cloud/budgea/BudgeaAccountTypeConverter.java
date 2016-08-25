package com.budgetview.server.cloud.budgea;

import com.budgetview.shared.model.AccountType;

public class BudgeaAccountTypeConverter {

  public static String convertName(String budgeaAccountType) {
    AccountType type = convert(budgeaAccountType);
    return type != null ? type.getName().toLowerCase() : null;
  }

  public static AccountType convert(String budgeaAccountType) {

    if (budgeaAccountType == null) {
      return null;
    }

    if  (budgeaAccountType.equals("checking")) {
      return AccountType.MAIN;
    }

    if  (budgeaAccountType.equals("savings") ||
         budgeaAccountType.equals("lifeinsurance") ||
         budgeaAccountType.equals("market")) {
      return AccountType.SAVINGS;
    }

    return null;

  }
}
