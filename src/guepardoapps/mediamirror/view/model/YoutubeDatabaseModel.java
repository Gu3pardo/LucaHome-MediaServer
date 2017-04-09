package guepardoapps.mediamirror.view.model;

import java.io.Serializable;

public class YoutubeDatabaseModel implements Serializable {

	private static final long serialVersionUID = 1167790558127364021L;
	
	private int _id;
	private String _youtubeId;
	private int _playCount;

	public YoutubeDatabaseModel(int id, String youtubeId, int playCount) {
		_id = id;
		_youtubeId = youtubeId;
		_playCount = playCount;
	}

	public int GetId() {
		return _id;
	}

	public String GetYoutubeId() {
		return _youtubeId;
	}

	public int GetPlayCount() {
		return _playCount;
	}

	public void IncreasePlayCount() {
		_playCount++;
	}

	public String GetCommunicationString() {
		return _id + "." + _youtubeId + "." + _playCount + ";";
	}

	@Override
	public String toString() {
		return YoutubeDatabaseModel.class.getName() + ":{_id:" + _id + ";_youtubeId:" + _youtubeId + ";_playCount:"
				+ String.valueOf(_playCount) + "}";
	}
}
