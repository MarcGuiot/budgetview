package com.budgetview.analytics;

import com.budgetview.analytics.functors.*;
import com.budgetview.analytics.model.*;
import com.budgetview.analytics.parsing.LogParser;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.GlobRepositoryBuilder;
import org.globsframework.model.format.GlobPrinter;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Analytics {
  public static void main(String[] args) throws Exception {

    GlobRepository repository = GlobRepositoryBuilder.createEmpty();

    if (args.length == 0) {
      System.out.println("Usage: analytics <log_file> <log_file> ...");
      return;
    }

    List<Reader> readers = new ArrayList<Reader>();
    for (String path : args) {
      File in = new File(path);
      readers.add(new FileReader(in));
    }
    run(readers, repository);

    GlobPrinter.print(repository.getAll(WeekPerfStat.TYPE).sort(WeekPerfStat.ID));
//    GlobPrinter.print(repository.getAll(User.TYPE, GlobMatchers.isNotNull(User.PURCHASE_DATE)).sort(User.PURCHASE_DATE));
    GlobPrinter.print(repository.getAll(WeekUsageStat.TYPE).sort(WeekUsageStat.LAST_DAY));
  }

  public static void run(Reader reader, GlobRepository repository) {
    run(Collections.singletonList(reader), repository);
  }

  public static void run(List<Reader> readers, GlobRepository repository) {

    repository.startChangeSet();

    LogParser parser = new LogParser();
    for (Reader reader : readers) {
      parser.load(reader, repository);
    }

    repository.getAll(LogEntry.TYPE).sort(LogEntry.ID)
      .safeApply(new UserCreationFunctor(), repository)
      .safeApply(new UserEntriesFunctor(), repository);

    repository.getAll(User.TYPE)
      .safeApply(new UserFieldsFunctor(), repository)
      .safeApply(new UserWeekStatFunctor(), repository);

    repository.getAll(UserProgressInfoEntry.TYPE)
      .safeApply(new UserProgressCountFunctor(), repository);

    repository.getAll(WeekPerfStat.TYPE)
      .safeApply(new WeekStatsFunctor(), repository)
      .safeApply(new UserWeekRatiosFunctor(), repository);

    repository.getAll(WeekUsageCount.TYPE)
      .safeApply(new UserProgressStatFunctor(), repository);

    repository.completeChangeSet();
  }
}
