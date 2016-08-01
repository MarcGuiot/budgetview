package com.budgetview.triggers;

import com.budgetview.gui.description.Formatting;
import com.budgetview.gui.description.PicsouDescriptionService;
import com.budgetview.model.NumericDateType;
import com.budgetview.model.TextDateType;
import com.budgetview.model.UserPreferences;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.utils.directory.Directory;

import java.util.Set;

public class DateFormatTrigger implements ChangeSetListener {

  private PicsouDescriptionService descriptionService;

  public DateFormatTrigger(Directory directory) {
    descriptionService = (PicsouDescriptionService)directory.get(DescriptionService.class);
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (changeSet.containsChanges(UserPreferences.KEY)) {
      update(repository);
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(UserPreferences.TYPE)) {
      update(repository);
    }
  }

  private void update(GlobRepository repository) {
    Glob preferences = repository.find(UserPreferences.KEY);
    if (preferences == null) {
      return;
    }
    if (preferences.get(UserPreferences.TEXT_DATE_TYPE) == null) {
      repository.update(UserPreferences.KEY,
                        UserPreferences.TEXT_DATE_TYPE,
                        TextDateType.getDefault().getId());
    }
    TextDateType textDate = TextDateType.get(preferences.get(UserPreferences.TEXT_DATE_TYPE));
    
    if (preferences.get(UserPreferences.NUMERIC_DATE_TYPE) == null) {
      repository.update(UserPreferences.KEY,
                        UserPreferences.NUMERIC_DATE_TYPE,
                        NumericDateType.getDefault().getId());
    }
    NumericDateType numericDate = NumericDateType.get(preferences.get(UserPreferences.NUMERIC_DATE_TYPE));

    Formatting.update(textDate, numericDate);
    descriptionService.updateFormats();
  }

}
