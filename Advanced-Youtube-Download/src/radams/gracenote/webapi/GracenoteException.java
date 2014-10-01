package radams.gracenote.webapi;

public class GracenoteException extends Exception {
	private static final long serialVersionUID = 3279913248865272991L;
	private String _message = "";

	public GracenoteException(final String message) {
		super();
		_message = message;
	}

	@Override
	public String getMessage() {
		return _message;
	}
}
