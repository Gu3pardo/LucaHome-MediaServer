package guepardoapps.mediamirror.rss;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;

import guepardoapps.library.lucahome.common.enums.RSSFeed;

import guepardoapps.mediamirror.common.SmartMirrorLogger;

public class RssService extends IntentService {

    private static final String TAG = RssService.class.getSimpleName();
    private SmartMirrorLogger _logger;

    private static final RSSFeed DEFAULT_FEED = RSSFeed.DEFAULT;

    public static final String ITEMS = "items";
    public static final String TITLE = "title";
    public static final String RECEIVER = "receiver";
    public static final String FEED = "feed";

    public RssService() {
        super(RssService.class.getName());
        _logger = new SmartMirrorLogger(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        _logger.Debug("Service started");

        RSSFeed feed = (RSSFeed) intent.getSerializableExtra(FEED);
        if (feed == null) {
            feed = DEFAULT_FEED;
        }

        List<RssItem> rssItems = null;
        try {
            RssParser parser = new RssParser();
            rssItems = parser.Parse(GetInputStream(feed.GetUrl()));
        } catch (XmlPullParserException e) {
            _logger.Error(e.getMessage());
        } catch (IOException e) {
            _logger.Warn(e.getMessage());
        }

        Bundle bundle = new Bundle();
        bundle.putString(TITLE, feed.GetTitle());
        bundle.putSerializable(ITEMS, (Serializable) rssItems);

        ResultReceiver receiver = intent.getParcelableExtra(RECEIVER);
        receiver.send(0, bundle);
    }

    public InputStream GetInputStream(@NonNull String link) {
        try {
            URL url = new URL(link);
            return url.openConnection().getInputStream();
        } catch (IOException e) {
            _logger.Warn("Exception while retrieving the input stream");
            return null;
        }
    }
}
