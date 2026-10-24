package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.testi.Articoli;
import com.threeamigos.foresta.tipi.Costanti;
import com.threeamigos.foresta.attributi.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoPersonaggio;


/**
 * Lo scheletro: non morto. Oltre a ciò che vale per tutti i non morti è immune ad avvelenamento e spavento.
 */
public class Scheletro extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Articoli.UNO; }
	public String getADS() { return Articoli.LO; }
	public String getAIP() { return Articoli.ALCUNI; }
	public String getADP() { return Articoli.GLI; }
	public String getDeS() { return Articoli.DELLO; }
	public String getDeP() { return Articoli.DEGLI; }
	public String getDa() { return Articoli.DA_UNO; }

	public String getNomeSingolare() { return "Scheletro"; }
	public String getNomePlurale() { return "Scheletri"; }
	public String getPronome() { return Articoli.ESSO; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.MASCHIO; }

	public Scheletro(int livello) {
		super(TipoPersonaggio.SCHELETRO, livello);
	}

	@Override
	protected void impostaValoriDiPartenza() {

		setQuantitaMassima(Costanti.SCHELETRO_MAX_NUMERO);
	}

	@Override
	public int getPuntiEsperienza() {
		return Costanti.SCHELETRO_PUNTI_ESPERIENZA;
	}

	@Override
	public double getSaluteBase() {
		return Costanti.SCHELETRO_SALUTE_BASE;
	}

	@Override
	public double getMagiaBase() {
		return Costanti.SCHELETRO_MAGIA_BASE;
	}

	@Override
	public double getLivellamentoMagia() {
		return Costanti.SCHELETRO_LIVELLAMENTO_MAGIA;
	}

	@Override
	public double getMoltiplicatoreCarico() {
		return Costanti.SCHELETRO_MOLTIPLICATORE_CARICO;
	}


	@Override
	public double getMoltiplicatoreCritico() {
		return Costanti.SCHELETRO_MOLTIPLICATORE_CRITICO;
	}


	@Override
	public double getMoltiplicatorePrecisione() {
		return Costanti.SCHELETRO_MOLTIPLICATORE_PRECISIONE;
	}


	@Override
	public double getMoltiplicatoreVelocita() {
		return Costanti.SCHELETRO_MOLTIPLICATORE_VELOCITA;
	}


	@Override
	public double getMoltiplicatoreFurtivita() {
		return Costanti.SCHELETRO_MOLTIPLICATORE_FURTIVITA;
	}


	@Override
	public double getMoltiplicatoreParata() {
		return Costanti.SCHELETRO_MOLTIPLICATORE_PARATA;
	}


	@Override
	public double getMoltiplicatoreResistenzaMagica() {
		return Costanti.SCHELETRO_MOLTIPLICATORE_RESISTENZA_MAGICA;
	}


	@Override
	public double getMoltiplicatorePercezione() {
		return Costanti.SCHELETRO_MOLTIPLICATORE_PERCEZIONE;
	}


	@Override
	public double getMoltiplicatoreSoggezione() {
		return Costanti.SCHELETRO_MOLTIPLICATORE_SOGGEZIONE;
	}


	@Override
	public double getMoltiplicatoreFuria() {
		return Costanti.SCHELETRO_MOLTIPLICATORE_FURIA;
	}


	@Override
	public double getMoltiplicatoreCoraggio() {
		return Costanti.SCHELETRO_MOLTIPLICATORE_CORAGGIO;
	}


	@Override
	public double getMoltiplicatoreValore() {
		return Costanti.SCHELETRO_MOLTIPLICATORE_VALORE;
	}


	@Override
	public double getMoltiplicatoreNumeroBersagli() {
		return Costanti.SCHELETRO_MOLTIPLICATORE_NUMERO_BERSAGLI;
	}


	@Override
	public double getMoltiplicatoreStanchezza() {
		return Costanti.SCHELETRO_MOLTIPLICATORE_STANCHEZZA;
	}


	@Override
	public double getMoltiplicatoreDanniMagici() {
		return Costanti.SCHELETRO_MOLTIPLICATORE_DANNI_MAGICI;
	}


	@Override
	public double getMoltiplicatoreDanniFisici() {
		return Costanti.SCHELETRO_MOLTIPLICATORE_DANNI_FISICI;
	}


	@Override
	public double getMoltiplicatoreRecuperoMagico() {
		return Costanti.SCHELETRO_MOLTIPLICATORE_RECUPERO_MAGICO;
	}


	@Override
	public double getMoltiplicatoreRecuperoFisico() {
		return Costanti.SCHELETRO_MOLTIPLICATORE_RECUPERO_FISICO;
	}


	@Override
	public double getMaxStatistica(TipoAttributo tipoAttributo) {
		switch (tipoAttributo) {
			case FORZA:
				return Costanti.SCHELETRO_MAX_FORZA;
			case DESTREZZA:
				return Costanti.SCHELETRO_MAX_DESTREZZA;
			case COSTITUZIONE:
				return Costanti.SCHELETRO_MAX_COSTITUZIONE;
			case INTELLIGENZA:
				return Costanti.SCHELETRO_MAX_INTELLIGENZA;
			case SAGGEZZA:
				return Costanti.SCHELETRO_MAX_SAGGEZZA;
			case CARISMA:
				return Costanti.SCHELETRO_MAX_CARISMA;
			case FORTUNA:
				return Costanti.SCHELETRO_MAX_FORTUNA;
			default:
				throw new IllegalArgumentException(tipoAttributo + " non è un tipo primario");
		}
	}
}
