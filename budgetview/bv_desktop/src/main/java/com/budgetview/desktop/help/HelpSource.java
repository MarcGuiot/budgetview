package com.budgetview.desktop.help;

public interface HelpSource {
  String getTitle(String ref);
  String findContent(String ref);
}
