package radams.gracenote.webapi;

// A simple class to encapsulate OET data

public class GracenoteMetadataOET {
	private String _id = "";
	private String _text = "";

	public GracenoteMetadataOET(final String id, final String text) {
		_id = id;
		_text = text;
	}

	public String getID() {
		return _id;
	}

	public String getText() {
		return _text;
	}

	public void print() {
		System.out.println(toString());
	}

	@Override
	public String toString() {
		return "OET id:" + _id + "| Text:" + _text;
	}
}
