package guepardoapps.mediamirror.view.controller;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.Locale;

import es.dmoral.toasty.Toasty;

import guepardoapps.library.lucahome.common.dto.BirthdayDto;

import guepardoapps.library.toolset.common.classes.SerializableList;
import guepardoapps.library.toolset.common.dto.CalendarEntryDto;
import guepardoapps.library.toolset.controller.BroadcastController;
import guepardoapps.library.toolset.controller.PermissionController;
import guepardoapps.library.toolset.controller.ReceiverController;

import guepardoapps.mediamirror.R;
import guepardoapps.mediamirror.common.SmartMirrorLogger;
import guepardoapps.mediamirror.common.constants.Broadcasts;
import guepardoapps.mediamirror.common.constants.Bundles;

public class EventViewController {

    private static final String TAG = EventViewController.class.getSimpleName();
    private SmartMirrorLogger _logger;

    private static final int PERMISSION_READ_CALENDAR_ID = 69;
    private static final int INVERT_TIME = 1000;
    private static final int MAX_CALENDAR_COUNT = 2;

    private boolean _calendarPermissionGranted;
    private boolean _isInitialized;
    private boolean _screenEnabled;

    private Context _context;
    private BroadcastController _broadcastController;
    private PermissionController _permissionController;
    private ReceiverController _receiverController;

    private View _birthdayAlarmView;
    private TextView _birthdayTextView;
    private boolean _hasBirthday;
    private Handler _updateBirthdayAlarmHandler = new Handler();

    private SerializableList<CalendarEntryDto> _calendarList = new SerializableList<>();
    private View[] _calendarAlarmViewArray = new View[MAX_CALENDAR_COUNT];
    private TextView[] _calendarTextViewArray = new TextView[MAX_CALENDAR_COUNT];
    private boolean[] _isToday = new boolean[MAX_CALENDAR_COUNT];
    private Handler _updateCalendarAlarmHandler = new Handler();

