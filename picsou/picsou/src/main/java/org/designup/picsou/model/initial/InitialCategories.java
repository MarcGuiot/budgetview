package org.designup.picsou.model.initial;

import org.designup.picsou.model.Category;
import org.designup.picsou.model.MasterCategory;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.GlobRepository;

public class InitialCategories {
  public static void run(GlobRepository repository) {
    create(repository, MasterCategory.FOOD,
           "Courant", "Restaurant", "Livraison / A emporter", "Réceptions");

    create(repository, MasterCategory.HOUSE,
           "Crédit / Loyer", "Assurance multi-risques", "Electro-ménager", "Bricolage",
           "Entretien", "Mobilier", "Equipements divers");

    create(repository, MasterCategory.TELECOMS,
           "Téléphone fixe", "Téléphone mobile", "Internet");

    create(repository, MasterCategory.TRANSPORTS,
           "Assurance", "Achat véhicule", "Transports en commun", "Entretien/Réparations",
           "Essence", "Péages", "Voyages", "Parking");

    create(repository, MasterCategory.INCOME,
           "Salaires et revenus non salariés", "Revenus d'investissement", "Revenus immobiliers");

    create(repository, MasterCategory.HEALTH,
           "Médecin", "Pharmacie", "Mutuelle", "Remboursements", "Hopital");

    create(repository, MasterCategory.EDUCATION,
           "Ecole / Université", "Livres", "Visites / musées");

    create(repository, MasterCategory.LEISURES,
           "Cinéma", "TV", "Musique", "Sorties", "Lecture", "Equipement sportif", "Activités sportives");

    create(repository, MasterCategory.TAXES,
           "Impôts sur le revenu", "Taxe d'habitation", "Taxe foncière");

    create(repository, MasterCategory.BEAUTY);

    create(repository, MasterCategory.CLOTHING);

    create(repository, MasterCategory.SAVINGS,
           "Assurance vie", "Epargne", "Titres");
  }

  private static void create(GlobRepository repository, MasterCategory master, String... subcats) {
    for (String subcat : subcats) {
      repository.create(Category.TYPE,
                        value(Category.MASTER, master.getId()),
                        value(Category.NAME, subcat));
    }
  }
}
