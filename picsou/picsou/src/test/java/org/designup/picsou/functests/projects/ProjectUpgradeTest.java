package org.designup.picsou.functests.projects;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;

public class ProjectUpgradeTest extends LoggedInFunctionalTestCase {
  public void test() throws Exception {
    fail("tbd - revoir les cas de UpgradeTriggerTest");
    fail("tbd - cas où on a affecté des opérations directement à l'enveloppe niveau projet");
    fail("tbd - version pré-été 2013");
//  private void updateProjetItemSeries(GlobRepository repository) {
//    for (Glob projectItem : repository.getAll(ProjectItem.TYPE)) {
//      if (projectItem.get(ProjectItem.SERIES) == null) {
//        Glob project = repository.findLinkTarget(projectItem, ProjectItem.PROJECT);
//        if (project != null) {
//          repository.update(projectItem.getKey(), ProjectItem.SERIES, project.get(Project.SERIES_GROUP));
//        }
//      }
//    }
//  }

  }
}
