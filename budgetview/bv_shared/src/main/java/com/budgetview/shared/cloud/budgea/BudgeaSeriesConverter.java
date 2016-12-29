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
    add(DefaultSeries.UNCATEGORIZED,
        BudgeaCategory.INDEFINI,
        BudgeaCategory.CHEQUE,
        BudgeaCategory.CHEQUE2,
        BudgeaCategory.E_COMMERCE,
        BudgeaCategory.VIREMENT,
        BudgeaCategory.VIREMENT_INTERNE,
        BudgeaCategory.EPARGNE
    );

    add(DefaultSeries.INCOME,
        BudgeaCategory.SALAIRE,
        BudgeaCategory.AIDE_ALLOCATIONS,
        BudgeaCategory.RETRAITE
    );

    add(DefaultSeries.RENT,
        BudgeaCategory.LOCATION,
        BudgeaCategory.CREDIT_PRET_IMMOBILIER
    );

    add(DefaultSeries.RENT
//        BudgeaCategory.LOYERS
    );

    add(DefaultSeries.ELECTRICITY,
        BudgeaCategory.ELECTRICITE
    );

    add(DefaultSeries.GAS,
        BudgeaCategory.GAZ
    );
    add(DefaultSeries.WATER,
        BudgeaCategory.EAU
    );
    add(DefaultSeries.CAR_CREDIT);
    add(DefaultSeries.CAR_INSURANCE,
        BudgeaCategory.ASSURANCE_AUTO_HABITATION
    );
    add(DefaultSeries.INCOME_TAXES,
        BudgeaCategory.IMPOT_SUR_LE_REVENU
    );
    add(DefaultSeries.HOUSING_TAXES,
        BudgeaCategory.TAXE_D_HABITATION
    );
    add(DefaultSeries.HEALTH_INSURANCE,
        BudgeaCategory.MUTUELLE
    );
    add(DefaultSeries.PHONE,
        BudgeaCategory.TELEPHONE
    );
    add(DefaultSeries.INTERNET,
        BudgeaCategory.INTERNET_TV
    );
    add(DefaultSeries.GROCERIES,
        BudgeaCategory.ALIMENTATION,
        BudgeaCategory.BOUCHER_POISSONNIER,
        BudgeaCategory.BOULANGER,
        BudgeaCategory.CANTINE,
        BudgeaCategory.CAVISTE,
        BudgeaCategory.EPICIER,
        BudgeaCategory.MARAICHER,
        BudgeaCategory.SUPERMARCHE,
        BudgeaCategory.TRAITEUR_CHOCOLATIER
    );
    add(DefaultSeries.HEALTH,
        BudgeaCategory.SANTE
    );
    add(DefaultSeries.PHYSICIAN,
        BudgeaCategory.DENTAIRE,
        BudgeaCategory.MEDECIN_HOPITAL,
        BudgeaCategory.OPTIQUE,
        BudgeaCategory.SPECIALISTE
    );
    add(DefaultSeries.PHARMACY,
        BudgeaCategory.PHARMACIE
    );
    add(DefaultSeries.REIMBURSEMENTS);
    add(DefaultSeries.RESTAURANT
    );
    add(DefaultSeries.LEISURES,
        BudgeaCategory.LOISIRS,
        BudgeaCategory.BARS_SORTIES,
        BudgeaCategory.CINEMA_FILMS,
        BudgeaCategory.CULTURE_INFOS,
        BudgeaCategory.HIGHTECH,
        BudgeaCategory.LOISIRS_CREATIFS,
        BudgeaCategory.MULTIMEDIA_LIVRES,
        BudgeaCategory.MUSIQUE,
        BudgeaCategory.TABAC_JEUX_D_ARGENT
    );
    add(DefaultSeries.RESTAURANT,
        BudgeaCategory.RESTAURANT);
    add(DefaultSeries.GIFTS,
        BudgeaCategory.CADEAUX,
        BudgeaCategory.CADEAUX2,
        BudgeaCategory.JOUETS
    );

    add(DefaultSeries.CLOTHING,
        BudgeaCategory.SHOPPING_VETEMENTS,
        BudgeaCategory.HABILLEMENT_ENFANT
    );
    add(DefaultSeries.BEAUTY,
        BudgeaCategory.BEAUTE_SHOPPING,
        BudgeaCategory.ACCESSOIRES_BIJOUX,
        BudgeaCategory.COIFFEUR,
        BudgeaCategory.ESTHETIQUE,
        BudgeaCategory.PARFUMERIE
    );

    add(DefaultSeries.ANIMALS,
        BudgeaCategory.ANIMALERIE,
        BudgeaCategory.VETERINAIRE
    );

    add(DefaultSeries.FUEL,
        BudgeaCategory.ESSENCE
    );

    add(DefaultSeries.TOLL,
        BudgeaCategory.PEAGE
    );
    add(DefaultSeries.PARKING,
        BudgeaCategory.PARKING
    );
    add(DefaultSeries.CASH,
        BudgeaCategory.RETRAIT
    );
    add(DefaultSeries.BANK_FEES,
        BudgeaCategory.FRAIS_BANCAIRES
    );
    add(DefaultSeries.MISC);
  }

  private void add(DefaultSeries series, BudgeaCategory... budgeaCategories) {
    for (BudgeaCategory category : budgeaCategories) {
      categoriesToSeries.put(category.getId(), series);
    }
  }
}
