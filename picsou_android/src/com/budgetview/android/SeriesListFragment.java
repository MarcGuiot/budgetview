package com.budgetview.android;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import com.budgetview.android.utils.SectionHeaderBlock;
import com.budgetview.shared.model.BudgetAreaEntity;
import com.budgetview.shared.model.BudgetAreaValues;
import com.budgetview.shared.model.SeriesValues;
import com.budgetview.shared.utils.SeriesValuesComparator;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.Key;

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

    AmountsBlockView amountsBlock = (AmountsBlockView)view.findViewById(R.id.budget_area_amounts);
    App app = (App)getActivity().getApplication();
    Glob budgetAreaEntity = app.getRepository().find(Key.create(BudgetAreaEntity.TYPE, budgetAreaId));
    Glob budgetAreaValues = app.getRepository().find(Key.create(BudgetAreaValues.BUDGET_AREA, budgetAreaId,
                                                                 BudgetAreaValues.MONTH, monthId));
    amountsBlock.update(budgetAreaValues,
                        BudgetAreaValues.ACTUAL,
                        BudgetAreaValues.INITIALLY_PLANNED,
                        BudgetAreaValues.OVERRUN,
                        BudgetAreaValues.REMAINDER,
                        budgetAreaEntity.get(BudgetAreaEntity.INVERT_AMOUNTS));

    ListView list = (ListView)view.findViewById(R.id.seriesList);
    list.setAdapter(new SeriesListAdapter(inflater));

    EmptyListView emptyView = (EmptyListView)view.findViewById(R.id.emptyListView);
    emptyView.update(R.string.seriesListEmptyMessage);
    list.setEmptyView(emptyView);

    return view;
  }

  private class SeriesListAdapter extends BaseAdapter {

    private GlobList seriesValuesList;
    private LayoutInflater inflater;
    private SectionHeaderBlock headerBlock;

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
      if (seriesValuesList.isEmpty()) {
        return 0;
      }
      return seriesValuesList.size() + 1;
    }

    public Object getItem(int index) {
      if (index == 0) {
        return "Header";
      }
      return seriesValuesList.get(index - 1);
    }

    public long getItemId(int i) {
      return getItem(i).hashCode();
    }

    public View getView(int index, View previousView, ViewGroup parent) {

      if (index == 0) {
        if (headerBlock == null) {
          headerBlock = new SectionHeaderBlock(R.string.seriesListSectionLabel, getResources());
        }
        return headerBlock.getView(inflater, previousView, parent);
      }

      View view = previousView;
      if (view == null || !(view instanceof SeriesBlockView)) {
        view = new SeriesBlockView(getActivity(), null);
      }

      final App app = (App)getActivity().getApplication();
      final Glob seriesValues = seriesValuesList.get(index - 1);
      Glob seriesEntity = app.getRepository().findLinkTarget(seriesValues, SeriesValues.SERIES_ENTITY);

      ((SeriesBlockView)view).update(monthId, seriesEntity, seriesValues, getActivity());

      return view;
    }
  }
}
