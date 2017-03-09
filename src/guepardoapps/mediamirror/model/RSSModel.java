package guepardoapps.mediamirror.model;

import java.io.Serializable;

import guepardoapps.lucahomelibrary.mediamirror.common.enums.RSSFeed;

public class RSSModel implements Serializable {

	private static final long serialVersionUID = -7114381765862344762L;

	private RSSFeed _rssFeed;
	private boolean _visibility;

	public RSSModel(RSSFeed rssFeed, boolean visibility) {
		_rssFeed = rssFeed;
		_visibility = visibility;
	}

	public RSSFeed GetRSSFeed() {
		return _rssFeed;
	}

	public boolean GetVisibility() {
		return _visibility;
	}

	@Override
	public String toString() {
		return RSSModel.class.getName() + ":{RSSFeed:" + _rssFeed.toString() + ";Visibility:"
				+ String.valueOf(_visibility) + "}";
	}
}
