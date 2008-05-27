package org.crossbowlabs.globs.wicket.table.columns;

import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.Key;
import org.crossbowlabs.globs.model.MutableFieldValues;
import org.crossbowlabs.globs.model.format.DescriptionService;
import org.crossbowlabs.globs.wicket.utils.OnClickConfirmationModifier;
import org.crossbowlabs.globs.wicket.utils.AlertModifier;
import wicket.Component;
import wicket.markup.html.link.Link;

public class DeleteButtonColumn extends ButtonColumn {
  public DeleteButtonColumn(String title, String buttonLabel, String buttonIdPrefix) {
    super(title, buttonLabel, buttonIdPrefix);
  }

  public Component getComponent(String id,
                                String tableId,
                                Key key,
                                MutableFieldValues fieldValues,
                                int rowIndex,
                                Component row,
                                GlobRepository repository,
                                DescriptionService descriptionService) {

    if (!deletionEnabled(key, fieldValues, rowIndex, repository, descriptionService)) {
      final String alertMessage = getDeletionDisabledMessage(key, fieldValues, rowIndex, repository,
                                                             descriptionService);
      return new ButtonPanel(id, buttonLabel, buttonIdPrefix, this, key, fieldValues, rowIndex) {
        protected void init(Link link) {
          link.add(new AlertModifier(alertMessage));
        }
      };
    }

    final String confirmationMessage = getConfirmationMessage(key, fieldValues, rowIndex, repository,
                                                              descriptionService);
    return new ButtonPanel(id, buttonLabel, buttonIdPrefix, this, key, fieldValues, rowIndex) {
      protected void init(Link link) {
        link.add(new OnClickConfirmationModifier(confirmationMessage));
      }
    };
  }

  protected boolean deletionEnabled(Key key,
                                    MutableFieldValues fieldValues,
                                    int rowIndex,
                                    GlobRepository repository,
                                    DescriptionService descriptionService) {
    return true;
  }

  protected String getDeletionDisabledMessage(Key key,
                                              MutableFieldValues fieldValues,
                                              int rowIndex,
                                              GlobRepository repository,
                                              DescriptionService descriptionService) {
    return "Unable to delete this object";
  }

  protected String getConfirmationMessage(Key key,
                                          MutableFieldValues fieldValues,
                                          int rowIndex,
                                          GlobRepository repository,
                                          DescriptionService descriptionService) {
    return "Do you want to delete this object?";
  }

  public void onSubmit(Key key,
                       MutableFieldValues fieldValues,
                       int rowIndex,
                       GlobRepository repository,
                       DescriptionService descriptionService) {
    repository.delete(key);
  }
}
