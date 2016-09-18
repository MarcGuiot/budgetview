package com.budgetview.desktop.importer.series;

import com.budgetview.desktop.series.utils.SeriesMatchers;
import com.budgetview.model.Account;
import com.budgetview.model.ImportedSeries;
import com.budgetview.model.Series;
import com.budgetview.model.SubSeries;
import com.budgetview.shared.model.BudgetArea;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.format.GlobPrinter;
import org.globsframework.model.repository.LocalGlobRepository;
import org.globsframework.utils.Strings;
import org.globsframework.utils.exceptions.ItemNotFound;

import java.util.Iterator;
import java.util.Set;

import static org.globsframework.model.FieldValue.value;
import static org.globsframework.model.utils.GlobMatchers.and;
import static org.globsframework.model.utils.GlobMatchers.fieldEquals;

public class SeriesImporter {

  public static void createSeries(Set<Key> importedSeriesKeys, LocalGlobRepository localRepository) {
    for (Key importedSeriesKey : importedSeriesKeys) {
      Glob importedSeries = localRepository.get(importedSeriesKey);
      Integer budgetArea = importedSeries.get(ImportedSeries.BUDGET_AREA);
      String name = importedSeries.get(ImportedSeries.NAME);
      if (Strings.isNotEmpty(name)) {
        String[] splitted = name.split(":");
        String seriesName = splitted[0];
        String subSeriesName = splitted.length == 2 ? splitted[1] : null;
        if (budgetArea != null) {
          Glob subSeries = null;
          Glob series =
            localRepository.getAll(Series.TYPE,
                                   and(fieldEquals(Series.BUDGET_AREA, budgetArea),
                                       fieldEquals(Series.NAME, seriesName))).getFirst();
          if (series != null) {
            if (subSeriesName != null) {
              subSeries =
                localRepository.getAll(SubSeries.TYPE, and(fieldEquals(SubSeries.SERIES, series.get(Series.ID)),
                                                           fieldEquals(SubSeries.NAME, subSeriesName)
                )).getFirst();
              if (subSeries == null) {
                subSeries = localRepository.create(SubSeries.TYPE,
                                                   value(SubSeries.NAME, subSeriesName),
                                                   value(SubSeries.SERIES, series.get(Series.ID)));
              }
            }
          }
          else {
            series = localRepository.create(Series.TYPE,
                                            value(Series.IS_AUTOMATIC,
                                                  BudgetArea.RECURRING.getId().equals(importedSeries.get(ImportedSeries.BUDGET_AREA))),
                                            value(Series.BUDGET_AREA, importedSeries.get(ImportedSeries.BUDGET_AREA)),
                                            value(Series.NAME, seriesName));
            if (subSeriesName != null) {
              subSeries = localRepository.create(SubSeries.TYPE,
                                                 value(SubSeries.NAME, subSeriesName),
                                                 value(SubSeries.SERIES, series.get(Series.ID)));
            }
          }
          localRepository.update(importedSeriesKey,
                                 value(ImportedSeries.SERIES, series.get(Series.ID)),
                                 value(ImportedSeries.SUB_SERIES, subSeries != null ? subSeries.get(SubSeries.ID) : null));
        }
      }
    }
  }

  public static void updateToKnownSeries(Set<Key> importedSeriesSet, Glob targetAccount, GlobRepository localRepository) {

    System.out.println("SeriesImporter.updateToKnownSeries");
    GlobPrinter.print(localRepository, ImportedSeries.TYPE);

    for (Iterator<Key> iterator = importedSeriesSet.iterator(); iterator.hasNext(); ) {
      Glob importedSeries = localRepository.get(iterator.next());

      Glob initialSeries = localRepository.findLinkTarget(importedSeries, ImportedSeries.SERIES);
      if (initialSeries != null && !Series.isSeriesForAccount(initialSeries, targetAccount.get(Account.ID), localRepository)) {
        localRepository.update(importedSeries.getKey(),
                               value(ImportedSeries.SERIES, null),
                               value(ImportedSeries.SUB_SERIES, null),
                               value(ImportedSeries.BUDGET_AREA, null));
      }

      String name = importedSeries.get(ImportedSeries.NAME);
      String[] splitted = name.split(":");

      Integer seriesId = null;
      Integer subSeriesId = null;
      Integer budgetArea = null;
      boolean duplicate = false;

      if (splitted.length == 1) {
        GlobList seriesList = localRepository.getAll(Series.TYPE, and(fieldEquals(Series.NAME, splitted[0]),
                                                                      SeriesMatchers.seriesForAccount(targetAccount)));
        for (Glob series : seriesList) {
          if (seriesId != null) {
            duplicate = true;
          }
          seriesId = series.get(Series.ID);
          subSeriesId = null;
          budgetArea = series.get(Series.BUDGET_AREA);
        }
      }
      else if (splitted.length == 2) {
        GlobList seriesList = localRepository.getAll(Series.TYPE, and(fieldEquals(Series.NAME, splitted[0]),
                                                                      SeriesMatchers.seriesForAccount(targetAccount)));
        for (Glob series : seriesList) {
          GlobList subSeriesList = localRepository.findLinkedTo(series, SubSeries.SERIES);
          for (Glob subSeries : subSeriesList) {
            if (subSeries.get(SubSeries.NAME).equals(splitted[1])) {
              if (seriesId != null) {
                duplicate = true;
              }
              seriesId = series.get(Series.ID);
              subSeriesId = subSeries.get(SubSeries.ID);
              budgetArea = series.get(Series.BUDGET_AREA);
            }
          }
        }
      }

      if (!duplicate && budgetArea != null) {
        localRepository.update(importedSeries.getKey(),
                               value(ImportedSeries.SERIES, seriesId),
                               value(ImportedSeries.SUB_SERIES, subSeriesId),
                               value(ImportedSeries.BUDGET_AREA, budgetArea));
        iterator.remove();
      }
    }
  }
}
