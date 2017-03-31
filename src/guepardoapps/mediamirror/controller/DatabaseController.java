package guepardoapps.mediamirror.controller;

import java.util.ArrayList;

import android.content.Context;

import guepardoapps.mediamirror.common.SmartMirrorLogger;
import guepardoapps.mediamirror.database.DatabaseYoutubeIds;
import guepardoapps.mediamirror.model.YoutubeDatabaseModel;

public class DatabaseController {

	private static final DatabaseController SINGLETON = new DatabaseController();

	private static final String TAG = DatabaseController.class.getSimpleName();
	private SmartMirrorLogger _logger;

	private boolean _isInitialized;

	private Context _context;
	private static DatabaseYoutubeIds _databaseYoutubeIds;

	public static DatabaseController getInstance() {
		return SINGLETON;
	}

	private DatabaseController() {
		_logger = new SmartMirrorLogger(TAG);
		_logger.Debug("DatabaseController created...");
	}

	public void Initialize(Context context) {
		_logger.Debug("Initialize");

		if (_isInitialized) {
			_logger.Warn("Already initialized!");
			return;
		}

		_context = context;

		_databaseYoutubeIds = new DatabaseYoutubeIds(_context);
		_databaseYoutubeIds.Open();

		_isInitialized = true;
	}

	public ArrayList<YoutubeDatabaseModel> GetYoutubeIds() {
		_logger.Debug("Loading youtube ids from database");
		ArrayList<YoutubeDatabaseModel> entries = _databaseYoutubeIds.GetYoutubeIds();
		return entries;
	}

	public void SaveYoutubeId(YoutubeDatabaseModel newEntry) {
		_logger.Debug("Saving new youtube id to database");
		_databaseYoutubeIds.CreateEntry(newEntry);
	}

	public void UpdateYoutubeId(YoutubeDatabaseModel updateEntry) {
		_logger.Debug("Updating youtube id to database");
		_databaseYoutubeIds.Update(updateEntry);
	}

	public int GetHighesId() {
		_logger.Debug("Loading highest id from database");
		int highestId = _databaseYoutubeIds.GetHighestId();
		return highestId;
	}

	public void Dispose() {
		_logger.Debug("Dispose");
		_databaseYoutubeIds.Close();
	}
}
