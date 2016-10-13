package lexico;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import taulasimbols.Bloc;
import taulasimbols.Constant;
import taulasimbols.DimensioArray;
import taulasimbols.Funcio;
import taulasimbols.ITipus;
import taulasimbols.Parametre;
import taulasimbols.TaulaSimbols;
import taulasimbols.TipusArray;
import taulasimbols.TipusCadena;
import taulasimbols.TipusIndefinit;
import taulasimbols.TipusPasParametre;
import taulasimbols.TipusSimple;
import taulasimbols.Variable;

public class Semantic {
	private static Bloc blocMain;
	private static Bloc blocFunc;
	private static TaulaSimbols tSimbol;
	private static TipusSimple TSencer;
	private static TipusSimple TLogic;
	private static TipusIndefinit TIndefinit;
	private PrintWriter writerERR;
	private Lex l;
	
	public Semantic(Lex l, PrintWriter writerERR) {
		blocMain = new Bloc();
		tSimbol = new TaulaSimbols();
		tSimbol.inserirBloc(blocMain);
		tSimbol.setBlocActual(0);
		TSencer = new TipusSimple("sencer",Integer.SIZE);
		TLogic = new TipusSimple("logic",1);
		TIndefinit = new TipusIndefinit("indefinit",0);
		this.writerERR = writerERR;
		this.l = l;
	}
	
	public void verificarConstant(String nom, int value) {
		if (tSimbol.getBlocActual() == 0 && blocMain.obtenirConstant(nom) == null
				&& blocMain.obtenirVariable(nom) == null
				&& blocMain.obtenirProcediment(nom) == null) {
				Constant constDec = new Constant(nom,TSencer,value);
				blocMain.inserirConstant(constDec);
		} else if (tSimbol.getBlocActual() == 1 && blocFunc.obtenirVariable(nom) == null
						&& blocFunc.obtenirConstant(nom) == null) {
					Constant constDec = new Constant(nom,TSencer,value);
					blocFunc.inserirConstant(constDec);
		} else {
			imprimirErrorSemantic(1,nom,"","");
		}
	}
	
	public void verificarVariable(String nom, ITipus itip) {
		if (tSimbol.getBlocActual() == 0 && blocMain.obtenirConstant(nom) == null
				&& blocMain.obtenirVariable(nom) == null
				&& blocMain.obtenirProcediment(nom) == null) {
				Variable varDec = new Variable(nom, itip, 0);
				blocMain.inserirVariable(varDec);
		} else if (tSimbol.getBlocActual() == 1 && blocFunc.obtenirVariable(nom) == null
						&& blocFunc.obtenirConstant(nom) == null) {
					Variable varDec = new Variable(nom, itip, 0);
					blocFunc.inserirVariable(varDec);
		} else {
			imprimirErrorSemantic(2,nom,"","");
		}
	}
	
	public void crearBlocFuncio() {
		blocFunc = new Bloc();
		tSimbol.inserirBloc(blocFunc);
		tSimbol.setBlocActual(1);
	}
	
	public void borrarBlocFuncio() {
		tSimbol.setBlocActual(0);
		tSimbol.esborrarBloc(1);
	}
	
	public void guardarFuncionEnBloc(Funcio func, String tipus) {
		if (blocMain.obtenirConstant(func.getNom()) == null
				&& blocMain.obtenirVariable(func.getNom()) == null
				&& blocMain.obtenirProcediment(func.getNom()) == null) {
			//System.out.println("Tipus a guardar en funcio "+func.getNom()+" es "+tipus);
			if (tipus.equals("sencer")) {
				func.setTipus(TSencer);
				blocFunc.guardarTipusRetornBloc(func.getNom(),TSencer);
			} else if (tipus.equals("logic")){
				func.setTipus(TLogic);
				blocFunc.guardarTipusRetornBloc(func.getNom(),TLogic);
			} else {
				imprimirErrorSemantic(20,"","","");
				func.setTipus(TIndefinit);
				blocFunc.guardarTipusRetornBloc(func.getNom(),TIndefinit);
			}
			blocMain.inserirProcediment(func);
		} else {
			imprimirErrorSemantic(3,func.getNom(),"","");
		}
	}

