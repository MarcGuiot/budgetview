package org.designup.picsou.importer.ofx;

public interface OfxFunctor {
  void processHeader(String key, String value);

  void enterTag(String tag);

  void leaveTag(String tag);

  void processTag(String tag, String content);

  void end();
}
