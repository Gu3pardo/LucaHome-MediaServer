package guepardoapps.mediamirror.controller;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.Locale;

import guepardoapps.library.lucahome.common.dto.MenuDto;
import guepardoapps.library.lucahome.common.dto.ShoppingEntryDto;
import guepardoapps.library.lucahome.common.dto.WirelessSocketDto;
import guepardoapps.library.lucahome.controller.LucaDialogController;

import guepardoapps.library.toolset.common.classes.SerializableList;

import guepardoapps.mediamirror.R;
import guepardoapps.mediamirror.common.SmartMirrorLogger;
import guepardoapps.mediamirror.common.constants.Broadcasts;
import guepardoapps.mediamirror.common.constants.Bundles;
import guepardoapps.mediamirror.customadapter.MenuListAdapter;
import guepardoapps.mediamirror.customadapter.ShoppingListAdapter;
import guepardoapps.mediamirror.customadapter.SocketListAdapter;

public class MediaMirrorDialogController extends LucaDialogController {

    private static final String TAG = MediaMirrorDialogController.class.getSimpleName();

    public MediaMirrorDialogController(@NonNull Context context) {
        super(context,
                ContextCompat.getColor(context, R.color.TextIcon),
                ContextCompat.getColor(context, R.color.Background));
        _logger = new SmartMirrorLogger(TAG);
        _logger.Debug(TAG + " created...");
    }

    public void ShowMenuListDialog(@NonNull SerializableList<MenuDto> menu) {
        _logger.Debug(String.format(Locale.GERMAN, "ShowMenuListDialog with menu %s", menu));

        checkOpenDialog();
        createDialog("ShowShoppingListDialog", R.layout.dialog_skeleton_list);

        TextView titleTextView = (TextView) _dialog.findViewById(R.id.dialog_list_title);
        ListView listView = (ListView) _dialog.findViewById(R.id.dialog_list_view);

        titleTextView.setText(R.string.menu);
        listView.setAdapter(new MenuListAdapter(_context, menu));

        showDialog(true);
    }

    public void ShowShoppingListDialog(@NonNull final SerializableList<ShoppingEntryDto> shoppingList) {
        _logger.Debug(String.format(Locale.GERMAN, "ShowShoppingListDialog with shoppingList %s", shoppingList));

        checkOpenDialog();
        createDialog("ShowShoppingListDialog", R.layout.dialog_skeleton_list);

        TextView titleTextView = (TextView) _dialog.findViewById(R.id.dialog_list_title);
        ListView listView = (ListView) _dialog.findViewById(R.id.dialog_list_view);

        titleTextView.setText(R.string.shoppingList);
        listView.setAdapter(new ShoppingListAdapter(_context, shoppingList));

        showDialog(true);
    }

    public void ShowSocketListDialog(@NonNull SerializableList<WirelessSocketDto> socketList) {
        _logger.Debug(String.format(Locale.GERMAN, "ShowSocketListDialog with socketList %s", socketList));

        checkOpenDialog();
        createDialog("ShowSocketListDialog", R.layout.dialog_skeleton_list);

        TextView titleTextView = (TextView) _dialog.findViewById(R.id.dialog_list_title);
        ListView listView = (ListView) _dialog.findViewById(R.id.dialog_list_view);

        titleTextView.setText(R.string.sockets);
        listView.setAdapter(new SocketListAdapter(_context, socketList));

        showDialog(true);
    }

    public void ShowUpdateApkDialog(@NonNull String updateFilePath) {
        _logger.Debug(String.format(Locale.GERMAN, "ShowUpdateApkDialog with path %s", updateFilePath));

        ShowDialogDouble(
                "Update available",
                "Install new update?",
                "Yes",
                () -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(new File(updateFilePath)), "application/vnd.android.package-archive");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    _context.startActivity(intent);

                    _broadcastController.SendStringBroadcast(
                            Broadcasts.FTP_FILE_UPDATE_DOWNLOAD_FINISHED,
                            Bundles.FILE_PATH,
                            "");
                },
                "Cancel", CloseDialogCallback,
                true);
    }

    @Override
    public void Dispose() {
        _logger.Debug("Dispose");
        super.Dispose();
    }
}
