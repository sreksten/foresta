package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.testi.Articoli;
import com.threeamigos.foresta.tipi.Costanti;
import com.threeamigos.foresta.attributi.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoOfferta;
import com.threeamigos.foresta.tipi.TipoPersonaggio;


/**
 * Il gigante: creatura umanoide e gigantesca, corrompibile e amichevole. Con l'amicizia offre aiuto, incantesimi,
 * informazioni, mappe e un pasto.
 */
public class Gigante extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Articoli.UN; }
	public String getADS() { return Articoli.IL; }
	public String getAIP() { return Articoli.ALCUNI; }
	public String getADP() { return Articoli.I; }
	public String getDeS() { return Articoli.DEL; }
	public String getDeP() { return Articoli.DEI; }
	public String getDa() { return Articoli.DA_UN; }

	public String getNomeSingolare() { return "Gigante"; }
	public String getNomePlurale() { return "Giganti"; }
	public String getPronome() { return Articoli.EGLI; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.MASCHIO; }

	public Gigante(int livello) {
		super(TipoPersonaggio.GIGANTE, livello);
	}

	@Override
	protected void impostaValoriDiPartenza() {
		setCorrompibile(true);
		setAmichevole(true);

		setQuantitaMassima(Costanti.GIGANTE_MAX_NUMERO);
	}

	@Override
	public int getPuntiEsperienza() {
		return Costanti.GIGANTE_PUNTI_ESPERIENZA;
	}

	@Override
	public double getSaluteBase() {
		return Costanti.GIGANTE_SALUTE_BASE;
	}

	@Override
	public double getMagiaBase() {
		return Costanti.GIGANTE_MAGIA_BASE;
	}

	@Override
	public double getLivellamentoMagia() {
		return Costanti.GIGANTE_LIVELLAMENTO_MAGIA;
	}

	@Override
	public double getMoltiplicatoreCarico() {
		return Costanti.GIGANTE_MOLTIPLICATORE_CARICO;
	}


	@Override
	public double getMoltiplicatoreCritico() {
		return Costanti.GIGANTE_MOLTIPLICATORE_CRITICO;
	}


	@Override
	public double getMoltiplicatorePrecisione() {
		return Costanti.GIGANTE_MOLTIPLICATORE_PRECISIONE;
	}


	@Override
	public double getMoltiplicatoreVelocita() {
		return Costanti.GIGANTE_MOLTIPLICATORE_VELOCITA;
	}


	@Override
	public double getMoltiplicatoreFurtivita() {
		return Costanti.GIGANTE_MOLTIPLICATORE_FURTIVITA;
	}


	@Override
	public double getMoltiplicatoreParata() {
		return Costanti.GIGANTE_MOLTIPLICATORE_PARATA;
	}


	@Override
	public double getMoltiplicatoreResistenzaMagica() {
		return Costanti.GIGANTE_MOLTIPLICATORE_RESISTENZA_MAGICA;
	}


	@Override
	public double getMoltiplicatorePercezione() {
		return Costanti.GIGANTE_MOLTIPLICATORE_PERCEZIONE;
	}


	@Override
	public double getMoltiplicatoreSoggezione() {
		return Costanti.GIGANTE_MOLTIPLICATORE_SOGGEZIONE;
	}


	@Override
	public double getMoltiplicatoreFuria() {
		return Costanti.GIGANTE_MOLTIPLICATORE_FURIA;
	}


	@Override
	public double getMoltiplicatoreCoraggio() {
		return Costanti.GIGANTE_MOLTIPLICATORE_CORAGGIO;
	}


	@Override
	public double getMoltiplicatoreValore() {
		return Costanti.GIGANTE_MOLTIPLICATORE_VALORE;
	}


	@Override
	public double getMoltiplicatoreContrattazione() {
		return Costanti.GIGANTE_MOLTIPLICATORE_CONTRATTAZIONE;
	}


	@Override
	public double getMoltiplicatoreNumeroBersagli() {
		return Costanti.GIGANTE_MOLTIPLICATORE_NUMERO_BERSAGLI;
	}


	@Override
	public double getMoltiplicatoreStanchezza() {
		return Costanti.GIGANTE_MOLTIPLICATORE_STANCHEZZA;
	}


	@Override
	public double getMoltiplicatoreDanniMagici() {
		return Costanti.GIGANTE_MOLTIPLICATORE_DANNI_MAGICI;
	}


	@Override
	public double getMoltiplicatoreDanniFisici() {
		return Costanti.GIGANTE_MOLTIPLICATORE_DANNI_FISICI;
	}


	@Override
	public double getMoltiplicatoreRecuperoMagico() {
		return Costanti.GIGANTE_MOLTIPLICATORE_RECUPERO_MAGICO;
	}


	@Override
	public double getMoltiplicatoreRecuperoFisico() {
		return Costanti.GIGANTE_MOLTIPLICATORE_RECUPERO_FISICO;
	}


	@Override
	public double getMaxStatistica(TipoAttributo tipoAttributo) {
		switch (tipoAttributo) {
			case FORZA:
				return Costanti.GIGANTE_MAX_FORZA;
			case DESTREZZA:
				return Costanti.GIGANTE_MAX_DESTREZZA;
			case COSTITUZIONE:
				return Costanti.GIGANTE_MAX_COSTITUZIONE;
			case INTELLIGENZA:
				return Costanti.GIGANTE_MAX_INTELLIGENZA;
			case SAGGEZZA:
				return Costanti.GIGANTE_MAX_SAGGEZZA;
			case CARISMA:
				return Costanti.GIGANTE_MAX_CARISMA;
			case FORTUNA:
				return Costanti.GIGANTE_MAX_FORTUNA;
			default:
				throw new IllegalArgumentException(tipoAttributo + " non è un tipo primario");
		}
	}

	@Override
	public TipoOfferta[] getOfferteAmicizia() {
		return new TipoOfferta[] {
				TipoOfferta.AIUTO_GRATUITO,
				TipoOfferta.INCANTESIMI,
				TipoOfferta.INFORMAZIONI,
				TipoOfferta.MAPPA_FORESTA,
				TipoOfferta.MAPPA_ZONA,
				TipoOfferta.PASTO
		};
	}
}
