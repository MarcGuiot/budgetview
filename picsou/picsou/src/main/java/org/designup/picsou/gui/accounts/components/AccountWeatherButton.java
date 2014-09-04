package org.designup.picsou.gui.accounts.components;

import org.designup.picsou.gui.components.tips.DetailsTip;
import org.designup.picsou.gui.model.MainAccountWeather;
import org.designup.picsou.gui.model.WeatherType;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.model.Account;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.splits.PanelBuilder;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.KeyChangeListener;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class AccountWeatherButton implements Disposable {

  public static final Icon SUNNY_ICON;
  public static final Icon CLOUDY_ICON;
  public static final Icon RAINY_ICON;

  static {
    SUNNY_ICON = Gui.IMAGE_LOCATOR.get("dashboard/sunny_small.png");
    CLOUDY_ICON = Gui.IMAGE_LOCATOR.get("dashboard/cloudy_small.png");
    RAINY_ICON = Gui.IMAGE_LOCATOR.get("dashboard/rainy_small.png");
  }

  private GlobRepository repository;
  private final Directory directory;
  private final Key accountWeatherKey;
  private KeyChangeListener updater;
  private JButton weatherButton;

  public static AccountWeatherButton create(Key accountKey, PanelBuilder builder, String componentName, GlobRepository repository, Directory directory) {
    AccountWeatherButton button = new AccountWeatherButton(accountKey, repository, directory);
    button.register(builder, componentName);
    builder.addDisposable(button);
    return button;
  }

  private AccountWeatherButton(Key accountKey, final GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.directory = directory;
    this.accountWeatherKey = Key.create(MainAccountWeather.TYPE, accountKey.get(Account.ID));
    this.updater = new KeyChangeListener(accountWeatherKey) {
      public void update() {
        Glob weather = repository.find(accountWeatherKey);
        if (weather == null) {
          weatherButton.setIcon(null);
          weatherButton.setToolTipText(null);
        }
        else {
          WeatherType weatherType = WeatherType.get(weather.get(MainAccountWeather.WEATHER));
          weatherButton.setIcon(getIcon(weatherType));
          weatherButton.setToolTipText(getTooltipText(weatherType));
        }
      }
    };
  }

  private String getTooltipText(WeatherType weatherType) {
    return Lang.get("accountView.status." + weatherType.getName() + ".tooltip");
  }

  private Icon getIcon(WeatherType weatherType) {
    switch (weatherType) {
      case SUNNY:
        return SUNNY_ICON;
      case CLOUDY:
        return CLOUDY_ICON;
      case RAINY:
        return RAINY_ICON;
    }
    return null;
  }

  private void register(PanelBuilder builder, String componentName) {
    weatherButton = new JButton(new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        DetailsTip tip = new DetailsTip(weatherButton, weatherButton.getToolTipText(), directory);
        tip.show();
      }
    });
    builder.add(componentName, weatherButton);
    repository.addChangeListener(updater);
    updater.update();
  }

  public void dispose() {
    repository.removeChangeListener(updater);
    updater = null;
    repository = null;
  }
}
