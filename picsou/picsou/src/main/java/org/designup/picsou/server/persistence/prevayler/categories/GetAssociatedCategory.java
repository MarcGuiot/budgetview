package org.designup.picsou.server.persistence.prevayler.categories;

import org.prevayler.Query;

import java.util.Date;
import java.util.List;

public class GetAssociatedCategory implements Query {
  private List<String> infos;

  public GetAssociatedCategory(List<String> infos) {
    this.infos = infos;
  }

  public Object query(Object prevalentSystem, Date executionTime) throws Exception {
    PCategoriesData userData = (PCategoriesData) prevalentSystem;
    return userData.getAssociatedCategory(infos);
  }
}
