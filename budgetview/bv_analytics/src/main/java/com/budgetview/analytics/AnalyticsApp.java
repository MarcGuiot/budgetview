package com.budgetview.analytics;

import com.budgetview.analytics.gui.AnalyticsWindow;
import com.budgetview.utils.Lang;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.utils.DefaultDescriptionService;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import java.util.Locale;

public class AnalyticsApp {
  
  public static final int MIN_WEEK = 201145;
  
  public static void main(String[] args) throws Exception {
    Lang.setLocale(Locale.ENGLISH);
    GlobRepository repository = Analytics.load(args);
    Directory directory = createDirectory();

    Experiments experiments = new Experiments(repository);
    experiments.register();

    AnalyticsWindow window = new AnalyticsWindow(repository, directory);
    window.show();
  }

  private static Directory createDirectory() {
    Directory directory = new DefaultDirectory();
    directory.add(new SelectionService());
    directory.add(DescriptionService.class, new DefaultDescriptionService());
    directory.add(new ColorService(AnalyticsApp.class, "/colors/analytics_colors.properties"));
    return directory;
  }
}
