package guepardoapps.mediamirror.tts;

import java.util.Locale;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import guepardoapps.mediamirror.common.Constants;
import guepardoapps.mediamirror.common.SmartMirrorLogger;
import guepardoapps.toolset.controller.ReceiverController;

public class TTSService {

	private static final String TAG = TTSService.class.getName();
	private SmartMirrorLogger _logger;

	private Context _context;

	private ReceiverController _receiverController;

	private boolean _ttsInitialized;
	private TextToSpeech _ttsSpeaker;

	private BroadcastReceiver _speekReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_logger.Debug("_speekReceiver onReceive");
			String text = intent.getStringExtra(Constants.BUNDLE_SPEAK_TEXT);
			if (text != null) {
				speak(text);
			}
		}
	};

	public TTSService(Context context) {
		_logger = new SmartMirrorLogger(TAG);
		_context = context;
		_receiverController = new ReceiverController(_context);
	}

	public void Init() {
		if (_ttsInitialized) {
			_logger.Warn(TTSService.class.getName() + " is already initialized!");
			return;
		}

		_ttsSpeaker = new TextToSpeech(_context, new TextToSpeech.OnInitListener() {
			@Override
			public void onInit(int status) {
				if (status == TextToSpeech.SUCCESS) {
					int result = _ttsSpeaker.setLanguage(Locale.US);
					if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
						_logger.Error("This Language is not supported!");
					} else {
						_receiverController.RegisterReceiver(_speekReceiver,
								new String[] { Constants.BROADCAST_SPEAK_TEXT });
						_ttsInitialized = true;
					}
				} else {
					_logger.Error("Initilization Failed!");
				}
			}
		});
	}

	public void Dispose() {
		_logger.Debug("Dispose");
		if (_ttsSpeaker != null) {
			_ttsSpeaker.stop();
			_ttsSpeaker.shutdown();
		}
		_receiverController.UnregisterReceiver(_speekReceiver);
	}

	private void speak(String text) {
		_logger.Debug("Speak: " + text);
		if (_ttsInitialized) {
			_ttsSpeaker.speak(text, TextToSpeech.QUEUE_FLUSH, null, "");
		} else {
			_logger.Warn("TTSSpeaker not initialized!");
		}
	}
}