	public Funcio guardarParametreAFuncio(Funcio func, String tipusPasPar, String nomPar, ITipus itip) {
		if (blocFunc.obtenirVariable(nomPar) != null) {
			imprimirErrorSemantic(4,nomPar,"","");
			return func;
		}
		Parametre parFunc;
		if (tipusPasPar.equals("perref")) {
			 parFunc = new Parametre(nomPar, itip, 0, TipusPasParametre.REFERENCIA);
		} else {
			parFunc = new Parametre(nomPar, itip, 0, TipusPasParametre.VALOR);
		}
		func.inserirParametre(parFunc);
		blocFunc.inserirVariable(parFunc);
		return func;
	}

	public ITipus verificarTipusDeTipus(String tipus) {
		if (tipus.equals("sencer")) return TSencer;
		else return TLogic;
	}

	public void verificarMinMaxVector(int minVector, int maxVector) {
		if (maxVector < minVector) {
			imprimirErrorSemantic(5,"","","");
		}
	}

	public ITipus crearTipusArray(String tipusVar, int minVector, int maxVector) {
		DimensioArray dmArray = new DimensioArray(TSencer, minVector, maxVector);
		TipusArray tArray;
		if (tipusVar.equals("sencer")){
			tArray = new TipusArray("v_"+minVector+"_"+maxVector+"_sencer", maxVector-minVector+1, TSencer);
			tArray.inserirDimensio(dmArray);
		} else {
			tArray = new TipusArray("v_"+minVector+"_"+maxVector+"_logic", maxVector-minVector+1, TLogic);
			tArray.inserirDimensio(dmArray);
		}
		return tArray;
	}

	public ITipus verificarExpTipusAmbSigne(ITipus itip, String signe, ITipus itip2) {
		//System.out.println("Tipus de l'expressi� es "+itip.getNom());
		if (itip == TIndefinit) {
			//EN AQUEST PUNT JA HI HA HAGUT UN ERROR ABANS, NO FA FALTA TREURE UN ALTRE
			return TIndefinit;
		}
		if (signe.equals("not")) {
			if (itip != TLogic) {
				imprimirErrorSemantic(6,"LOGIC","","");
				return TIndefinit;
			}
			return itip;
		} else if (signe.equals("+")) {
			if (itip != TSencer || itip2 != TSencer) {
				imprimirErrorSemantic(6,"SENCER","","");
				return TIndefinit;
			}
			return itip;
		} else if (signe.equals("-")) {
			if (itip == TSencer && itip2 == null) {}
			else if (itip != TSencer || itip2 != TSencer) {
				imprimirErrorSemantic(6,"SENCER","","");
				return TIndefinit;
			}
			return itip;
		} else if (signe.equals("or")) {
			if (itip != TLogic || itip2 != TLogic) {
				imprimirErrorSemantic(6,"LOGIC","","");
				return TIndefinit;
			}
			return itip;
		} else {
			return itip;
		}
	}

	public ITipus verificarFactorTipusAmbSigne(ITipus itip, String signe, ITipus itip2) {
		if (itip == TIndefinit) {
			//EN AQUEST PUNT JA HI HA HAGUT UN ERROR ABANS, NO FA FALTA TREURE UN ALTRE
			return TIndefinit;
		}
		if (signe.equals("and")) {
			if (itip != TLogic || itip2 != TLogic) {
				imprimirErrorSemantic(7, "LOGIC","","");
				return TIndefinit;
			}
			return itip;
		} else if (signe.equals("*")) {
			if (itip != TSencer || itip2 != TSencer) {
				imprimirErrorSemantic(7, "SENCER","","");
				return TIndefinit;
			}
			return itip;
		} else if (signe.equals("/")) {
			if (itip != TSencer || itip2 != TSencer) {
				imprimirErrorSemantic(7, "SENCER","","");
				return TIndefinit;
			}
			return itip;
		} else {
			return itip;
		}
	}

	public Funcio comprovarExistenciaVariableFuncio(String nomID) {
		Funcio func = (Funcio) blocMain.obtenirProcediment(nomID);
		if (func != null) {
			//numParamReals = 0;
			return func;
		}
		imprimirErrorSemantic(9,nomID,"","");
		return null;
	}

	public ITipus getTipusSencer() {
		return TSencer;
	}

	public ITipus getTipusLogic() {
		return TLogic;
	}

	public ITipus crearTipusCardena(String valorString) {
		TipusCadena TCadena = new TipusCadena(valorString,valorString.length()*Character.SIZE,valorString.length());
		return TCadena;
	}

