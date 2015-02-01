package org.designup.picsou.gui.dashboard;

import org.designup.picsou.gui.accounts.utils.AccountMatchers;
import org.designup.picsou.gui.categorization.utils.Uncategorized;
import org.designup.picsou.gui.model.AccountWeather;
import org.designup.picsou.gui.model.DashboardStat;
import org.designup.picsou.gui.model.WeatherType;
import org.designup.picsou.gui.time.TimeService;
import org.designup.picsou.model.*;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.Directory;

import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import static org.globsframework.model.FieldValue.value;

public class DashboardStatUpdater implements ChangeSetListener, GlobSelectionListener {

  private GlobRepository repository;
  private SelectionService selectionService;

  public static void init(GlobRepository repository, Directory directory) {
    DashboardStatUpdater updater = new DashboardStatUpdater(repository, directory);
    updater.updateAll();
  }

  private DashboardStatUpdater(GlobRepository repository, Directory directory) {
    this.repository = repository;
    repository.addTrigger(this);
    this.selectionService = directory.get(SelectionService.class);
    selectionService.addListener(this, Account.TYPE);
  }

  public void selectionUpdated(GlobSelection selection) {
    updateAll();
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (changeSet.containsChanges(AccountWeather.TYPE) ||
        changeSet.containsChanges(CurrentMonth.TYPE) ||
        changeSet.containsChanges(Account.TYPE)) {
      updateWeather(getSelectedAccounts());
    }
    if (changeSet.containsChanges(Account.TYPE)) {
      updateAmounts(getSelectedAccounts());
    }
    if (changeSet.containsCreations(TransactionImport.TYPE)) {
      updateImport(getSelectedAccounts());
    }
    if (changeSet.containsChanges(Transaction.TYPE)) {
      updateUncategorized(getSelectedAccounts());
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(AccountWeather.TYPE) || changedTypes.contains(CurrentMonth.TYPE) ||
        changedTypes.contains(Account.TYPE) || changedTypes.contains(TransactionImport.TYPE) ||
        changedTypes.contains(Transaction.TYPE)) {
      updateAll();
    }
  }

  private void updateAll() {
    repository.findOrCreate(DashboardStat.KEY);
    Set<Integer> selectedAccountIds = getSelectedAccounts();
    updateWeather(selectedAccountIds);
    updateAmounts(selectedAccountIds);
    updateImport(selectedAccountIds);
    updateUncategorized(selectedAccountIds);
  }

  private Set<Integer> getSelectedAccounts() {
    return selectionService.getSelection(Account.TYPE).getValueSet(Account.ID);
  }

  private void updateImport(Set<Integer> selectedAccountIds) {
    Date lastDate = getLastImportDate(selectedAccountIds);
    Integer dayCount;
    if (lastDate == null) {
      dayCount = null;
    }
    else {
      Date today = TimeService.getToday();
      dayCount = Month.distance(Month.getMonthId(lastDate), Month.getDay(lastDate),
                                Month.getMonthId(today), Month.getDay(today));
    }
    repository.update(DashboardStat.KEY, DashboardStat.DAYS_SINCE_LAST_IMPORT, dayCount);
  }

  private Date getLastImportDate(Set<Integer> selectedAccounts) {
    GlobList imports = repository.getAll(TransactionImport.TYPE).reverseSort(TransactionImport.IMPORT_DATE);
    if (selectedAccounts.isEmpty()) {
      if (!imports.isEmpty()) {
        return imports.getFirst().get(TransactionImport.IMPORT_DATE);
      }
    }
    Date last = null;
    for (Integer accountId : selectedAccounts) {
      Glob account = repository.find(Key.create(Account.TYPE, accountId));
      if (account != null) {
        Glob lastTransaction = repository.findLinkTarget(account, Account.LAST_TRANSACTION);
        if (lastTransaction != null) {
          Glob transactionImport = repository.findLinkTarget(lastTransaction, Transaction.IMPORT);
          if (transactionImport != null) {
            Date date = transactionImport.get(TransactionImport.IMPORT_DATE);
            if (date != null) {
              if (last == null || date.compareTo(last) > 0) {
                last = date;
              }
            }
          }
        }
      }
    }
    return last;
  }

  public void updateWeather(Set<Integer> selectedAccountIds) {
    WeatherType summaryWeather = WeatherType.SUNNY;
    double mainSummaryMin = 0;
    Integer lastMonth = Integer.MAX_VALUE;
    for (Glob accountWeather : repository.getAll(AccountWeather.TYPE, new AccountFieldMatcher(AccountWeather.ACCOUNT, selectedAccountIds))) {
      if (AccountWeather.isForMainAccount(accountWeather, repository)) {
        Double min = accountWeather.get(AccountWeather.FUTURE_MIN);
        if (!Double.isNaN(min)) {
          mainSummaryMin += min;
        }
      }
      WeatherType weather = WeatherType.get(accountWeather.get(AccountWeather.WEATHER));
      summaryWeather = weather.worseThan(summaryWeather) ? weather : summaryWeather;
      if (accountWeather.get(AccountWeather.LAST_FORECAST_MONTH) < lastMonth) {
        lastMonth = accountWeather.get(AccountWeather.LAST_FORECAST_MONTH);
      }
    }
    if (Utils.equal(lastMonth, Integer.MAX_VALUE)) {
      lastMonth = Month.getMonthId(TimeService.getToday());
    }
    repository.update(DashboardStat.KEY,
                      value(DashboardStat.WEATHER, summaryWeather.getId()),
                      value(DashboardStat.LAST_FORECAST_MONTH, lastMonth),
                      value(DashboardStat.REMAINDER, mainSummaryMin));
  }

