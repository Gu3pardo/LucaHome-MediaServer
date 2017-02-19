package guepardoapps.mediamirror.controller;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;

import guepardoapps.mediamirror.R;
import guepardoapps.mediamirror.common.SmartMirrorLogger;

import guepardoapps.toolset.controller.DialogController;

public class MediaMirrorDialogController extends DialogController {

	private static final String TAG = MediaMirrorDialogController.class.getName();
	private SmartMirrorLogger _logger;

	public MediaMirrorDialogController(Context context) {
		super(context, ContextCompat.getColor(context, R.color.TextIcon),
				ContextCompat.getColor(context, R.color.Background));
		_logger = new SmartMirrorLogger(TAG);

		_context = context;

		_isDialogOpen = false;
	}

	@SuppressLint("SetJavaScriptEnabled")
	public void ShowTemperatureGraphDialog(String graphPath) {
		checkOpenDialog();

		createDialog("ShowTemperatureGraphDialog: " + graphPath, R.layout.dialog_temperature_graph);

		final ProgressBar progressBar = (ProgressBar) _dialog.findViewById(R.id.temperature_dialog_progressbar);

		final WebView webView = (WebView) _dialog.findViewById(R.id.temperature_dialog_webview);
		webView.getSettings().setBuiltInZoomControls(true);
		webView.getSettings().setSupportZoom(true);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setLoadWithOverviewMode(true);
		webView.setWebViewClient(new WebViewClient());
		webView.setWebChromeClient(new WebChromeClient());
		webView.setInitialScale(100);
		CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.setAcceptCookie(false);
		webView.loadUrl("http://" + graphPath);
		webView.setWebViewClient(new WebViewClient() {
			public void onPageFinished(WebView view, String url) {
				progressBar.setVisibility(View.GONE);
				webView.setVisibility(View.VISIBLE);
			}
		});

		Button btnOk = (Button) _dialog.findViewById(R.id.temperature_dialog_button);
		btnOk.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				CloseDialogCallback.run();
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
