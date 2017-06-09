package guepardoapps.mediamirror.customadapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import guepardoapps.library.lucahome.R;
import guepardoapps.library.lucahome.common.constants.Broadcasts;
import guepardoapps.library.lucahome.common.constants.Bundles;
import guepardoapps.library.lucahome.common.dto.ShoppingEntryDto;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.lucahome.controller.ServiceController;

import guepardoapps.library.toolset.common.classes.SerializableList;

public class ShoppingListAdapter extends BaseAdapter {

    private static final String TAG = ShoppingListAdapter.class.getSimpleName();
    private LucaHomeLogger _logger;
    private SerializableList<ShoppingEntryDto> _entryList;
    private ServiceController _serviceController;
    private static LayoutInflater _inflater = null;

    public ShoppingListAdapter(
            @NonNull Context context,
            @NonNull SerializableList<ShoppingEntryDto> entryList) {
        _logger = new LucaHomeLogger(TAG);
        _logger.Debug("Created...");

        _entryList = entryList;

        SerializableList<ShoppingEntryDto> sortedList = new SerializableList<>();
        for (int index = 0; index < _entryList.getSize(); index++) {
            ShoppingEntryDto entry = _entryList.getValue(index);
            if (!entry.IsBought()) {
                sortedList.setFirstValue(entry);
            } else {
                sortedList.addValue(entry);
            }
        }
        _entryList = sortedList;

        for (int index = 0; index < _entryList.getSize(); index++) {
            _logger.Debug(_entryList.getValue(index).toString());
        }

        _serviceController = new ServiceController(context);

        _inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return _entryList.getSize();
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
        private ImageView _image;
        private TextView _name;
        private Button _quantity;
        private ImageButton _increase;
        private ImageButton _decrease;
        private ImageButton _delete;
        private CheckBox _bought;
    }

    @SuppressLint({"InflateParams", "ViewHolder"})
    @Override
    public View getView(final int index, View convertView, ViewGroup parent) {
        Holder holder = new Holder();
        View rowView = _inflater.inflate(R.layout.list_shopping_item, null);

        final ShoppingEntryDto entry = _entryList.getValue(index);
        _logger.Info(String.format("entry is %s", entry.toString()));

        holder._image = (ImageView) rowView.findViewById(R.id.shopping_item_image);
        holder._image.setImageResource(entry.GetGroup().GetDrawable());

        holder._name = (TextView) rowView.findViewById(R.id.shopping_item_name);
        holder._name.setText(entry.GetName());

        holder._quantity = (Button) rowView.findViewById(R.id.shopping_button_quantity);
        holder._quantity.setText(String.valueOf(entry.GetQuantity()));
        holder._quantity.setEnabled(false);

        holder._increase = (ImageButton) rowView.findViewById(R.id.shopping_button_increase);
        holder._increase.setOnClickListener(view -> {
            _logger.Debug("onClick _increase button: " + entry.GetName());
            entry.IncreaseQuantity();
            _serviceController.StartRestService(
                    Bundles.SHOPPING_LIST,
                    entry.GetCommandUpdate(),
                    Broadcasts.RELOAD_SHOPPING_LIST);
        });

        holder._decrease = (ImageButton) rowView.findViewById(R.id.shopping_button_decrease);
        holder._decrease.setOnClickListener(view -> {
            _logger.Debug("onClick _decrease button: " + entry.GetName());
            entry.DecreaseQuantity();
            _serviceController.StartRestService(
                    Bundles.SHOPPING_LIST,
                    entry.GetCommandUpdate(),
                    Broadcasts.RELOAD_SHOPPING_LIST);
        });

        holder._delete = (ImageButton) rowView.findViewById(R.id.shopping_button_delete);
        holder._delete.setOnClickListener(view -> {
            _logger.Debug("onClick _delete button: " + entry.GetName());
            _serviceController.StartRestService(
                    Bundles.SHOPPING_LIST,
                    entry.GetCommandDelete(),
                    Broadcasts.RELOAD_SHOPPING_LIST);
        });

        holder._bought = (CheckBox) rowView.findViewById(R.id.shopping_checkbox_bought);
        holder._bought.setVisibility(View.GONE);

        return rowView;
    }
}