package org.designup.picsou.importer.ofx;

import java.util.ArrayList;
import java.util.List;

public class AccountInfoOfxFunctor implements OfxFunctor {
  private List<OfxConnection.AccountInfo> accounts = new ArrayList<OfxConnection.AccountInfo>();
  private OfxConnection.AccountInfo currentAccount;

  public AccountInfoOfxFunctor() {
  }

  public void processHeader(String key, String value) {
  }

  public void enterTag(String tag) {
    if ("ACCTINFO".equals(tag)) {
      currentAccount = new OfxConnection.AccountInfo();
    }
  }

  public void leaveTag(String tag) {
    if ("ACCTINFO".equals(tag)) {
      if (currentAccount != null) {
        accounts.add(currentAccount);
      }
      currentAccount = null;
    }
  }

  public void processTag(String tag, String content) {
    if (currentAccount != null) {
      if (tag.equals("ACCTID")) {
        currentAccount.number = content;
      }
      else if (tag.equals("ACCTTYPE")) {
        currentAccount.accType = content;
      }
      else if (tag.equals("BANKID")) {
        currentAccount.bankId = content;
      }
    }
  }

  public void end() {
  }

  public List<OfxConnection.AccountInfo> getAccounts() {
    return accounts;
  }
}
