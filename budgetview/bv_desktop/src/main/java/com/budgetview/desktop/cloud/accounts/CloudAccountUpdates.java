package com.budgetview.desktop.cloud.accounts;

import java.util.ArrayList;
import java.util.List;

public class CloudAccountUpdates {

  public static Builder build() {
    return new Builder();
  }

  public interface Visitor {
    void visit(int connectionId, int accountId, boolean enabled);

  }

  public static class Builder {
    private CloudAccountUpdates updates;

    private Builder() {
      this.updates = new CloudAccountUpdates();
    }

    public Builder add(int connectionId, int accountId, boolean enabled) {
      updates.list.add(new Update(connectionId, accountId, enabled));
      return this;
    }

    public CloudAccountUpdates get() {
      return updates;
    }
  }

  private static class Update {
    private final int connectionId;
    private final int accountId;

    private boolean enabled;
    public Update(int connectionId, int accountId, boolean enabled) {
      this.connectionId = connectionId;
      this.accountId = accountId;
      this.enabled = enabled;
    }

  }

  private List<Update> list = new ArrayList<Update>();

  public boolean isEmpty() {
    return list.isEmpty();
  }

  public void visit(Visitor visitor) {
    for (Update update : list) {
      visitor.visit(update.connectionId, update.accountId, update.enabled);
    }
  }
}
