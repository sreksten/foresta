package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.tipi.Costanti;
import com.threeamigos.foresta.strumenti.Misc;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

import java.util.function.Function;

/**
 * Chi nelle scene non combatte: il locandiere, l'armaiolo, l'alchimista, il venditore di pergamene, l'incantatore, la
 * moglie del bardo. Ha le sue caratteristiche (Costanti.NON_COMBATTENTE_*, un poco più basse di quelle di un bardo),
 * nessuna capacità propria (niente dardo arcano né alba sacra né canti), non si incontra, non si recluta e non si
 * corrompe: compare nelle scene e viaggia con il gruppo come ospite quando una missione lo deve scortare (vedi
 * Viandante). Non è un Bardo.
 * <p>
 * Gli articoli e il genere sono quelli del maschile: chi è femmina li ridefinisce (vedi MoglieDelBardo).
 */
public abstract class PersonaggioNonCombattente extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UN; }
	public String getADS() { return Misc.IL; }
	public String getAIP() { return Misc.ALCUNI; }
	public String getADP() { return Misc.I; }
	public String getDeS() { return Misc.DEL; }
	public String getDeP() { return Misc.DEI; }
	public String getDa() { return Misc.DA_UN; }
	public String getPronome() { return Misc.EGLI; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.MASCHIO; }

	protected PersonaggioNonCombattente(String nome, TipoPersonaggio classe, int livello) {
		super(nome, classe, livello);
	}

	@Override
	protected void impostaValoriDiPartenza(Function<Integer, Integer> funzione) {
		setCorrompibile(false);
		setAmichevole(false);
		md.setForza(funzione.apply(getAttributoAdeguatoALivello(getMaxStatistica(TipoAttributo.FORZA))));
		md.setDestrezza(funzione.apply(getAttributoAdeguatoALivello(getMaxStatistica(TipoAttributo.DESTREZZA))));
		md.setCostituzione(funzione.apply(getAttributoAdeguatoALivello(getMaxStatistica(TipoAttributo.COSTITUZIONE))));
		md.setIntelligenza(funzione.apply(getAttributoAdeguatoALivello(getMaxStatistica(TipoAttributo.INTELLIGENZA))));
		md.setSaggezza(funzione.apply(getAttributoAdeguatoALivello(getMaxStatistica(TipoAttributo.SAGGEZZA))));
		md.setCarisma(funzione.apply(getAttributoAdeguatoALivello(getMaxStatistica(TipoAttributo.CARISMA))));
		md.setFortuna(funzione.apply(getAttributoAdeguatoALivello(getMaxStatistica(TipoAttributo.FORTUNA))));
		setQuantitaMassima(Costanti.NON_COMBATTENTE_MAX_NUMERO);
	}

	@Override
	public int getPuntiEsperienza() {
		return Costanti.NON_COMBATTENTE_PUNTI_ESPERIENZA;
	}

	@Override
	public double getSaluteBase() {
		return Costanti.NON_COMBATTENTE_SALUTE_BASE;
	}

	@Override
	public double getMagiaBase() {
		return Costanti.NON_COMBATTENTE_MAGIA_BASE;
	}

	@Override
	public double getLivellamentoMagia() {
		return Costanti.NON_COMBATTENTE_LIVELLAMENTO_MAGIA;
	}

	@Override
	public double getMoltiplicatoreCarico() {
		return Costanti.NON_COMBATTENTE_MOLTIPLICATORE_CARICO;
	}

	@Override
	public String getNoteMoltiplicatoreCarico() {
		return Costanti.NON_COMBATTENTE_MOLTIPLICATORE_CARICO_NOTA;
	}

	@Override
	public double getMoltiplicatoreCritico() {
		return Costanti.NON_COMBATTENTE_MOLTIPLICATORE_CRITICO;
	}

	@Override
	public String getNoteMoltiplicatoreCritico() {
		return Costanti.NON_COMBATTENTE_MOLTIPLICATORE_CRITICO_NOTA;
	}

	@Override
	public double getMoltiplicatorePrecisione() {
		return Costanti.NON_COMBATTENTE_MOLTIPLICATORE_PRECISIONE;
	}

	@Override
	public String getNoteMoltiplicatorePrecisione() {
		return Costanti.NON_COMBATTENTE_MOLTIPLICATORE_PRECISIONE_NOTA;
	}

	@Override
	public double getMoltiplicatoreVelocita() {
		return Costanti.NON_COMBATTENTE_MOLTIPLICATORE_VELOCITA;
	}

	@Override
	public String getNoteMoltiplicatoreVelocita() {
		return Costanti.NON_COMBATTENTE_MOLTIPLICATORE_VELOCITA_NOTA;
	}

	@Override
	public double getMoltiplicatoreFurtivita() {
		return Costanti.NON_COMBATTENTE_MOLTIPLICATORE_FURTIVITA;
	}

	@Override
	public String getNoteMoltiplicatoreFurtivita() {
		return Costanti.NON_COMBATTENTE_MOLTIPLICATORE_FURTIVITA_NOTA;
	}

	@Override
	public double getMoltiplicatoreParata() {
		return Costanti.NON_COMBATTENTE_MOLTIPLICATORE_PARATA;
	}

	@Override
	public String getNoteMoltiplicatoreParata() {
		return Costanti.NON_COMBATTENTE_MOLTIPLICATORE_PARATA_NOTA;
	}

	@Override
	public double getMoltiplicatoreResistenzaMagica() {
		return Costanti.NON_COMBATTENTE_MOLTIPLICATORE_RESISTENZA_MAGICA;
	}

	@Override
	public String getNoteMoltiplicatoreResistenzaMagica() {
		return Costanti.NON_COMBATTENTE_MOLTIPLICATORE_RESISTENZA_MAGICA_NOTA;
	}

	@Override
	public double getMoltiplicatorePercezione() {
		return Costanti.NON_COMBATTENTE_MOLTIPLICATORE_PERCEZIONE;
	}

	@Override
	public String getNoteMoltiplicatorePercezione() {
		return Costanti.NON_COMBATTENTE_MOLTIPLICATORE_PERCEZIONE_NOTA;
	}

	@Override
	public double getMoltiplicatoreSoggezione() {
		return Costanti.NON_COMBATTENTE_MOLTIPLICATORE_SOGGEZIONE;
	}

	@Override
	public String getNoteMoltiplicatoreSoggezione() {
		return Costanti.NON_COMBATTENTE_MOLTIPLICATORE_SOGGEZIONE_NOTA;
	}

	@Override
	public double getMoltiplicatoreFuria() {
		return Costanti.NON_COMBATTENTE_MOLTIPLICATORE_FURIA;
	}

	@Override
	public String getNoteMoltiplicatoreFuria() {
		return Costanti.NON_COMBATTENTE_MOLTIPLICATORE_FURIA_NOTA;
	}

	@Override
	public double getMoltiplicatoreCoraggio() {
		return Costanti.NON_COMBATTENTE_MOLTIPLICATORE_CORAGGIO;
	}

	@Override
	public String getNoteMoltiplicatoreCoraggio() {
		return Costanti.NON_COMBATTENTE_MOLTIPLICATORE_CORAGGIO_NOTA;
	}

	@Override
	public double getMoltiplicatoreValore() {
		return Costanti.NON_COMBATTENTE_MOLTIPLICATORE_VALORE;
	}

	@Override
	public String getNoteMoltiplicatoreValore() {
		return Costanti.NON_COMBATTENTE_MOLTIPLICATORE_VALORE_NOTA;
	}

	@Override
	public double getMoltiplicatoreContrattazione() {
		return Costanti.NON_COMBATTENTE_MOLTIPLICATORE_CONTRATTAZIONE;
	}

	@Override
	public String getNoteMoltiplicatoreContrattazione() {
		return Costanti.NON_COMBATTENTE_MOLTIPLICATORE_CONTRATTAZIONE_NOTA;
	}

	@Override
	public double getMoltiplicatoreNumeroBersagli() {
		return Costanti.NON_COMBATTENTE_MOLTIPLICATORE_NUMERO_BERSAGLI;
	}

	@Override
	public String getNoteMoltiplicatoreNumeroBersagli() {
		return Costanti.NON_COMBATTENTE_MOLTIPLICATORE_NUMERO_BERSAGLI_NOTA;
	}

	@Override
	public double getMoltiplicatoreStanchezza() {
		return Costanti.NON_COMBATTENTE_MOLTIPLICATORE_STANCHEZZA;
	}

	@Override
	public String getNoteMoltiplicatoreStanchezza() {
		return Costanti.NON_COMBATTENTE_MOLTIPLICATORE_STANCHEZZA_NOTA;
	}

	@Override
	public double getMoltiplicatoreDanniMagici() {
		return Costanti.NON_COMBATTENTE_MOLTIPLICATORE_DANNI_MAGICI;
	}

	@Override
	public String getNoteMoltiplicatoreDanniMagici() {
		return Costanti.NON_COMBATTENTE_MOLTIPLICATORE_DANNI_MAGICI_NOTA;
	}

	@Override
	public double getMoltiplicatoreDanniFisici() {
		return Costanti.NON_COMBATTENTE_MOLTIPLICATORE_DANNI_FISICI;
	}

	@Override
	public String getNoteMoltiplicatoreDanniFisici() {
		return Costanti.NON_COMBATTENTE_MOLTIPLICATORE_DANNI_FISICI_NOTA;
	}

	@Override
	public double getMoltiplicatoreRecuperoMagico() {
		return Costanti.NON_COMBATTENTE_MOLTIPLICATORE_RECUPERO_MAGICO;
	}

	@Override
	public String getNoteMoltiplicatoreRecuperoMagico() {
		return Costanti.NON_COMBATTENTE_MOLTIPLICATORE_RECUPERO_MAGICO_NOTA;
	}

	@Override
	public double getMoltiplicatoreRecuperoFisico() {
		return Costanti.NON_COMBATTENTE_MOLTIPLICATORE_RECUPERO_FISICO;
	}

	@Override
	public String getNoteMoltiplicatoreRecuperoFisico() {
		return Costanti.NON_COMBATTENTE_MOLTIPLICATORE_RECUPERO_FISICO_NOTA;
	}

	@Override
	public double getMaxStatistica(TipoAttributo tipoAttributo) {
		switch (tipoAttributo) {
			case FORZA:
				return Costanti.NON_COMBATTENTE_MAX_FORZA;
			case DESTREZZA:
				return Costanti.NON_COMBATTENTE_MAX_DESTREZZA;
			case COSTITUZIONE:
				return Costanti.NON_COMBATTENTE_MAX_COSTITUZIONE;
			case INTELLIGENZA:
				return Costanti.NON_COMBATTENTE_MAX_INTELLIGENZA;
			case SAGGEZZA:
				return Costanti.NON_COMBATTENTE_MAX_SAGGEZZA;
			case CARISMA:
				return Costanti.NON_COMBATTENTE_MAX_CARISMA;
			case FORTUNA:
				return Costanti.NON_COMBATTENTE_MAX_FORTUNA;
			default:
				throw new IllegalArgumentException(tipoAttributo + " non è un tipo primario");
		}
	}
}
