package lexico;

public class Error {
	private String error;

    public Error(String error) {
        this.error = error;
    }

    public String printError() {
        return error;
    }
}
