package org.designup.picsou.gui.model;

import org.designup.picsou.gui.accounts.utils.MonthDay;
import org.designup.picsou.gui.series.view.SeriesWrapper;
import org.designup.picsou.gui.series.view.SeriesWrapperType;
import org.designup.picsou.model.*;
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
    Collection<GlobType> types = new ArrayList<GlobType>(MODEL.getAll());
    Set<GlobType> typeNotToRemove = MODEL.getConstants().getTypes();
    typeNotToRemove.addAll(Arrays.asList(PreTransactionTypeMatcher.TYPE, BankFormat.TYPE,
                                         User.TYPE, AppVersionInformation.TYPE));
    types.removeAll(typeNotToRemove);
    return types.toArray(new GlobType[types.size()]);
  }

}
