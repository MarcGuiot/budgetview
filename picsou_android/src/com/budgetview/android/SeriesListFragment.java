package com.budgetview.android;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.budgetview.android.components.GaugeView;
import com.budgetview.shared.model.SeriesEntity;
import com.budgetview.shared.model.SeriesValues;
import com.budgetview.shared.utils.AmountFormat;
import com.budgetview.shared.utils.SeriesValuesComparator;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;

import static org.globsframework.model.utils.GlobMatchers.*;

public class SeriesListFragment extends Fragment {
  public static String MONTH_PARAMETER = "com.budgetview.seriesListActivity.parameters.month";
  public static String BUDGET_AREA_PARAMETER = "com.budgetview.seriesListActivity.parameters.series";

  private Integer monthId;
  private Integer budgetAreaId;

  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    Bundle args = getArguments();
    monthId = args.getInt(MONTH_PARAMETER, -1);
    budgetAreaId = args.getInt(BUDGET_AREA_PARAMETER, -1);

    View view = inflater.inflate(R.layout.series_list, container, false);

    ListView list = (ListView)view.findViewById(R.id.seriesList);
    list.setAdapter(new SeriesListAdapter(inflater));

    return view;
  }

  private class SeriesListAdapter extends BaseAdapter {

    private GlobList seriesValuesList;
    private LayoutInflater inflater;

    private SeriesListAdapter(LayoutInflater inflater) {
      this.inflater = inflater;
      App app = (App)getActivity().getApplication();
      seriesValuesList =
        app.getRepository()
          .getAll(SeriesValues.TYPE,
                  and(
                    fieldEquals(SeriesValues.MONTH, monthId),
                    fieldEquals(SeriesValues.BUDGET_AREA, budgetAreaId)
                  ))
          .sort(new SeriesValuesComparator());
    }

    public int getCount() {
      return seriesValuesList.size();
    }

    public Object getItem(int i) {
      return seriesValuesList.get(i);
    }

    public long getItemId(int i) {
      return getItem(i).hashCode();
    }

    public View getView(int i, View previousView, ViewGroup parent) {

      View view = previousView;
      if (view == null) {
        view = inflater.inflate(R.layout.series_block, parent, false);
      }

      App app = (App)getActivity().getApplication();
      final Glob seriesValues = seriesValuesList.get(i);
      Glob seriesEntity = app.getRepository().findLinkTarget(seriesValues, SeriesValues.SERIES_ENTITY);

      setText(view, R.id.seriesLabel, seriesEntity.get(SeriesEntity.NAME));
      setText(view, R.id.seriesActual, seriesValues.get(SeriesValues.AMOUNT));
      setText(view, R.id.seriesPlanned, seriesValues.get(SeriesValues.PLANNED_AMOUNT));

      GaugeView gaugeView = (GaugeView)view.findViewById(R.id.seriesGauge);
      gaugeView.getModel()
        .setValues(seriesValues.get(SeriesValues.AMOUNT, 0.00),
                   seriesValues.get(SeriesValues.PLANNED_AMOUNT, 0.00),
                   seriesValues.get(SeriesValues.OVERRUN_AMOUNT, 0.00),
                   seriesValues.get(SeriesValues.REMAINING_AMOUNT, 0.00),
                   "", false);

      view.setOnClickListener(new View.OnClickListener() {
        public void onClick(View view) {
          Intent intent = new Intent(getActivity(), TransactionListActivity.class);
          intent.putExtra(TransactionListActivity.MONTH_PARAMETER, seriesValues.get(SeriesValues.MONTH));
          intent.putExtra(TransactionListActivity.SERIES_ENTITY_PARAMETER, seriesValues.get(SeriesValues.SERIES_ENTITY));
          startActivity(intent);
        }
      });

      return view;
    }

    private void setText(View view, int textId, Double value) {
      String text = (value == null) ? "-" : AmountFormat.DECIMAL_FORMAT.format(value);
      setText(view, textId, text);
    }

    private void setText(View view, int textId, String text) {
      TextView textView = (TextView)view.findViewById(textId);
      textView.setText(text);
    }
  }
}
