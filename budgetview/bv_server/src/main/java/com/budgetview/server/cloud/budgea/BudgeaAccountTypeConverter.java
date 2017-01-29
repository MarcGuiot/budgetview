package com.budgetview.server.cloud.budgea;

import com.budgetview.shared.model.AccountType;

public class BudgeaAccountTypeConverter {

  public static String convertName(String budgeaAccountType) {
    AccountType type = convert(budgeaAccountType);
    return type != null ? type.getName().toLowerCase() : null;
  }

  public static AccountType convert(String budgeaAccountType) {

    if (budgeaAccountType == null) {
      return AccountType.MAIN;
    }

    if  (budgeaAccountType.equals("checking") ||
         budgeaAccountType.equals("joint") ||
         budgeaAccountType.equals("card")) {
      return AccountType.MAIN;
    }

    if  (budgeaAccountType.equals("savings") ||
         budgeaAccountType.equals("lifeinsurance") ||
         budgeaAccountType.equals("market") ||
         budgeaAccountType.equals("loan")) {
      return AccountType.SAVINGS;
    }

    return AccountType.MAIN;

  }
}
