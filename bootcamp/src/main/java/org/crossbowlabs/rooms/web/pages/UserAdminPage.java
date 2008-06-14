package org.globsframework.rooms.web.pages;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.Model;
import org.globsframework.rooms.model.PersistenceManager;
import org.globsframework.rooms.model.User;
import org.globsframework.rooms.model.Room;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class UserAdminPage extends Skin {
  private String userNameFilter;
  private WebMarkupContainer userTable;

  public UserAdminPage() {
    WebMarkupContainer rootTable = new WebMarkupContainer("FilterUser");
    ResourceBundle resourceBundle = ResourceBundle.getBundle("translation/rooms");
    selectAndFilter(rootTable, resourceBundle);
    createTableView(rootTable, resourceBundle);
    add(rootTable);
    createNewUser();
  }

  private void createTableView(WebMarkupContainer rootTable, ResourceBundle resourceBundle) {
    userTable = new WebMarkupContainer("UserTable");
    userTable.setOutputMarkupId(true);
    rootTable.add(userTable);
    Label label = new Label("login", resourceBundle.getString("login"));
    userTable.add(label);
    ListView item = new ListView("ShowRooms", new UserModel()) {

      protected void populateItem(ListItem listItem) {
        User user = (User) listItem.getModelObject();
        final Integer userId = user.getId();
        listItem.add(new Label("login", user.getLogin()));
        listItem.add(new Label("firstName", user.getFirstName()));
        listItem.add(new Label("lastName", user.getLastName()));
        listItem.add(new Label("mail", user.getMail()));
        WebMarkupContainer remove = new WebMarkupContainer("remove");
        listItem.add(remove);
        remove.add(new Link("removeLink") {
          public void onClick() {
            PersistenceManager instance = PersistenceManager.getInstance();
            Session session = instance.getNewSession();
            Transaction transaction = session.beginTransaction();
            try {
              session.delete(session.get(User.class, userId));
            } finally {
              transaction.commit();
            }
          }
        });
      }
    };
    userTable.add(item);
  }

  private void selectAndFilter(WebMarkupContainer select, ResourceBundle bundle) {
    Label label = new Label("labelForFilterName", bundle.getString("userNameFilter"));
    select.add(label);
    final TextField userFilter = new TextField("UserNameFilter", new Model());
    select.add(userFilter);
    userFilter.add(new OnChangeAjaxBehavior() {

      protected void onUpdate(AjaxRequestTarget ajaxRequestTarget) {
        userNameFilter = userFilter.getModelObjectAsString();
        ajaxRequestTarget.addComponent(userTable);
      }
    });
  }

  private void createNewUser() {
    WebMarkupContainer createUser = new WebMarkupContainer("CreateUser");
    final TextField login = new TextField("login", new Model(""));
    final TextField firstName = new TextField("firstName", new Model(""));
    final TextField lastName = new TextField("lastName", new Model(""));
    final TextField mail = new TextField("mail", new Model(""));
    Form form = new Form("userForm") {
      protected void onSubmit() {
        Session session = PersistenceManager.getInstance().getNewSession();
        Transaction transaction = session.beginTransaction();
        try {
          transaction.begin();
          User user = new User(login.getModelObjectAsString(),
                               firstName.getModelObjectAsString(),
                               lastName.getModelObjectAsString(),
                               mail.getModelObjectAsString());
          session.save(user);
        } finally {
          transaction.commit();
          session.close();
        }
      }
    };
    createUser.add(form);
    form.add(login);
    form.add(firstName);
    form.add(lastName);
    form.add(mail);
    ResourceBundle resourceBundle = ResourceBundle.getBundle("translation/rooms");
    form.add(new Label("loginLabel", resourceBundle.getString("login")));
    form.add(new Label("firstNameLabel", resourceBundle.getString("firstName")));
    form.add(new Label("lastNameLabel", resourceBundle.getString("lastName")));
    form.add(new Label("mailLabel", resourceBundle.getString("mail")));
    add(createUser);
  }

  class UserModel extends Model {


    public Object getObject() {
      Session session = PersistenceManager.getInstance().getNewSession();
      List<User> filteredResult = null;
      try {
        List<User> list = session.createCriteria(User.class).list();
        if (userNameFilter == null || "".equals(userNameFilter)) {
          return list;
        }
        filteredResult = new ArrayList<User>();
        for (User o : list) {
          if (o.getFirstName().contains(userNameFilter) ||
              o.getLastName().contains(userNameFilter) ||
              o.getLogin().contains(userNameFilter)) {
            filteredResult.add(o);
          }
        }
      } finally {
        session.close();
      }
      return filteredResult;
    }
  }
}
