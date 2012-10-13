package com.budgetview.android;

import android.app.Application;
import android.util.Log;
import com.budgetview.shared.model.BudgetAreaValues;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.repository.DefaultGlobRepository;

public class App extends Application {

  private GlobRepository repository;

  public App() {
    Log.d("App", "new");
  }

  public void onCreate() {
    super.onCreate();
    Log.d("App", "onCreate");
    repository = new DefaultGlobRepository();
  }

  public GlobRepository getRepository() {
    return repository;
  }

  public boolean isLoaded() {
    return getRepository().contains(BudgetAreaValues.TYPE);
  }
}
