package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.tipi.Costanti;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.strumenti.Misc;

import java.util.function.Function;

/**
 * Il ladro giocante, al maschile: umanoide, corrompibile e amichevole. Può impugnare una seconda arma. Il femminile è la
 * {@link Ladra}.
 */
public class Ladro extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UN; }
	public String getADS() { return Misc.IL; }
	public String getAIP() { return Misc.ALCUNI; }
	public String getADP() { return Misc.I; }
	public String getDeS() { return Misc.DEL; }
	public String getDeP() { return Misc.DEI; }
	public String getDa() { return Misc.DA_UN; }

	public String getNomeSingolare() { return "Ladro"; }
	public String getNomePlurale() { return "Ladri"; }
	public String getPronome() { return Misc.EGLI; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.MASCHIO; }

	public Ladro(int livello) {
		super(TipoPersonaggio.LADRO, livello);
	}

	public Ladro(String nome, int livello) {
		super(nome, TipoPersonaggio.LADRO, livello);
	}

	@Override
	protected void impostaValoriDiPartenza(Function<Integer, Integer> funzione) {
		setCorrompibile(true);
		setAmichevole(true);

		md.setForza(funzione.apply(getAttributoAdeguatoALivello(Costanti.LADRO_MAX_FORZA)));
		md.setDestrezza(funzione.apply(getAttributoAdeguatoALivello(Costanti.LADRO_MAX_DESTREZZA)));
		md.setCostituzione(funzione.apply(getAttributoAdeguatoALivello(Costanti.LADRO_MAX_COSTITUZIONE)));
		md.setIntelligenza(funzione.apply(getAttributoAdeguatoALivello(Costanti.LADRO_MAX_INTELLIGENZA)));
		md.setSaggezza(funzione.apply(getAttributoAdeguatoALivello(Costanti.LADRO_MAX_SAGGEZZA)));
		md.setCarisma(funzione.apply(getAttributoAdeguatoALivello(Costanti.LADRO_MAX_CARISMA)));
		md.setFortuna(funzione.apply(getAttributoAdeguatoALivello(Costanti.LADRO_MAX_FORTUNA)));

		setQuantitaMassima(Costanti.LADRO_MAX_NUMERO);
	}

	@Override
	public int getPuntiEsperienza() {
		return Costanti.LADRO_PUNTI_ESPERIENZA;
	}

	@Override
	public double getSaluteBase() {
		return Costanti.LADRO_SALUTE_BASE;
	}

	@Override
	public double getMagiaBase() {
		return Costanti.LADRO_MAGIA_BASE;
	}

	@Override
	public double getLivellamentoMagia() {
		return Costanti.LADRO_LIVELLAMENTO_MAGIA;
	}

	@Override
	public double getMoltiplicatoreCarico() {
		return Costanti.LADRO_MOLTIPLICATORE_CARICO;
	}


	@Override
	public double getMoltiplicatoreCritico() {
		return Costanti.LADRO_MOLTIPLICATORE_CRITICO;
	}


	@Override
	public double getMoltiplicatorePrecisione() {
		return Costanti.LADRO_MOLTIPLICATORE_PRECISIONE;
	}


	@Override
	public double getMoltiplicatoreVelocita() {
		return Costanti.LADRO_MOLTIPLICATORE_VELOCITA;
	}


	@Override
	public double getMoltiplicatoreFurtivita() {
		return Costanti.LADRO_MOLTIPLICATORE_FURTIVITA;
	}


	@Override
	public double getMoltiplicatoreParata() {
		return Costanti.LADRO_MOLTIPLICATORE_PARATA;
	}


	@Override
	public double getMoltiplicatoreResistenzaMagica() {
		return Costanti.LADRO_MOLTIPLICATORE_RESISTENZA_MAGICA;
	}


	@Override
	public double getMoltiplicatorePercezione() {
		return Costanti.LADRO_MOLTIPLICATORE_PERCEZIONE;
	}


	@Override
	public double getMoltiplicatoreSoggezione() {
		return Costanti.LADRO_MOLTIPLICATORE_SOGGEZIONE;
	}


	@Override
	public double getMoltiplicatoreFuria() {
		return Costanti.LADRO_MOLTIPLICATORE_FURIA;
	}


	@Override
	public double getMoltiplicatoreCoraggio() {
		return Costanti.LADRO_MOLTIPLICATORE_CORAGGIO;
	}


	@Override
	public double getMoltiplicatoreValore() {
		return Costanti.LADRO_MOLTIPLICATORE_VALORE;
	}


	@Override
	public double getMoltiplicatoreContrattazione() {
		return Costanti.LADRO_MOLTIPLICATORE_CONTRATTAZIONE;
	}


	@Override
	public double getMoltiplicatoreNumeroBersagli() {
		return Costanti.LADRO_MOLTIPLICATORE_NUMERO_BERSAGLI;
	}


	@Override
	public double getMoltiplicatoreStanchezza() {
		return Costanti.LADRO_MOLTIPLICATORE_STANCHEZZA;
	}


	@Override
	public double getMoltiplicatoreDanniMagici() {
		return Costanti.LADRO_MOLTIPLICATORE_DANNI_MAGICI;
	}


	@Override
	public double getMoltiplicatoreDanniFisici() {
		return Costanti.LADRO_MOLTIPLICATORE_DANNI_FISICI;
	}


	@Override
	public double getMoltiplicatoreRecuperoMagico() {
		return Costanti.LADRO_MOLTIPLICATORE_RECUPERO_MAGICO;
	}


	@Override
	public double getMoltiplicatoreRecuperoFisico() {
		return Costanti.LADRO_MOLTIPLICATORE_RECUPERO_FISICO;
	}


	@Override
	public double getMaxStatistica(TipoAttributo tipoAttributo) {
		switch (tipoAttributo) {
			case FORZA:
				return Costanti.LADRO_MAX_FORZA;
			case DESTREZZA:
				return Costanti.LADRO_MAX_DESTREZZA;
			case COSTITUZIONE:
				return Costanti.LADRO_MAX_COSTITUZIONE;
			case INTELLIGENZA:
				return Costanti.LADRO_MAX_INTELLIGENZA;
			case SAGGEZZA:
				return Costanti.LADRO_MAX_SAGGEZZA;
			case CARISMA:
				return Costanti.LADRO_MAX_CARISMA;
			case FORTUNA:
				return Costanti.LADRO_MAX_FORTUNA;
			default:
				throw new IllegalArgumentException(tipoAttributo + " non è un tipo primario");
		}
	}
}
