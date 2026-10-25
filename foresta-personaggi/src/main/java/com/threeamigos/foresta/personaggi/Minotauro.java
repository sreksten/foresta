package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.tipi.CostantiPersonaggi;

import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.testi.Articoli;
import com.threeamigos.foresta.tipi.Costanti;
import com.threeamigos.foresta.tipi.TipoOfferta;
import com.threeamigos.foresta.tipi.TipoPersonaggio;


/**
 * Il minotauro: creatura umanoide e ibrida, corrompibile ma non amichevole. Con la corruzione offre un aiuto a pagamento o la
 * mappa della Foresta. Per la sua versione gigante vedi {@link MinotauroGigante}.
 */
public class Minotauro extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Articoli.UN; }
	public String getADS() { return Articoli.IL; }
	public String getAIP() { return Articoli.ALCUNI; }
	public String getADP() { return Articoli.I; }
	public String getDeS() { return Articoli.DEL; }
	public String getDeP() { return Articoli.DEI; }
	public String getDa() { return Articoli.DAL; }

	public TipoPersonaggio getClasse() { return TipoPersonaggio.MINOTAURO; }

	public String getNomeSingolare() { return "Minotauro"; }
	public String getNomePlurale() { return "Minotauri"; }
	public String getPronome() { return Articoli.ESSO; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.MASCHIO; }

	public Minotauro(int livello) {
		super(TipoPersonaggio.MINOTAURO, livello);
	}

	@Override
	protected void impostaValoriDiPartenza() {
		setCorrompibile(true);

		setQuantitaMassima(CostantiPersonaggi.MINOTAURO_MAX_NUMERO);
	}

	@Override
	public int getPuntiEsperienza() {
		return CostantiPersonaggi.MINOTAURO_PUNTI_ESPERIENZA;
	}

	@Override
	public TipoOfferta[] getOfferteCorruzione() {
		return new TipoOfferta[]{
				TipoOfferta.AIUTO_MERCENARIO,
				TipoOfferta.MAPPA_FORESTA,
		};
	}

	@Override
	public double getSaluteBase() {
		return CostantiPersonaggi.MINOTAURO_SALUTE_BASE;
	}

	@Override
	public double getMagiaBase() {
		return CostantiPersonaggi.MINOTAURO_MAGIA_BASE;
	}

	@Override
	public double getLivellamentoMagia() {
		return CostantiPersonaggi.MINOTAURO_LIVELLAMENTO_MAGIA;
	}

	@Override
	public double getMoltiplicatoreCarico() {
		return CostantiPersonaggi.MINOTAURO_MOLTIPLICATORE_CARICO;
	}


	@Override
	public double getMoltiplicatoreCritico() {
		return CostantiPersonaggi.MINOTAURO_MOLTIPLICATORE_CRITICO;
	}


	@Override
	public double getMoltiplicatorePrecisione() {
		return CostantiPersonaggi.MINOTAURO_MOLTIPLICATORE_PRECISIONE;
	}


	@Override
	public double getMoltiplicatoreVelocita() {
		return CostantiPersonaggi.MINOTAURO_MOLTIPLICATORE_VELOCITA;
	}


	@Override
	public double getMoltiplicatoreFurtivita() {
		return CostantiPersonaggi.MINOTAURO_MOLTIPLICATORE_FURTIVITA;
	}


	@Override
	public double getMoltiplicatoreParata() {
		return CostantiPersonaggi.MINOTAURO_MOLTIPLICATORE_PARATA;
	}


	@Override
	public double getMoltiplicatoreResistenzaMagica() {
		return CostantiPersonaggi.MINOTAURO_MOLTIPLICATORE_RESISTENZA_MAGICA;
	}


	@Override
	public double getMoltiplicatorePercezione() {
		return CostantiPersonaggi.MINOTAURO_MOLTIPLICATORE_PERCEZIONE;
	}


	@Override
	public double getMoltiplicatoreSoggezione() {
		return CostantiPersonaggi.MINOTAURO_MOLTIPLICATORE_SOGGEZIONE;
	}


	@Override
	public double getMoltiplicatoreFuria() {
		return CostantiPersonaggi.MINOTAURO_MOLTIPLICATORE_FURIA;
	}


	@Override
	public double getMoltiplicatoreCoraggio() {
		return CostantiPersonaggi.MINOTAURO_MOLTIPLICATORE_CORAGGIO;
	}


	@Override
	public double getMoltiplicatoreValore() {
		return CostantiPersonaggi.MINOTAURO_MOLTIPLICATORE_VALORE;
	}


	@Override
	public double getMoltiplicatoreContrattazione() {
		return CostantiPersonaggi.MINOTAURO_MOLTIPLICATORE_CONTRATTAZIONE;
	}


	@Override
	public double getMoltiplicatoreNumeroBersagli() {
		return CostantiPersonaggi.MINOTAURO_MOLTIPLICATORE_NUMERO_BERSAGLI;
	}


	@Override
	public double getMoltiplicatoreStanchezza() {
		return CostantiPersonaggi.MINOTAURO_MOLTIPLICATORE_STANCHEZZA;
	}


	@Override
	public double getMoltiplicatoreDanniMagici() {
		return CostantiPersonaggi.MINOTAURO_MOLTIPLICATORE_DANNI_MAGICI;
	}


	@Override
	public double getMoltiplicatoreDanniFisici() {
		return CostantiPersonaggi.MINOTAURO_MOLTIPLICATORE_DANNI_FISICI;
	}


	@Override
	public double getMoltiplicatoreRecuperoMagico() {
		return CostantiPersonaggi.MINOTAURO_MOLTIPLICATORE_RECUPERO_MAGICO;
	}


	@Override
	public double getMoltiplicatoreRecuperoFisico() {
		return CostantiPersonaggi.MINOTAURO_MOLTIPLICATORE_RECUPERO_FISICO;
	}


	@Override
	public double getMaxStatistica(TipoAttributo tipoAttributo) {
		switch (tipoAttributo) {
			case FORZA:
				return CostantiPersonaggi.MINOTAURO_MAX_FORZA;
			case DESTREZZA:
				return CostantiPersonaggi.MINOTAURO_MAX_DESTREZZA;
			case COSTITUZIONE:
				return CostantiPersonaggi.MINOTAURO_MAX_COSTITUZIONE;
			case INTELLIGENZA:
				return CostantiPersonaggi.MINOTAURO_MAX_INTELLIGENZA;
			case SAGGEZZA:
				return CostantiPersonaggi.MINOTAURO_MAX_SAGGEZZA;
			case CARISMA:
				return CostantiPersonaggi.MINOTAURO_MAX_CARISMA;
			case FORTUNA:
				return CostantiPersonaggi.MINOTAURO_MAX_FORTUNA;
			default:
				throw new IllegalArgumentException(tipoAttributo + " non è un tipo primario");
		}
	}

}
