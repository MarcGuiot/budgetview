package com.budgetview.gui.model;

import com.budgetview.gui.series.view.SeriesWrapperType;
import com.budgetview.model.*;
import com.budgetview.gui.accounts.utils.MonthDay;
import com.budgetview.gui.series.view.SeriesWrapper;
import org.globsframework.metamodel.GlobModel;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.utils.DefaultGlobModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

public class PicsouGuiModel {
  private static GlobModel MODEL = new DefaultGlobModel(
    PicsouModel.get(),
    SeriesType.TYPE,
    SeriesShape.TYPE,
    BudgetStat.TYPE,
    Card.TYPE,
    PeriodSeriesStat.TYPE,
    SavingsBudgetStat.TYPE,
    SeriesStat.TYPE,
    SubSeriesStat.TYPE,
    SeriesWrapper.TYPE,
    SeriesWrapperType.TYPE,
    MonthDay.TYPE,
    ColorTheme.TYPE
  );

  public static GlobModel get() {
    return MODEL;
  }

  public static GlobType[] getUserSpecificTypes() {
    Set<GlobType> typeNotToRemove = MODEL.getConstants().getTypes();
    typeNotToRemove.addAll(Arrays.asList(PreTransactionTypeMatcher.TYPE, BankFormat.TYPE,
                                         User.TYPE, AppVersionInformation.TYPE));

    Collection<GlobType> allTypes = new ArrayList<GlobType>(MODEL.getAll());
    allTypes.removeAll(typeNotToRemove);
    return allTypes.toArray(new GlobType[allTypes.size()]);
  }
}
