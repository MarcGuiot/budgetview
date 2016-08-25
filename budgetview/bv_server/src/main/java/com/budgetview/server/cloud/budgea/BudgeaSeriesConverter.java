package com.budgetview.server.cloud.budgea;

import com.budgetview.shared.model.DefaultSeries;

import java.util.HashMap;
import java.util.Map;

public class BudgeaSeriesConverter {

  private Map<Integer, DefaultSeries> categoriesToSeries = new HashMap<Integer, DefaultSeries>();

  public DefaultSeries convert(Integer budgeaCategoryId) {
    if (budgeaCategoryId == null) {
      return null;
    }
    return categoriesToSeries.get(budgeaCategoryId);
  }

  private void init() {
    add(DefaultSeries.INCOME,
        139 // Salaires nets
    );
    add(DefaultSeries.RENT,
        225 // Loyers et charges locatives
    );

    add(DefaultSeries.ELECTRICITY,
        230 // Électricité
    );

    add(DefaultSeries.GAS,
        229 // Gaz
    );
    add(DefaultSeries.WATER,
        228  // Eau
    );
    add(DefaultSeries.CAR_CREDIT);
    add(DefaultSeries.CAR_INSURANCE);
    add(DefaultSeries.INCOME_TAXES);
    add(DefaultSeries.CELL_PHONE,
        226 // Télécom
    );
    add(DefaultSeries.INTERNET);
    add(DefaultSeries.FIXED_PHONE);
    add(DefaultSeries.GROCERIES);
    add(DefaultSeries.HEALTH);
    add(DefaultSeries.PHYSICIAN);
    add(DefaultSeries.PHARMACY);
    add(DefaultSeries.REIMBURSEMENTS);
    add(DefaultSeries.RESTAURANT,
        143, // Restaurants
        191  // Restaurants
    );
    add(DefaultSeries.LEISURES);
    add(DefaultSeries.CLOTHING);
    add(DefaultSeries.BEAUTY);
    add(DefaultSeries.FUEL,
        193, // Essence
        194  // Gas-Oil
    );

    add(DefaultSeries.CASH,
        1  // Retrait
    );
    add(DefaultSeries.BANK_FEES,
        167, // Frais bancaires
        168  // Agios
    );
    add(DefaultSeries.MISC);
  }

  private void add(DefaultSeries series, int... budgeaCategoryIds) {
    for (int id : budgeaCategoryIds) {
      categoriesToSeries.put(id, series);
    }
  }
}
