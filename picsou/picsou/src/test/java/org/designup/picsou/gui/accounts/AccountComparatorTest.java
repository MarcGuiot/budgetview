package org.designup.picsou.gui.accounts;

import junit.framework.TestCase;
import static org.designup.picsou.model.Account.*;
import org.globsframework.model.*;
import org.globsframework.model.impl.DefaultGlobRepository;
import org.globsframework.utils.TestUtils;

import java.util.Collections;

public class AccountComparatorTest extends TestCase {
  private GlobRepository globRepository;
  private GlobList accounts = new GlobList();

  protected void setUp() throws Exception {
    globRepository = new DefaultGlobRepository();
  }

  public void test() throws Exception {
    Glob account3 = createAccount("b", "bb", false, 2);
    Glob account4 = createAccount("c", "cc", null, 4);
    Glob account6 = createAccount("bb", "bb", true, 1);
    Glob account2 = createAccount("a", "aa", null, 7);
    Glob account1 = createAccount(SUMMARY_ACCOUNT_NUMBER, "blah", false, MAIN_SUMMARY_ACCOUNT_ID);
    Glob account5 = createAccount("aa", "aa", true, 6);
    Glob account7 = createAccount("cc", "cc", true, 5);

    Collections.sort(accounts, new AccountComparator());

    TestUtils.assertEquals(accounts,
                           account1, account2, account3, account4, account5, account6, account7);
  }

  private Glob createAccount(String id, String name, Boolean isCreditCard, int serverId) {
    Glob account = globRepository.create(Key.create(TYPE, serverId),
                                         FieldValue.value(NUMBER, id),
                                         FieldValue.value(NAME, name),
                                         FieldValue.value(IS_CARD_ACCOUNT, isCreditCard));
    accounts.add(account);
    return account;
  }
}
