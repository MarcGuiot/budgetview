package com.budgetview.gui.series;

import com.budgetview.model.ProfileType;
import com.budgetview.model.Series;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.model.*;

import java.util.Collections;
import java.util.Set;

public class ProfileTypeSeriesTrigger implements ChangeSetListener {
  private UserMonth userMonth;

  public interface UserMonth {
    Set<Integer> getMonthWithTransaction();
  }

  public static UserMonth NULL = new UserMonth() {
    public Set<Integer> getMonthWithTransaction() {
      return Collections.emptySet();
    }
  };

  public ProfileTypeSeriesTrigger(UserMonth userMonth) {
    this.userMonth = userMonth;
  }

  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    changeSet.safeVisit(Series.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        ProfileType profileType = ProfileType.get(values.get(Series.PROFILE_TYPE));
        if (profileType == ProfileType.IRREGULAR ||
            profileType == ProfileType.EVERY_MONTH) {
          for (BooleanField monthField : Series.getMonths()) {
            repository.update(key, monthField, true);
          }
          return;
        }
        updateProfileType(repository.get(key), true);
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        ProfileType profileType = ProfileType.get(repository.get(key).get(Series.PROFILE_TYPE));
        if (profileType == ProfileType.IRREGULAR ||
            profileType == ProfileType.EVERY_MONTH) {
          for (BooleanField field : Series.getMonths()) {
            repository.update(key, field, true);
          }
          return;
        }

        updateProfileType(repository.get(key), values.contains(Series.PROFILE_TYPE));
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
      }

      private void updateProfileType(Glob series, boolean profileTypeChange) {
        boolean atLeatOneIsSelected = false;

        if (profileTypeChange) {
          tryUpdate(repository, series);
        }

        for (BooleanField field : Series.getMonths()) {
          if (series.isTrue(field)) {
            atLeatOneIsSelected = true;
            break;
          }
        }
        if (!atLeatOneIsSelected) {
          repository.update(series.getKey(), Series.JANUARY, true);
        }
        Integer profileTypeId = series.get(Series.PROFILE_TYPE);
        if (profileTypeId.equals(ProfileType.CUSTOM.getId())) {
          return;
        }
        ProfileType profileType;
        if (profileTypeId == null) {
          profileType = ProfileType.IRREGULAR;
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
          if (series.isTrue(field)) {
            for (int i = 1; i < 12; i++) {
              int id = ((i + month) % 12) + 1;
              boolean value = (i % profileType.getMonthStep()) == 0;
              repository.update(series.getKey(), Series.getMonthField(id), value);
            }
            return;
          }
        }
      }
    });
  }

  private void tryUpdate(GlobRepository repository, Glob series) {
    Set<Integer> months = userMonth.getMonthWithTransaction();
    if (months.isEmpty()) {
      return;
    }
    for (BooleanField field : Series.getMonths()) {
      repository.update(series.getKey(), field, false);
    }
    if (series.get(Series.PROFILE_TYPE).equals(ProfileType.CUSTOM.getId())) {
      for (Integer monthId : months) {
        repository.update(series.getKey(), Series.getMonthField(monthId), true);
      }
    }
    else {
      Integer monthId = months.iterator().next();
      repository.update(series.getKey(), Series.getMonthField(monthId), true);
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
  }
}
