package lexico;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import taulasimbols.*;

public class Parser {
	private static Lex l;
	private static Semantic s;
	private static String name[];
	private static BufferedReader br;
	private static PrintWriter writerLEX;
	private static PrintWriter writerERR;
	private static String currentInstr;
	
	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Ha de tener un solo fichero .bab como argumento de entrada");
			System.exit(0);
		}
		name = args[0].split("\\.",2);
		if (name.length != 2 || !name[1].equals("bab")) {
			System.out.println("Extension del fichero ha de ser .bab");
			System.exit(0);
		}
		File file = new File(args[0]);
		try {
			br = new BufferedReader(new FileReader(file));
			writerLEX = new PrintWriter(name[0]+".lex", "UTF-8");
			writerERR = new PrintWriter(name[0]+".err", "UTF-8");
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		l = new Lex(br);
		s = new Semantic(l,writerERR);
		l.getNextToken();
		try {
			P();
		} catch (ParserException e) {
			String message = e.getMessage();
			System.out.println(message);
			crearErrorSintactico("[ERROR_SIN_1] Linea "+l.getLinea()
							+", S'esperava un token "+e.getErrorToken()+" i ha rebut un "+l.lookahead().getToken());
			ArrayList<String> conjuntSinc = new ArrayList<String>();
			conjuntSinc.add("fiprog");
			Consume(conjuntSinc);
		}
		try {
			Accept("");
		} catch (ParserException e) {
			String message = e.getMessage();
			System.out.println(message);
			crearErrorSintactico("[ERROR_SIN_2] Linea "+l.getLinea()+", Hi ha codi després del fi del programa");
		}
		l.printLex(writerLEX);
		writerLEX.close();
		l.printErrors(writerERR);
		writerERR.close();
	}
	
	private static void P() throws ParserException {
		Decl();
		Accept("prog");
		Llista_Inst();
		Accept("fiprog");
		//s.treureXML(1);
	}
	
	private static void Decl() throws ParserException {
		Dec_Cte_Var();
		Dec_Fun();		
	}
	
	private static void Dec_Cte_Var() throws ParserException {
		switch(l.lookahead().getToken()) {
			case "const":
				Accept("const");
				try {
					String nom = Accept("ID").getLexema();
					Accept("=");
					int value = Integer.parseInt(Accept("CTE_ENTERA").getLexema());
					//Exp();
					//TODO AQUI GUARDA CONSTANT SI NO EXISTEIX
					s.verificarConstant(nom,value);
					Accept(";");
				} catch(ParserException e) {
					String message = e.getMessage();
					System.out.println(message);
					crearErrorSintactico("[ERROR_SIN_3] Linea "+l.getLinea()
										+", La construccio de la declaracio de la constant no es correcta");
					ArrayList<String> conjuntSinc = new ArrayList<String>();
					conjuntSinc.add("const"); conjuntSinc.add("var"); conjuntSinc.add("funcio");
					Consume(conjuntSinc);
				}
				Dec_Cte_Var();
				break;
			case "var":
				Accept("var");
				try {
					String nomVar = Accept("ID").getLexema();
					//System.out.println("Error aqui en "+nomVar);
					Accept(":");
					ITipus itip = Tipus();
					//System.out.println("No. Error aqui en"+nomVar);
					//TODO GUARDA VARIABLE SI NO EXISTEIX
					s.verificarVariable(nomVar,itip);
					Accept(";");
				} catch(ParserException e) {
					String message = e.getMessage();
					System.out.println(message);
					crearErrorSintactico("[ERROR_SIN_4] Linea "+l.getLinea()
										+", La construccio de la declaracio de la variable no es correcta");
					ArrayList<String> conjuntSinc = new ArrayList<String>();
					conjuntSinc.add("const"); conjuntSinc.add("var"); conjuntSinc.add("funcio");
					Consume(conjuntSinc);
				}
				Dec_Cte_Var();
				break;
			default:
				break;
		}
	}

	private static void Dec_Fun() throws ParserException {
		switch(l.lookahead().getToken()) {
			case "funcio":
				Accept("funcio");
				try {
					//TODO CREA BLOC FUNCIO
					s.crearBlocFuncio();
					String nomFuncio = Accept("ID").getLexema();
					Accept("(");
					Funcio func = new Funcio(nomFuncio);
					func = Llista_Param(func);
					Accept(")");
					Accept(":");
					String tipoFunc = Accept("TIPO_PREDEFINIDO").getLexema();
					//System.out.println("Tipo del retorno sera "+tipoFunc);
					//TODO GUARDA FUNCIO EN BLOC SI NO EXISTEIX
					s.guardarFuncionEnBloc(func,tipoFunc);
					Accept(";");
				} catch(ParserException e) {
					String message = e.getMessage();
					System.out.println(message);
					crearErrorSintactico("[ERROR_SIN_5] Linea "+l.getLinea()
									+", La construccio de la declaracio de la funcio no es correcta");
					ArrayList<String> conjuntSinc = new ArrayList<String>();
					conjuntSinc.add("const"); conjuntSinc.add("var"); conjuntSinc.add("func");
					Consume(conjuntSinc);
				}
				Dec_Cte_Var();
				try {
					Accept("func");
				} catch(ParserException e) {
					String message = e.getMessage();
					System.out.println(message);
					crearErrorSintactico("[ERROR_SIN_1] Linea "+l.getLinea()
									+", S'esperava un token func i ha rebut un "+l.lookahead().getToken());
					ArrayList<String> conjuntSinc2 = new ArrayList<String>();
					conjuntSinc2.add("escriure"); conjuntSinc2.add("llegir"); conjuntSinc2.add("cicle");
					conjuntSinc2.add("mentre"); conjuntSinc2.add("si"); conjuntSinc2.add("retornar");
					conjuntSinc2.add("ID"); conjuntSinc2.add("fifunc");
					Consume(conjuntSinc2);
				}
				Llista_Inst();
				try {
					//TODO COMPROVA SI HI HA UN RETORN
					s.verificarExistenciaRetorn();
					//TODO BORRA BLOC FUNCIO
					s.treureXML(0);
					s.borrarBlocFuncio();
					Accept("fifunc");
					Accept(";");
				} catch(ParserException e) {
					String message = e.getMessage();
					System.out.println(message);
					crearErrorSintactico("[ERROR_SIN_1] Linea "+l.getLinea()
									+", S'esperava un token "+e.getErrorToken()+" i ha rebut un "+l.lookahead().getToken());
					ArrayList<String> conjuntSinc3 = new ArrayList<String>();
					conjuntSinc3.add("funcio"); conjuntSinc3.add("prog"); 
					Consume(conjuntSinc3);
				}
				Dec_Fun();
				break;
			default:
				break;
		}
	}
	
	private static void crearErrorSintactico(String error) {
		writerERR.println(error);
	}

	private static Funcio Llista_Param(Funcio func) throws ParserException {
		switch(l.lookahead().getToken()) {
			case "PER_VALREF":
				String tipusPasPar = Accept("PER_VALREF").getLexema();
				String nomPar = Accept("ID").getLexema();
				Accept(":");
				ITipus itip = Tipus();
				//TODO GUARDA PARAMETRE A FUNCIO D'ENTRADA
				s.guardarParametreAFuncio(func,tipusPasPar,nomPar,itip);
				return LlistPar2(func);
				//break;
			default:
				return func;
				//break;
		}
	}
	
	private static Funcio LlistPar2(Funcio func) throws ParserException {	
		switch(l.lookahead().getToken()) {
			case ",":
				Accept(",");
				String tipusPasPar = Accept("PER_VALREF").getLexema();
				String nomPar = Accept("ID").getLexema();
				Accept(":");
				ITipus itip = Tipus();
				//TODO GUARDA PARAMETRE A FUNCIO D'ENTRADA
				s.guardarParametreAFuncio(func,tipusPasPar,nomPar,itip);
				//Tipus()
				return LlistPar2(func);
				//break;
			default:
				return func;
				//break;
		}
	}
	
	private static ITipus Tipus() throws ParserException {
		switch(l.lookahead().getToken()) {
			case "TIPO_PREDEFINIDO":
				String tipus = Accept("TIPO_PREDEFINIDO").getLexema();
				return s.verificarTipusDeTipus(tipus);
			case "vector":
				Accept("vector");
				Accept("[");
				int minVector = Integer.parseInt(Accept("CTE_ENTERA").getLexema());
				//ES SOLO CONSTANTE ENTERA.......Exp();
				Accept("..");
				int maxVector = Integer.parseInt(Accept("CTE_ENTERA").getLexema());
				s.verificarMinMaxVector(minVector,maxVector);
				//ES SOLO CONSTANTE ENTERA.......Exp();
				Accept("]");
				Accept("de");
				String tipusVar = Accept("TIPO_PREDEFINIDO").getLexema();
				return s.crearTipusArray(tipusVar,minVector,maxVector);
			default:
				throw new ParserException(l.lookahead().getToken(), l.lookahead().getLexema());
		}
	}
	
	private static ITipus Exp() throws ParserException {
		ITipus itip;
		itip = Exp_Simple();
		itip = Exp_2(itip);
		return itip;
	}
	
	private static ITipus Exp_2(ITipus itip) throws ParserException {
		switch(l.lookahead().getToken()) {
			case "OPER_RELACIONAL":
				Accept("OPER_RELACIONAL");
				ITipus itip2 = Exp_Simple();
				return s.verificarTipusExp2(itip,itip2);
			default:
				return itip;
		}
	}
	
	private static ITipus Exp_Simple() throws ParserException {
		String signe = ExpSi1();
		ITipus itip = Terme();
		itip = s.verificarExpTipusAmbSigne(itip,signe,null);
		itip = Exp_Simple2(itip);
		return itip;
	}
	
	private static String ExpSi1() throws ParserException {
		switch(l.lookahead().getToken()) {
			case "OPER_SUMARESTA":
				return Accept("OPER_SUMARESTA").getLexema();
			case "not":
				Accept("not");
				return "not";
			default:
				return "";
		}
	}
	
	private static ITipus Exp_Simple2(ITipus itip) throws ParserException {
		switch(l.lookahead().getToken()) {
			case "OPER_SUMARESTA":
				String signe = Accept("OPER_SUMARESTA").getLexema();
				ITipus itip2 = Terme();
				itip2 = s.verificarExpTipusAmbSigne(itip,signe,itip2);
				itip2 = Exp_Simple2(itip2);
				return itip2;
			case "or":
				Accept("or");
				ITipus itip3 = Terme();
				itip3 = s.verificarExpTipusAmbSigne(itip,"or",itip3);
				itip3 = Exp_Simple2(itip3);
				return itip3;
			default:
				return itip;
		}
	}
	
	private static ITipus Terme() throws ParserException {
		ITipus itip = Factor();
		itip = Terme_Factor(itip);
		return itip;
	}
	
	private static ITipus Terme_Factor(ITipus itip) throws ParserException {
		switch(l.lookahead().getToken()) {
			case "OPER_MULDIV":
				String signe = Accept("OPER_MULDIV").getLexema();
				ITipus itip2 = Factor();
				s.verificarFactorTipusAmbSigne(itip, signe, itip2);
				itip2 = Terme_Factor(itip2);
				return itip2;
			case "and":
				Accept("and");
				ITipus itip3 = Factor();
				s.verificarFactorTipusAmbSigne(itip, "and", itip3);
				itip3 = Terme_Factor(itip3);
				return itip3;
			default:
				return itip;
		}
	}
	
	private static ITipus Factor() throws ParserException {
		switch(l.lookahead().getToken()) {
			case "CTE_ENTERA":
				Accept("CTE_ENTERA");
				return s.getTipusSencer();
			case "CTE_LOGICA":
				Accept("CTE_LOGICA");
				return s.getTipusLogic();
				//break;
			case "CTE_CADENA":
				String valorString = Accept("CTE_CADENA").getLexema();
				return s.crearTipusCardena(valorString);
			case "(":
				Accept("(");
				ITipus itexp = Exp();
				Accept(")");
				return itexp;
				//break;
			case "ID":
				String nomID = Accept("ID").getLexema();
				ITipus itfac = Fac2(nomID);
				return itfac;
				//break;
			default:
				throw new ParserException(l.lookahead().getToken(),l.lookahead().getLexema());
		}
	}
	
	private static ITipus Fac2(String nomID) throws ParserException {
		switch(l.lookahead().getToken()) {
			case "(":
				Accept("(");
				//TODO COMPROBA QUE FUNCIO JA EXISTEIX
				s.comprovarExistenciaVariableFuncio(nomID);
				return Factor_Plus(nomID);
			default:
				return Variable_Plus_ID(nomID);
		}
	}
	
	private static ITipus Factor_Plus(String nomID) throws ParserException {
		Funcio f = s.verificarDefFuncio(nomID);
		int numParamReals = 0;
		switch(l.lookahead().getToken()) {
			case ")":
				Accept(")");
				//TODO VERIFIQUEM QUE EL NOMBRE DE PARAMETRES QUADRA
				s.verificarNumParam(f,numParamReals);
				return f.getTipus();
			default:
				Llista_Exp(f,numParamReals);
				Accept(")");
				return f.getTipus();
		}
	}
	
	private static void Llista_Exp(Funcio f,int numParamReals) throws ParserException {
		ITipus itexpllista = Exp();
		//TODO AUGMENTA EL NOMBRE PARAMETRES REBUTS
		//TODO VERIFIQUEM QUE EL PARAMETRE DE LA LLISTA SIGUI DEL MATEIX TIPUS QUE EL QUE HA DE TOCAR
		s.verificarFuncio(f, itexpllista, numParamReals);
		//s.augmentarNumParametres();
		numParamReals++;
		LlistExp2(f,numParamReals);
	}
	
	private static void LlistExp2(Funcio f, int numParamReals) throws ParserException {
		switch(l.lookahead().getToken()) {
			case ",":
				Accept(",");
				Llista_Exp(f,numParamReals);
				break;
			default:
				//TODO VERIFIQUEM QUE EL NOMBRE DE PARAMETRES QUADRA
				s.verificarNumParam(f,numParamReals);
				break;
		}
	}
	
	private static ITipus Variable() throws ParserException {
		String nomID = Accept("ID").getLexema();
		ITipus itvar = Variable_Plus(nomID);
		return itvar;
	}
	
	
	
	private static ITipus Variable_Plus(String nomID) throws ParserException {
		//TODO AQUI SE COMPRUEBA SI VARIABLE ES TIPO SIMPLE, O SI ESTA EXISTE
		switch(l.lookahead().getToken()) {
			case "[":
				Accept("[");
				ITipus itExpVector = Exp();
				s.checkTipusIndexVector(itExpVector);
				Accept("]");
				ITipus itvare = s.getTipusElementArraysVar(nomID);
				return s.checkTipusVar(nomID,itvare);
			default:
				ITipus itvar = s.getTipusVariableSimple(nomID);
				//System.out.println("Nom de la variable "+nomID+" amb tipus "+itvar.getNom());
				return s.checkTipusVar(nomID,itvar);
		}
	}
	
	private static ITipus Variable_Plus_ID(String nomID) throws ParserException {
		switch(l.lookahead().getToken()) {
			case "[":
				Accept("[");
				ITipus itExpVector = Exp();
				s.checkTipusIndexVector(itExpVector);
				Accept("]");
				ITipus itvare = s.getTipusElementArraysVar(nomID);
				return itvare;
			default:
				ITipus itvar = s.getTipusVariableSimple(nomID);
				return itvar;
		}
	}
	
	private static void Llista_Inst() throws ParserException {
		try {
			Inst();
			Accept(";");
		} catch(ParserException e) {
			String message = e.getMessage();
			System.out.println(message);
			crearErrorSintactico("[ERROR_SIN_6] Linea "+l.getLinea()
							+", La instruccio "+currentInstr+" esperava un token "+e.getErrorToken()
							+" pero ha rebut un "+l.lookahead().getToken());
			ArrayList<String> conjuntSinc2 = new ArrayList<String>();
			conjuntSinc2.add("escriure"); conjuntSinc2.add("llegir"); conjuntSinc2.add("cicle");
			conjuntSinc2.add("mentre"); conjuntSinc2.add("si"); conjuntSinc2.add("retornar");
			conjuntSinc2.add("ID"); conjuntSinc2.add("fifunc"); conjuntSinc2.add("fiprog");
			conjuntSinc2.add("fimentre"); conjuntSinc2.add("fisi"); conjuntSinc2.add("sino");
			conjuntSinc2.add("fiper"); conjuntSinc2.add("fins");
			Consume(conjuntSinc2);
		}
		LlistInst2();
	}
	
	private static void LlistInst2() throws ParserException {
		switch(l.lookahead().getToken()) {
			case "escriure":
				Llista_Inst();
				break;
			case "llegir":
				Llista_Inst();
				break;
			case "cicle":
				Llista_Inst();
				break;
			case "mentre":
				Llista_Inst();
				break;
			case "si":
				Llista_Inst();
				break;
			case "retornar":
				Llista_Inst();
				break;
			case "percada":
				Llista_Inst();
				break;
			case "ID":
				Llista_Inst();
				break;
			default:
				break;
		}
	}
	
	private static void Inst() throws ParserException {
		switch(l.lookahead().getToken()) {
			case "escriure":
				Accept("escriure");
				currentInstr = "escriure";
				Accept("(");
				ITipus itexp = Exp();
				//TODO AQUI SE COMPRUEBA SI ESCRIURE IS TIPO SIMPLE O CADENA
				s.verificarEscriureExp(itexp);
				Llista_Exp_Escriure();
				Accept(")");
				break;
			case "llegir":
				Accept("llegir");
				currentInstr = "llegir";
				Accept("(");
				//TODO AQUI SE COMPRUEBA LLEGIR ES TIPO SIMPLE
				Llista_Var();
				Accept(")");
				break;
			case "cicle":
				Accept("cicle");
				currentInstr = "cicle";
				Llista_Inst();
				Accept("fins");
				ITipus itcicle = Exp();
				//TODO AQUI SE COMPRUEBA CICLE ES TIPUS LOGIC
				s.chechTipusCicle(itcicle);
				break;
			case "mentre":
				Accept("mentre");
				currentInstr = "mentre";
				ITipus itmentre = Exp();
				s.checkTipusMentre(itmentre);
				Accept("fer");
				Llista_Inst();
				Accept("fimentre");
				break;
			case "si":
				Accept("si");
				currentInstr = "si";
				ITipus itsi = Exp();
				s.checkTipusSi(itsi);
				Accept("llavors");
				Llista_Inst();
				Inst_2();
				Accept("fisi");
				break;
			case "retornar":
				Accept("retornar");
				currentInstr = "retornar";
				ITipus itret = Exp();
				//TODO AQUI COMPRUEBA SI HAY RETORNO EN UN MAIN O SI EL TIPO COINCIDE CON LA FUNCION
				s.checkRetornarTipus(itret);
				break;
			case "percada":
				Accept("percada");
				currentInstr = "percada";
				String idPer1 = Accept("ID").getLexema();
				//TODO AQUI COMPRUEBA QUE LA PRIMERA VARIABLE DE PERCADA EXISTA
				s.verificarIDPercada(idPer1);
				Accept("en");
				String idPer2 = Accept("ID").getLexema();
				//TODO AQUI COMPRUEBA QUE LA SEGUNDA VARIABLE DE PERCADA EXISTA Y QUE SEA VECTOR
				s.verificarIDPercadaVector(idPer2);
				Accept("fer");	
				Llista_Inst();
				Accept("fiper");
				break;
			case "ID":
				String idNom = Accept("ID").getLexema();
				currentInstr = "ID";
				ITipus itIDInst = Variable_Plus_ID(idNom);
				s.comprovarVariableIzquierdaTipo(idNom);
				Accept("=");
				Inst_1(idNom,itIDInst);
				break;
			default:
				throw new ParserException(l.lookahead().getToken(),l.lookahead().getLexema());
		}
	}
	
	private static void Llista_Exp_Escriure() throws ParserException {
		switch(l.lookahead().getToken()) {
			case ",":
				Accept(",");
				ITipus itexp = Exp();
				//TODO AQUI COMPRUEBA QUE EXP RECIBIDO SEA SIMPLE O TIPO CADENA
				s.verificarEscriureExp(itexp);
				Llista_Exp_Escriure();
				break;
			default:
				break;
		}
	}
	
	private static void Inst_2() throws ParserException{
		switch(l.lookahead().getToken()) {
			case "sino":
				Accept("sino");
				Llista_Inst();
				break;
			default:
				break;
		}
	}
	
	private static void Inst_1(String idNom, ITipus itIDInst) throws ParserException {
		switch(l.lookahead().getToken()) {
			case "(":
				Accept("(");
				ITipus itinst12 = Exp();
				//TODO AQUI COMPRUEBA QUE EN LA ASIGNACION VALOR TIPO COINCIDA
				s.verificarTipusInst1(idNom,itIDInst,itinst12);
				Accept(")");
				Inst_1_2();
				break;
			default:
				ITipus itinst1 = Exp();
				//TODO AQUI COMPRUEBA QUE EN LA ASIGNACION VALOR TIPO COINCIDA
				s.verificarTipusInst1(idNom,itIDInst,itinst1);
				break;
		}
	}
	
	private static void Inst_1_2() throws ParserException {
		switch(l.lookahead().getToken()) {
			case "?":
				Accept("?");
				Exp();
				Accept(":");
				Exp();
				break;
			default:
				break;
		}
	}
	
	private static void Llista_Var() throws ParserException{
		Variable();
		LlistVar2();
	}
	
	private static void LlistVar2() throws ParserException{
		switch(l.lookahead().getToken()) {
			case ",":
				Accept(",");
				Llista_Var();
				break;
			default:
				break;
		}
	}
	
	private static Token Accept(String sToken) throws ParserException {
		Token t = l.lookahead();
		//System.out.println("\nToken "+t.getToken()+" recibido");
		if (t.getToken().equals(sToken)) {
			l.getNextToken();
			return t;
		} else {
			throw new ParserException(t.getToken(),t.getLexema());
		}
	}
	
	private static void Consume(ArrayList<String> conjuntSinc) {
		boolean bFi = false;
		l.getNextToken();
		while(!bFi) {
			for (int i = 0; !bFi && i < conjuntSinc.size(); i++) {
				if (l.lookahead().getToken().equals(conjuntSinc.get(i)) || l.lookahead().getToken().isEmpty()) {
					bFi = true;
				}
			}
			if (!bFi) l.getNextToken();
		}
		
	}
	
}
