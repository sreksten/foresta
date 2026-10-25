package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.tipi.CostantiPersonaggi;

import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.testi.Articoli;
import com.threeamigos.foresta.tipi.Costanti;
import com.threeamigos.foresta.tipi.TipoPersonaggio;


/**
 * Il guerriero giocante, al maschile: umanoide, corrompibile e amichevole. Quando è in berserk è immune a confusione e
 * spavento. Il femminile è la {@link Guerriera}.
 */
public class Guerriero extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Articoli.UN; }
	public String getADS() { return Articoli.IL; }
	public String getAIP() { return Articoli.ALCUNI; }
	public String getADP() { return Articoli.I; }
	public String getDeS() { return Articoli.DEL; }
	public String getDeP() { return Articoli.DEI; }
	public String getDa() { return Articoli.DA_UN; }

	public String getNomeSingolare() { return "Guerriero"; }
	public String getNomePlurale() { return "Guerrieri"; }
	public String getPronome() { return Articoli.EGLI; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.MASCHIO; }

	public Guerriero(int livello) {
		super(TipoPersonaggio.GUERRIERO, livello);
	}

	public Guerriero(String nome, int livello) {
		super(nome, TipoPersonaggio.GUERRIERO, livello);
	}

	/**
	 * Per chi ha le caratteristiche del guerriero sotto un'altra classe (vedi CapitanoDelleGuardie).
	 */
	protected Guerriero(String nome, TipoPersonaggio classe, int livello) {
		super(nome, classe, livello);
	}

	@Override
	protected void impostaValoriDiPartenza() {
		setCorrompibile(true);
		setAmichevole(true);

		setQuantitaMassima(CostantiPersonaggi.GUERRIERO_MAX_NUMERO);
	}

	@Override
	public int getPuntiEsperienza() {
		return CostantiPersonaggi.GUERRIERO_PUNTI_ESPERIENZA;
	}

	@Override
	public double getSaluteBase() {
		return CostantiPersonaggi.GUERRIERO_SALUTE_BASE;
	}

	@Override
	public double getMagiaBase() {
		return CostantiPersonaggi.GUERRIERO_MAGIA_BASE;
	}

	@Override
	public double getLivellamentoMagia() {
		return CostantiPersonaggi.GUERRIERO_LIVELLAMENTO_MAGIA;
	}

	@Override
	public double getMoltiplicatoreCarico() {
		return CostantiPersonaggi.GUERRIERO_MOLTIPLICATORE_CARICO;
	}


	@Override
	public double getMoltiplicatoreCritico() {
		return CostantiPersonaggi.GUERRIERO_MOLTIPLICATORE_CRITICO;
	}


	@Override
	public double getMoltiplicatorePrecisione() {
		return CostantiPersonaggi.GUERRIERO_MOLTIPLICATORE_PRECISIONE;
	}


	@Override
	public double getMoltiplicatoreVelocita() {
		return CostantiPersonaggi.GUERRIERO_MOLTIPLICATORE_VELOCITA;
	}


	@Override
	public double getMoltiplicatoreFurtivita() {
		return CostantiPersonaggi.GUERRIERO_MOLTIPLICATORE_FURTIVITA;
	}


	@Override
	public double getMoltiplicatoreParata() {
		return CostantiPersonaggi.GUERRIERO_MOLTIPLICATORE_PARATA;
	}


	@Override
	public double getMoltiplicatoreResistenzaMagica() {
		return CostantiPersonaggi.GUERRIERO_MOLTIPLICATORE_RESISTENZA_MAGICA;
	}


	@Override
	public double getMoltiplicatorePercezione() {
		return CostantiPersonaggi.GUERRIERO_MOLTIPLICATORE_PERCEZIONE;
	}


	@Override
	public double getMoltiplicatoreSoggezione() {
		return CostantiPersonaggi.GUERRIERO_MOLTIPLICATORE_SOGGEZIONE;
	}


	@Override
	public double getMoltiplicatoreFuria() {
		return CostantiPersonaggi.GUERRIERO_MOLTIPLICATORE_FURIA;
	}


	@Override
	public double getMoltiplicatoreCoraggio() {
		return CostantiPersonaggi.GUERRIERO_MOLTIPLICATORE_CORAGGIO;
	}


	@Override
	public double getMoltiplicatoreValore() {
		return CostantiPersonaggi.GUERRIERO_MOLTIPLICATORE_VALORE;
	}


	@Override
	public double getMoltiplicatoreContrattazione() {
		return CostantiPersonaggi.GUERRIERO_MOLTIPLICATORE_CONTRATTAZIONE;
	}


	@Override
	public double getMoltiplicatoreNumeroBersagli() {
		return CostantiPersonaggi.GUERRIERO_MOLTIPLICATORE_NUMERO_BERSAGLI;
	}


	@Override
	public double getMoltiplicatoreStanchezza() {
		return CostantiPersonaggi.GUERRIERO_MOLTIPLICATORE_STANCHEZZA;
	}


	@Override
	public double getMoltiplicatoreDanniMagici() {
		return CostantiPersonaggi.GUERRIERO_MOLTIPLICATORE_DANNI_MAGICI;
	}


	@Override
	public double getMoltiplicatoreDanniFisici() {
		return CostantiPersonaggi.GUERRIERO_MOLTIPLICATORE_DANNI_FISICI;
	}


	@Override
	public double getMoltiplicatoreRecuperoMagico() {
		return CostantiPersonaggi.GUERRIERO_MOLTIPLICATORE_RECUPERO_MAGICO;
	}


	@Override
	public double getMoltiplicatoreRecuperoFisico() {
		return CostantiPersonaggi.GUERRIERO_MOLTIPLICATORE_RECUPERO_FISICO;
	}


	@Override
	public double getMaxStatistica(TipoAttributo tipoAttributo) {
		switch (tipoAttributo) {
			case FORZA:
				return CostantiPersonaggi.GUERRIERO_MAX_FORZA;
			case DESTREZZA:
				return CostantiPersonaggi.GUERRIERO_MAX_DESTREZZA;
			case COSTITUZIONE:
				return CostantiPersonaggi.GUERRIERO_MAX_COSTITUZIONE;
			case INTELLIGENZA:
				return CostantiPersonaggi.GUERRIERO_MAX_INTELLIGENZA;
			case SAGGEZZA:
				return CostantiPersonaggi.GUERRIERO_MAX_SAGGEZZA;
			case CARISMA:
				return CostantiPersonaggi.GUERRIERO_MAX_CARISMA;
			case FORTUNA:
				return CostantiPersonaggi.GUERRIERO_MAX_FORTUNA;
			default:
				throw new IllegalArgumentException(tipoAttributo + " non è un tipo primario");
		}
	}

	@Override
	protected boolean isImmuneAEffettoPerIstanza(TipoEffettoDiStato effettoStato) {
		return (effettoStato == TipoEffettoDiStato.CONFUSO ||
				effettoStato == TipoEffettoDiStato.SPAVENTATO) &&
				hasEffettoDiStato(TipoEffettoDiStato.BERSERK);
	}
}