  private void updateUncategorized(Set<Integer> selectedAccountIds) {
    int count = selectedAccountIds.isEmpty() ? Uncategorized.getCount(repository) : Uncategorized.getCount(selectedAccountIds, repository);
    repository.update(DashboardStat.KEY, value(DashboardStat.UNCATEGORIZED_COUNT, count));
  }

  private void updateAmounts(Set<Integer> selectedAccountIds) {
    if (!repository.contains(Account.MAIN_SUMMARY_KEY) || !repository.contains(Account.SAVINGS_SUMMARY_KEY)) {
      repository.update(DashboardStat.KEY,
                        value(DashboardStat.TOTAL_MAIN_ACCOUNTS, 0.00),
                        value(DashboardStat.TOTAL_MAIN_ACCOUNTS_DATE, TimeService.getToday()),
                        value(DashboardStat.SINGLE_MAIN_ACCOUNT, false),
                        value(DashboardStat.TOTAL_ALL_ACCOUNTS, 0.00),
                        value(DashboardStat.TOTAL_ALL_ACCOUNTS_DATE, TimeService.getToday()),
                        value(DashboardStat.SHOW_ALL_ACCOUNTS, false));
      return;
    }

    Glob mainSummary = repository.find(Account.MAIN_SUMMARY_KEY);
    double mainAmount = mainSummary.get(Account.POSITION_WITH_PENDING, 0.00);
    Date mainAmountDate = mainSummary.get(Account.POSITION_DATE, TimeService.getToday());
    Glob savingsSummary = repository.find(Account.SAVINGS_SUMMARY_KEY);
    double savingsAmount = savingsSummary.get(Account.POSITION_WITH_PENDING, 0.00);
    Date savingsAmountDate = savingsSummary.get(Account.POSITION_DATE, TimeService.getToday());
    TreeSet<Integer> months = new TreeSet<Integer>();
    months.add(Month.getMonthId(TimeService.getToday()));
    GlobList mainAccounts = repository.getAll(Account.TYPE, AccountMatchers.activeUserCreatedAccounts(months));
    GlobList savingsAccounts = repository.getAll(Account.TYPE, AccountMatchers.activeUserCreatedSavingsAccounts(months));
    double totalMainAccounts = 0;
    Date totalMainAccountsDate = TimeService.getToday();
    boolean singleMainAccount;
    boolean showAllAccounts;
    if (selectedAccountIds.isEmpty()) {
      totalMainAccounts = mainAmount;
      totalMainAccountsDate = mainAmountDate;
      singleMainAccount = false;
      showAllAccounts = !savingsAccounts.isEmpty();
    }
    else {
      for (Integer accountId : selectedAccountIds) {
        Glob account = repository.get(Key.create(Account.TYPE, accountId));
        totalMainAccounts += account.get(Account.POSITION_WITH_PENDING, 0.00);
        totalMainAccountsDate = Utils.min(totalMainAccountsDate, account.get(Account.POSITION_DATE));
      }
      singleMainAccount = selectedAccountIds.size() == 1;
      showAllAccounts = selectedAccountIds.size() < mainAccounts.size() + savingsAccounts.size();
    }

    repository.update(DashboardStat.KEY,
                      value(DashboardStat.TOTAL_MAIN_ACCOUNTS, totalMainAccounts),
                      value(DashboardStat.TOTAL_MAIN_ACCOUNTS_DATE, totalMainAccountsDate),
                      value(DashboardStat.SINGLE_MAIN_ACCOUNT, singleMainAccount),
                      value(DashboardStat.TOTAL_ALL_ACCOUNTS, mainAmount + savingsAmount),
                      value(DashboardStat.TOTAL_ALL_ACCOUNTS_DATE, Utils.min(mainAmountDate, savingsAmountDate)),
                      value(DashboardStat.SHOW_ALL_ACCOUNTS, showAllAccounts));
  }

  private static class AccountFieldMatcher implements GlobMatcher {

    private LinkField field;
    private Set<Integer> accountIds;

    private AccountFieldMatcher(LinkField field, Set<Integer> accountIds) {
      this.field = field;
      this.accountIds = accountIds;
    }

    public boolean matches(Glob item, GlobRepository repository) {
      if (accountIds.isEmpty()) {
        return true;
      }
      return accountIds.contains(item.get(field));
    }
  }
}
