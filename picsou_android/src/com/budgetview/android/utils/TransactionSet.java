package com.budgetview.android.utils;

import android.content.Intent;
import android.os.Bundle;
import com.budgetview.shared.model.*;
import org.apache.http.entity.ByteArrayEntity;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.utils.exceptions.InvalidParameter;
import org.globsframework.utils.exceptions.InvalidState;

import static org.globsframework.model.utils.GlobMatchers.*;

public class TransactionSet {

  public static String MONTH_PARAMETER = "transactionSet.parameters.month";
  public static String SERIES_ENTITY_PARAMETER = "transactionSet.parameters.series";
  public static String ACCOUNT_PARAMETER = "transactionSet.parameters.account";

  private int monthId;
  private Integer accountId;
  private Integer seriesEntityId;

  private String sectionLabel;
  private GlobMatcher matcher;

  private GlobRepository repository;

  public TransactionSet(int monthId, Integer seriesEntityId, Integer accountId, GlobRepository repository) {
    this.monthId = monthId;
    this.seriesEntityId = seriesEntityId;
    this.accountId = accountId;
    init(repository);
  }

  public TransactionSet(Intent intent, GlobRepository repository) {
    this.monthId = intent.getIntExtra(MONTH_PARAMETER, -1);
    if (intent.hasExtra(SERIES_ENTITY_PARAMETER)) {
      this.seriesEntityId = intent.getIntExtra(SERIES_ENTITY_PARAMETER, -1);
    }
    else if (intent.hasExtra(ACCOUNT_PARAMETER)) {
      this.accountId = intent.getIntExtra(ACCOUNT_PARAMETER, -1);
    }
    else {
      throw new InvalidParameter("Missing filtering parameter");
    }
    init(repository);
  }

  public TransactionSet(Bundle args, GlobRepository repository) {
    this.monthId = args.getInt(MONTH_PARAMETER, -1);
    if (args.containsKey(SERIES_ENTITY_PARAMETER)) {
      this.seriesEntityId = args.getInt(SERIES_ENTITY_PARAMETER);
    }
    else if (args.containsKey(ACCOUNT_PARAMETER)) {
      this.accountId = args.getInt(ACCOUNT_PARAMETER);
    }
    else {
      throw new InvalidParameter("Missing filtering parameter");
    }
    init(repository);
  }

  private void init(GlobRepository repository) {
    this.repository = repository;
    if (seriesEntityId != null) {
      matcher = and(fieldEquals(TransactionValues.BANK_MONTH, monthId),
                    fieldEquals(TransactionValues.SERIES, seriesEntityId));
      Glob seriesEntity = repository.get(Key.create(SeriesEntity.TYPE, seriesEntityId));
      sectionLabel = seriesEntity.get(SeriesEntity.NAME);
    }
    else if (accountId != null) {
      matcher = and(fieldEquals(TransactionValues.BANK_MONTH, monthId),
                    fieldEquals(TransactionValues.ACCOUNT, accountId));
      Glob accountEntity = repository.get(Key.create(AccountEntity.TYPE, accountId));
      sectionLabel = accountEntity.get(AccountEntity.LABEL);
    }
    else {
      throw new InvalidState("Must declare a series or an account");
    }
  }

  public int getMonthId() {
    return monthId;
  }

  public String getSectionLabel() {
    return sectionLabel;
  }

  public GlobMatcher getMatcher() {
    return matcher;
  }

  public boolean isSeriesList() {
    return seriesEntityId != null;
  }

  public boolean isUncategorized() {
    if (seriesEntityId == null) {
      return false;
    }
    Glob seriesEntity = repository.find(Key.create(SeriesEntity.TYPE, seriesEntityId));
    return seriesEntity != null && BudgetAreaEntity.isUncategorized(seriesEntity.get(SeriesEntity.BUDGET_AREA));
  }

  public Glob getSeriesValues() {
    if (seriesEntityId == null) {
      return null;
    }
    return repository.find(Key.create(SeriesValues.SERIES_ENTITY, seriesEntityId, SeriesValues.MONTH, monthId));
  }

  public boolean isAccountEntity() {
    return accountId != null;
  }

  public Glob getAccountEntity() {
    if (accountId == null) {
      return null;
    }
    return repository.find(Key.create(AccountEntity.TYPE, accountId));
  }

  public TransactionSet copy(int newMonthId) {
    return new TransactionSet(newMonthId, seriesEntityId, accountId, repository);
  }

  public void save(Bundle bundle) {
    bundle.putInt(MONTH_PARAMETER, monthId);
    if (accountId != null) {
      bundle.putInt(ACCOUNT_PARAMETER, accountId);
    }
    if (seriesEntityId != null) {
      bundle.putInt(SERIES_ENTITY_PARAMETER, seriesEntityId);
    }
  }

  public void save(Intent intent) {
    intent.putExtra(MONTH_PARAMETER, monthId);
    if (accountId != null) {
      intent.putExtra(ACCOUNT_PARAMETER, accountId);
    }
    if (seriesEntityId != null) {
      intent.putExtra(SERIES_ENTITY_PARAMETER, seriesEntityId);
    }
  }
}
