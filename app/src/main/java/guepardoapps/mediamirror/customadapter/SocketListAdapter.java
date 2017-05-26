package guepardoapps.mediamirror.customadapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import guepardoapps.library.lucahome.R;
import guepardoapps.library.lucahome.common.constants.Broadcasts;
import guepardoapps.library.lucahome.common.dto.WirelessSocketDto;
import guepardoapps.library.lucahome.common.enums.LucaObject;
import guepardoapps.library.lucahome.common.enums.RaspberrySelection;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.lucahome.controller.ServiceController;
import guepardoapps.library.toolset.common.classes.SerializableList;

public class SocketListAdapter extends BaseAdapter {

    private static final String TAG = SocketListAdapter.class.getSimpleName();
    private LucaHomeLogger _logger;
    private SerializableList<WirelessSocketDto> _socketList;
    private ServiceController _serviceController;
    private static LayoutInflater _inflater = null;

    public SocketListAdapter(
            @NonNull Context context,
            @NonNull SerializableList<WirelessSocketDto> socketList) {
        _logger = new LucaHomeLogger(TAG);

        _socketList = socketList;
        for (int index = 0; index < _socketList.getSize(); index++) {
            _logger.Debug(_socketList.getValue(index).toString());
        }

        _serviceController = new ServiceController(context);

        _inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return _socketList.getSize();
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
        private Button _name;
        private TextView _code;
        private TextView _area;
        private Switch _state;
    }

    @SuppressLint({"InflateParams", "ViewHolder"})
    @Override
    public View getView(final int index, View convertView, ViewGroup parent) {
        Holder holder = new Holder();
        View rowView = _inflater.inflate(R.layout.list_socket_item, null);

        holder._name = (Button) rowView.findViewById(R.id.socket_item_name);
        holder._name.setText(_socketList.getValue(index).GetName());
        holder._name.setEnabled(false);

        holder._code = (TextView) rowView.findViewById(R.id.socket_item_code);
        holder._code.setText(_socketList.getValue(index).GetCode());

        holder._area = (TextView) rowView.findViewById(R.id.socket_item_area);
        holder._area.setText(_socketList.getValue(index).GetArea());

        holder._state = (Switch) rowView.findViewById(R.id.socket_item_switch);
        holder._state.setChecked(_socketList.getValue(index).IsActivated());
        holder._state.setOnCheckedChangeListener((buttonView, isChecked) -> {
            _logger.Debug("onCheckedChanged _name button: " + _socketList.getValue(index).GetName());
            _serviceController.StartRestService(
                    _socketList.getValue(index).GetName(),
                    _socketList.getValue(index).GetCommandSet(isChecked),
                    Broadcasts.RELOAD_SOCKETS,
                    LucaObject.WIRELESS_SOCKET,
                    RaspberrySelection.BOTH);
        });

        return rowView;
    }
}