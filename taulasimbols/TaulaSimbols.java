
package taulasimbols;

import java.util.Vector;

/**
 * <p>Classe que representa la taula de simols del llenguatge Babel<p>
 */
public class TaulaSimbols {

	/**<p>index del bloc actual</p>*/
    private int blocActual;

    /**<p>llista de blocs de la taula de simbols</p>*/
    @SuppressWarnings("rawtypes")
	private Vector llistaBlocs = new Vector();

	/**
	 * <p>Insereix el nou bloc dintre de la llista de blocs</p>
	 * @param (Bloc) bloc 
	 */
    @SuppressWarnings("unchecked")
	public void inserirBloc(Bloc bloc) {        
    	llistaBlocs.add(bloc);
    } 

	/**
	 * <p>Obt� el bloc que est� en la possici� <b>index</b></p>
	 * @param (int) index 
	 * @return Bloc
	 */
    public Bloc obtenirBloc(int index) {        
        return (Bloc) llistaBlocs.toArray()[index];
    } 

	/**
	 * <p>Esborra el bloc que est� en la possici� <b>index</b></p>
	 * @param (int) index 
	 */
    public void esborrarBloc(int index) {        
        llistaBlocs.remove(obtenirBloc(index));
    } 

	/**
	 * <p>Obt� el n�mero de blocs de la taula de s�mbols</p>
	 * @return int
	 */
    public int getNumeroBlocs() {        
        return llistaBlocs.size();
    } 

	/**
	 * <p>Obt� el bloc actual</p>
	 * @return int
	 */
    public int getBlocActual() {        
        return blocActual;
    } 

	/**
	 * <p>Estableix el bloc actual</p>
	 * @param (int) blocActual 
	 */
    public void setBlocActual(int blocActual) {        
        this.blocActual = blocActual;
    } 

    /**
	 * <p>Obt� tota la informaci� del objecte en format XML</p>
	 * @return String
	 */
    public String toXml() {        
    	String result = "<TaulaSimbols>";
    	result += "<Blocs>";
    	for (int i=0; i<getNumeroBlocs(); i++)
    		result += obtenirBloc(i).toXml();
    	result += "</Blocs>";
    	result += "</TaulaSimbols>";
        return result;
    } 
 }
