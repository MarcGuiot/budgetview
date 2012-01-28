package com.budgetview.analytics;

import com.budgetview.analytics.functors.*;
import com.budgetview.analytics.model.*;
import com.budgetview.analytics.parsing.LogParser;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.GlobRepositoryBuilder;
import org.globsframework.model.format.GlobPrinter;
import org.globsframework.model.utils.GlobMatchers;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;

public class Analytics {
  public static void main(String[] args) throws Exception {

    GlobRepository repository = GlobRepositoryBuilder.createEmpty();

    File in = new File(args[0]);
    FileReader reader = new FileReader(in);
    run(reader, repository);

    GlobPrinter.print(repository.getAll(WeekPerfStat.TYPE).sort(WeekPerfStat.ID));
//    GlobPrinter.print(repository.getAll(User.TYPE, GlobMatchers.isNotNull(User.PURCHASE_DATE)).sort(User.PURCHASE_DATE));
    GlobPrinter.print(repository.getAll(WeekUsageCount.TYPE).sort(WeekUsageCount.LAST_DAY));
    GlobPrinter.print(repository.getAll(WeekUsageStat.TYPE).sort(WeekUsageStat.LAST_DAY));
  }

  public static void run(Reader reader, GlobRepository repository) {

    repository.startChangeSet();

    LogParser parser = new LogParser();
    parser.load(reader, repository);

    repository.getAll(LogEntry.TYPE).sort(LogEntry.ID)
      .safeApply(new UserCreationFunctor(), repository)
      .safeApply(new UserEntriesFunctor(), repository);

    repository.getAll(User.TYPE)
      .safeApply(new UserFieldsFunctor(), repository)
      .safeApply(new UserWeekStatFunctor(), repository);

    repository.getAll(UserProgressInfoEntry.TYPE)
      .safeApply(new UserProgressCountFunctor(), repository);

    repository.getAll(WeekPerfStat.TYPE)
      .safeApply(new WeekFieldsFunctor(), repository)
      .safeApply(new UserWeekRatiosFunctor(), repository);

    repository.getAll(WeekUsageCount.TYPE)
      .safeApply(new UserProgressStatFunctor(), repository);

    repository.completeChangeSet();
  }
}
