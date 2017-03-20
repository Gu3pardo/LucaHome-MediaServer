package guepardoapps.mediamirror.controller;

import java.util.ArrayList;

import android.content.Context;

import guepardoapps.mediamirror.common.SmartMirrorLogger;
import guepardoapps.mediamirror.database.DatabaseYoutubeIds;
import guepardoapps.mediamirror.model.YoutubeDatabaseModel;

public class DatabaseController {

	private static final String TAG = DatabaseController.class.getName();
	private SmartMirrorLogger _logger;

	private Context _context;
	private static DatabaseYoutubeIds _databaseYoutubeIds;

	public DatabaseController(Context context) {
		_logger = new SmartMirrorLogger(TAG);
		_logger.Debug("DatabaseController created...");

		_context = context;
		_databaseYoutubeIds = new DatabaseYoutubeIds(_context);
	}

	public ArrayList<YoutubeDatabaseModel> GetYoutubeIds() {
		_logger.Debug("Loading youtube ids from database");
		_databaseYoutubeIds.Open();
		ArrayList<YoutubeDatabaseModel> entries = _databaseYoutubeIds.GetYoutubeIds();
		_databaseYoutubeIds.Close();
		return entries;
	}

	public void SaveYoutubeId(YoutubeDatabaseModel newEntry) {
		_logger.Debug("Saving new youtube id to database");
		_databaseYoutubeIds.Open();
		_databaseYoutubeIds.CreateEntry(newEntry);
		_databaseYoutubeIds.Close();
	}

	public void UpdateYoutubeId(YoutubeDatabaseModel updateEntry) {
		_logger.Debug("Updating youtube id to database");
		_databaseYoutubeIds.Open();
		_databaseYoutubeIds.Update(updateEntry);
		_databaseYoutubeIds.Close();
	}

	public int GetHighesId() {
		_logger.Debug("Loading highest id from database");
		_databaseYoutubeIds.Open();
		int highestId = _databaseYoutubeIds.GetHighestId();
		_databaseYoutubeIds.Close();
		return highestId;
	}
}
