package org.globsframework.wicket.table.columns;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.link.Link;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.MutableFieldValues;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.wicket.utils.AlertModifier;
import org.globsframework.wicket.utils.OnClickConfirmationModifier;

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
