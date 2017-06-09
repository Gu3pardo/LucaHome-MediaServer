package guepardoapps.mediamirror.common;

import java.io.Serializable;

import guepardoapps.library.toolset.common.Logger;

import guepardoapps.mediamirror.common.constants.Enables;

public class SmartMirrorLogger extends Logger implements Serializable {
	public SmartMirrorLogger(String tag) {
		super(tag, Enables.LOGGING);
	}
}
