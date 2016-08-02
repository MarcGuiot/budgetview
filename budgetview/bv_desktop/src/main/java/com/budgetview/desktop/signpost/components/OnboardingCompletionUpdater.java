package com.budgetview.desktop.signpost.components;

import com.budgetview.desktop.model.PeriodSeriesStat;
import com.budgetview.model.SignpostStatus;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.GlobRepository;

import java.util.Set;

import static org.globsframework.model.utils.GlobMatchers.isTrue;

public class OnboardingCompletionUpdater {
  public static void init(GlobRepository repository) {
    repository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsChanges(PeriodSeriesStat.TYPE)) {
          update(repository);
        }
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
        if (changedTypes.contains(PeriodSeriesStat.TYPE)) {
          update(repository);
        }
      }
    });
  }

  private static void update(GlobRepository repository) {
    if (!repository.contains(SignpostStatus.TYPE) || SignpostStatus.isCompleted(SignpostStatus.SERIES_AMOUNT_DONE, repository)) {
      return;
    }

    boolean completed = repository.contains(PeriodSeriesStat.TYPE, isTrue(PeriodSeriesStat.ACTIVE)) &&
                        SignpostStatus.isCompleted(SignpostStatus.SERIES_AMOUNT_SHOWN, repository) &&
                        !repository.contains(PeriodSeriesStat.TYPE, isTrue(PeriodSeriesStat.TO_SET));
    repository.update(SignpostStatus.KEY, SignpostStatus.SERIES_AMOUNT_DONE, completed);
  }
}
