package org.globsframework.wicket.form;

import wicket.ajax.AjaxRequestTarget;

import java.io.Serializable;

public interface GlobFormCancelAction extends Serializable {
  String getName();

  void run(GlobForm form, AjaxRequestTarget target);

  GlobFormCancelAction NULL = new GlobFormCancelAction() {
    public String getName() {
      return "Cancel";
    }

    public void run(GlobForm form, AjaxRequestTarget target) {
    }
  };
}