	public void verificarEscriureExp(ITipus itexp) {
		if (itexp != TSencer && itexp != TLogic && !(itexp instanceof TipusCadena)) {
			imprimirErrorSemantic(14,"","","");
		}
	}

	public ITipus getTipusElementArraysVar(String nomID) {
		Variable varray = null;
		if (tSimbol.getBlocActual()==0) {
			varray = blocMain.obtenirVariable(nomID);
		} else if (tSimbol.getBlocActual()==1) {
			varray = blocFunc.obtenirVariable(nomID);
		}
		if (varray != null) {
			TipusArray itarray = (TipusArray) varray.getTipus();
			return itarray.getTipusElements();
		} else {
			imprimirErrorSemantic(9,nomID,"","");
			return TIndefinit;
		}
	}

	public ITipus checkTipusVar(String idNom, ITipus itvare) {
		if (itvare == TSencer || itvare == TLogic) {
			System.out.println("La variable comprobada es "+idNom+" amb tipus "+itvare.getNom());
			return itvare;
		}
		imprimirErrorSemantic(10,idNom,"","");
		return TIndefinit;
	}

	public ITipus getTipusVariableSimple(String nomID) {
		if (tSimbol.getBlocActual() == 1) {
			if (blocFunc.obtenirConstant(nomID) != null) return blocFunc.obtenirConstant(nomID).getTipus();
			else if (blocFunc.obtenirVariable(nomID) != null) return blocFunc.obtenirVariable(nomID).getTipus();
		}
		if (blocMain.obtenirConstant(nomID) != null) return blocMain.obtenirConstant(nomID).getTipus();
		else if (blocMain.obtenirVariable(nomID) != null) return blocMain.obtenirVariable(nomID).getTipus();
		return TIndefinit;
		/*if (blocMain.obtenirConstant(nomID) != null
				|| blocMain.obtenirProcediment(nomID) != null) {
			imprimirErrorSemantic(11, nomID,"","");
			return TIndefinit;
		} else if (blocFunc.obtenirConstant(nomID) != null) {
			imprimirErrorSemantic(11, nomID,"","");
			return TIndefinit;
		}
		Variable var = blocMain.obtenirVariable(nomID);
		if (var == null) var = blocFunc.obtenirVariable(nomID);
		if (var != null) {
			return var.getTipus();
		}
		imprimirErrorSemantic(9, nomID,"","");
		return TIndefinit;*/
	}

	public void chechTipusCicle(ITipus itcicle) {
		if (itcicle != TLogic) {
			imprimirErrorSemantic(8, "LOGIC","","");
		}
	}

	public void checkTipusMentre(ITipus itmentre) {
		if (itmentre != TLogic) {
			imprimirErrorSemantic(8, "LOGIC","","");
		}
	}

	public void checkTipusSi(ITipus itsi) {
		if (itsi != TLogic) {
			imprimirErrorSemantic(8, "LOGIC","","");
		}
	}

	public void checkRetornarTipus(ITipus itret) {
		if (tSimbol.getBlocActual() == 0) {
			imprimirErrorSemantic(21,"","","");
		} else {
			ITipus itbloc = blocFunc.getTipusRetornBloc();
			//System.out.println("Tipus: "+itret.getNom());
			if (itbloc == null) {
				imprimirErrorSemantic(18,blocFunc.getNomFuncio(),"indefinit",itret.getNom());
			} else if (itret != itbloc) {
				imprimirErrorSemantic(18,blocFunc.getNomFuncio(),itbloc.getNom(),itret.getNom());
			}
			blocFunc.afirmarRetornCreat();
		}
	}

	public void verificarIDPercada(String idPer1) {
		if (blocMain.obtenirVariable(idPer1) == null) {
			if (tSimbol.getBlocActual() == 1) {
				if (blocFunc.obtenirVariable(idPer1) == null) {
					imprimirErrorSemantic(9,idPer1, "", "");
				}
			}
		}
	}

	public void verificarIDPercadaVector(String idPer2) {
		Variable var = blocMain.obtenirVariable(idPer2);
		if (var == null) {
			if (tSimbol.getBlocActual() == 1) {
				var = blocFunc.obtenirVariable(idPer2);
				if (var == null) {
					imprimirErrorSemantic(9,idPer2, "", "");
				} else if (!(var.getTipus() instanceof TipusArray)) {
					imprimirErrorSemantic(22,idPer2,var.getTipus().getNom(),"");
				}
			}
		} else if (!(var.getTipus() instanceof TipusArray)) {
			imprimirErrorSemantic(22,idPer2,var.getTipus().getNom(),"");
		}
	}

