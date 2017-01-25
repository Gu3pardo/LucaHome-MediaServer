package guepardoapps.mediamirror.database;

import java.util.ArrayList;

import android.content.Context;

import guepardoapps.mediamirror.common.SmartMirrorLogger;
import guepardoapps.mediamirror.model.YoutubeDatabaseModel;

public class DBController {

	private static final String TAG = DBController.class.getName();
	private SmartMirrorLogger _logger;

	private Context _context;
	private static DatabaseYoutubeIds _databaseYoutubeIds;

	public DBController(Context context) {
		_logger = new SmartMirrorLogger(TAG);
		_logger.Debug("DatabaseController created...");

		_context = context;
		_databaseYoutubeIds = new DatabaseYoutubeIds(_context);
	}

	public ArrayList<YoutubeDatabaseModel> GetYoutubeIds() {
		_databaseYoutubeIds.Open();
		ArrayList<YoutubeDatabaseModel> entries = _databaseYoutubeIds.GetYoutubeIds();
		_databaseYoutubeIds.Close();
		return entries;
	}

	public void SaveYoutubeId(YoutubeDatabaseModel newEntry) {
		_databaseYoutubeIds.Open();
		_databaseYoutubeIds.CreateEntry(newEntry);
		_databaseYoutubeIds.Close();
	}

	public void UpdateYoutubeId(YoutubeDatabaseModel updateEntry) {
		_databaseYoutubeIds.Open();
		_databaseYoutubeIds.Update(updateEntry);
		_databaseYoutubeIds.Close();
	}

	public int GetHighesId() {
		_databaseYoutubeIds.Open();
		int highestId = _databaseYoutubeIds.GetHighestId();
		_databaseYoutubeIds.Close();
		return highestId;
	}
}
