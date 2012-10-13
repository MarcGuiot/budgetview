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
import com.budgetview.shared.model.BudgetAreaEntity;
import com.budgetview.shared.model.BudgetAreaValues;
import com.budgetview.shared.utils.AmountFormat;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.utils.GlobFieldComparator;

import static org.globsframework.model.utils.GlobMatchers.fieldEquals;

public class BudgetOverviewFragment extends Fragment {

  private int monthId;

  public BudgetOverviewFragment(int monthId) {
    this.monthId = monthId;
  }

  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.budget_overview, container, false);

    TextView text = (TextView)view.findViewById(R.id.overviewMonthLabel);
    text.setText(Text.monthToString(monthId, getResources()));

    ListView list = (ListView)view.findViewById(R.id.budgetAreaList);
    list.setAdapter(new BudgetAreaListAdapter(inflater));

    return view;
  }

  private class BudgetAreaListAdapter extends BaseAdapter {

    private GlobList budgetAreaValues;
    private LayoutInflater inflater;

    private BudgetAreaListAdapter(LayoutInflater inflater) {
      this.inflater = inflater;
      App app = (App)getActivity().getApplication();
      this.budgetAreaValues =
        app.getRepository()
          .getAll(BudgetAreaValues.TYPE, fieldEquals(BudgetAreaValues.MONTH, monthId))
          .sort(new GlobFieldComparator(BudgetAreaValues.BUDGET_AREA));
    }

    public int getCount() {
      return budgetAreaValues.size();
    }

    public Object getItem(int i) {
      return budgetAreaValues.get(i);
    }

    public long getItemId(int i) {
      return getItem(i).hashCode();
    }

    public View getView(int i, View previousView, ViewGroup parent) {

      View view = previousView;
      if (view == null) {
        view = inflater.inflate(R.layout.budget_area, parent, false);
      }

      final Glob values = budgetAreaValues.get(i);
      App app = (App)getActivity().getApplication();
      Glob entity = app.getRepository().findLinkTarget(values, BudgetAreaValues.BUDGET_AREA);

      view.setOnClickListener(new View.OnClickListener() {
        public void onClick(View view) {
          Intent intent = new Intent(getActivity(), SeriesListActivity.class);
          intent.putExtra(SeriesListActivity.MONTH_PARAMETER, values.get(BudgetAreaValues.MONTH));
          intent.putExtra(SeriesListActivity.BUDGET_AREA_PARAMETER, values.get(BudgetAreaValues.BUDGET_AREA));
          startActivity(intent);
        }
      });

      setText(view, R.id.overviewMonthLabel, entity.get(BudgetAreaEntity.LABEL));
      setText(view, R.id.budgetAreaActual, values.get(BudgetAreaValues.ACTUAL));
      setText(view, R.id.budgetAreaPlanned, values.get(BudgetAreaValues.INITIALLY_PLANNED));

      GaugeView gaugeView = (GaugeView)view.findViewById(R.id.budgetAreaGauge);
      gaugeView.getModel()
        .setValues(values.get(BudgetAreaValues.ACTUAL),
                   values.get(BudgetAreaValues.INITIALLY_PLANNED),
                   values.get(BudgetAreaValues.OVERRUN),
                   values.get(BudgetAreaValues.REMAINDER),
                   "", false);

      return view;
    }

    private void setText(View view, int textId, Double value) {
      setText(view, textId, AmountFormat.DECIMAL_FORMAT.format(value));
    }

    private void setText(View view, int textId, String text) {
      TextView textView = (TextView)view.findViewById(textId);
      textView.setText(text);
    }
  }
}
