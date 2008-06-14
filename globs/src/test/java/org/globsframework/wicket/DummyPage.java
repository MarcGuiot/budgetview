package org.globsframework.wicket;

import org.globsframework.model.GlobRepository;
import wicket.Component;
import wicket.markup.html.panel.FeedbackPanel;

public class DummyPage extends GlobPage {
  public static final String COMPONENT_ID = "component";
  public static final String FEEDBACK_COMPONENT_ID = "feedback";

  private static ComponentFactory componentFactory;

  public DummyPage() {
    FeedbackPanel feedbackPanel = new FeedbackPanel(FEEDBACK_COMPONENT_ID);
    feedbackPanel.setOutputMarkupId(true);
    add(feedbackPanel);

    Component component = componentFactory.create(COMPONENT_ID, getRepositoryLoader());
    component.setRenderBodyOnly(true);
    add(component);
  }

  public static void reset() {
    componentFactory = null;
  }

  public static void setFactory(ComponentFactory factory) {
    componentFactory = factory;
  }

  protected GlobRepositoryLoader newGlobRepositoryLoader() {
    return new GlobRepositoryLoader() {
      public GlobRepository getRepository() {
        DummyApplication application = (DummyApplication)getApplication();
        return application.getRepository();
      }

      public void detach() {
      }
    };
  }
}
