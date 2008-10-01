package org.designup.picsou.triggers;

import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.SeriesBudget;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;

import java.util.Set;

public class SeriesBudgetUpdateOccasionnalTrigger implements ChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {

    changeSet.safeVisit(SeriesBudget.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        Glob series = repository.get(Key.create(Series.TYPE, values.get(SeriesBudget.SERIES)));
        Double amount = values.get(SeriesBudget.AMOUNT);
        update(series, amount, values.get(SeriesBudget.MONTH), repository);
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        if (values.contains(SeriesBudget.AMOUNT)) {
          Glob seriesBudget = repository.get(key);
          Glob series = repository.get(Key.create(Series.TYPE, seriesBudget.get(SeriesBudget.SERIES)));
          Double amount = values.get(SeriesBudget.AMOUNT) - values.getPrevious(SeriesBudget.AMOUNT);
          update(series, amount, seriesBudget.get(SeriesBudget.MONTH), repository);
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        GlobList seriesList = repository.getAll(Series.TYPE);
        Integer monthId = previousValues.get(SeriesBudget.MONTH);
        GlobList accasionalSeriesBudget =
          repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, Series.OCCASIONAL_SERIES_ID)
            .findByIndex(SeriesBudget.MONTH, monthId)
            .getGlobs();
        if (accasionalSeriesBudget.isEmpty()) {
          return;
        }
        repository.update(accasionalSeriesBudget.get(0).getKey(), SeriesBudget.AMOUNT, 0.0);
        for (Glob series : seriesList) {
          GlobList seriesBudgets =
            repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, series.get(Series.ID))
              .findByIndex(SeriesBudget.MONTH, monthId)
              .getGlobs();
          for (Glob seriesBudget : seriesBudgets) {
            update(series, seriesBudget.get(SeriesBudget.AMOUNT), seriesBudget.get(SeriesBudget.MONTH),
                   repository);
          }
        }
      }
    });
  }

  private void update(Glob series, Double amount, Integer monthId, GlobRepository repository) {
    if (BudgetArea.OCCASIONAL_EXPENSES.getId().equals(series.get(Series.BUDGET_AREA))) {
      return;
    }
    GlobList seriesBudget =
      repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, Series.OCCASIONAL_SERIES_ID)
        .findByIndex(SeriesBudget.MONTH, monthId).getGlobs();
    if (seriesBudget.isEmpty()) {
      throw new RuntimeException("SeriesBudgetUpdateOccasionnalTrigger.update " + monthId);
    }
    updateOccasional(series, amount, repository, monthId, seriesBudget.get(0));
  }

  private void updateOccasional(Glob series, Double amount, GlobRepository repository, Integer monthId, Glob seriesBudget) {
    updateOccasionalSeriesBudget(-amount, repository, monthId, seriesBudget);
  }

  private void updateOccasionalSeriesBudget(Double amount, GlobRepository repository, Integer monthId,
                                            Glob seriesBudget) {
    repository.update(seriesBudget.getKey(), SeriesBudget.AMOUNT,
                      seriesBudget.get(SeriesBudget.AMOUNT) + amount);
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
  }
}
