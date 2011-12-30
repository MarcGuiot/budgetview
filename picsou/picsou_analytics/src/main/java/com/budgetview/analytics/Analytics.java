package com.budgetview.analytics;

import com.budgetview.analytics.functors.*;
import com.budgetview.analytics.model.LogEntry;
import com.budgetview.analytics.model.User;
import com.budgetview.analytics.model.WeekStat;
import com.budgetview.analytics.parsing.LogParser;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.GlobRepositoryBuilder;
import org.globsframework.model.format.GlobPrinter;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;

public class Analytics {
  public static void main(String[] args) throws Exception {

    File in = new File("/Users/rmedina/Code/all_hg/ref/budgetview_server.log");
    FileReader reader = new FileReader(in);
    GlobRepository repository = GlobRepositoryBuilder.createEmpty();

    run(reader, repository);

    GlobPrinter.print(repository.getAll(WeekStat.TYPE).sort(WeekStat.ID));
  }

  public static void run(Reader reader, GlobRepository repository) {
    LogParser.load(reader, repository);

    repository.getAll(LogEntry.TYPE).sort(LogEntry.ID)
      .safeApply(new UserCreationFunctor(), repository)
      .safeApply(new UserEntriesFunctor(), repository);

    repository.getAll(User.TYPE)
      .safeApply(new UserFieldsFunctor(), repository)
      .safeApply(new UserWeekStatFunctor(), repository);

    repository.getAll(WeekStat.TYPE)
      .safeApply(new WeekFieldsFunctor(), repository)
      .safeApply(new UserWeekRatiosFunctor(), repository);
  }
}
