package guepardoapps.mediamirror.controller;

import java.io.File;

import android.content.Context;
import android.os.Environment;

import guepardoapps.library.toolset.common.classes.SerializableList;
import guepardoapps.library.toolset.controller.CommandController;
import guepardoapps.library.toolset.controller.FileController;

import guepardoapps.mediamirror.common.SmartMirrorLogger;
import guepardoapps.mediamirror.common.constants.Constants;

public class MediaStorageController {

	private static final MediaStorageController SINGLETON_CONTROLLER = new MediaStorageController();

	private static final String TAG = MediaStorageController.class.getSimpleName();
	private SmartMirrorLogger _logger;

	private Context _context;

	private CommandController _commandController;
	private FileController _fileController;

	private boolean _isInitialized;

	private String _nasDir = Environment.getExternalStorageDirectory() + File.separator + Constants.NAS;
	private String _galleryDir = Environment.getExternalStorageDirectory() + File.separator + Constants.NAS_GALLERY;
	private String _libraryDir = Environment.getExternalStorageDirectory() + File.separator + Constants.NAS_LIBRARY;
	private String _musicDir = Environment.getExternalStorageDirectory() + File.separator + Constants.NAS_MUSIC;
	private String _videothekDir = Environment.getExternalStorageDirectory() + File.separator + Constants.NAS_VIDEOTHEK;

	private boolean _nasGalleryMounted;
	private boolean _nasLibraryMounted;
	private boolean _nasMusicMounted;
	private boolean _nasVideothekMounted;

	public static MediaStorageController getInstance() {
		return SINGLETON_CONTROLLER;
	}

	private MediaStorageController() {
		_logger = new SmartMirrorLogger(TAG);
		_logger.Debug(TAG + " created...");
	}

	public void Initialize(Context context) {
		if (!_isInitialized) {
			_context = context;

			_commandController = new CommandController(_context);
			_fileController = new FileController();

			mountNAS();

			_isInitialized = true;
		}
	}

	public boolean IsInitialized() {
		return _isInitialized;
	}

	public boolean NasGalleryMounted() {
		return _nasGalleryMounted;
	}

	public boolean NasLibraryMounted() {
		return _nasLibraryMounted;
	}

	public boolean NasMusicMounted() {
		return _nasMusicMounted;
	}

	public boolean NasVideothekMounted() {
		return _nasVideothekMounted;
	}

	public String GetGalleryDir() {
		return _galleryDir;
	}

	public String GetLibraryDir() {
		return _libraryDir;
	}

	public String GetMusicDir() {
		return _musicDir;
	}

	public String GetVideothekDir() {
		return _videothekDir;
	}

	public SerializableList<File> GetGalleryFolder() {
		return _fileController.GetSubFolder(_galleryDir);
	}

	public SerializableList<File> GetLibraryFolder() {
		return _fileController.GetSubFolder(_libraryDir);
	}

	public SerializableList<File> GetMusicFolder() {
		return _fileController.GetSubFolder(_musicDir);
	}

	public SerializableList<File> GetVideothekFolder() {
		return _fileController.GetSubFolder(_videothekDir);
	}

	public boolean Dispose() {
		_logger.Debug("Dispose");

		if (!_isInitialized) {
			_logger.Error("not initialized!");
			return false;
		}

		return true;
	}

	private void mountNAS() {
		_logger.Debug("mountNAS");

		_logger.Debug("Checking NASDir: " + _nasDir);
		if (_fileController.CreateDirectory(_nasDir)) {

			_logger.Debug("Checking GalleryDir: " + _galleryDir);
			if (_fileController.CreateDirectory(_galleryDir)) {
				_logger.Debug("Mounting GalleryDir: " + _galleryDir);
				String[] command = new String[] { "su", "-c", "mount -t cifs -o username=mediamirror, "
						+ "password=mediamirror //192.168.178.24/Galerie " + _galleryDir };
				_nasGalleryMounted = _commandController.PerformCommand(command);
			} else {
				_logger.Error("Failed to create directory for NAS gallery!");
			}

			_logger.Debug("Checking LibraryDir: " + _libraryDir);
			if (_fileController.CreateDirectory(_libraryDir)) {
				_logger.Debug("Mounting LibraryDir: " + _libraryDir);
				String[] command = new String[] { "su", "-c", "mount -t cifs -o username=mediamirror, "
						+ "password=mediamirror //192.168.178.24/Bibliothek " + _libraryDir };
				_nasLibraryMounted = _commandController.PerformCommand(command);
			} else {
				_logger.Error("Failed to create directory for NAS library!");
			}

			_logger.Debug("Checking MusicDir: " + _musicDir);
			if (_fileController.CreateDirectory(_musicDir)) {
				_logger.Debug("Mounting MusicDir: " + _musicDir);
				String[] command = new String[] { "su", "-c", "mount -t cifs -o username=mediamirror, "
						+ "password=mediamirror //192.168.178.24/Musik " + _musicDir };
				_nasMusicMounted = _commandController.PerformCommand(command);
			} else {
				_logger.Error("Failed to create directory for NAS music!");
			}

			_logger.Debug("Checking VideothekDir: " + _videothekDir);
			if (_fileController.CreateDirectory(_videothekDir)) {
				_logger.Debug("Mounting VideothekDir: " + _videothekDir);
				String[] command = new String[] { "su", "-c", "mount -t cifs -o username=mediamirror, "
						+ "password=mediamirror //192.168.178.27/Filme&Serien " + _videothekDir };
				_nasVideothekMounted = _commandController.PerformCommand(command);
			} else {
				_logger.Error("Failed to create directory for NAS videothek!");
			}
		} else {
			_logger.Error("Failed to create directory for NAS!");
		}
	}
}
