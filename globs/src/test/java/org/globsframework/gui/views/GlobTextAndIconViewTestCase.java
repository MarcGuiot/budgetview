package org.globsframework.gui.views;

import org.globsframework.gui.DummyObjectIconifier;
import org.globsframework.metamodel.DummyObject;
import org.globsframework.metamodel.Field;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobListStringifier;

public abstract class GlobTextAndIconViewTestCase extends GlobTextViewTestCase {

  protected abstract AbstractGlobTextAndIconView initView(GlobRepository repository, GlobListStringifier stringifier);

  protected abstract AbstractGlobTextAndIconView initView(GlobRepository repository, Field field);

  public void testIconifier() throws Exception {
    DummyObjectIconifier iconifier = new DummyObjectIconifier();
    AbstractGlobTextAndIconView view = initView(repository, DummyObject.NAME)
      .setIconifier(iconifier);
    TextComponent component = createComponent(view);

    assertThat(component.iconEquals(DummyObjectIconifier.EMPTY_ICON));

    selectionService.select(glob1);
    assertThat(component.iconEquals(DummyObjectIconifier.ID1_ICON));

    selectionService.select(glob2);
    assertThat(component.iconEquals(DummyObjectIconifier.ID2_ICON));

    selectionService.select(new GlobList(glob1, glob2) , DummyObject.TYPE);
    assertThat(component.iconEquals(DummyObjectIconifier.MULTI_ICON));

    selectionService.clear(DummyObject.TYPE);
    assertThat(component.iconEquals(DummyObjectIconifier.EMPTY_ICON));

    selectionService.select(glob1);
    assertThat(component.iconEquals(DummyObjectIconifier.ID1_ICON));

    Glob glob3 = repository.create(key3);
    selectionService.select(glob3);
    assertThat(component.iconEquals(null));
  }
}
