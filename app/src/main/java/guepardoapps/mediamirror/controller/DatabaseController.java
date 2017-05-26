package guepardoapps.mediamirror.controller;

import java.util.ArrayList;
import java.util.Locale;

import android.content.Context;
import android.support.annotation.NonNull;

import guepardoapps.mediamirror.common.SmartMirrorLogger;
import guepardoapps.mediamirror.database.DatabaseYoutubeIds;
import guepardoapps.mediamirror.view.model.YoutubeDatabaseModel;

public class DatabaseController {

    private static final String TAG = DatabaseController.class.getSimpleName();
    private SmartMirrorLogger _logger;

    private boolean _isInitialized;

    private DatabaseYoutubeIds _databaseYoutubeIds;

    public DatabaseController(@NonNull Context context) {
        _logger = new SmartMirrorLogger(TAG);
        _logger.Debug(TAG + " created...");

        if (_isInitialized) {
            _logger.Warn("Already initialized!");
            return;
        }

        _databaseYoutubeIds = new DatabaseYoutubeIds(context);
        _databaseYoutubeIds.Open();

        _isInitialized = true;
    }

    public ArrayList<YoutubeDatabaseModel> GetYoutubeIds() {
        _logger.Debug("Loading youtube ids from database");
        return _databaseYoutubeIds.GetYoutubeIds();
    }

    public void SaveYoutubeId(@NonNull YoutubeDatabaseModel newEntry) {
        _logger.Debug("Saving new youtube id to database");
        _databaseYoutubeIds.CreateEntry(newEntry);
    }

    public void UpdateYoutubeId(@NonNull YoutubeDatabaseModel updateEntry) {
        _logger.Debug("Updating youtube id to database");
        _databaseYoutubeIds.Update(updateEntry);
    }

    public int GetHighestId() {
        _logger.Debug("Loading highest id from database");
        return _databaseYoutubeIds.GetHighestId();
    }

    public void DeleteYoutubeId(@NonNull YoutubeDatabaseModel deleteEntry) {
        _logger.Debug(String.format(Locale.GERMAN, "Deleting youtube id %s from database", deleteEntry));
        _databaseYoutubeIds.Delete(deleteEntry);
    }

    public void RemoveDatabase() {
        _logger.Debug("Removing database!");
        _databaseYoutubeIds.Remove();
    }

    public void Dispose() {
        _logger.Debug("Dispose");
        _databaseYoutubeIds.Close();
        _isInitialized = false;
    }
}
