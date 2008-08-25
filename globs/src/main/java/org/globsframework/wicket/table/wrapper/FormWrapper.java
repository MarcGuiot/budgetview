package org.globsframework.wicket.table.wrapper;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.globsframework.wicket.table.rows.Submittable;

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