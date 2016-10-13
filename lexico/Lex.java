package lexico;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class Lex {
	private static ArrayList<Token> alTokens = new ArrayList<Token>();
	private static ArrayList<Error> alErrors = new ArrayList<Error>();
	private Token lookaheadToken;
	private static char cAnterior = ' ';
	private static boolean bAnterior = false;
	private static int linea = 1;
	private static BufferedReader br;
	private static boolean fiExplore = false;
	
	public Lex(BufferedReader brReader) {
		br = brReader;
	}

	public boolean exploreToken() {
		int estat = 0;
		int numCarID = 0;
		String id = "";
		String numero = "";
		String stringToken = "";
		char c;
		if (fiExplore) {
			lookaheadToken = new Token("","");
			return false;
		}
		if (!bAnterior) {
			c = leerCaracter(br);
		} else {
			c = cAnterior;
			bAnterior = false;
		}
		while (true) {
			switch (estat) {
				case 0:
					if ((c>='a' && c<='z') || (c >= 'A' && c<= 'Z')) {
						//System.out.print("\nEs una letra "+c);
						id = "" + c;
						numCarID = 1;
						estat = 44;
					} else if (c>='0' && c<='9') {
						//System.out.print("\nEs un digito "+c);
						estat = 2;
					} else {
						//System.out.print("\nEs un simbolo "+c);
						estat = 3;
					}
					break;
				case 2:
					numero += c;
					c = leerCaracter(br);
					if (c<'0' || c>'9') {
						cAnterior = c;
						bAnterior = true;
						lookaheadToken = crearToken("CTE_ENTERA",numero);
						return true;
					}
					break;
				case 3:
					if (c == '=') {
						estat = 4;
					} else if (c == '>') {
						estat = 6;
					} else if (c == '<') {
						estat = 8;
					} else if (c == '+') {
						estat = 11;
					} else if (c == '-') {
						estat = 12;
					} else if (c == '*') {
						estat = 13;
					} else if (c == '/') {
						estat = 14;
					} else if (c == '"') {
						estat = 18;
					} else if (c == 9 || c == 32) {
						estat = 20;
					} else if (c == 13) {
						estat = 21;
					} else if (c == ';') {
						estat = 23;
					} else if (c == ':') {
						estat = 24;
					} else if (c == '(') {
						estat = 25;
					} else if (c == ')') {
						estat = 26;
					} else if (c == '[') {
						estat = 27;
					} else if (c == ']') {
						estat = 28;
					} else if (c == ',') {
						estat = 29;
					} else if (c == '?') {
						estat = 30;
					} else if (c == '.') {
						estat = 31;
					} else if (c == '\0') {
						fiExplore = true;
						lookaheadToken = crearToken("","");
						return false;
					} else {
						//System.out.println("\nValor simbolo leido: "+(int)c);
						estat = 33;
					}
					break;
				case 4:
					c = leerCaracter(br);
					if (c == '=') {
						estat = 5;
					} else {
						cAnterior = c;
						bAnterior = true;
						lookaheadToken = crearToken("=","");
						return true;
					}
					break;
				case 5:
					lookaheadToken = crearToken("OPER_RELACIONAL","==");
					return true;
				case 6:
					c = leerCaracter(br);
					if (c == '=') {
						estat = 7;
					} else {
						cAnterior = c;
						bAnterior = true;
						lookaheadToken = crearToken("OPER_RELACIONAL",">");
						return true;
					}
					break;
				case 7:
					lookaheadToken = crearToken("OPER_RELACIONAL",">=");
					return true;
				case 8:
					c = leerCaracter(br);
					if (c == '=') {
						estat = 9;
					} else if (c == '>') {
						estat = 10;
					} else {
						cAnterior = c;
						bAnterior = true;
						lookaheadToken = crearToken("OPER_RELACIONAL","<");
						return true;
					}
					break;
				case 9:
					lookaheadToken = crearToken("OPER_RELACIONAL","<=");
					return true;
				case 10:
					lookaheadToken = crearToken("OPER_RELACIONAL","<>");
					return true;
				case 11:
					lookaheadToken = crearToken("OPER_SUMARESTA","+");
					return true;
				case 12:
					lookaheadToken = crearToken("OPER_SUMARESTA","-");
					return true;
				case 13:
					lookaheadToken = crearToken("OPER_MULDIV","*");
					return true;
				case 14:
					c = leerCaracter(br);
					if (c == '/') {
						estat = 15;
						stringToken = "/";
					} else {
						cAnterior = c;
						bAnterior = true;
						lookaheadToken = crearToken("OPER_MULDIV","/");
						return true;
					}
					break;
				case 15:
					//Para los comentarios, se va de ellos completamente si detecta un error
					stringToken += c;
					c = leerCaracter(br);
					if (c >= 32) {
						estat = 16;
					} else if (c == 13) {
						estat = 17;
					} else if (c != 9){
						//String s = ""+c;
						crearError(5,stringToken);
						estat = 0;
					}
					break;
				case 16:
					stringToken += c;
					c = leerCaracter(br);
					if (c == 13) {
						estat = 17;
					} else if (c >= 32) {
						estat = 16;
					} else if (c != 9){
						//String s = ""+c;
						crearError(5,stringToken);
						estat = 0;
					}
					break;
				case 17:
					estat = 0;
					break;
				case 18:
					stringToken += c;
					c = leerCaracter(br);
					if (c == '"') {
						estat = 19;
					} else if (c < 32) {
						//String s = ""+c;
						crearError(4,stringToken);
						stringToken = "";
						estat = 0;
					}
					break;
				case 19:
					stringToken += c;
					lookaheadToken = crearToken("CTE_CADENA",stringToken);
					return true;
				case 20:
					c = leerCaracter(br);
					estat = 0;
					break;
				case 21:
					c = leerCaracter(br);
					if (c == 10) {
						estat = 22;
					} else {
						String s = ""+c;
						crearError(7,s);
						estat = 0;
					}
					break;
				case 22:
					linea++;
					c = leerCaracter(br);
					estat = 0;
					break;
				case 23:
					lookaheadToken = crearToken(";","");
					return true;
				case 24:
					lookaheadToken = crearToken(":","");
					return true;
				case 25:
					lookaheadToken = crearToken("(","");
					return true;
				case 26:
					lookaheadToken = crearToken(")","");
					return true;
				case 27:
					lookaheadToken = crearToken("[","");
					return true;
				case 28:
					lookaheadToken = crearToken("]","");
					return true;
				case 29:
					lookaheadToken = crearToken(",","");
					return true;
				case 30:
					lookaheadToken = crearToken("?","");
					return true;
				case 31:
					c = leerCaracter(br);
					if (c == '.') {
						estat = 32;
					} else {
						String s = ""+c;
						crearError(3,s);
						estat = 0;
					}
					break;
				case 32:
					lookaheadToken = crearToken("..","");
					return true;
				case 33:
					String s = ""+c;
					crearError(2,s);
					c = leerCaracter(br);
					estat = 0;
					break;
				case 39:
					lookaheadToken = crearToken("TIPO_PREDEFINIDO","sencer");
					return true;
				case 41:
					lookaheadToken = crearToken("si","");
					return true;
				case 43:
					lookaheadToken = crearToken("sino","");
					return true;
				case 51:
					lookaheadToken = crearToken("llavors","");
					return true;
				case 55:
					lookaheadToken = crearToken("llegir","");
					return true;
				case 44:
					c = leerCaracter(br);
					//System.out.print(c);
					if (c==0 || ((c<'a' || c>'z') && (c<'A' || c>'Z') && (c<'0' || c>'9') && c!='_')) {
						//System.out.println("Es una letra "+c);
						cAnterior = c;
						bAnterior = true;
						if (numCarID >= 32) {
							String s1 = ""+c;
							crearError(10,s1);
							estat = 0;
						} else {
							id = lowerCase(id);
							if (id.equals("sencer")) {
								estat = 39;
							} else if (id.equals("si")) {
								estat = 41;
							} else if (id.equals("sino")) {
								estat = 43;
							} else if (id.equals("llegir")) {
								estat = 55;
							} else if (id.equals("llavors")) {
								estat = 51;
							} else if (id.equals("and")) {
								estat = 59;
							} else if (id.equals("or")) {
								estat = 64;
							} else if (id.equals("not")) {
								estat = 69;
							} else if (id.equals("var")) {
								estat = 75;
							} else if (id.equals("vector")) {
								estat = 80;
							} else if (id.equals("de")) {
								estat = 82;
							} else if (id.equals("escriure")) {
								estat = 91;
							} else if (id.equals("en")) {
								estat = 84;
							} else if (id.equals("cert")) {
								estat = 95;
							} else if (id.equals("cicle")) {
								estat = 100;
							} else if (id.equals("const")) {
								estat = 104;
							} else if (id.equals("prog")) {
								estat = 108;
							} else if (id.equals("perref")) {
								estat = 113;
							} else if (id.equals("perval")) {
								estat = 116;
							} else if (id.equals("percada")) {
								estat = 120;
							} else if (id.equals("logic")) {
								estat = 125;
							} else if (id.equals("retornar")) {
								estat = 133;
							} else if (id.equals("mentre")) {
								estat = 139;
							} else if (id.equals("func")) {
								estat = 143;
							} else if (id.equals("funcio")) {
								estat = 145;
							} else if (id.equals("fisi")) {
								estat = 148;
							} else if (id.equals("fals")) {
								estat = 152;
							} else if (id.equals("fer")) {
								estat = 154;
							} else if (id.equals("fins")) {
								estat = 156;
							} else if (id.equals("fiper")) {
								estat = 159;
							} else if (id.equals("fiprog")) {
								estat = 167;
							} else if (id.equals("fifunc")) {
								estat = 163;
							} else if (id.equals("fimentre")) {
								estat = 173;
							} else {
								lookaheadToken = crearToken("ID",id);
								return true;
							}
						}
					} else {
						numCarID++;
						id += c;
					}
					break;
				case 59:
					lookaheadToken = crearToken("and","");
					return true;
				case 64:
					lookaheadToken = crearToken("or","");
					return true;
				case 69:
					lookaheadToken = crearToken("not","");
					return true;
				case 75:
					lookaheadToken = crearToken("var","");
					return true;
				 case 80:
					 lookaheadToken = crearToken("vector","");
					 return true;
				 case 82:
					 lookaheadToken = crearToken("de","");
					 return true;
				 case 84:
					 lookaheadToken = crearToken("en","");
					 return true;
				 case 91:
					 lookaheadToken = crearToken("escriure","");
					 return true;
				 case 95:
					 lookaheadToken = crearToken("CTE_LOGICA","cert");
					 return true;
				 case 100:
					 lookaheadToken = crearToken("cicle","");
					 return true;
				 case 104:
					 lookaheadToken = crearToken("const","");
					 return true;
				 case 108: 
					 lookaheadToken = crearToken("prog","");
					 return true;
				 case 113:
					 lookaheadToken = crearToken("PER_VALREF","perref");
					 return true;
				 case 116:
					 lookaheadToken = crearToken("PER_VALREF","perval");
					 return true;
				 case 120:
					 lookaheadToken = crearToken("percada","");
					 return true;
				 case 125:
					 lookaheadToken = crearToken("TIPO_PREDEFINIDO","logic");
					 return true;
				 case 133:
					 lookaheadToken = crearToken("retornar","");
					 return true;
				 case 139:
					 lookaheadToken = crearToken("mentre","");
					 return true;
				 case 143:
					 lookaheadToken = crearToken("func","");
					 return true;
				 case 145:
					 lookaheadToken = crearToken("funcio","");
					 return true;
				 case 148:
					 lookaheadToken = crearToken("fisi","");
					 return true;
				 case 152:
					 lookaheadToken = crearToken("CTE_LOGICA", "fals");
					 return true;
				 case 154:
					 lookaheadToken = crearToken("fer","");
					 return true;
				 case 156:
					 lookaheadToken = crearToken("fins","");
					 return true;
				 case 159:
					 lookaheadToken = crearToken("fiper","");
					 return true;
				 case 163:
					 lookaheadToken = crearToken("fifunc","");
					 return true;
				 case 167:
					 lookaheadToken = crearToken("fiprog","");
					 return true;
				 case 173:
					 lookaheadToken = crearToken("fimentre","");
					 return true;
				 default:
					 //Nunca entrara en default del bucle de lectura
					 System.out.println("Nunca entrara en default del bucle de lectura");
					 break;
			}
		}
	}
	
	private static Token crearToken(String token, String lexema) {
		Token t = new Token(token, lexema);
		return t;
	}
	
	public static void crearError(int error, String c) {
		switch(error) {
			//ERRORES BASE Y LEXICO
			case 0:
				Error err = new Error("[ERR_BASE_0] Error al intentar cerrar el fichero");
				alErrors.add(err);
				break;
			case 1:
				Error err1 = new Error("[ERR_BASE_1] Error al intentar abrir el fichero");
				alErrors.add(err1);
				break;
			case 2:
				String l1 = Integer.toString(linea);
				Error err2;
				if ((Integer.parseInt(c) >= 32) && (Integer.parseInt(c) <= 126)) {
					err2 = new Error("[ERR_LEX_0] Carácter "+c+" erróneo en línea "+l1);
				} else {
					err2 = new Error("[ERR_LEX_0] Carácter con ASCII "+Integer.parseInt(c)+" erróneo en línea "+l1);
				}
				alErrors.add(err2);
				break;
			case 3:
				String l2 = Integer.toString(linea);
				Error err3 = new Error("[ERR_LEX_1] Carácter . no va acompañado de otro . en línea "+l2);
				alErrors.add(err3);
				break;
			case 4:
				String l3 = Integer.toString(linea);
				Error err4 = new Error("[ERR_LEX_2] String <"+c+"> erróneo en la línea "+l3);
				alErrors.add(err4);
				break;
			case 5:
				String l4 = Integer.toString(linea);
				Error err5 = new Error("[ERR_LEX_3] Comentario <"+c+"> erróneo en la línea "+l4);
				alErrors.add(err5);
				break;
			case 6:
				Error err6 = new Error("[ERR_BASE_2] Error al intentar leer un carácter del fichero");
				alErrors.add(err6);
				break;
			case 7:
				String l6 = Integer.toString(linea);
				Error err7 = new Error("[ERR_LEX_4] Carácter New Line no va acompañado de Carriage Return en linea "+l6);
				alErrors.add(err7);
				break;
			case 8:
				Error err8 = new Error("[ERR_BASE_3] Error al intentar abrir o crear el fichero .lex");
				alErrors.add(err8);
			case 9:
				Error err9 = new Error("[ERR_BASE_4] Error al intentar abrir o crear el fichero .err");
				alErrors.add(err9);
			case 10:
				String l7 = Integer.toString(linea);
				Error err10 = new Error("[ERR_LEX_5] Id en linea "+l7+" no puede tener más de 32 caracteres");
				alErrors.add(err10);
				break;
			default:
				//Nunca entrará aquí
				System.out.println("Nunca entrará en default de Errors");
				break;
		}
	}
	
	public static void crearErrorSintactico(int error, String c) {
			//ERRORES SINTACTICOS
		switch(error) {
			case 20:
				String s1 = Integer.toString(linea);
				Error err20 = new Error("[ERR_SIN_1] "+s1+", Falta un "+c);
				alErrors.add(err20);
				break;
			case 21:
				String s2 = Integer.toString(linea);
				Error err21 = new Error("[ERR_SIN_2] "+s2+", S'esperava un token "+c+" per a poder tancar");
				alErrors.add(err21);
				break;
			case 22:
				String s3 = Integer.toString(linea);
				Error err22 = new Error("[ERR_SIN_3] "+s3+", Constant erronea, s'esperava un token "+c);
				alErrors.add(err22);
				break;
			case 23:
				String s4 = Integer.toString(linea);
				Error err23 = new Error("[ERR_SIN_4] "+s4+", Falta una "+c+" al final de la linea");
				alErrors.add(err23);
			case 24:
				String s5 = Integer.toString(linea);
				Error err24 = new Error("[ERR_SIN_5] "+s5+", Var erronea, s'esperava un token "+c);
				alErrors.add(err24);
				break;
			case 25:
				String s6 = Integer.toString(linea);
				Error err25 = new Error("[ERR_SIN_6] "+s6+", Funcio erronea, s'esperava un token "+c);
				alErrors.add(err25);
				break;
			case 26:
				String s7 = Integer.toString(linea);
				Error err26 = new Error("[ERR_SIN_7] "+s7+", Parametre erroni, s'esperava un token "+c);
				alErrors.add(err26);
				break;
			default:
				//Nunca entrará aquí
				System.out.println("Nunca entrará en default de Errors");
				break;
		}
	}
	
	private static String lowerCase(String toLower) {
		char c;
		char newC;
		for (int i = 0; i < toLower.length(); i++) {
			c = toLower.charAt(i);
			if (c >= 'A' && c <= 'Z') {
				newC = (char) (c-'A'+'a');
				toLower = toLower.replace(c,newC);
			}		
		}
		return toLower;
	}

	private static char leerCaracter(BufferedReader br) {
		int i;
		try {
			i = br.read();
			if (i != -1) {
				return (char) i;
			} else {
				fiExplore = true;
				return 0;
			}
		} catch (IOException e) {
			crearError(6,"\0");
		}
		return '\0';

	}

	public void printLex(PrintWriter writerLEX) {
		if (alTokens.size()>0) {
			//PrintWriter writerLEX = new PrintWriter(name+".lex", "UTF-8");
			for (int i = 0; i < alTokens.size(); i++) {
				writerLEX.println("<"+alTokens.get(i).getToken()+"> "+alTokens.get(i).getLexema());
			}
			//writerLEX.close();
		}
	}

	public void printErrors(PrintWriter writerERR) {
		if (alErrors.size()>0) {
			//PrintWriter writerERR = new PrintWriter(name+".err", "UTF-8");
			for (int j = 0; j < alErrors.size(); j++) {
				writerERR.println(alErrors.get(j).printError());
			}
			//writerERR.close();
		}
	}
	
	public Token lookahead() {
		return lookaheadToken;
	}
	
	public void getNextToken() {
		if (lookaheadToken != null) alTokens.add(lookaheadToken);
		exploreToken();
	}
	
	public int getLinea() {
		return linea;
	}
	
}