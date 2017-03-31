package guepardoapps.mediamirror.database;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import guepardoapps.mediamirror.common.SmartMirrorLogger;
import guepardoapps.mediamirror.model.YoutubeDatabaseModel;

public class DatabaseYoutubeIds {

	private static final String TAG = DatabaseYoutubeIds.class.getSimpleName();
	private SmartMirrorLogger _logger;

	public static final String KEY_ROWID = "_id";

	public static final String KEY_YOUTUBE_ID = "YoutubeId";
	public static final String KEY_PLAY_COUNT = "PlayCount";

	private static final String DATABASE_NAME = "YoutubeIdDatabase";
	private static final String DATABASE_TABLE = "YoutubeIdTable";
	private static final int DATABASE_VERSION = 1;

	private DatabaseHelper _databaseHelper;
	private final Context _context;
	private SQLiteDatabase _database;

	private static class DatabaseHelper extends SQLiteOpenHelper {

		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase database) {
			database.execSQL(
					" CREATE TABLE " + DATABASE_TABLE + " ( " + KEY_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
							+ KEY_YOUTUBE_ID + " TEXT NOT NULL, " + KEY_PLAY_COUNT + " TEXT NOT NULL); ");
		}

		@Override
		public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
			database.execSQL(" DROP TABLE IF EXISTS " + DATABASE_TABLE);
			onCreate(database);
		}

		public void Remove(Context context) {
			context.deleteDatabase(DATABASE_NAME);
		}
	}

	public DatabaseYoutubeIds(Context context) {
		_logger = new SmartMirrorLogger(TAG);
		_context = context;
	}

	public DatabaseYoutubeIds Open() throws SQLException {
		_databaseHelper = new DatabaseHelper(_context);
		_database = _databaseHelper.getWritableDatabase();
		return this;
	}

	public void Close() {
		_databaseHelper.close();
	}

	public long CreateEntry(YoutubeDatabaseModel newEntry) {
		boolean entryExists = false;
		String[] columns = new String[] { KEY_YOUTUBE_ID };

		Cursor cursor = _database.query(DATABASE_TABLE, columns, null, null, null, null, null);
		int youtubeIdIndex = cursor.getColumnIndex(KEY_YOUTUBE_ID);
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			String youtubeId = cursor.getString(youtubeIdIndex);
			if (youtubeId.contains(newEntry.GetYoutubeId())) {
				entryExists = true;
				break;
			}
		}

		cursor.close();

		if (entryExists) {
			newEntry.IncreasePlayCount();
			Update(newEntry);
			return 0;
		} else {
			ContentValues contentValues = new ContentValues();

			contentValues.put(KEY_YOUTUBE_ID, newEntry.GetYoutubeId());
			contentValues.put(KEY_PLAY_COUNT, String.valueOf(newEntry.GetPlayCount()));

			return _database.insert(DATABASE_TABLE, null, contentValues);
		}
	}

	public ArrayList<YoutubeDatabaseModel> GetYoutubeIds() {
		String[] columns = new String[] { KEY_ROWID, KEY_YOUTUBE_ID, KEY_PLAY_COUNT };

		Cursor cursor = _database.query(DATABASE_TABLE, columns, null, null, null, null, null);
		ArrayList<YoutubeDatabaseModel> result = new ArrayList<YoutubeDatabaseModel>();

		int idIndex = cursor.getColumnIndex(KEY_ROWID);

		int youtubeIdIndex = cursor.getColumnIndex(KEY_YOUTUBE_ID);
		int playCountIndex = cursor.getColumnIndex(KEY_PLAY_COUNT);

		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			int id = cursor.getInt(idIndex);

			String youtubeId = cursor.getString(youtubeIdIndex);
			int playCount;
			try {
				playCount = Integer.parseInt(cursor.getString(playCountIndex));
			} catch (Exception ex) {
				_logger.Error(ex.toString());
				playCount = 0;
			}

			result.add(new YoutubeDatabaseModel(id, youtubeId, playCount));
		}

		cursor.close();

		return result;
	}

	public void Update(YoutubeDatabaseModel updateEntry) throws SQLException {
		ContentValues contentValues = new ContentValues();

		contentValues.put(KEY_YOUTUBE_ID, updateEntry.GetYoutubeId());
		contentValues.put(KEY_PLAY_COUNT, String.valueOf(updateEntry.GetPlayCount()));

		_database.update(DATABASE_TABLE, contentValues, KEY_ROWID + "=" + updateEntry.GetId(), null);
	}

	public int GetHighestId() {
		String[] columns = new String[] { KEY_ROWID };

		Cursor cursor = _database.query(DATABASE_TABLE, columns, null, null, null, null, null);
		int result = -1;

		int idIndex = cursor.getColumnIndex(KEY_ROWID);

		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			int id = cursor.getInt(idIndex);
			if (id > result) {
				result = id;
			}
		}

		cursor.close();

		return result;
	}

	public void Delete(YoutubeDatabaseModel deleteEntry) throws SQLException {
		_database.delete(DATABASE_TABLE, KEY_ROWID + "=" + deleteEntry.GetId(), null);
	}

	public void Remove() {
		_databaseHelper.Remove(_context);
	}
}
