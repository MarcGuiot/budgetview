package org.designup.picsou.functests.utils;

import org.designup.picsou.gui.model.Card;
import org.designup.picsou.gui.model.PicsouGuiModel;
import org.designup.picsou.model.*;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.GlobRepositoryBuilder;
import org.globsframework.model.impl.DefaultGlobIdGenerator;
import org.globsframework.model.utils.CachedGlobIdGenerator;

import java.util.ArrayList;
import java.util.List;

public class ApplicationReset {

  public static void run(GlobRepository repository) {

    final DefaultGlobIdGenerator generator = new DefaultGlobIdGenerator();
    GlobRepository temp =
      GlobRepositoryBuilder.init(new CachedGlobIdGenerator(generator))
        .add(PicsouGuiModel.get().getConstants())
        .get();
    generator.setRepository(temp);

//    PicsouInit.createDataForNewUser(temp);
    temp.getAll();

    GlobType[] typesToDelete = getTypesToDelete(
      User.TYPE, VersionInformation.TYPE, BudgetArea.TYPE,
      TransactionType.TYPE, TransactionTypeMatcher.TYPE, Bank.TYPE, BankEntity.TYPE,
      ProfileType.TYPE, Card.TYPE);
    repository.reset(temp.getAll(), typesToDelete);
  }

  private static GlobType[] getTypesToDelete(GlobType... typesToKeep) {
    List<GlobType> types = new ArrayList<GlobType>();
    types.addAll(PicsouGuiModel.get().getAll());
    for (GlobType type : typesToKeep) {
      types.remove(type);
    }
    return types.toArray(new GlobType[types.size()]);
  }

}
