package guepardoapps.mediamirror.controller;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import guepardoapps.lucahomelibrary.common.classes.SerializableList;
import guepardoapps.lucahomelibrary.common.controller.LucaDialogController;
import guepardoapps.lucahomelibrary.common.dto.ShoppingEntryDto;
import guepardoapps.lucahomelibrary.common.dto.WirelessSocketDto;
import guepardoapps.lucahomelibrary.view.customadapter.ShoppingListAdapter;
import guepardoapps.lucahomelibrary.view.customadapter.SocketListAdapter;

import guepardoapps.mediamirror.R;
import guepardoapps.mediamirror.common.Constants;
import guepardoapps.mediamirror.common.SmartMirrorLogger;

import guepardoapps.toolset.controller.BroadcastController;
import guepardoapps.toolset.controller.ReceiverController;

public class MediaMirrorDialogController extends LucaDialogController {

	private static final String TAG = MediaMirrorDialogController.class.getName();
	private SmartMirrorLogger _logger;

	private ReceiverController _receiverController;

	public MediaMirrorDialogController(Context context) {
		super(context, ContextCompat.getColor(context, R.color.TextIcon),
				ContextCompat.getColor(context, R.color.Background));
		_logger = new SmartMirrorLogger(TAG);

		_context = context;
		_broadcastController = new BroadcastController(_context);
		_receiverController = new ReceiverController(_context);

		_isDialogOpen = false;
	}

	public void ShowSocketListDialog(SerializableList<WirelessSocketDto> socketList) {
		checkOpenDialog();

		createDialog("ShowSocketListDialog", R.layout.dialog_skeleton_list);

		TextView titleTextView = (TextView) _dialog.findViewById(R.id.dialog_list_title);
		titleTextView.setText("Sockets");
		ListView listView = (ListView) _dialog.findViewById(R.id.dialog_list_view);
		SocketListAdapter listAdapter = new SocketListAdapter(_context, socketList);
		listView.setAdapter(listAdapter);

		Button btnClose = (Button) _dialog.findViewById(R.id.btnDialogClose);
		btnClose.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				CloseDialogCallback.run();
			}
		});

		Button btnAdd = (Button) _dialog.findViewById(R.id.btnAddListView);
		btnAdd.setVisibility(View.GONE);

		showDialog(false);
	}

	public void ShowShoppingListDialog(final SerializableList<ShoppingEntryDto> shoppingList) {
		checkOpenDialog();

		createDialog("ShowShoppingListDialog", R.layout.dialog_skeleton_list);

		TextView titleTextView = (TextView) _dialog.findViewById(R.id.dialog_list_title);
		titleTextView.setText("Shopping List");
		final ListView listView = (ListView) _dialog.findViewById(R.id.dialog_list_view);

		if (shoppingList != null) {
			ShoppingListAdapter listAdapter = new ShoppingListAdapter(_context, shoppingList);
			listView.setAdapter(listAdapter);
		}

		Button btnClose = (Button) _dialog.findViewById(R.id.btnDialogClose);
		btnClose.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				CloseDialogCallback.run();
			}
		});

		final BroadcastReceiver shoppingListReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				@SuppressWarnings("unchecked")
				SerializableList<ShoppingEntryDto> receivedShoppingList = (SerializableList<ShoppingEntryDto>) intent
						.getSerializableExtra(Constants.BUNDLE_SHOPPING_LIST);
				if (receivedShoppingList != null) {
					ShoppingListAdapter listAdapter = new ShoppingListAdapter(_context, receivedShoppingList);
					listView.setAdapter(listAdapter);
				}
			}
		};

		_receiverController.RegisterReceiver(shoppingListReceiver, new String[] { Constants.BROADCAST_SHOPPING_LIST });

		Button btnAdd = (Button) _dialog.findViewById(R.id.btnAddListView);
		btnAdd.setOnClickListener(new OnClickListener() {
			Runnable getShoppingListDataRunnable = new Runnable() {
				public void run() {
					_broadcastController.SendSimpleBroadcast(Constants.BROADCAST_PERFORM_SHOPPING_LIST_UPDATE);
				}
			};

			@Override
			public void onClick(View v) {
				_receiverController.UnregisterReceiver(shoppingListReceiver);
				CloseDialogCallback.run();

				int size = 0;
				if (shoppingList != null) {
					size = shoppingList.getSize();
				}
				ShowAddShoppingEntryDialog(getShoppingListDataRunnable, null, true, false, size);
			}
		});

		showDialog(false);
	}

	private void createDialog(String dialogType, int layout) {
		_logger.Debug(dialogType);

		_dialog = new Dialog(_context);

		_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		_dialog.setContentView(layout);
	}

	@SuppressWarnings("deprecation")
	private void showDialog(boolean isCancelable) {
		_logger.Debug("showDialog, isCancelable: " + String.valueOf(isCancelable));

		_dialog.setCancelable(isCancelable);
		_dialog.show();

		Window window = _dialog.getWindow();
		window.setLayout(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);

		_isDialogOpen = true;
	}

	private void checkOpenDialog() {
		if (_isDialogOpen) {
			_logger.Warn("Closing other Dialog...");
			CloseDialogCallback.run();
		}
	}
}
