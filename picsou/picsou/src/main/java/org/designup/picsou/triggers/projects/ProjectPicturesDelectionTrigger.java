package org.designup.picsou.triggers.projects;

import org.designup.picsou.model.Picture;
import org.designup.picsou.model.Project;
import org.designup.picsou.model.ProjectItem;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobMatchers;

import java.util.HashSet;
import java.util.Set;

import static org.globsframework.model.utils.GlobMatchers.fieldIn;
import static org.globsframework.model.utils.GlobMatchers.not;

public class ProjectPicturesDelectionTrigger implements ChangeSetListener {
  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (changeSet.containsDeletions(Project.TYPE) || changeSet.containsDeletions(ProjectItem.TYPE)) {
      deleteUnusedPictures(repository);
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(Project.TYPE) || changedTypes.contains(ProjectItem.TYPE)) {
      deleteUnusedPictures(repository);
    }
  }

  private void deleteUnusedPictures(GlobRepository repository) {
    Set<Integer> usedPictureIds = new HashSet<Integer>();
    usedPictureIds.addAll(repository.getAll(Project.TYPE).getValueSet(Project.PICTURE));
    usedPictureIds.addAll(repository.getAll(ProjectItem.TYPE).getValueSet(ProjectItem.PICTURE));
    usedPictureIds.remove(null);
    repository.delete(Picture.TYPE, not(fieldIn(Picture.ID, usedPictureIds)));
  }
}