	public void verificarTipusInst1(String nomID, ITipus itIDInst, ITipus itinst1) {
		if (itIDInst != itinst1) {
			imprimirErrorSemantic(12, nomID, itIDInst.getNom(), itinst1.getNom());
		}
	}
	
	public void verificarFuncio(Funcio f, ITipus it, int numParamReals) {
		if (f.getNumeroParametres() > numParamReals) {
	        if(!f.obtenirParametre(numParamReals).getTipus().equals(it)){
	        	imprimirErrorSemantic(16,Integer.toString(numParamReals+1),f.obtenirParametre(numParamReals).getTipus().getNom(),it.getNom());
	        }
		} else {
			//NO ES TRACTA JA QUE ENS HEM PASSAT DE PARAMETRES. ES TRACTARA AL FINAL
		}
    }
	
	public void augmentarNumParametres(int numParamReals) {
		numParamReals++;
	}
    
    public Funcio verificarDefFuncio(String nomFuncio){
    	Funcio f = (Funcio) blocMain.obtenirProcediment(nomFuncio);
	    if (f == null){
	    	imprimirErrorSemantic(9,nomFuncio, "", "");
	    	return new Funcio("", TIndefinit);
	    }
	    return f;
    }
    
    public void verificarNumParam(Funcio f, int numParamReals) {
    	if(f.getNumeroParametres() != numParamReals) {
    		imprimirErrorSemantic(15,Integer.toString(f.getNumeroParametres()),Integer.toString(numParamReals),"");
        }
    }

	public void verificarExistenciaRetorn() {
		if (!blocFunc.getRetornCreat()) {
			imprimirErrorSemantic(19,"","","");
		}
	}
	
	public void checkTipusIndexVector(ITipus itExpVector) {
		if (itExpVector != TSencer) {
			imprimirErrorSemantic(13,"","","");
		}
	}
	
	public ITipus verificarTipusExp2(ITipus itip, ITipus itip2) {
		if (itip != TSencer && itip != TLogic) {
			imprimirErrorSemantic(24,"","","");
			return TIndefinit;
		}
		if (itip != itip2) {
			imprimirErrorSemantic(23, itip.getNom(), itip2.getNom(), "");
			return TIndefinit;
		}
		return TLogic;
	}
	
