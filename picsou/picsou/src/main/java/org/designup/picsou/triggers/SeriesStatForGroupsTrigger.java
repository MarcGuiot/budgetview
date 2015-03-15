package org.designup.picsou.triggers;

import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.gui.model.SeriesType;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.SeriesGroup;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.DefaultChangeSetVisitor;

import java.util.HashSet;
import java.util.Set;

import static org.designup.picsou.gui.model.SeriesStat.*;
import static org.globsframework.model.utils.GlobMatchers.*;

public class SeriesStatForGroupsTrigger implements ChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {

    final Set<Integer> groupIds = new HashSet<Integer>();

    changeSet.safeVisit(SeriesStat.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        addToChangedGroupsIfNeeded(key, values);
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        addToChangedGroupsIfNeeded(key, values);
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        addToChangedGroupsIfNeeded(key, previousValues);
      }

      protected void addToChangedGroupsIfNeeded(Key seriesStatKey, FieldValues values) {
        if (SeriesStat.isSummaryForGroup(seriesStatKey)) {
          return;
        }
        Glob series = SeriesStat.findSeries(values, repository);
        if ((series == null) || (series.get(Series.GROUP) == null)) {
          return;
        }
        groupIds.add(series.get(Series.GROUP));
      }
    });

    changeSet.safeVisit(Series.TYPE, new DefaultChangeSetVisitor() {
      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        if (values.contains(Series.GROUP)) {
          Integer groupId = values.get(Series.GROUP);
          addGroup(groupId);
          Integer previousGroupId = values.getPrevious(Series.GROUP);
          addGroup(previousGroupId);
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        addGroup(previousValues.get(Series.GROUP));
      }

      protected void addGroup(Integer groupId) {
        if (groupId != null) {
          groupIds.add(groupId);
        }
      }
    });

    changeSet.safeVisit(SeriesGroup.TYPE, new DefaultChangeSetVisitor() {
      public void visitDeletion(Key key, FieldValues values) throws Exception {
        Integer groupId = key.get(SeriesGroup.ID);
        groupIds.remove(groupId);
        repository.delete(SeriesStat.TYPE,
                          and(fieldEquals(SeriesStat.TARGET, groupId),
                              fieldEquals(SeriesStat.TARGET_TYPE, SeriesType.SERIES_GROUP.getId())));
      }
    });

    updateAll(groupIds, repository);
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(SeriesStat.TYPE)) {
      repository.delete(SeriesStat.TYPE, isSummaryForGroup());
      updateAll(repository.getAll(SeriesGroup.TYPE).getValueSet(SeriesGroup.ID), repository);
    }
  }

  protected void updateAll(Set<Integer> groupIds, GlobRepository repository) {
    for (Integer groupId : groupIds) {
      updateStatForGroup(groupId, repository);
    }
  }

  private void updateStatForGroup(Integer groupId, GlobRepository repository) {

    Set<Integer> seriesIds = repository.getAll(Series.TYPE, fieldEquals(Series.GROUP, groupId)).getValueSet(Series.ID);
    GlobList seriesStatList = repository.getAll(SeriesStat.TYPE, and(isSummaryForSeries(), fieldIn(SeriesStat.TARGET, seriesIds)));

    for (Integer monthId : seriesStatList.getValueSet(SeriesStat.MONTH)) {
      FieldValuesBuilder values = new FieldValuesBuilder();
      values.set(SeriesStat.ACTIVE, false);

      for (Integer seriesId : seriesIds) {
        Glob seriesStat = SeriesStat.findSummaryForSeries(seriesId, monthId, repository);
        if (seriesStat != null && seriesStat.isTrue(SeriesStat.ACTIVE)) {
          values.set(SeriesStat.ACTIVE, true);
          values.add(SeriesStat.PLANNED_AMOUNT, seriesStat.get(SeriesStat.PLANNED_AMOUNT));
          values.add(SeriesStat.ACTUAL_AMOUNT, seriesStat.get(SeriesStat.ACTUAL_AMOUNT));
          values.add(SeriesStat.OVERRUN_AMOUNT, seriesStat.get(SeriesStat.OVERRUN_AMOUNT));
          values.add(SeriesStat.REMAINING_AMOUNT, seriesStat.get(SeriesStat.REMAINING_AMOUNT));
          values.add(SeriesStat.SUMMARY_AMOUNT, seriesStat.get(SeriesStat.SUMMARY_AMOUNT));
        }
      }

      Key key = SeriesStat.keyForGroupSummary(groupId, monthId);
      if (repository.contains(key)) {
        repository.delete(key);
      }
      repository.findOrCreate(key);
      repository.update(key, values.toArray());
    }
  }
}
