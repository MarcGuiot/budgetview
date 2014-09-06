package org.designup.picsou.triggers;

import org.designup.picsou.gui.model.MainAccountStat;
import org.designup.picsou.gui.model.MainAccountWeather;
import org.designup.picsou.gui.model.WeatherType;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.CurrentMonth;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.UserPreferences;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.globsframework.model.FieldValue.value;
import static org.globsframework.model.utils.GlobMatchers.*;

public class AccountWeatherTrigger implements ChangeSetListener {
  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (changeSet.containsChanges(MainAccountStat.TYPE)) {
      recomputeWeather(repository);
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(MainAccountStat.TYPE)) {
      recomputeWeather(repository);
    }
  }

  private void recomputeWeather(GlobRepository repository) {
    repository.deleteAll(MainAccountWeather.TYPE);
    if (!repository.contains(UserPreferences.TYPE)) {
      return;
    }

    SortedSet<Integer> months = getForecastMonths(repository);
    double threshold = repository.get(UserPreferences.KEY).get(UserPreferences.RAINY_WEATHER_THRESHOLD);
    for (Glob account : repository.getAll(Account.TYPE, Account.activeUserCreatedMainAccounts(months))) {
      Integer accountId = account.get(Account.ID);
      double periodMin = Double.MAX_VALUE;
      WeatherType weather = WeatherType.SUNNY;
      for (Glob accountStat : repository.getAll(MainAccountStat.TYPE,
                                                and(fieldEquals(MainAccountStat.ACCOUNT, accountId),
                                                    fieldIn(MainAccountStat.MONTH, months)))) {
        Double monthMin = accountStat.get(MainAccountStat.FUTURE_MIN_POSITION, 0.00);
        if (Double.isNaN(monthMin)) {
          monthMin = accountStat.get(MainAccountStat.END_POSITION, 0.00);
        }
        if (monthMin < 0.0) {
          if (monthMin <= threshold) {
            weather = WeatherType.RAINY;
          }
          else if (weather == WeatherType.SUNNY) {
            weather = WeatherType.CLOUDY;
          }
        }
        periodMin = Math.min(periodMin, monthMin);
      }
      repository.create(MainAccountWeather.TYPE,
                        value(MainAccountWeather.ACCOUNT, accountId),
                        value(MainAccountWeather.WEATHER, weather.getId()),
                        value(MainAccountWeather.LAST_FORECAST_MONTH, months.last()),
                        value(MainAccountWeather.FUTURE_MIN, periodMin));
    }
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
