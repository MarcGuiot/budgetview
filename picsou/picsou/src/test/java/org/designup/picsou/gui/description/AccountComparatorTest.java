package org.designup.picsou.gui.description;

import junit.framework.TestCase;
import static org.designup.picsou.model.Account.*;
import org.designup.picsou.model.AccountCardType;
import org.designup.picsou.gui.description.AccountComparator;
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
    Glob account3 = createAccount("b", "bb", null, 2);
    Glob account4 = createAccount("c", "cc", null, 4);
    Glob account6 = createAccount("bb", "bb", true, 1);
    Glob account2 = createAccount("a", "aa", null, 7);
    Glob account1 = createAccount(SUMMARY_ACCOUNT_NUMBER, "blah", false, MAIN_SUMMARY_ACCOUNT_ID);
    Glob account5 = createAccount("aa", "aa", true, 6);
    Glob account7 = createAccount("cc", "cc", false, 5);

    Collections.sort(accounts, new AccountComparator());

    TestUtils.assertEquals(accounts,
                           account1, account2, account3, account4, account5, account6, account7);
  }

  private Glob createAccount(String id, String name, Boolean isDeferredCard, int serverId) {
    Glob account = globRepository.create(Key.create(TYPE, serverId),
                                         FieldValue.value(NUMBER, id),
                                         FieldValue.value(NAME, name),
                                         FieldValue.value(CARD_TYPE, isDeferredCard == null ?
                                                                     AccountCardType.NOT_A_CARD.getId() :
                                                                     (isDeferredCard ? AccountCardType.DEFERRED.getId() :
                                                                      AccountCardType.CREDIT.getId())));
    accounts.add(account);
    return account;
  }
}
