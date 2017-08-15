package com.budgetview.triggers;

import com.budgetview.desktop.accounts.utils.AccountMatchers;
import com.budgetview.desktop.model.AccountStat;
import com.budgetview.desktop.model.AccountWeather;
import com.budgetview.desktop.model.WeatherType;
import com.budgetview.model.Account;
import com.budgetview.model.CurrentMonth;
import com.budgetview.model.Month;
import com.budgetview.model.UserPreferences;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.globsframework.model.FieldValue.value;
import static org.globsframework.model.utils.GlobMatchers.*;

public class AccountWeatherTrigger implements ChangeSetListener {
  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (changeSet.containsChanges(AccountStat.TYPE)) {
      recomputeWeather(repository);
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(AccountStat.TYPE)) {
      recomputeWeather(repository);
    }
  }

  private void recomputeWeather(GlobRepository repository) {
    repository.deleteAll(AccountWeather.TYPE);
    if (!repository.contains(UserPreferences.TYPE)) {
      return;
    }

    SortedSet<Integer> months = getForecastMonths(repository);
    double threshold = repository.get(UserPreferences.KEY).get(UserPreferences.RAINY_WEATHER_THRESHOLD);
    for (Glob account : repository.getAll(Account.TYPE, AccountMatchers.activeUserCreatedAccounts(months))) {
      Integer accountId = account.get(Account.ID);
      double periodMin = Double.MAX_VALUE;
      WeatherType weather = WeatherType.SUNNY;
      GlobList stats = repository.getAll(AccountStat.TYPE,
                                         and(fieldEquals(AccountStat.ACCOUNT, accountId),
                                             fieldIn(AccountStat.MONTH, months)));
      if (!stats.isEmpty()) {
        for (Glob accountStat : stats) {
          Double monthMin = accountStat.get(AccountStat.FUTURE_MIN_POSITION, 0.00);
          if (Double.isNaN(monthMin)) {
            monthMin = accountStat.get(AccountStat.END_POSITION, 0.00);
          }
          weather = getWeatherType(monthMin, threshold, weather);
          periodMin = Math.min(periodMin, monthMin);
        }
      }
      else {
        periodMin = 0;
      }
      repository.create(AccountWeather.TYPE,
                        value(AccountWeather.ACCOUNT, accountId),
                        value(AccountWeather.WEATHER, weather.getId()),
                        value(AccountWeather.LAST_FORECAST_MONTH, months.last()),
                        value(AccountWeather.FUTURE_MIN, periodMin));
    }
  }

  private WeatherType getWeatherType(Double monthMin, double threshold, WeatherType currentWeather) {
    if (monthMin < 0.0) {
      if (monthMin <= threshold) {
        currentWeather = WeatherType.RAINY;
      }
      else if (currentWeather == WeatherType.SUNNY) {
        currentWeather = WeatherType.CLOUDY;
      }
    }
    return currentWeather;
  }

  private SortedSet<Integer> getForecastMonths(GlobRepository repository) {
    SortedSet<Integer> months = new TreeSet<Integer>();
    Integer currentMonth = CurrentMonth.findCurrentMonth(repository);
    if (currentMonth != null) {
      months.add(currentMonth);
      months.add(Month.next(currentMonth));
    }
    return months;
  }
}
