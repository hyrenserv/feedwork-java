package fd.ng.cmdtools.loadrunner;

public class TaskException extends Exception {
	public TaskException(Throwable cause){
		super(cause);
	}
	public TaskException(String message){
		super(message);
	}
}
