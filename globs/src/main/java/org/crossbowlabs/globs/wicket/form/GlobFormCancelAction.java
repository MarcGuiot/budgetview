package org.crossbowlabs.globs.wicket.form;

import java.io.Serializable;
import wicket.ajax.AjaxRequestTarget;

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