    private BroadcastReceiver _dateChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_dateChangedReceiver onReceive");
            final String action = intent.getAction();
            if (action.equals(Intent.ACTION_DATE_CHANGED)) {
                _logger.Debug("ACTION_DATE_CHANGED");

                _broadcastController.SendSimpleBroadcast(Broadcasts.PERFORM_BIRTHDAY_UPDATE);

                if (_calendarPermissionGranted) {
                    _broadcastController.SendSimpleBroadcast(Broadcasts.PERFORM_CALENDAR_UPDATE);
                } else {
                    _logger.Error("No permission to read calendar!");
                    Toasty.error(_context, "No permission to read calendar!", Toast.LENGTH_LONG).show();
                }
            }
        }
    };

    private BroadcastReceiver _permissionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_permissionReceiver onReceive");

            int permissionGrantedResult = intent.getIntExtra(guepardoapps.library.lucahome.common.constants.Bundles.PERMISSION_READ_CALENDAR, -1);
            _calendarPermissionGranted = permissionGrantedResult == PackageManager.PERMISSION_GRANTED;
            _logger.Info(String.format("Permission READ_CALENDAR result %s is granted %s!", permissionGrantedResult, _calendarPermissionGranted));

            if (_calendarPermissionGranted) {
                _broadcastController.SendSimpleBroadcast(Broadcasts.PERFORM_CALENDAR_UPDATE);
            } else {
                _logger.Error("No permission to read calendar!");
                Toasty.error(_context, "No permission to read calendar!", Toast.LENGTH_LONG).show();
            }
        }
    };

    private BroadcastReceiver _screenDisableReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _screenEnabled = false;
            _updateBirthdayAlarmHandler.removeCallbacks(_updateBirthdayAlarmViewRunnable);
            _updateCalendarAlarmHandler.removeCallbacks(_updateCalendarAlarmViewRunnable);
        }
    };

    private BroadcastReceiver _screenEnableReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            initializeViews();

            _broadcastController.SendSimpleBroadcast(Broadcasts.PERFORM_BIRTHDAY_UPDATE);
            if (_calendarPermissionGranted) {
                _broadcastController.SendSimpleBroadcast(Broadcasts.PERFORM_CALENDAR_UPDATE);
            } else {
                _logger.Error("No permission to read calendar!");
                Toasty.error(_context, "No permission to read calendar!", Toast.LENGTH_LONG).show();
            }
        }
    };

    private BroadcastReceiver _updateBirthdayViewReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_updateBirthdayViewReceiver onReceive");
            SerializableList<BirthdayDto> birthdayList;

            if (!_screenEnabled) {
                _logger.Debug("Screen is not enabled!");
                return;
            }

            _logger.Debug("Receiving Serializable object from intent and bundle birthdayModel and casting it to SerializableList<BirthdayDto>");
            birthdayList = (SerializableList<BirthdayDto>) intent.getSerializableExtra(Bundles.BIRTHDAY_MODEL);

            _logger.Debug("Trying to work with received birthdayList");
            if (birthdayList != null) {
                _logger.Debug("birthdayList: " + birthdayList.toString());

                for (int index = 0; index < birthdayList.getSize(); index++) {
                    BirthdayDto entry = birthdayList.getValue(index);
                    _logger.Debug(String.format(Locale.GERMAN, "Birthday: %s", entry));

                    if (entry != null) {
                        if (entry.HasBirthday()) {
                            _logger.Debug("Entry has today birthday!");
                            _hasBirthday = true;
                            _birthdayTextView.setText(entry.GetNotificationBody(entry.GetAge()));
                            _birthdayAlarmView.setVisibility(View.VISIBLE);
                            checkPlayBirthdaySong(entry);
                        } else {
                            _hasBirthday = false;
                            _birthdayTextView.setText(entry.GetName() + ": " + entry.GetBirthdayString());
                            _birthdayAlarmView.setVisibility(View.INVISIBLE);
                        }
                    } else {
                        _logger.Warn("Birthday entry is null!");
                        _hasBirthday = false;
                        _birthdayTextView.setText("");
                        _birthdayAlarmView.setVisibility(View.INVISIBLE);
                    }
                }

                _updateBirthdayAlarmHandler.removeCallbacks(_updateBirthdayAlarmViewRunnable);
                _updateBirthdayAlarmHandler.postDelayed(_updateBirthdayAlarmViewRunnable, INVERT_TIME);
            } else {
                _logger.Warn("birthdayList is null!");
            }
        }

        private void checkPlayBirthdaySong(@NonNull BirthdayDto entry) {
            _logger.Debug("checkPlayBirthdaySong");
            if (entry.GetName().contains("Sandra Huber")
                    || entry.GetName().contains("Jonas Schubert")) {
                _broadcastController.SendSimpleBroadcast(Broadcasts.PLAY_BIRTHDAY_SONG);
            }
        }
    };

    private BroadcastReceiver _updateCalendarViewReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!_screenEnabled) {
                _logger.Debug("Screen is not enabled!");
                return;
            }

            _logger.Debug("_updateCalendarViewReceiver onReceive");
            @SuppressWarnings("unchecked")
            SerializableList<CalendarEntryDto> calendarlist = (SerializableList<CalendarEntryDto>) intent.getSerializableExtra(Bundles.CALENDAR_MODEL);
            if (calendarlist != null) {
                _logger.Debug(calendarlist.toString());
                _calendarList.clear();

                for (int index = 0; index < calendarlist.getSize(); index++) {
                    CalendarEntryDto entry = calendarlist.getValue(index);

                    if (entry.BeginIsAfterNow()) {
                        _logger.Debug(entry.toString() + ": begin is after now!");
                        _calendarList.addValue(entry);
                    }
                }

                for (int index = 0; index < MAX_CALENDAR_COUNT; index++) {
                    _isToday[index] = false;
                }

                for (int index = 0; index < MAX_CALENDAR_COUNT; index++) {
                    if (index < _calendarList.getSize()) {
                        CalendarEntryDto entry = _calendarList.getValue(index);
                        if (entry.IsToday()) {
                            _logger.Debug(entry.toString() + " is today!");
                            _isToday[index] = true;
                            _calendarAlarmViewArray[index].setVisibility(View.VISIBLE);
                        } else {
                            _logger.Debug(entry.toString() + " is not today!");
                            _isToday[index] = false;
                            _calendarAlarmViewArray[index].setVisibility(View.INVISIBLE);
                        }
                        _calendarTextViewArray[index].setText(entry.GetMirrorText());
                    } else {
                        _calendarTextViewArray[index].setVisibility(View.INVISIBLE);
                        _calendarAlarmViewArray[index].setVisibility(View.INVISIBLE);
                    }
                }

                _updateCalendarAlarmHandler.removeCallbacks(_updateCalendarAlarmViewRunnable);
                _updateCalendarAlarmHandler.postDelayed(_updateCalendarAlarmViewRunnable, INVERT_TIME);
            }
        }
    };

    private Runnable _updateBirthdayAlarmViewRunnable = new Runnable() {
        private boolean _invert;

        public void run() {
            if (!_screenEnabled) {
                _logger.Debug("Screen is not enabled!");
                return;
            }

            if (_hasBirthday) {
                if (_invert) {
                    _birthdayAlarmView.setBackgroundResource(R.drawable.circle_red);
                } else {
                    _birthdayAlarmView.setBackgroundResource(R.drawable.circle_yellow);
                }
            }

            _invert = !_invert;
            _updateBirthdayAlarmHandler.postDelayed(this, INVERT_TIME);
        }
    };

    private Runnable _updateCalendarAlarmViewRunnable = new Runnable() {
        private boolean _invert;

        public void run() {
            if (!_screenEnabled) {
                _logger.Debug("Screen is not enabled!");
                return;
            }

            for (int index = 0; index < MAX_CALENDAR_COUNT; index++) {
                if (_isToday[index]) {
                    if (_invert) {
                        _calendarAlarmViewArray[index].setBackgroundResource(R.drawable.circle_red);
                    } else {
                        _calendarAlarmViewArray[index].setBackgroundResource(R.drawable.circle_yellow);
                    }
                }
            }

            _invert = !_invert;
            _updateCalendarAlarmHandler.postDelayed(this, INVERT_TIME);
        }
    };

    public EventViewController(@NonNull Context context) {
        _logger = new SmartMirrorLogger(TAG);
        _context = context;
        _broadcastController = new BroadcastController(_context);
        _permissionController = new PermissionController(_context);
        _receiverController = new ReceiverController(_context);
    }

    public void onCreate() {
        _logger.Debug("onCreate");
        initializeViews();
    }

    public void onPause() {
        _logger.Debug("onPause");
    }

    public void onResume() {
        _logger.Debug("onResume");
        if (!_isInitialized) {
            _logger.Debug("Initializing!");

            _receiverController.RegisterReceiver(_dateChangedReceiver, new String[]{Intent.ACTION_DATE_CHANGED});
            _receiverController.RegisterReceiver(_permissionReceiver, new String[]{guepardoapps.library.lucahome.common.constants.Broadcasts.PERMISSION_READ_CALENDAR});
            _receiverController.RegisterReceiver(_screenDisableReceiver, new String[]{Broadcasts.SCREEN_OFF});
            _receiverController.RegisterReceiver(_screenEnableReceiver, new String[]{Broadcasts.SCREEN_ENABLED});
            _receiverController.RegisterReceiver(_updateBirthdayViewReceiver, new String[]{Broadcasts.SHOW_BIRTHDAY_MODEL});
            _receiverController.RegisterReceiver(_updateCalendarViewReceiver, new String[]{Broadcasts.SHOW_CALENDAR_MODEL});

            _isInitialized = true;

            if (_screenEnabled) {
                _permissionController.CheckPermissions(PERMISSION_READ_CALENDAR_ID, Manifest.permission.READ_CALENDAR);
            }
        } else {
            _logger.Warn("Is ALREADY initialized!");
        }
    }

    public void onDestroy() {
        _logger.Debug("onDestroy");
        _receiverController.Dispose();
        _isInitialized = false;
    }

    private void initializeViews() {
        _logger.Debug("initializeViews");

        _screenEnabled = true;

        _birthdayAlarmView = ((Activity) _context).findViewById(R.id.birthdayAlarmView);
        _birthdayTextView = (TextView) ((Activity) _context).findViewById(R.id.birthdayTextView);

        _calendarAlarmViewArray[0] = ((Activity) _context).findViewById(R.id.calendar1AlarmView);
        _calendarTextViewArray[0] = (TextView) ((Activity) _context).findViewById(R.id.calendar1TextView);
        _calendarAlarmViewArray[1] = ((Activity) _context).findViewById(R.id.calendar2AlarmView);
        _calendarTextViewArray[1] = (TextView) ((Activity) _context).findViewById(R.id.calendar2TextView);
    }
}
