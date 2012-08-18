package org.designup.picsou.functests.accounts;

import org.designup.picsou.functests.checkers.AccountEditionChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.TransactionType;

public class AccountEditionTest extends LoggedInFunctionalTestCase {
  public void testEditingAnExistingAccount() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "0000123", 100.00, "2008/10/15")
      .addTransaction("2008/10/01", 15.00, "MacDo")
      .load();

    views.selectHome();

    mainAccounts.edit("Account n. 0000123")
      .checkTitle("Edit account")
      .checkAccountName("Account n. 0000123")
      .checkAccountNumber("0000123")
      .checkBalanceDisplayed(false)
      .setName("My account")
      .setAccountNumber("12345")
      .validate();

    mainAccounts.checkAccountNames("My account");
  }

  public void testCreatingAMainAccount() throws Exception {
    views.selectHome();

    mainAccounts.createNewAccount()
      .checkTitle("Create account")
      .checkAccountName("")
      .checkAstericsErrorOnName()
      .setName("Main CIC account")
      .checkAstericsClearOnName()
      .checkTypes("Main", "Credit card", "Deferred debit card", "Savings")
      .checkTypesHelp("Account types")
      .selectBank("CIC")
      .checkIsMain()
      .validate();

    mainAccounts.checkAccountNames("Main CIC account");
  }

  public void testCreatingASavingsAccount() throws Exception {
    views.selectHome();
    savingsAccounts.createNewAccount()
      .checkTitle("Create account")
      .setName("Savings")
      .setAccountNumber("123")
      .checkIsSavings()
      .selectBank("CIC")
      .validate();

    savingsAccounts.edit("Savings")
      .checkIsSavings()
      .cancel();
  }

  public void testCreatingAnAccountThroughTheMainMenu() throws Exception {
    views.selectHome();
    operations.createAccount().cancel();
    views.checkDataSelected();

    views.selectHome();
    operations.createAccount()
      .checkAccountTypeEditable()
      .setName("Livret")
      .setAsSavings()
      .selectBank("CIC")
      .validate();
    views.checkDataSelected();

    savingsAccounts.checkAccountNames("Livret");
  }

  public void testABankMustBeSelectedWhenCreatingAnAccount() throws Exception {
    views.selectHome();

    mainAccounts.createNewAccount()
      .setName("Main")
      .checkNoBankSelected()
      .checkBankValidationError("You must select a bank for this account")
      .selectBank("Other")
      .checkNoErrorDisplayed()
      .validate();

    mainAccounts.edit("Main")
      .checkTitle("Edit account")
      .checkSelectedBank("Other")
      .cancel();
  }

  public void testEmptyAccountNamesAreNotAllowed() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "0000123", 100.00, "2008/10/15")
      .addTransaction("2008/10/01", 15.00, "MacDo")
      .load();

    views.selectHome();

    mainAccounts.edit("Account n. 0000123")
      .checkAccountName("Account n. 0000123")
      .setName("")
      .checkNameValidationError("You must enter a name for this account")
      .setName("a")
      .checkNoErrorDisplayed()
      .cancel();

    mainAccounts.checkAccountNames("Account n. 0000123");
  }

  public void testChangingAMainAccountIntoASavingsAccount() throws Exception {
    views.selectHome();

    mainAccounts.createNewAccount()
      .setName("Main CIC account")
      .selectBank("CIC")
      .checkIsMain()
      .validate();

    savingsAccounts.checkNoAccountsDisplayed();

    mainAccounts.edit("Main CIC account")
      .setAsSavings()
      .validate();

    savingsAccounts.checkAccountNames("Main CIC account");
    mainAccounts.checkNoAccountsDisplayed();
  }

  public void testMainAccountTypeIsTheDefault() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "0000123", 100.00, "2008/10/15")
      .addTransaction("2008/10/01", 15.00, "MacDo")
      .load();

    views.selectHome();

    mainAccounts.edit("Account n. 0000123")
      .checkAccountName("Account n. 0000123")
      .checkIsMain()
      .setAsCreditCard()
      .checkCreditCardWarning()
      .validate();

    mainAccounts.edit("Account n. 0000123")
      .checkIsCreditCard()
      .checkCreditCardWarning()
      .setAsMain()
      .validate();

    mainAccounts.edit("Account n. 0000123")
      .checkIsMain()
      .cancel();

    mainAccounts.edit("Account n. 0000123")
      .setAsDeferredCard()
      .checkDeferredWarning()
      .validate();

    mainAccounts.edit("Account n. 0000123")
      .checkIsDeferredCard()
      .cancel();
  }

  public void testDeletingAnEmptyAccount() throws Exception {
    views.selectHome();
    mainAccounts.createNewAccount()
      .setName("Main")
      .selectBank("CIC")
      .validate();

    mainAccounts.edit("Main").delete()
      .checkMessageContains("No operations are related to this account")
      .validate();

    mainAccounts.checkNotPresent("Main");
  }

  public void testDeletingAnAccountAndRelatedTransactions() throws Exception {

    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "0000123", 100.00, "2008/10/15")
      .addTransaction("2008/10/01", 1000.00, "WorldCo")
      .addTransaction("2008/10/05", -15.00, "MacDo")
      .load();

    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "0000666", 100.00, "2008/10/15")
      .addTransaction("2008/10/10", -15.00, "Quick")
      .load();

    views.selectCategorization();
    categorization.setNewIncome("WorldCo", "Salaire");
    categorization.setNewVariable("MacDo", "Gastronomie");
    categorization.setNewVariable("Quick", "Sante");

    views.selectBudget();
    budgetView.income.checkTotalObserved(1000);
    budgetView.variable.checkTotalObserved(-30);

    views.selectData();
    transactions.initContent()
      .add("10/10/2008", TransactionType.PRELEVEMENT, "Quick", "", -15.00, "Sante")
      .add("05/10/2008", TransactionType.PRELEVEMENT, "MacDo", "", -15.00, "Gastronomie")
      .add("01/10/2008", TransactionType.VIREMENT, "WorldCo", "", 1000.00, "Salaire")
      .check();

    views.selectHome();
    mainAccounts.edit("Account n. 0000123").delete()
      .checkMessageContains("All the operations associated to this account will be deleted")
      .validate();
    mainAccounts.checkNotPresent("Account n. 0000123");

    views.selectData();
    transactions.initContent()
      .add("10/10/2008", TransactionType.PRELEVEMENT, "Quick", "", -15.00, "Sante")
      .check();

    views.selectBudget();
    budgetView.income.checkTotalObserved(0);
    budgetView.variable.checkTotalObserved(-15);
  }

  public void testDeletingASavingsAccountWithSeries() throws Exception {

    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "0000100", 900.00, "2008/10/15")
      .addTransaction("2008/10/01", 1000.00, "Salaire/oct")
      .load();

    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "0000123", 200000.00, "2008/10/15")
      .addTransaction("2008/10/05", 200.00, "Virement octobre")
      .load();

    views.selectCategorization();
    categorization.setNewIncome("Salaire/oct", "Salaire");
    categorization.setNewVariable("Virement octobre", "Savings");

    views.selectBudget();
    budgetView.income.checkTotalObserved(1000);

    views.selectHome();
    mainAccounts.edit("Account n. 0000123")
      .setName("Livret")
      .selectBank("ING Direct")
      .setAsSavings()
      .validate();
    savingsAccounts.createNewAccount()
      .setName("Codevi")
      .selectBank("ING Direct")
      .validate();

    views.selectBudget();
    budgetView.savings.createSeries()
      .setName("Series 1 for Livret")
      .setFromAccount("Main accounts")
      .setToAccount("Livret")
      .validate();
    budgetView.savings.createSeries()
      .setName("Series 2 for Livret")
      .setFromAccount("Livret")
      .setToAccount("Main accounts")
      .validate();
    budgetView.savings.createSeries()
      .setName("Series 3 for Codevi")
      .setFromAccount("Main accounts")
      .setToAccount("Codevi")
      .validate();

    views.selectHome();
    savingsAccounts.edit("Livret")
      .delete()
      .checkMessageContains("All the operations and series associated to this account will be deleted")
      .validate();
    savingsAccounts.checkNotPresent("Livret");

    views.selectBudget();
    budgetView.savings.checkSeriesNotPresent("Series 1 for Livret", "Series 2 for Livret");
    budgetView.savings.checkSeriesPresent("Series 3 for Codevi");

    views.selectData();
    transactions.initContent()
      .add("01/10/2008", TransactionType.VIREMENT, "Salaire/oct", "", 1000.00, "Salaire")
      .check();

    views.selectHome();
    savingsAccounts.edit("Codevi").delete()
      .checkMessageContains("All the series associated to this account will be deleted")
      .validate();
    savingsAccounts.checkNotPresent("Codevi");
  }

  public void testCreateAccountWithStartDate() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "0000100", 900.00, "2008/10/15")
      .addTransaction("2008/10/01", 1000.00, "Salaire/oct")
      .addTransaction("2008/09/01", 1000.00, "Salaire/oct")
      .load();
    operations.openPreferences().setFutureMonthsCount(12).validate();

    views.selectHome();
    mainAccounts.createNewAccount()
      .setName("Main")
      .selectBank("ING Direct")
      .setPosition(1000)
      .setStartDate("2008/10/01")
      .setEndDate("2009/06/03")
      .checkDisplayedEndDate("2009/06/03")
      .cancelEndDate()
      .validate();

    mainAccounts.edit("Account n. 0000100")
      .setStartDate("2008/09/01")
      .checkDisplayedStartDate("2008/09/01")
      .validate();

    timeline.selectMonth("2008/10");
    mainAccounts.edit("Main")
      .checkStartDate("2008/10/01")
      .checkDisplayedStartDate("2008/10/01")
      .cancel();

    timeline.selectMonth("2008/09");
    mainAccounts.checkAccountNames("Account n. 0000100");
    mainAccounts.checkAccount("Account n. 0000100", 900, "2008/10/01");
    mainAccounts.checkEstimatedPosition(-100);

    timeline.selectMonth("2008/10");
    mainAccounts.checkAccountNames("Main", "Account n. 0000100");
    mainAccounts.checkAccount("Account n. 0000100", 900, "2008/10/01");
    mainAccounts.checkAccount("Main", 1000, "2008/08/31");
    mainAccounts.checkEstimatedPosition(1900);
  }

  public void testCreateAccountWithEndDate() throws Exception {
    operations.openPreferences().setFutureMonthsCount(12).validate();
    views.selectHome();
    mainAccounts.createNewAccount()
      .setName("Main")
      .selectBank("ING Direct")
      .setPosition(1000)
      .validate();

    mainAccounts.createNewAccount()
      .setName("Closed main")
      .selectBank("ING Direct")
      .setEndDate("2008/12/03")
      .setPosition(1000)
      .validate();

    // creation d'un series pour avoir des transactions

    views.selectBudget();
    budgetView.recurring.createSeries()
      .setName("edf")
      .selectAllMonths()
      .setAmount(50)
      .validate();

    timeline.selectMonth("2008/10");
    mainAccounts.edit("Closed main")
      .checkEndDate("2008/12/03")
      .validate();
    mainAccounts.checkAccountNames("Main", "Closed main");
    mainAccounts.checkEstimatedPosition(1850);

    timeline.selectMonth("2009/01");
    mainAccounts.checkAccountNames("Main");
    mainAccounts.checkEstimatedPosition(700);
  }

  public void testBeginEndInThePastWithTransactions() throws Exception {

    operations.openPreferences().setFutureMonthsCount(2).validate();

    OfxBuilder
      .init(this)
      .addTransaction("2008/04/30", -100, "Free")
      .addTransaction("2008/05/25", -100, "France Telecom")
      .addTransaction("2008/06/15", -100, "Auchan")
      .addTransaction("2008/07/15", -100, "Auchan")
      .addTransaction("2008/08/15", -100, "Auchan")
      .load();

    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "0000100", 100.0, "2008/07/15")
      .addTransaction("2008/07/01", 1000.00, "Salaire/oct")
      .addTransaction("2008/06/01", 1000.00, "Salaire/oct")
      .load();

    mainAccounts.edit("Account n. 0000100")
      .setStartDate("2008/06/01")
      .checkDisplayedStartDate("2008/06/01")
      .setEndDate("2008/07/01")
      .checkDisplayedEndDate("2008/07/01")
      .validate();

    timeline.selectMonth("2008/04");
    mainAccounts.checkAccountNames(OfxBuilder.DEFAULT_ACCOUNT_NAME);
    mainAccounts.checkEstimatedPosition(400);

    timeline.selectMonth("2008/05");
    mainAccounts.checkAccountNames(OfxBuilder.DEFAULT_ACCOUNT_NAME);
    mainAccounts.checkEstimatedPosition(300);

    timeline.selectMonth("2008/06");
    mainAccounts.checkAccountNames(OfxBuilder.DEFAULT_ACCOUNT_NAME, "Account n. 0000100");
    mainAccounts.checkEstimatedPosition(-700);

    timeline.selectMonth("2008/07");
    mainAccounts.checkAccountNames(OfxBuilder.DEFAULT_ACCOUNT_NAME, "Account n. 0000100");
    mainAccounts.checkEstimatedPosition(100);

    timeline.selectMonth("2008/08");
    mainAccounts.checkAccountNames(OfxBuilder.DEFAULT_ACCOUNT_NAME);
    mainAccounts.checkEstimatedPosition(0);
  }

  public void testCreateCard() throws Exception {
    operations.openPreferences().setFutureMonthsCount(4).validate();
    views.selectHome();
    AccountEditionChecker newAccount = mainAccounts.createNewAccount();
    AccountEditionChecker accountEditionChecker = newAccount
      .setName("Carte a débit Différé")
      .selectBank("ING Direct")
      .setAsDeferredCard()
      .setPosition(1000);

    accountEditionChecker.validate();

    OfxBuilder.init(this)
      .addTransaction("2008/06/01", 1000.00, "prelevement")
      .loadInNewAccount();

    views.selectCategorization();
    categorization.selectTransaction("prelevement")
      .selectOther()
      .selectDeferred()
      .checkActiveSeries("Carte a débit Différé");

    views.selectHome();
    mainAccounts.edit("Carte a débit Différé")
      .setName("other name")
      .validate();
    views.selectCategorization();

    categorization.selectTransaction("prelevement")
      .selectOther()
      .selectDeferred()
      .checkActiveSeries("other name");
  }

  public void testUpdateDeferredCardAmount() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/01", 1000.00, "Salaire/oct")
      .load();

    operations.openPreferences().setFutureMonthsCount(12).validate();
    views.selectHome();
    AccountEditionChecker newAccount = mainAccounts.createNewAccount();
    newAccount
      .setName("Carte a DD")
      .selectBank("ING Direct")
      .setAsDeferredCard()
      .setPosition(1000)
      .validate();
    timeline.selectMonth("2008/10");
  }

  public void testAccountViewDisplaysLinksToWebsites() throws Exception {
    mainAccounts.createNewAccount()
      .setName("Account 1")
      .selectBank("BNP Paribas")
      .validate();

    mainAccounts.createNewAccount()
      .setName("No site account")
      .selectBank("Other")
      .validate();

    mainAccounts.checkAccountWebsite("Account 1", "BNP Paribas website", "http://www.bnpparibas.net");
    mainAccounts.checkAccountWebsiteLinkNotShown("No site account");

    mainAccounts.edit("Account 1")
      .selectBank("Other")
      .validate();
    mainAccounts.checkAccountWebsiteLinkNotShown("Account 1");

    mainAccounts.edit("Account 1")
      .selectBank("CIC")
      .validate();
    mainAccounts.checkAccountWebsite("Account 1", "CIC website", "http://www.cic.fr");
  }

  public void testImportWithFirstTransactionBeforeAndAfterOpenCloseAccount() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/01", 1000.00, "Salaire/oct")
      .load();

    mainAccounts.edit(OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .setStartDate("2008/05/01")
      .setEndDate("2008/07/01")
      .validate();

    OfxBuilder.init(this)
      .addTransaction("2008/05/01", 1000.00, "Salaire/oct")
      .addTransaction("2008/08/01", 1000.00, "Salaire/oct")
      .load();

    timeline.selectAll();
    views.selectData();
    transactions
      .initAmountContent()
      .add("01/08/2008", "SALAIRE/OCT", 1000.00, "To categorize", 0.00, 0.00, "Account n. 00001123")
      .add("01/06/2008", "SALAIRE/OCT", 1000.00, "To categorize", -1000.00, -1000.00, "Account n. 00001123")
      .add("01/05/2008", "SALAIRE/OCT", 1000.00, "To categorize", -2000.00, -2000.00, "Account n. 00001123")
      .check();
  }

  public void testImportCardWithMonthBeforeFirstMonth() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/01", 1000.00, "Salaire/oct")
      .load();

    operations.openPreferences().setFutureMonthsCount(12).validate();
    views.selectHome();

    OfxBuilder.init(this)
      .addCardAccount("1111", 100, "2008/06/30")
      .addTransaction("2008/06/27", -50, "Auchan")
      .addBankAccount("1234", 1000, "2008/06/30")
      .addTransaction("2008/06/28", -550, "Prelevement")
      .loadDeferredCard("Card n. 1111");

    mainAccounts.edit("Card n. 1111")
      .setStartDate("2008/06/01")
      .validate();

    OfxBuilder.init(this)
      .addCardAccount("1111", 100, "2008/06/30")
      .addTransaction("2008/05/29", -50, "Auchan")
      .addBankAccount("1234", 1000, "2008/06/30")
      .addTransaction("2008/06/28", -550, "Prelevement")
      .load();
    views.selectCategorization();
    categorization.setDeferred("Prelevement", "Card n. 1111");

    timeline.selectMonth("2008/06");
    views.selectData();
    transactions.initContent()
      .add("28/06/2008", TransactionType.PRELEVEMENT, "PRELEVEMENT", "", -550.00, "Card n. 1111")
      .add("27/06/2008", TransactionType.CREDIT_CARD, "AUCHAN", "", -50.00)
      .add("01/06/2008", TransactionType.VIREMENT, "SALAIRE/OCT", "", 1000.00)
      .check();
  }

  public void testUpdateOperationsOnBankChange() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount("30002", 123, "1111", 0., "2008/06/01")
      .addTransaction("2008/06/01", 1000.00, "F COM INTERVENTION POUR VIREMENT EXTERNE")
      .load();

    views.selectData();
    transactions.initContent()
      .add("01/06/2008", TransactionType.VIREMENT, "F COM INTERVENTION POUR VIREMENT EXTERNE", "", 1000.00)
      .check();

    views.selectHome();
    mainAccounts.edit("Account n. 1111")
      .selectBank("CIC")
      .validate();
    views.selectData();
    transactions.initContent()
      .add("01/06/2008", TransactionType.BANK_FEES, "COMMISSIONS POUR VIREMENT EXTERNE", "", 1000.00)
      .check();
  }

}