package org.designup.picsou.functests.accounts;

import org.designup.picsou.functests.checkers.AccountEditionChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.TransactionType;

public class AccountEditionTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentDate("2008/10/15");
    super.setUp();
  }

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

    mainAccounts.checkAccounts("My account");
  }

  public void testCreatingAMainAccount() throws Exception {
    views.selectHome();

    accounts.createNewAccount()
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

    mainAccounts.checkAccounts("Main CIC account");
  }

  public void testCreatingASavingsAccount() throws Exception {
    views.selectHome();
    accounts.createNewAccount()
      .checkTitle("Create account")
      .setName("Savings")
      .setAccountNumber("123")
      .setAsSavings()
      .selectBank("CIC")
      .validate();

    savingsAccounts.edit("Savings")
      .checkIsSavings()
      .cancel();
  }

  public void testCreatingAnAccountThroughTheMainMenu() throws Exception {
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

    savingsAccounts.checkAccounts("Livret");
  }

  public void testABankMustBeSelectedWhenCreatingAnAccount() throws Exception {
    views.selectHome();

    accounts.createNewAccount()
      .setName("Main")
      .checkNoBankSelected()
      .selectAdvancedTab()
      .checkBankValidationError("You must select a bank for this account")
      .checkStandardTabSelected()
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
      .selectAdvancedTab()
      .checkNameValidationError("You must enter a name for this account")
      .checkStandardTabSelected()
      .setName("a")
      .checkNoErrorDisplayed()
      .cancel();

    mainAccounts.checkAccounts("Account n. 0000123");
  }

  public void testAccountNamesMustBe25CharsOrLess() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "0000123", 100.00, "2008/10/15")
      .addTransaction("2008/10/01", 15.00, "MacDo")
      .load();

    views.selectHome();

    mainAccounts.edit("Account n. 0000123")
      .checkAccountName("Account n. 0000123")
      .setName("123456789_123456789_123456")
      .selectAdvancedTab()
      .checkNameValidationError("The name must not exceed 25 characters")
      .checkStandardTabSelected()
      .setName("123456789_123456789_12345")
      .checkNoErrorDisplayed()
      .validate();

    mainAccounts.checkAccounts("123456789_123456789_12345");
  }

  public void testChangingAMainAccountIntoASavingsAccount() throws Exception {
    views.selectHome();

    accounts.createNewAccount()
      .setName("Main CIC account")
      .selectBank("CIC")
      .checkIsMain()
      .validate();

    savingsAccounts.checkNoAccountsDisplayed();

    mainAccounts.edit("Main CIC account")
      .setAsSavings()
      .validate();

    savingsAccounts.checkAccounts("Main CIC account");
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

  public void testCreateAccountWithStartDate() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "0000100", 900.00, "2008/10/15")
      .addTransaction("2008/10/01", 1000.00, "Salaire/oct")
      .addTransaction("2008/09/01", 1000.00, "Salaire/oct")
      .load();
    operations.openPreferences().setFutureMonthsCount(12).validate();

    views.selectHome();
    accounts.createNewAccount()
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
    mainAccounts.checkAccounts("Account n. 0000100");
    mainAccounts.checkAccount("Account n. 0000100", 900, "2008/10/01");
    mainAccounts.checkEndOfMonthPosition("Account n. 0000100", -100);

    timeline.selectMonth("2008/10");
    mainAccounts.checkAccounts("Main", "Account n. 0000100");
    mainAccounts.checkAccount("Account n. 0000100", 900, "2008/10/01");
    mainAccounts.checkAccount("Main", 1000, "2008/10/01");
    mainAccounts.checkEndOfMonthPosition("Account n. 0000100", 900);
  }

  public void testCreateAccountWithEndDate() throws Exception {
    operations.openPreferences().setFutureMonthsCount(12).validate();
    views.selectHome();
    accounts.createNewAccount()
      .setName("Main")
      .selectBank("ING Direct")
      .setPosition(1000)
      .validate();

    accounts.createNewAccount()
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
      .checkAvailableTargetAccounts("Main", "Closed main", "Main accounts")
      .setTargetAccount("Main")
      .validate();

    timeline.selectMonth("2008/10");
    mainAccounts.edit("Closed main")
      .checkEndDate("2008/12/03")
      .validate();
    mainAccounts.checkAccounts("Main", "Closed main");
    mainAccounts.checkEndOfMonthPosition("Main", 950);
    mainAccounts.checkReferencePosition(2000, "2008/10/01");
    mainAccounts
      .checkContent("| Main        | 1000.00 on 2008/10/01 | sunny |\n" +
                    "| Closed main | 1000.00 on 2008/10/01 | sunny |");

    timeline.selectMonth("2009/01");
    mainAccounts.checkAccounts("Main");
    mainAccounts.checkEndOfMonthPosition("Main", 800);
    accounts.checkContent("| Main | 1000.00 on 2008/10/01 | sunny |");
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
    mainAccounts.checkAccounts(OfxBuilder.DEFAULT_ACCOUNT_NAME);
    mainAccounts.checkEndOfMonthPosition(OfxBuilder.DEFAULT_ACCOUNT_NAME, 400);

    timeline.selectMonth("2008/05");
    mainAccounts.checkAccounts(OfxBuilder.DEFAULT_ACCOUNT_NAME);
    mainAccounts.checkEndOfMonthPosition(OfxBuilder.DEFAULT_ACCOUNT_NAME, 300);

    timeline.selectMonth("2008/06");
    mainAccounts.checkAccounts(OfxBuilder.DEFAULT_ACCOUNT_NAME, "Account n. 0000100");
    mainAccounts.checkEndOfMonthPosition(OfxBuilder.DEFAULT_ACCOUNT_NAME, 200);

    accounts.checkContent(
      "| Account n. 00001123 | 0.00 on 2008/08/15 | sunny |\n" +
      "| Account n. 0000100  | 0.00 on 2008/07/01 | -     |");
    mainAccounts.getChart("Account n. 00001123")
      .checkValue(200806, 1, 300.00)
      .checkValue(200806, 15, 200.00)
      .checkValue(200807, 15, 100.00);
    mainAccounts.select("Account n. 0000100");
    mainAccounts.getChart("Account n. 0000100")
      .checkValue(200806, 1, -900.00)
      .checkValue(200807, 1, 0.00);

    timeline.selectMonth("2008/07");
    mainAccounts.checkAccounts(OfxBuilder.DEFAULT_ACCOUNT_NAME, "Account n. 0000100");
    mainAccounts.select(OfxBuilder.DEFAULT_ACCOUNT_NAME);
    mainAccounts.checkEndOfMonthPosition(OfxBuilder.DEFAULT_ACCOUNT_NAME, 100.00);
    accounts.checkContent(
      "| Account n. 00001123 | 0.00 on 2008/08/15 | sunny |\n" +
      "| Account n. 0000100  | 0.00 on 2008/07/01 | -     |");
    mainAccounts.getChart("Account n. 00001123")
      .checkValue(200807, 1, 200.00)
      .checkValue(200807, 15, 100.00)
      .checkValue(200808, 15, 0.00);
    mainAccounts.select("Account n. 0000100");
    mainAccounts.getChart("Account n. 0000100")
      .checkValue(200807, 1, 0.00);

    timeline.selectMonth("2008/08");
    mainAccounts.checkAccounts(OfxBuilder.DEFAULT_ACCOUNT_NAME);
    accounts.checkContent(
      "| Account n. 00001123 | 0.00 on 2008/08/15 | sunny |");
    mainAccounts.getChart("Account n. 00001123")
      .checkValue(200808, 1, 100.00)
      .checkValue(200808, 15, 0.00);
    mainAccounts.checkEndOfMonthPosition("Account n. 00001123", 0);
  }

  public void testCreateCard() throws Exception {
    operations.openPreferences().setFutureMonthsCount(4).validate();
    views.selectHome();
    AccountEditionChecker newAccount = accounts.createNewAccount();
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
    AccountEditionChecker newAccount = accounts.createNewAccount();
    newAccount
      .setName("Carte a DD")
      .selectBank("ING Direct")
      .setAsDeferredCard()
      .setPosition(1000)
      .validate();
    timeline.selectMonth("2008/10");
  }

  public void testAccountViewDisplaysLinksToWebsites() throws Exception {
    accounts.createNewAccount()
      .setName("Account 1")
      .selectBank("BNP Paribas")
      .validate();

    accounts.createNewAccount()
      .setName("No site account")
      .selectBank("Other")
      .validate();

    mainAccounts.checkAccountWebsite("Account 1", "BNP Paribas website", "http://www.bnpparibas.net");
    mainAccounts.checkAccountWebsiteLinkDisabled("No site account");

    mainAccounts.edit("Account 1")
      .selectBank("Other")
      .validate();
    mainAccounts.checkAccountWebsiteLinkDisabled("Account 1");

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
    transactions
      .initAmountContent()
      .add("01/06/2008", "SALAIRE/OCT", 1000.00, "To categorize", 0.00, 0.00, "Account n. 00001123")
      .check();

    OfxBuilder.init(this)
      .addTransaction("2008/05/01", 1000.00, "Salaire/oct")
      .addTransaction("2008/08/01", 1000.00, "Salaire/oct")
      .load();

    timeline.selectAll();
    views.selectData();
    transactions
      .initAmountContent()
      .add("01/08/2008", "SALAIRE/OCT", 1000.00, "To categorize", 1000.00, 1000.00, "Account n. 00001123")
      .add("01/06/2008", "SALAIRE/OCT", 1000.00, "To categorize", 0.00, 0.00, "Account n. 00001123")
      .add("01/05/2008", "SALAIRE/OCT", 1000.00, "To categorize", -1000.00, -1000.00, "Account n. 00001123")
      .check();
  }

  public void testImportWithFirstTransactionRealyBeforeAndAfterOpenCloseAccount() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/01", 1000.00, "Salaire/oct")
      .load();

    mainAccounts.edit(OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .setStartDate("2008/05/01")
      .setEndDate("2008/07/01")
      .validate();
    transactions
      .initAmountContent()
      .add("01/06/2008", "SALAIRE/OCT", 1000.00, "To categorize", 0.00, 0.00, "Account n. 00001123")
      .check();

    OfxBuilder.init(this)
      .addTransaction("2008/04/01", 1000.00, "Salaire/oct")
      .addTransaction("2008/08/01", 1000.00, "Salaire/oct")
      .load();

    timeline.selectAll();
    views.selectData();
    transactions
      .initAmountContent()
      .add("01/08/2008", "SALAIRE/OCT", 1000.00, "To categorize", 1000.00, 1000.00, "Account n. 00001123")
      .add("01/06/2008", "SALAIRE/OCT", 1000.00, "To categorize", 0.00, 0.00, "Account n. 00001123")
      .add("01/04/2008", "SALAIRE/OCT", 1000.00, "To categorize", -1000.00, -1000.00, "Account n. 00001123")
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
      .loadDeferredCard("Card n. 1111", "Account n. 1234");

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
      .add("01/06/2008", TransactionType.VIREMENT, "SALAIRE/OCT", "", 1000.00)
      .add("29/05/2008", TransactionType.CREDIT_CARD, "AUCHAN", "", -50.00)
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
