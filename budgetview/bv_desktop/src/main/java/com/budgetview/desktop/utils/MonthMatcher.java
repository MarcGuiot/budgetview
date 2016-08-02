package com.budgetview.desktop.utils;

import org.globsframework.model.utils.GlobMatcher;

import java.util.Set;

public interface MonthMatcher extends GlobMatcher {
  void filterMonths(Set<Integer> monthIds);
}
