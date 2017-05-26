package guepardoapps.mediamirror.controller;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;

import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

import guepardoapps.library.toolset.controller.BroadcastController;
import guepardoapps.library.toolset.controller.FTPController;

import guepardoapps.mediamirror.common.SmartMirrorLogger;
import guepardoapps.mediamirror.common.constants.Broadcasts;
import guepardoapps.mediamirror.common.constants.Bundles;
import guepardoapps.mediamirror.common.constants.Constants;

public class MediaMirrorFTPController extends FTPController {

    private static final String TAG = MediaMirrorFTPController.class.getSimpleName();
    private SmartMirrorLogger _logger;

    private static final String APK_FILE_NAME = "MediaMirror";

    private Context _context;
    private BroadcastController _broadCastController;

    public MediaMirrorFTPController(Context context) {
        super();
        _logger = new SmartMirrorLogger(TAG);
        _logger.Debug(TAG + " created...");

        _context = context;
        _broadCastController = new BroadcastController(_context);
    }

    public FTPFile[] ReadGallery() {
        _logger.Debug("ReadGallery");
        return ConnectToFTP(Constants.NAS_GALLERY, Constants.USER, Constants.PASSPHRASE);
    }

    public FTPFile[] ReadLibrary() {
        _logger.Debug("ReadLibrary");
        return ConnectToFTP(Constants.NAS_LIBRARY, Constants.USER, Constants.PASSPHRASE);
    }

    public FTPFile[] ReadMovies() {
        _logger.Debug("ReadMovies");
        return ConnectToFTP(Constants.NAS_MOVIES, Constants.USER, Constants.PASSPHRASE);
    }

    public FTPFile[] ReadMusic() {
        _logger.Debug("ReadMusic");
        return ConnectToFTP(Constants.NAS_MUSIC, Constants.USER, Constants.PASSPHRASE);
    }

    public FTPFile[] ReadPublic() {
        _logger.Debug("ReadPublic");
        return ConnectToFTP(Constants.NAS_PUBLIC, Constants.USER, Constants.PASSPHRASE);
    }

    public boolean IsUpdateAvailable() {
        _logger.Debug("IsUpdateAvailable");

        FTPFile[] applicationFiles = ConnectToFTP(Constants.NAS_PUBLIC + "Shared Applications/", Constants.USER, Constants.PASSPHRASE);
        for (FTPFile file : applicationFiles) {
            if (file.isFile()) {
                if (file.getName().contains(APK_FILE_NAME)) {
                    String serverVersionName = file.getName();
                    serverVersionName = serverVersionName.replace(APK_FILE_NAME, "");
                    serverVersionName = serverVersionName.replace("_v", "");
                    serverVersionName = serverVersionName.replace(".apk", "");

                    ArrayList<Integer> separatorList = new ArrayList<>();
                    char separator = '.';
                    for (int index = 0; index < serverVersionName.length(); index++) {
                        if (serverVersionName.charAt(index) == separator) {
                            separatorList.add(index);
                        }
                    }

                    if (separatorList.size() > 3) {
                        _logger.Error(String.format(Locale.GERMAN, "Invalid size %d of separatorList!", separatorList.size()));
                        return false;
                    }

                    String serverMajorVersionString = serverVersionName.substring(0, separatorList.get(0)).replace(".", "");
                    String serverMinorVersionString = serverVersionName.substring(separatorList.get(0), separatorList.get(1)).replace(".", "");
                    String serverPatchVersionString = serverVersionName.substring(separatorList.get(1), separatorList.get(2)).replace(".", "");
                    String serverBuildDateString = serverVersionName.substring(separatorList.get(2)).replace(".", "");

                    int serverMajorVersion;
                    int serverMinorVersion;
                    int serverPatchVersion;
                    int serverBuildDate;

                    try {
                        serverMajorVersion = Integer.parseInt(serverMajorVersionString);
                        serverMinorVersion = Integer.parseInt(serverMinorVersionString);
                        serverPatchVersion = Integer.parseInt(serverPatchVersionString);
                        serverBuildDate = Integer.parseInt(serverBuildDateString);
                    } catch (Exception exception) {
                        _logger.Error(exception.getMessage());
                        return false;
                    }

                    String localVersion;
                    try {
                        PackageInfo packageInfo = _context.getPackageManager().getPackageInfo(_context.getPackageName(), 0);
                        localVersion = packageInfo.versionName;
                    } catch (PackageManager.NameNotFoundException e) {
                        _logger.Error(e.toString());
                        return false;
                    }

                    String localMajorVersionString = localVersion.substring(0, separatorList.get(0)).replace(".", "");
                    String localMinorVersionString = localVersion.substring(separatorList.get(0), separatorList.get(1)).replace(".", "");
                    String localPatchVersionString = localVersion.substring(separatorList.get(1), separatorList.get(2)).replace(".", "");
                    String localBuildDateString = localVersion.substring(separatorList.get(2)).replace(".", "");

                    int localMajorVersion;
                    int localMinorVersion;
                    int localPatchVersion;
                    int localBuildDate;

                    try {
                        localMajorVersion = Integer.parseInt(localMajorVersionString);
                        localMinorVersion = Integer.parseInt(localMinorVersionString);
                        localPatchVersion = Integer.parseInt(localPatchVersionString);
                        localBuildDate = Integer.parseInt(localBuildDateString);
                    } catch (Exception exception) {
                        _logger.Error(exception.getMessage());
                        return false;
                    }

                    if (serverMajorVersion > localMajorVersion) {
                        downloadNewApkFile(file);
                        return true;
                    }

                    if (serverMajorVersion >= localMajorVersion
                            && serverMinorVersion > localMinorVersion) {
                        downloadNewApkFile(file);
                        return true;
                    }

                    if (serverMajorVersion >= localMajorVersion
                            && serverMinorVersion >= localMinorVersion
                            && serverPatchVersion > localPatchVersion) {
                        downloadNewApkFile(file);
                        return true;
                    }

                    if (serverMajorVersion >= localMajorVersion
                            && serverMinorVersion >= localMinorVersion
                            && serverPatchVersion >= localPatchVersion
                            && serverBuildDate > localBuildDate) {
                        downloadNewApkFile(file);
                        return true;
                    }

                    return false;
                }
            }
        }

        return false;
    }

    private void downloadNewApkFile(FTPFile file) {
        _logger.Debug(String.format(Locale.GERMAN, "Downloading new file: %s", file));

        String remoteFilePath = Constants.NAS_PUBLIC + "Shared Applications/" + file.getName();
        File downloadFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + file.getName());

        new Thread(() -> {
            if (DownloadSingleFile(Constants.NAS_1, Constants.USER, Constants.PASSPHRASE, remoteFilePath, downloadFile)) {
                _broadCastController.SendStringBroadcast(
                        Broadcasts.FTP_FILE_UPDATE_DOWNLOAD_FINISHED,
                        Bundles.FILE_PATH,
                        downloadFile.getAbsolutePath());
            } else {
                _logger.Error("Download failed!");
            }
        }).start();
    }
}
