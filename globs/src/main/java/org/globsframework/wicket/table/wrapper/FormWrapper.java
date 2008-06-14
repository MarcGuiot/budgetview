package org.globsframework.wicket.table.wrapper;

import org.globsframework.wicket.table.rows.Submittable;
import wicket.Component;
import wicket.markup.html.form.Form;
import wicket.markup.html.panel.Panel;

public class FormWrapper extends Panel {
  public static final String CONTENT_ID = "formContent";
  private final Submittable submittable;

  public FormWrapper(String parentId, Component component, Submittable submittable) {
    super(parentId);
    this.submittable = submittable;
    add(new MyForm(component));
  }

  private class MyForm extends Form {
    MyForm(Component component) {
      super("form");
      add(component);
    }

    protected void onSubmit() {
      submittable.submit();
    }
  }
}