package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.tipi.CostantiPersonaggi;

import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.testi.Articoli;
import com.threeamigos.foresta.tipi.TipoPersonaggio;


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

	public String getAIS() { return Articoli.UN; }
	public String getADS() { return Articoli.IL; }
	public String getAIP() { return Articoli.ALCUNI; }
	public String getADP() { return Articoli.I; }
	public String getDeS() { return Articoli.DEL; }
	public String getDeP() { return Articoli.DEI; }
	public String getDa() { return Articoli.DA_UN; }
	public String getPronome() { return Articoli.EGLI; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.MASCHIO; }

	protected PersonaggioNonCombattente(String nome, TipoPersonaggio classe, int livello) {
		super(nome, classe, livello);
	}

	@Override
	protected void impostaValoriDiPartenza() {
		setCorrompibile(false);
		setAmichevole(false);
		setQuantitaMassima(CostantiPersonaggi.NON_COMBATTENTE_MAX_NUMERO);
	}

	@Override
	public int getPuntiEsperienza() {
		return CostantiPersonaggi.NON_COMBATTENTE_PUNTI_ESPERIENZA;
	}

	@Override
	public double getSaluteBase() {
		return CostantiPersonaggi.NON_COMBATTENTE_SALUTE_BASE;
	}

	@Override
	public double getMagiaBase() {
		return CostantiPersonaggi.NON_COMBATTENTE_MAGIA_BASE;
	}

	@Override
	public double getLivellamentoMagia() {
		return CostantiPersonaggi.NON_COMBATTENTE_LIVELLAMENTO_MAGIA;
	}

	@Override
	public double getMoltiplicatoreCarico() {
		return CostantiPersonaggi.NON_COMBATTENTE_MOLTIPLICATORE_CARICO;
	}


	@Override
	public double getMoltiplicatoreCritico() {
		return CostantiPersonaggi.NON_COMBATTENTE_MOLTIPLICATORE_CRITICO;
	}


	@Override
	public double getMoltiplicatorePrecisione() {
		return CostantiPersonaggi.NON_COMBATTENTE_MOLTIPLICATORE_PRECISIONE;
	}


	@Override
	public double getMoltiplicatoreVelocita() {
		return CostantiPersonaggi.NON_COMBATTENTE_MOLTIPLICATORE_VELOCITA;
	}


	@Override
	public double getMoltiplicatoreFurtivita() {
		return CostantiPersonaggi.NON_COMBATTENTE_MOLTIPLICATORE_FURTIVITA;
	}


	@Override
	public double getMoltiplicatoreParata() {
		return CostantiPersonaggi.NON_COMBATTENTE_MOLTIPLICATORE_PARATA;
	}


	@Override
	public double getMoltiplicatoreResistenzaMagica() {
		return CostantiPersonaggi.NON_COMBATTENTE_MOLTIPLICATORE_RESISTENZA_MAGICA;
	}


	@Override
	public double getMoltiplicatorePercezione() {
		return CostantiPersonaggi.NON_COMBATTENTE_MOLTIPLICATORE_PERCEZIONE;
	}


	@Override
	public double getMoltiplicatoreSoggezione() {
		return CostantiPersonaggi.NON_COMBATTENTE_MOLTIPLICATORE_SOGGEZIONE;
	}


	@Override
	public double getMoltiplicatoreFuria() {
		return CostantiPersonaggi.NON_COMBATTENTE_MOLTIPLICATORE_FURIA;
	}


	@Override
	public double getMoltiplicatoreCoraggio() {
		return CostantiPersonaggi.NON_COMBATTENTE_MOLTIPLICATORE_CORAGGIO;
	}


	@Override
	public double getMoltiplicatoreValore() {
		return CostantiPersonaggi.NON_COMBATTENTE_MOLTIPLICATORE_VALORE;
	}


	@Override
	public double getMoltiplicatoreContrattazione() {
		return CostantiPersonaggi.NON_COMBATTENTE_MOLTIPLICATORE_CONTRATTAZIONE;
	}


	@Override
	public double getMoltiplicatoreNumeroBersagli() {
		return CostantiPersonaggi.NON_COMBATTENTE_MOLTIPLICATORE_NUMERO_BERSAGLI;
	}


	@Override
	public double getMoltiplicatoreStanchezza() {
		return CostantiPersonaggi.NON_COMBATTENTE_MOLTIPLICATORE_STANCHEZZA;
	}


	@Override
	public double getMoltiplicatoreDanniMagici() {
		return CostantiPersonaggi.NON_COMBATTENTE_MOLTIPLICATORE_DANNI_MAGICI;
	}


	@Override
	public double getMoltiplicatoreDanniFisici() {
		return CostantiPersonaggi.NON_COMBATTENTE_MOLTIPLICATORE_DANNI_FISICI;
	}


	@Override
	public double getMoltiplicatoreRecuperoMagico() {
		return CostantiPersonaggi.NON_COMBATTENTE_MOLTIPLICATORE_RECUPERO_MAGICO;
	}


	@Override
	public double getMoltiplicatoreRecuperoFisico() {
		return CostantiPersonaggi.NON_COMBATTENTE_MOLTIPLICATORE_RECUPERO_FISICO;
	}


	@Override
	public double getMaxStatistica(TipoAttributo tipoAttributo) {
		switch (tipoAttributo) {
			case FORZA:
				return CostantiPersonaggi.NON_COMBATTENTE_MAX_FORZA;
			case DESTREZZA:
				return CostantiPersonaggi.NON_COMBATTENTE_MAX_DESTREZZA;
			case COSTITUZIONE:
				return CostantiPersonaggi.NON_COMBATTENTE_MAX_COSTITUZIONE;
			case INTELLIGENZA:
				return CostantiPersonaggi.NON_COMBATTENTE_MAX_INTELLIGENZA;
			case SAGGEZZA:
				return CostantiPersonaggi.NON_COMBATTENTE_MAX_SAGGEZZA;
			case CARISMA:
				return CostantiPersonaggi.NON_COMBATTENTE_MAX_CARISMA;
			case FORTUNA:
				return CostantiPersonaggi.NON_COMBATTENTE_MAX_FORTUNA;
			default:
				throw new IllegalArgumentException(tipoAttributo + " non è un tipo primario");
		}
	}
}
