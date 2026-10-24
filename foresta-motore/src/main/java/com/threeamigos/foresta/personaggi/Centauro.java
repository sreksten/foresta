package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.testi.Articoli;
import com.threeamigos.foresta.tipi.Costanti;
import com.threeamigos.foresta.attributi.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoOfferta;
import com.threeamigos.foresta.tipi.TipoPersonaggio;


/**
 * Il centauro: creatura umanoide e ibrida, corrompibile e amichevole. Con l'amicizia o con la corruzione può fare qualsiasi
 * genere di offerta.
 */
public class Centauro extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Articoli.UN; }
	public String getADS() { return Articoli.IL; }
	public String getAIP() { return Articoli.ALCUNI; }
	public String getADP() { return Articoli.I; }
	public String getDeS() { return Articoli.DEL; }
	public String getDeP() { return Articoli.DEI; }
	public String getDa() { return Articoli.DA_UN; }

	public String getNomeSingolare() { return "Centauro"; }
	public String getNomePlurale() { return "Centauri"; }
	public String getPronome() { return Articoli.EGLI; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.MASCHIO; }

	public Centauro(int livello) {
		super(TipoPersonaggio.CENTAURO, livello);
	}

	@Override
	protected void impostaValoriDiPartenza() {
		setCorrompibile(true);
		setAmichevole(true);

		setQuantitaMassima(Costanti.CENTAURO_MAX_NUMERO);
	}

	@Override
	public int getPuntiEsperienza() {
		return Costanti.CENTAURO_PUNTI_ESPERIENZA;
	}

	@Override
	public double getSaluteBase() {
		return Costanti.CENTAURO_SALUTE_BASE;
	}

	@Override
	public double getMagiaBase() {
		return Costanti.CENTAURO_MAGIA_BASE;
	}

	@Override
	public double getLivellamentoMagia() {
		return Costanti.CENTAURO_LIVELLAMENTO_MAGIA;
	}

	@Override
	public double getMoltiplicatoreCarico() {
		return Costanti.CENTAURO_MOLTIPLICATORE_CARICO;
	}


	@Override
	public double getMoltiplicatoreCritico() {
		return Costanti.CENTAURO_MOLTIPLICATORE_CRITICO;
	}


	@Override
	public double getMoltiplicatorePrecisione() {
		return Costanti.CENTAURO_MOLTIPLICATORE_PRECISIONE;
	}


	@Override
	public double getMoltiplicatoreVelocita() {
		return Costanti.CENTAURO_MOLTIPLICATORE_VELOCITA;
	}


	@Override
	public double getMoltiplicatoreFurtivita() {
		return Costanti.CENTAURO_MOLTIPLICATORE_FURTIVITA;
	}


	@Override
	public double getMoltiplicatoreParata() {
		return Costanti.CENTAURO_MOLTIPLICATORE_PARATA;
	}


	@Override
	public double getMoltiplicatoreResistenzaMagica() {
		return Costanti.CENTAURO_MOLTIPLICATORE_RESISTENZA_MAGICA;
	}


	@Override
	public double getMoltiplicatorePercezione() {
		return Costanti.CENTAURO_MOLTIPLICATORE_PERCEZIONE;
	}


	@Override
	public double getMoltiplicatoreSoggezione() {
		return Costanti.CENTAURO_MOLTIPLICATORE_SOGGEZIONE;
	}


	@Override
	public double getMoltiplicatoreFuria() {
		return Costanti.CENTAURO_MOLTIPLICATORE_FURIA;
	}


	@Override
	public double getMoltiplicatoreCoraggio() {
		return Costanti.CENTAURO_MOLTIPLICATORE_CORAGGIO;
	}


	@Override
	public double getMoltiplicatoreValore() {
		return Costanti.CENTAURO_MOLTIPLICATORE_VALORE;
	}


	@Override
	public double getMoltiplicatoreContrattazione() {
		return Costanti.CENTAURO_MOLTIPLICATORE_CONTRATTAZIONE;
	}


	@Override
	public double getMoltiplicatoreNumeroBersagli() {
		return Costanti.CENTAURO_MOLTIPLICATORE_NUMERO_BERSAGLI;
	}


	@Override
	public double getMoltiplicatoreStanchezza() {
		return Costanti.CENTAURO_MOLTIPLICATORE_STANCHEZZA;
	}


	@Override
	public double getMoltiplicatoreDanniMagici() {
		return Costanti.CENTAURO_MOLTIPLICATORE_DANNI_MAGICI;
	}


	@Override
	public double getMoltiplicatoreDanniFisici() {
		return Costanti.CENTAURO_MOLTIPLICATORE_DANNI_FISICI;
	}


	@Override
	public double getMoltiplicatoreRecuperoMagico() {
		return Costanti.CENTAURO_MOLTIPLICATORE_RECUPERO_MAGICO;
	}


	@Override
	public double getMoltiplicatoreRecuperoFisico() {
		return Costanti.CENTAURO_MOLTIPLICATORE_RECUPERO_FISICO;
	}


	@Override
	public double getMaxStatistica(TipoAttributo tipoAttributo) {
		switch (tipoAttributo) {
			case FORZA:
				return Costanti.CENTAURO_MAX_FORZA;
			case DESTREZZA:
				return Costanti.CENTAURO_MAX_DESTREZZA;
			case COSTITUZIONE:
				return Costanti.CENTAURO_MAX_COSTITUZIONE;
			case INTELLIGENZA:
				return Costanti.CENTAURO_MAX_INTELLIGENZA;
			case SAGGEZZA:
				return Costanti.CENTAURO_MAX_SAGGEZZA;
			case CARISMA:
				return Costanti.CENTAURO_MAX_CARISMA;
			case FORTUNA:
				return Costanti.CENTAURO_MAX_FORTUNA;
			default:
				throw new IllegalArgumentException(tipoAttributo + " non è un tipo primario");
		}
	}

	@Override
	public TipoOfferta[] getOfferteAmicizia() {
		return new TipoOfferta[] {
				TipoOfferta.AIUTO_GRATUITO,
				TipoOfferta.AIUTO_MERCENARIO,
				TipoOfferta.INCANTESIMI,
				TipoOfferta.INFORMAZIONI,
				TipoOfferta.MAPPA_FORESTA,
				TipoOfferta.MAPPA_ZONA,
				TipoOfferta.PASTO
		};
	}

	@Override
	public TipoOfferta[] getOfferteCorruzione() {
		return new TipoOfferta[] {
				TipoOfferta.AIUTO_GRATUITO,
				TipoOfferta.AIUTO_MERCENARIO,
				TipoOfferta.INCANTESIMI,
				TipoOfferta.INFORMAZIONI,
				TipoOfferta.MAPPA_FORESTA,
				TipoOfferta.MAPPA_ZONA,
				TipoOfferta.PASTO
		};
	}
}
