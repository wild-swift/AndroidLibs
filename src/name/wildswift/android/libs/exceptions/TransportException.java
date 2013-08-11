package name.wildswift.android.libs.exceptions;

/**
 * @author Wild Swift
 */
public class TransportException extends NetworkException{
	public TransportException() {
	}

	public TransportException(String detailMessage) {
		super(detailMessage);
	}

	public TransportException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public TransportException(Throwable throwable) {
		super(throwable);
	}
}
