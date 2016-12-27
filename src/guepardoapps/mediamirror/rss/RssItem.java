package guepardoapps.mediamirror.rss;

public class RssItem {

	private final String _title;
	private final String _description;
	private final String _link;
	private final String _guid;

	public RssItem(String title, String description, String link, String guid) {
		_title = title;
		_description = description;
		_link = link;
		_guid = guid;
	}

	public String GetTitle() {
		return _title;
	}

	public String GetDescription() {
		return _description;
	}

	public String GetLink() {
		return _link;
	}

	public String GetGuid() {
		return _guid;
	}

	@Override
	public String toString() {
		return "{RssItem: {Title: " + _title + "}{Description: " + _description + "}{Link: " + _link + "}{Guid: "
				+ _guid + "}}";
	}
}
