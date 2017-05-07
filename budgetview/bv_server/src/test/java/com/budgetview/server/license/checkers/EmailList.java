package com.budgetview.server.license.checkers;

import org.junit.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EmailList {
  private List<Email> emailList = new ArrayList<Email>();

  public EmailList(List<Email> emails) {
    this.emailList = emails;
  }

  public void checkContains(String to, String... contentParts) {
    for (Email email : emailList) {
      if (email.sentTo(to)) {
        email.checkContainsAny(contentParts);
        return;
      }
    }
    Assert.fail("Email to " + to + " with " + Arrays.toString(contentParts) + " not found in list:\n" + toString());
  }

  public Email get(String to) {
    Email result = null;
    for (Email email : emailList) {
      if (to.equalsIgnoreCase(email.to())) {
        if (result != null) {
          Assert.fail("Several emails received for " + to + " in list:\n" + toString());
        }
        result = email;
      }
    }
    if (result == null) {
      Assert.fail("No email received for " + to + " in list:\n" + toString());
    }
    return result;
  }

  public String toString() {
    if (emailList.isEmpty()) {
      return "<empty>";
    }
    StringBuilder list = new StringBuilder();
    int count = 1;
    for (Email email : emailList) {
      list.append(count++).append("| ").append(email).append("\n");
    }
    return list.toString();
  }
}
