package com.budgetview.android;

import android.app.Application;
import android.content.res.Configuration;

import com.budgetview.shared.model.BudgetAreaValues;
import com.budgetview.shared.model.MonthEntity;

import org.globsframework.model.GlobRepository;
import org.globsframework.model.repository.DefaultGlobRepository;

import java.util.Iterator;
import java.util.Locale;
import java.util.SortedSet;

public class App extends Application {

    private GlobRepository repository;

    public void onCreate() {
        super.onCreate();
        repository = new DefaultGlobRepository();
    }

    public GlobRepository getRepository() {
        return repository;
    }

    public SortedSet<Integer> getAllMonthIds() {
        return getRepository().getAll(MonthEntity.TYPE).getSortedSet(MonthEntity.ID);
    }

    public int getCurrentMonthId() {
        SortedSet<Integer> monthIds = getAllMonthIds();
        if (monthIds.isEmpty()) {
            throw new RuntimeException("No months found in repository");
        }
        if (monthIds.size() == 1) {
            return monthIds.first();
        }
        Iterator<Integer> iterator = monthIds.iterator();
        iterator.next();
        return iterator.next();
    }

    public void reset() {
        getRepository().deleteAll();
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
