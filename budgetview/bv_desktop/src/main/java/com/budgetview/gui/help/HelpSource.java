package com.budgetview.gui.help;

public interface HelpSource {
  String getTitle(String ref);
  String findContent(String ref);
}
