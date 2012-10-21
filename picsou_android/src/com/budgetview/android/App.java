package com.budgetview.android;

import android.app.Application;
import android.content.res.Configuration;
import android.util.Log;
import com.budgetview.shared.model.BudgetAreaValues;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.repository.DefaultGlobRepository;

import java.util.Locale;

public class App extends Application {

  private GlobRepository repository;

  public void onCreate() {
    super.onCreate();
    repository = new DefaultGlobRepository();
  }

  public GlobRepository getRepository() {
    return repository;
  }

  public boolean isLoaded() {
    return getRepository().contains(BudgetAreaValues.TYPE);
  }

  public void forceLocale(String languageToLoad) {
    Locale locale = new Locale(languageToLoad);
    Locale.setDefault(locale);
    Configuration config = new Configuration();
    config.locale = locale;
    getBaseContext().getResources().updateConfiguration(config,
                                                        getBaseContext().getResources().getDisplayMetrics());
  }
}
