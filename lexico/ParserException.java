package lexico;

@SuppressWarnings("serial")
public class ParserException extends Exception {
	public String message;
	private String eToken;
	private String eLexema;
	
	 public ParserException(String token, String lexema){
		 this.eToken = token;
		 this.eLexema = lexema;
		 this.message = "Ha fallado en token "+token+" y lexema "+lexema;
	 }
	 
	 public String getErrorToken() {
		 return eToken;
	 }
	 
	 public String getErrorLexema() {
		 return eLexema;
	 }
	 
	 @Override
    public String getMessage(){
        return message;
    }
}
