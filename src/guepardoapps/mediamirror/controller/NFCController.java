package guepardoapps.mediamirror.controller;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcAdapter.OnNdefPushCompleteCallback;
import android.nfc.NfcEvent;

import guepardoapps.mediamirror.common.SmartMirrorLogger;

public class NFCController implements CreateNdefMessageCallback, OnNdefPushCompleteCallback {

	private static final String TAG = NFCController.class.getSimpleName();
	private SmartMirrorLogger _logger;

	private boolean _isInitialized;

	private Context _context;
	private NfcAdapter _nfcAdapter;;

	public NFCController(Context context) {
		_logger = new SmartMirrorLogger(TAG);
		_logger.Debug(TAG + " created");
		_context = context;
	}

	@Override
	public void onNdefPushComplete(NfcEvent nfcEvent) {
		_logger.Debug("onNdefPushComplete NFC");
	}

	@Override
	public NdefMessage createNdefMessage(NfcEvent nfcEvent) {
		_logger.Debug("createNdefMessage NFC");
		Uri url = Uri.parse("http://192.168.178.22/index.php?page=home&action=main");
		return new NdefMessage(new NdefRecord[] { NdefRecord.createUri(url) });
	}

	public void Start() {
		_logger.Debug("Start");
		if (!_isInitialized) {
			_logger.Debug("Initializing!");

			_nfcAdapter = NfcAdapter.getDefaultAdapter(_context);
			if (_nfcAdapter != null) {
				// Register callback to set NDEF message
				_nfcAdapter.setNdefPushMessageCallback(this, (Activity) _context);

				// Register callback to listen for message-sent success
				_nfcAdapter.setOnNdefPushCompleteCallback(this, (Activity) _context);
			} else {
				_logger.Warn("NFC is not available on this device");
			}

			_isInitialized = true;
		} else {
			_logger.Warn("Is ALREADY initialized!");
		}
	}

	public void Dispose() {
		_logger.Debug("Dispose");
		_nfcAdapter = null;
		_isInitialized = false;
	}
}
