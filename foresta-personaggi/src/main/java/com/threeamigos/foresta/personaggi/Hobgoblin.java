package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.tipi.CostantiPersonaggi;

import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.testi.Articoli;
import com.threeamigos.foresta.tipi.Costanti;
import com.threeamigos.foresta.tipi.TipoOfferta;
import com.threeamigos.foresta.tipi.TipoPersonaggio;


/**
 * L'hobgoblin: creatura umanoide e corrompibile, ma non amichevole. Con la corruzione offre un aiuto a pagamento, incantesimi
 * o la mappa della Foresta.
 */
public class Hobgoblin extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Articoli.UN; }
	public String getADS() { return Articoli.L_APOSTROFO; }
	public String getAIP() { return Articoli.ALCUNI; }
	public String getADP() { return Articoli.GLI; }
	public String getDeS() { return Articoli.DELL_APOSTROFO; }
	public String getDeP() { return Articoli.DEGLI; }
	public String getDa() { return Articoli.DA_UN; }

	public String getNomeSingolare() { return "Hobgoblin"; }
	public String getNomePlurale() { return "Hobgoblin"; }
	public String getPronome() { return Articoli.ESSO; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.MASCHIO; }

	public Hobgoblin(int livello) {
		super(TipoPersonaggio.HOBGOBLIN, livello);
	}

	@Override
	protected void impostaValoriDiPartenza() {
		setCorrompibile(true);

		setQuantitaMassima(CostantiPersonaggi.HOBGOBLIN_MAX_NUMERO);
	}

	@Override
	public int getPuntiEsperienza() {
		return CostantiPersonaggi.HOBGOBLIN_PUNTI_ESPERIENZA;
	}

	@Override
	public double getSaluteBase() {
		return CostantiPersonaggi.HOBGOBLIN_SALUTE_BASE;
	}

	@Override
	public double getMagiaBase() {
		return CostantiPersonaggi.HOBGOBLIN_MAGIA_BASE;
	}

	@Override
	public double getLivellamentoMagia() {
		return CostantiPersonaggi.HOBGOBLIN_LIVELLAMENTO_MAGIA;
	}

	@Override
	public double getMoltiplicatoreCarico() {
		return CostantiPersonaggi.HOBGOBLIN_MOLTIPLICATORE_CARICO;
	}


	@Override
	public double getMoltiplicatoreCritico() {
		return CostantiPersonaggi.HOBGOBLIN_MOLTIPLICATORE_CRITICO;
	}


	@Override
	public double getMoltiplicatorePrecisione() {
		return CostantiPersonaggi.HOBGOBLIN_MOLTIPLICATORE_PRECISIONE;
	}


	@Override
	public double getMoltiplicatoreVelocita() {
		return CostantiPersonaggi.HOBGOBLIN_MOLTIPLICATORE_VELOCITA;
	}


	@Override
	public double getMoltiplicatoreFurtivita() {
		return CostantiPersonaggi.HOBGOBLIN_MOLTIPLICATORE_FURTIVITA;
	}


	@Override
	public double getMoltiplicatoreParata() {
		return CostantiPersonaggi.HOBGOBLIN_MOLTIPLICATORE_PARATA;
	}


	@Override
	public double getMoltiplicatoreResistenzaMagica() {
		return CostantiPersonaggi.HOBGOBLIN_MOLTIPLICATORE_RESISTENZA_MAGICA;
	}


	@Override
	public double getMoltiplicatorePercezione() {
		return CostantiPersonaggi.HOBGOBLIN_MOLTIPLICATORE_PERCEZIONE;
	}


	@Override
	public double getMoltiplicatoreSoggezione() {
		return CostantiPersonaggi.HOBGOBLIN_MOLTIPLICATORE_SOGGEZIONE;
	}


	@Override
	public double getMoltiplicatoreFuria() {
		return CostantiPersonaggi.HOBGOBLIN_MOLTIPLICATORE_FURIA;
	}


	@Override
	public double getMoltiplicatoreCoraggio() {
		return CostantiPersonaggi.HOBGOBLIN_MOLTIPLICATORE_CORAGGIO;
	}


	@Override
	public double getMoltiplicatoreValore() {
		return CostantiPersonaggi.HOBGOBLIN_MOLTIPLICATORE_VALORE;
	}


	@Override
	public double getMoltiplicatoreContrattazione() {
		return CostantiPersonaggi.HOBGOBLIN_MOLTIPLICATORE_CONTRATTAZIONE;
	}


	@Override
	public double getMoltiplicatoreNumeroBersagli() {
		return CostantiPersonaggi.HOBGOBLIN_MOLTIPLICATORE_NUMERO_BERSAGLI;
	}


	@Override
	public double getMoltiplicatoreStanchezza() {
		return CostantiPersonaggi.HOBGOBLIN_MOLTIPLICATORE_STANCHEZZA;
	}


	@Override
	public double getMoltiplicatoreDanniMagici() {
		return CostantiPersonaggi.HOBGOBLIN_MOLTIPLICATORE_DANNI_MAGICI;
	}


	@Override
	public double getMoltiplicatoreDanniFisici() {
		return CostantiPersonaggi.HOBGOBLIN_MOLTIPLICATORE_DANNI_FISICI;
	}


	@Override
	public double getMoltiplicatoreRecuperoMagico() {
		return CostantiPersonaggi.HOBGOBLIN_MOLTIPLICATORE_RECUPERO_MAGICO;
	}


	@Override
	public double getMoltiplicatoreRecuperoFisico() {
		return CostantiPersonaggi.HOBGOBLIN_MOLTIPLICATORE_RECUPERO_FISICO;
	}


	@Override
	public double getMaxStatistica(TipoAttributo tipoAttributo) {
		switch (tipoAttributo) {
			case FORZA:
				return CostantiPersonaggi.HOBGOBLIN_MAX_FORZA;
			case DESTREZZA:
				return CostantiPersonaggi.HOBGOBLIN_MAX_DESTREZZA;
			case COSTITUZIONE:
				return CostantiPersonaggi.HOBGOBLIN_MAX_COSTITUZIONE;
			case INTELLIGENZA:
				return CostantiPersonaggi.HOBGOBLIN_MAX_INTELLIGENZA;
			case SAGGEZZA:
				return CostantiPersonaggi.HOBGOBLIN_MAX_SAGGEZZA;
			case CARISMA:
				return CostantiPersonaggi.HOBGOBLIN_MAX_CARISMA;
			case FORTUNA:
				return CostantiPersonaggi.HOBGOBLIN_MAX_FORTUNA;
			default:
				throw new IllegalArgumentException(tipoAttributo + " non è un tipo primario");
		}
	}

	@Override
	public TipoOfferta[] getOfferteCorruzione() {
		return new TipoOfferta[] {
				TipoOfferta.AIUTO_MERCENARIO,
				TipoOfferta.INCANTESIMI,
				TipoOfferta.MAPPA_FORESTA,
		};
	}
}
