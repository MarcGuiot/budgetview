package org.designup.picsou.model.initial;

import org.designup.picsou.model.Category;
import org.designup.picsou.model.MasterCategory;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.GlobRepository;

public class InitialCategories {
  public static void run(GlobRepository repository) {

    MasterCategory[] masterCategories = MasterCategory.values();
    for (MasterCategory master : masterCategories) {
      repository.create(Category.TYPE,
                        value(Category.INNER_NAME, master.getName()),
                        value(Category.ID, master.getId()));
    }

    create(repository, MasterCategory.FOOD,
           "courant", "restaurant", "livraison", "receptions");

    create(repository, MasterCategory.HOUSE,
           "credit_loyer", "assurance", "electro_menager", "bricolage",
           "entretien", "mobilier", "equipements");

    create(repository, MasterCategory.TELECOMS,
           "telephone_fixe", "telephone_mobile", "internet");

    create(repository, MasterCategory.TRANSPORTS,
           "assurance", "achat", "transports_en_commun", "entretien",
           "essence", "peages", "voyages", "parking");

    create(repository, MasterCategory.INCOME,
           "salaires", "revenus_d_investissement");

    create(repository, MasterCategory.HEALTH,
           "medecin", "pharmacie", "mutuelle", "remboursements", "hopital");

    create(repository, MasterCategory.EDUCATION,
           "ecole", "livres", "visites");

    create(repository, MasterCategory.LEISURES,
           "cinema", "tv", "musique", "sorties", "lecture", "equipement", "activites");

    create(repository, MasterCategory.TAXES,
           "impots", "taxe_d_habitation", "taxe_fonciere");

    create(repository, MasterCategory.BEAUTY);

    create(repository, MasterCategory.CLOTHING);

    create(repository, MasterCategory.SAVINGS,
           "assurance", "epargne", "titres");
  }

  private static void create(GlobRepository repository, MasterCategory master, String... subcats) {
    for (String subcat : subcats) {
      repository.create(Category.TYPE,
                        value(Category.MASTER, master.getId()),
                        value(Category.INNER_NAME, master.getName() + "." + subcat));
    }
  }
}