	public void imprimirErrorSemantic(int code, String nom, String tipus1, String tipus2) {
		int linea = l.getLinea();
		switch(code) {
			case 1:
				writerERR.println("[ERR_SEM_1] L�nia "+linea+", Constant ["
						+nom+"] doblement definida");
				break;
			case 2:
				writerERR.println("[ERR_SEM_2] L�nia "+linea+", Variable ["
						+nom+"] doblement definida");
				break;
			case 3:
				writerERR.println("[ERR_SEM_3] L�nia "+linea+", Funci� ["
						+nom+"] doblement definida");
				break;
			case 4:
				writerERR.println("[ERR_SEM_4] L�nia "+linea+", Par�metre ["
						+nom+"] doblement definit");
				break;
			case 5:
				writerERR.println("[ERR_SEM_5] L�nia "+linea+", L�mits decreixents en vector");
				break;
			case 6:
				writerERR.println("[ERR_SEM_6] L�nia "+linea+", El tipus d'expressi� "
						+"no es "+nom);
				break;
			case 7:
				writerERR.println("[ERR_SEM_7] L�nia "+linea+", El tipus del factor "
						+"no es "+nom);
				break;
			case 8:
				writerERR.println("[ERR_SEM_8] L�nia "+linea+", La condici� "
						+"no es de tipus "+nom);
				break;
			case 9:
				writerERR.println("[ERR_SEM_9] L�nia "+linea+", L'identificador "
						+nom+" no ha estat declarat");
				break;
			case 10:
				writerERR.println("[ERR_SEM_10] L�nia "+linea+", L'identificador "
						+nom+" en la funci� LLEGIR no �s una variable de tipus simple");
				break;
			case 11:
				writerERR.println("[ERR_SEM_11] L�nia "+linea+", L'identificador "
						+nom+" en part esquerra d'assignaci� no �s una variable");
				break;
			case 12:
				writerERR.println("[ERR_SEM_12] L�nia "+linea+", La variable "
						+nom+" i l'expressi� de l'assignaci� tenen tipus diferents. "
						+"El tipus de la variable �s ["+tipus1+"] i el de l'expressi� �s ["
						+tipus2+"]");
				break;
			case 13:
				writerERR.println("[ERR_SEM_13] L�nia "+linea+", El tipus de l'�ndex d'acc�s "
						+"al vector no �s SENCER");
				break;
			case 14:
				writerERR.println("[ERR_SEM_14] L�nia "+linea+", El tipus de l'expressi� en ESCRIURE "
						+"no �s simple o no �s una constant cadena");
				break;
			case 15:
				writerERR.println("[ERR_SEM_15] L�nia "+linea+", La funci� en declaraci� t� "
						+nom+" par�metres mentre que el seu �s t� "+tipus1+" par�metres");
				break;
			case 16:
				writerERR.println("[ERR_SEM_16] L�nia "+linea+", El tipus de par�metre n�mero "
						+nom+" ["+tipus2+"] de la funci� no coincideix amb el tipus en la seva declaraci� ["+tipus1+"]");
				break;
			case 17:
				//FALTA TRACTAR AQUEST CAS
				writerERR.println("[ERR_SEM_17] L�nia "+linea+", El par�metre n�mero "
						+nom+" de la funci� no es pot passar per refer�ncia");
				break;
			case 18:
				writerERR.println("[ERR_SEM_18] L�nia "+linea+", La funci� ["
						+nom+"] ha de ser del tipus ["+tipus1+"] per� en l'expressi� del seu "
						+"valor el tipus �s ["+tipus2+"]");
				break;
			case 19:
				writerERR.println("[ERR_SEM_19] L�nia "+linea+", No existeix un RETORNAR dins de la funci�");
				break;
			case 20:
				writerERR.println("[ERR_SEM_20] L�nia "+linea+", La funci� sols pot ser de tipus SENCER o LOGIC");
				break;
			case 21:
				writerERR.println("[ERR_SEM_21] L�nia "+linea+", La funci� principal no pot tenir un RETORNAR");
				break;
			case 22:
				writerERR.println("[ERR_SEM_22] L�nia "+linea+", El segon identificador ["+nom+"] de percada "
						+"ha de ser de tipus Array pero es de tipus ["+tipus1+"]");
				break;
			case 23:
				writerERR.println("[ERR_SEM_23] L�nia "+linea+", L'expressi� de l'esquerra en l'OPERADOR RELACIONAL es de tipus ["+nom
						+"] pero l'expressio de la dreta �s de tipus ["+tipus1+"]");
				break;
			case 24:
				writerERR.println("[ERR_SEM_24] L�nia "+linea+", L'expressi� en els OPERADORS RELACIONALS no �s de tipus simple");
				break;
			case 25:
				writerERR.println("[ERR_SEM_25] L�nia "+linea+", No se puede asignar un valor a una ["+nom+"]");
				break;
		}
	}

	public void treureXML(int mode) {
		String info = "";
		String nom = "";
		if (mode == 0) {
			nom = blocFunc.getNomFuncio();
			info = blocFunc.toXml();
		} else {
			nom = "main";
			info = tSimbol.toXml();
		}
		//else tSimbol.toXml();
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(nom+".xml"));
			out.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" + "\n");
			out.write(info + "\n");
			out.write("</"+nom+">");
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public int comprovarVariableIzquierdaTipo(String idNom) {
		if (tSimbol.getBlocActual() == 1) {
			if (blocFunc.obtenirConstant(idNom) != null) {
				imprimirErrorSemantic(25, "constant", "", "");
				return -1;
			} else if (blocFunc.obtenirProcediment(idNom) != null) {
				imprimirErrorSemantic(25, "funcio", "", "");
				return -1;
			} else if (blocFunc.obtenirVariable(idNom) != null) {
				return 1;
			}
		}
		if (blocMain.obtenirConstant(idNom) != null) {
			imprimirErrorSemantic(25, "constant", "", "");
			return -1;
		} else if (blocMain.obtenirProcediment(idNom) != null) {
			imprimirErrorSemantic(25, "funcio", "", "");
			return -1;
		} else if (blocMain.obtenirVariable(idNom) != null) {
			return 1;
		}
		imprimirErrorSemantic(9, idNom, "", "");
		return -1;
	}

}
