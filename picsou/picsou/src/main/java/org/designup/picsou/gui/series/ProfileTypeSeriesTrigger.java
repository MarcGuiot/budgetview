package org.designup.picsou.gui.series;

import org.designup.picsou.model.ProfileType;
import org.designup.picsou.model.Series;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.model.*;

import java.util.Set;

public class ProfileTypeSeriesTrigger implements ChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    changeSet.safeVisit(Series.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        ProfileType profileType = ProfileType.get(values.get(Series.PROFILE_TYPE));
        if (profileType == ProfileType.CUSTOM) {
          return;
        }
        if (profileType == ProfileType.UNKNOWN ||
            profileType == ProfileType.EVERY_MONTH) {
          for (BooleanField field : Series.getMonths()) {
            repository.update(key, field, true);
          }
          return;
        }
        updateProfileType(repository.get(key));
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        ProfileType profileType = ProfileType.get(repository.get(key).get(Series.PROFILE_TYPE));
        if (profileType == ProfileType.CUSTOM) {
          return;
        }
        if (profileType == ProfileType.UNKNOWN ||
            profileType == ProfileType.EVERY_MONTH) {
          for (BooleanField field : Series.getMonths()) {
            repository.update(key, field, true);
          }
          return;
        }

        if (values.contains(Series.PROFILE_TYPE)) {
          for (BooleanField field : Series.getMonths()) {
            repository.update(key, field, false);
          }
        }
        updateProfileType(repository.get(key));
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
      }

      private void updateProfileType(Glob series) {
        boolean atLeatOneIsSelected = false;
        for (BooleanField field : Series.getMonths()) {
          if (series.get(field)) {
            atLeatOneIsSelected = true;
            break;
          }
        }
        if (!atLeatOneIsSelected) {
          repository.update(series.getKey(), Series.JANUARY, true);
        }
        Integer profileTypeId = series.get(Series.PROFILE_TYPE);
        ProfileType profileType;
        if (profileTypeId == null) {
          profileType = ProfileType.UNKNOWN;
        }
        else {
          profileType = ProfileType.get(profileTypeId);
        }
        if (profileType.getMonthStep() == -1) {
          return;
        }
        BooleanField[] months = Series.getMonths();
        for (int month = 0; month < months.length; month++) {
          BooleanField field = months[month];
          if (series.get(field)) {
            for (int i = 1; i < 12; i++) {
              int id = ((i + month) % 12) + 1;
              boolean value = (i % profileType.getMonthStep()) == 0;
              repository.update(series.getKey(), Series.getField(id), value);
            }
            return;
          }
        }
      }
    });
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
  }
}
