package com.budgetview.gui.accounts.utils;

import com.budgetview.model.AccountCardType;
import junit.framework.TestCase;
import com.budgetview.gui.time.TimeService;
import org.globsframework.model.*;
import org.globsframework.model.repository.DefaultGlobRepository;
import org.globsframework.utils.Dates;
import org.globsframework.utils.TestUtils;

import java.util.Collections;

import static com.budgetview.model.Account.*;

public class AutomaticAccountComparatorTest extends TestCase {
  private GlobRepository repository;
  private GlobList accounts = new GlobList();

  protected void setUp() throws Exception {
    repository = new DefaultGlobRepository();
  }

  public void test() throws Exception {
    TimeService.setCurrentDate(Dates.parse("2010/01/01"));
    Glob account3 = createAccount("b", "bb", null, 2);
    Glob account4 = createAccount("c", "cc", null, 4);
    Glob account6 = createAccount("bb", "bb", true, 1);
    Glob account2 = createAccount("a", "aa", null, 7);
    Glob account1 = createAccount(SUMMARY_ACCOUNT_NUMBER, "blah", false, MAIN_SUMMARY_ACCOUNT_ID);
    Glob account5 = createAccount("aa", "aa", true, 6);
    Glob account7 = createAccount("cc", "cc", false, 5);
    Glob account0 = createClosedAccount("aa", -10, "2009/12/12");

    AutomaticAccountComparator comparator = new AutomaticAccountComparator();
    Collections.sort(accounts, comparator);

    assertEquals(-1, comparator.compare(account1, account0));
    assertEquals(1, comparator.compare(account0, account1));
    assertEquals(1, comparator.compare(account0, account2));
    assertEquals(-1, comparator.compare(account2, account0));

    TestUtils.assertEquals(accounts,
                           account1, account2, account3, account4, account5, account6, account7, account0);
  }

  private Glob createAccount(String id, String name, Boolean isDeferredCard, int serverId) {
    Glob account = repository.create(Key.create(TYPE, serverId),
                                     FieldValue.value(NUMBER, id),
                                     FieldValue.value(NAME, name),
                                     FieldValue.value(CARD_TYPE, isDeferredCard == null ?
                                                                 AccountCardType.NOT_A_CARD.getId() :
                                                                 (isDeferredCard ? AccountCardType.DEFERRED.getId() :
                                                                  AccountCardType.CREDIT.getId())));
    accounts.add(account);
    return account;
  }

  private Glob createClosedAccount(String id, int serverId, String closedyyyymmdd) {
    Glob account = repository.create(Key.create(TYPE, serverId),
                                     FieldValue.value(NUMBER, id),
                                     FieldValue.value(NAME, id),
                                     FieldValue.value(CARD_TYPE, AccountCardType.NOT_A_CARD.getId()),
                                     FieldValue.value(CLOSED_DATE, Dates.parse(closedyyyymmdd)));
    accounts.add(account);
    return account;
  }
}
