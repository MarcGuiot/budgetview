package org.designup.picsou.gui.help;

public interface HelpSource {
  String getTitle(String ref);
  String getContent(String ref);
  String findContent(String ref);
}
