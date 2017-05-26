package guepardoapps.mediamirror.customadapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.Calendar;

import guepardoapps.library.lucahome.R;
import guepardoapps.library.lucahome.common.dto.MenuDto;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.toolset.common.classes.SerializableList;

public class MenuListAdapter extends BaseAdapter {

    private static final String TAG = MenuListAdapter.class.getSimpleName();
    private SerializableList<MenuDto> _menu;
    private static LayoutInflater _inflater = null;

    public MenuListAdapter(
            @NonNull Context context,
            @NonNull SerializableList<MenuDto> menu) {
        LucaHomeLogger logger = new LucaHomeLogger(TAG);
        logger.Debug("Created...");

        int dayOfMonth = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        int startIndex = -1;
        for (int index = 0; index < menu.getSize(); index++) {
            if (menu.getValue(index).GetDay() == dayOfMonth) {
                startIndex = index;
                break;
            }
        }
        _menu = menu;

        if (startIndex != -1) {
            SerializableList<MenuDto> sortedList = new SerializableList<>();
            int selectedIndex = startIndex;
            for (int index = 0; index < menu.getSize(); index++) {
                if (selectedIndex >= menu.getSize()) {
                    selectedIndex = selectedIndex - menu.getSize();
                }
                sortedList.addValue(menu.getValue(selectedIndex));
                selectedIndex++;
            }
            _menu = sortedList;
        }

        _inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return _menu.getSize();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private class Holder {
        private TextView _title;
        private TextView _description;
        private TextView _weekday;
        private TextView _date;
        private ImageButton _random;
        private ImageButton _update;
        private ImageButton _clear;
    }

    @SuppressLint({"InflateParams", "ViewHolder"})
    @Override
    public View getView(final int index, View convertView, ViewGroup parent) {
        Holder holder = new Holder();
        View rowView = _inflater.inflate(R.layout.list_menu_item, null);

        holder._title = (TextView) rowView.findViewById(R.id.menu_item_title);
        holder._title.setText(String.valueOf(_menu.getValue(index).GetTitle()));

        holder._description = (TextView) rowView.findViewById(R.id.menu_item_description);
        holder._description.setText(String.valueOf(_menu.getValue(index).GetDescription()));

        holder._weekday = (TextView) rowView.findViewById(R.id.menu_item_weekday);
        holder._weekday.setText(_menu.getValue(index).GetWeekday());

        holder._date = (TextView) rowView.findViewById(R.id.menu_item_date);
        holder._date.setText(_menu.getValue(index).GetDate());

        holder._random = (ImageButton) rowView.findViewById(R.id.menu_item_random);
        holder._random.setVisibility(View.INVISIBLE);
        holder._random.setEnabled(false);

        holder._update = (ImageButton) rowView.findViewById(R.id.menu_item_update);
        holder._update.setVisibility(View.INVISIBLE);
        holder._update.setEnabled(false);

        holder._clear = (ImageButton) rowView.findViewById(R.id.menu_item_clear);
        holder._clear.setVisibility(View.INVISIBLE);
        holder._clear.setEnabled(false);

        return rowView;
    }
}