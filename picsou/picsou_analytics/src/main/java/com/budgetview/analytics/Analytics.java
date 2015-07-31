package com.budgetview.analytics;

import com.budgetview.analytics.functors.*;
import com.budgetview.analytics.model.*;
import com.budgetview.analytics.parsing.LogParser;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.GlobRepositoryBuilder;
import org.globsframework.model.format.GlobPrinter;
import org.globsframework.utils.comparators.StringComparator;
import org.globsframework.utils.exceptions.InvalidParameter;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Analytics {
  public static void main(String[] args) throws Exception {

    GlobRepository repository = load(args);

    GlobPrinter.print(repository.getAll(WeekStats.TYPE).sort(WeekStats.ID));
//    GlobPrinter.print(repository.getAll(User.TYPE, GlobMatchers.isNotNull(User.PURCHASE_DATE)).sort(User.PURCHASE_DATE));
    GlobPrinter.print(repository.getAll(WeekUsageStats.TYPE).sort(WeekUsageStats.LAST_DAY));
  }

  public static GlobRepository load(String[] args) throws InvalidParameter, IOException {
    GlobRepository repository = GlobRepositoryBuilder.createEmpty();

    if (args.length == 0) {
      throw new InvalidParameter("Usage: analytics <log_file> <log_file> ... | <log_dir> ...");
    }

    List<String> paths = new ArrayList<String>();
    for (String arg : args) {
      loadPaths(new File(arg), paths);
    }
    Collections.sort(paths, StringComparator.instance());
    Collections.reverse(paths);
    List<Reader> readers = new ArrayList<Reader>();
    for (String path : paths) {
      readers.add(new FileReader(path));
    }
    run(readers, repository);

    return repository;
  }

  public static void loadPaths(File file, List<String> paths) throws FileNotFoundException {
    if (file.isDirectory()) {
      for (File subFile : file.listFiles()) {
        loadPaths(subFile, paths);
      }
    }
    else {
      paths.add(file.getAbsolutePath());
    }
  }

  public static void run(Reader reader, GlobRepository repository) {
    run(Collections.singletonList(reader), repository);
  }

  public static void run(List<Reader> readers, GlobRepository repository) {

    repository.startChangeSet();

    LogParser parser = new LogParser(repository);
    for (Reader reader : readers) {
      parser.load(reader);
    }
    parser.complete();

    repository.getAll(LogEntry.TYPE).sort(LogEntry.ID)
      .safeApply(new UserCreationFunctor(), repository)
      .safeApply(new UserEntriesFunctor(), repository);

    repository.getAll(User.TYPE)
      .safeApply(new UserStateFunctor(repository), repository)
      .safeApply(new CohortCountFunctor(repository), repository)
      .safeApply(new ActiveUsersCountFunctor(repository), repository)
      .safeApply(new PurchaseCountFunctor(), repository);

    repository.getAll(UserProgressInfoEntry.TYPE)
      .safeApply(new UserProgressCountFunctor(), repository);

    repository.getAll(WeekStats.TYPE)
      .safeApply(new WeekRatiosFunctor(), repository);

    repository.getAll(WeekUsageCount.TYPE)
      .safeApply(new UserProgressStatFunctor(), repository);

    repository.completeChangeSet();
  }
}
