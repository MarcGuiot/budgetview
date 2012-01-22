package org.designup.picsou.gui.feedback;

import org.designup.picsou.gui.config.ConfigService;
import org.designup.picsou.model.SignpostSectionType;
import org.designup.picsou.model.SignpostStatus;
import org.designup.picsou.model.User;
import org.designup.picsou.model.UserPreferences;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class UserProgressInfoSender {

  private static BooleanField[] FIELDS_TO_SEND = new BooleanField[]{
    SignpostStatus.IMPORT_DONE,
    SignpostStatus.CATEGORIZATION_SELECTION_DONE,
    SignpostStatus.CATEGORIZATION_AREA_SELECTION_DONE,
    SignpostStatus.FIRST_CATEGORIZATION_DONE,
    SignpostStatus.CATEGORIZATION_SKIPPED,
    SignpostStatus.GOTO_BUDGET_SHOWN
  };

  public static void send(GlobRepository repository, Directory directory) {
    Glob userPrefs = repository.find(UserPreferences.KEY);
    if (userPrefs == null){
      return;
    }
    int exitCount = userPrefs.get(UserPreferences.EXIT_COUNT, 0);

    if (!repository.get(User.KEY).isTrue(User.CONNECTED) || exitCount >= 3) {
      return;
    }

    StringBuilder builder = new StringBuilder();
    builder.append("use: ").append(exitCount);
    builder.append(", initialStepsCompleted: ").append(SignpostSectionType.isAllCompleted(repository));
    Glob status = repository.get(SignpostStatus.KEY);
    for (BooleanField field : FIELDS_TO_SEND) {
      builder.append(", " + field.getName() + ": " + status.get(field, Boolean.FALSE));
    }
    try {
      directory.get(ConfigService.class).sendUsageData(builder.toString());
    }
    catch (Exception exception) {
      // Ignore
    }
  }
}
