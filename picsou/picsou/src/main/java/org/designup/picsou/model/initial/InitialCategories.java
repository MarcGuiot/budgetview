package org.designup.picsou.model.initial;

import org.designup.picsou.model.Category;
import org.designup.picsou.model.MasterCategory;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;

public class InitialCategories {
  public static void run(GlobRepository repository) {

    if (repository.find(Key.create(Category.TYPE, Category.ALL)) != null) {
      return;
    }

    MasterCategory[] masterCategories = MasterCategory.values();
    for (MasterCategory master : masterCategories) {
      repository.create(Category.TYPE,
                        value(Category.INNER_NAME, master.getName()),
                        value(Category.ID, master.getId()));
    }

    create(repository, MasterCategory.FOOD);

    create(repository, MasterCategory.HOUSE,
           "credit", "loyer", "entretien", "mobilier", "energie");

    create(repository, MasterCategory.TELECOMS);

    create(repository, MasterCategory.TRANSPORTS,
           "assurance", "achat", "transports_en_commun", "entretien", "essence");

    create(repository, MasterCategory.INCOME);

    create(repository, MasterCategory.HEALTH,
           "medecin", "pharmacie", "mutuelle", "remboursements");

    create(repository, MasterCategory.EDUCATION);

    create(repository, MasterCategory.LEISURES);

    create(repository, MasterCategory.TAXES);

    create(repository, MasterCategory.BEAUTY);

    create(repository, MasterCategory.CLOTHING);

    create(repository, MasterCategory.SAVINGS);
  }

  private static void create(GlobRepository repository, MasterCategory master, String... subcats) {
    for (String subcat : subcats) {
      repository.create(Category.TYPE,
                        value(Category.MASTER, master.getId()),
                        value(Category.INNER_NAME, Category.getInnerName(master, subcat)));
    }
  }
}
