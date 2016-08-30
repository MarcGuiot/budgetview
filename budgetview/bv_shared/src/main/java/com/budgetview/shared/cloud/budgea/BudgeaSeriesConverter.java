package com.budgetview.shared.cloud.budgea;

import com.budgetview.shared.model.DefaultSeries;

import java.util.HashMap;
import java.util.Map;

public class BudgeaSeriesConverter {

  private Map<Integer, DefaultSeries> categoriesToSeries = new HashMap<Integer, DefaultSeries>();

  public BudgeaSeriesConverter() {
    init();
  }

  public DefaultSeries convert(Integer budgeaCategoryId) {
    if (budgeaCategoryId == null) {
      return null;
    }
    return categoriesToSeries.get(budgeaCategoryId);
  }

  private void init() {
    add(DefaultSeries.INCOME,
        BudgeaCategory.SALAIRES_NETS
    );
    add(DefaultSeries.RENT,
        BudgeaCategory.LOYERS
    );

    add(DefaultSeries.ELECTRICITY,
        BudgeaCategory.ELECTRICITE
    );

    add(DefaultSeries.GAS,
        BudgeaCategory.GAZ
    );
    add(DefaultSeries.WATER,
        BudgeaCategory.WATER
    );
    add(DefaultSeries.CAR_CREDIT);
    add(DefaultSeries.CAR_INSURANCE);
    add(DefaultSeries.INCOME_TAXES);
    add(DefaultSeries.CELL_PHONE,
        BudgeaCategory.TELECOMS
    );
    add(DefaultSeries.INTERNET);
    add(DefaultSeries.FIXED_PHONE);
    add(DefaultSeries.GROCERIES);
    add(DefaultSeries.HEALTH);
    add(DefaultSeries.PHYSICIAN);
    add(DefaultSeries.PHARMACY);
    add(DefaultSeries.REIMBURSEMENTS);
    add(DefaultSeries.RESTAURANT,
        BudgeaCategory.RESTAURANTS1,
        BudgeaCategory.RESTAURANTS2
    );
    add(DefaultSeries.LEISURES);
    add(DefaultSeries.CLOTHING);
    add(DefaultSeries.BEAUTY);
    add(DefaultSeries.FUEL,
        BudgeaCategory.FUEL1,
        BudgeaCategory.FUEL2
    );

    add(DefaultSeries.CASH,
        BudgeaCategory.CASH
    );
    add(DefaultSeries.BANK_FEES,
        BudgeaCategory.FRAIS_BANCAIRES,
        BudgeaCategory.AGOS
    );
    add(DefaultSeries.MISC);
  }

  private void add(DefaultSeries series, BudgeaCategory... budgeaCategories) {
    for (BudgeaCategory category : budgeaCategories) {
      categoriesToSeries.put(category.getId(), series);
    }
  }
}
