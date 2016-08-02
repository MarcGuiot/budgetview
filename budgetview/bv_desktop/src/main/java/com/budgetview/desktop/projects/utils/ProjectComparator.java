package com.budgetview.desktop.projects.utils;

import com.budgetview.desktop.model.ProjectStat;
import com.budgetview.model.Account;
import com.budgetview.model.Project;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.Utils;

import java.util.Comparator;

public class ProjectComparator implements Comparator<Glob> {

  public static Comparator<Glob> current(GlobRepository repository) {
    return new ProjectComparator(true, ProjectStat.FIRST_MONTH, repository);
  }

  public static Comparator<Glob> past(GlobRepository repository) {
    return new ProjectComparator(false, ProjectStat.LAST_MONTH, repository);
  }

  private int modifier = 1;
  private final IntegerField field;
  private GlobRepository repository;

  public ProjectComparator(boolean ascending, IntegerField field, GlobRepository repository) {
    this.modifier = ascending ? 1 : -1;
    this.field = field;
    this.repository = repository;
  }

  public int compare(Glob project1, Glob project2) {
    if (project1 == null && project2 == null) {
      return 0;
    }
    if (project1 == null) {
      return -1 * modifier;
    }
    if (project2 == null) {
      return 1 * modifier;
    }

    Glob stat1 = repository.find(Key.create(ProjectStat.TYPE, project1.get(Project.ID)));
    Integer month1 = stat1 != null ? stat1.get(field) : null;
    Glob stat2 = repository.find(Key.create(ProjectStat.TYPE, project2.get(Project.ID)));
    Integer month2 = stat2 != null ? stat2.get(field) : null;
    if (month1 == null && month2 == null) {
      return 0;
    }
    if (month1 == null) {
      return -1 * modifier;
    }
    if (month2 == null) {
      return 1 * modifier;
    }
    if (month1 < month2) {
      return -1 * modifier;
    }
    if (month1 > month2) {
      return 1 * modifier;
    }

    int nameDiff = Utils.compare(project1.get(Project.ID), project2.get(Project.ID));
    if (nameDiff != 0) {
      return nameDiff * modifier;
    }

    return (project2.get(Account.ID) - project1.get(Account.ID)) * modifier;
  }
}
