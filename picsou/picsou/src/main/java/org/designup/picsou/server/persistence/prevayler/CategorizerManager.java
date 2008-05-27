package org.designup.picsou.server.persistence.prevayler;

import org.designup.picsou.server.session.Persistence;

import java.util.List;

public interface CategorizerManager {
  List<Persistence.CategoryInfo> getAssociatedCategory(List<String> infos, Integer userId);

  void registerCategory(String info, int categoryId, Integer userId);

  void close();
}
