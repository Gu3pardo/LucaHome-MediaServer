package guepardoapps.mediamirror.rss;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

public class RssParser {

	private static final int MAX_ITEM_COUNT = 100;
	private static final String NAME_SPACE = null;

	public List<RssItem> Parse(InputStream inputStream) throws XmlPullParserException, IOException {
		if (inputStream != null) {
			try {
				XmlPullParser parser = Xml.newPullParser();
				parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
				parser.setInput(inputStream, null);
				parser.nextTag();
				return readFeed(parser);
			} finally {
				inputStream.close();
			}
		}
		return null;
	}

	private List<RssItem> readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
		int itemCount = 0;
		parser.require(XmlPullParser.START_TAG, null, "rss");

		String title = null;
		String description = null;
		String link = null;
		String guid = null;

		List<RssItem> items = new ArrayList<RssItem>();
		while (parser.next() != XmlPullParser.END_DOCUMENT && itemCount < MAX_ITEM_COUNT) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();
			if (name.equals("title")) {
				title = readElement(parser, "title");
			} else if (name.equals("description")) {
				description = readElement(parser, "description");
			} else if (name.equals("link")) {
				link = readElement(parser, "link");
			} else if (name.equals("guid")) {
				guid = readElement(parser, "guid");
			}

			if (title != null && description != null && link != null && guid != null) {
				RssItem item = new RssItem(title, description, link, guid);
				items.add(item);

				title = null;
				description = null;
				link = null;
				guid = null;
			}
			
			itemCount++;
		}
		return items;
	}

	private String readElement(XmlPullParser parser, String element) throws XmlPullParserException, IOException {
		parser.require(XmlPullParser.START_TAG, NAME_SPACE, element);
		String value = readText(parser);
		parser.require(XmlPullParser.END_TAG, NAME_SPACE, element);
		return value;
	}

	private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
		String result = "";
		
		if (parser.next() == XmlPullParser.TEXT) {
			result = parser.getText();
			parser.nextTag();
		}
		
		return result;
	}
}
