package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.tipi.Costanti;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.strumenti.Misc;

import java.util.function.Function;

/**
 * Il Bardo giocante, al maschile: umanoide, corrompibile e disposto all'amicizia. Conosce i canti (vedi il pacchetto
 * {@code canti}). Il femminile è la {@link Cantastorie}, che ha lo stesso ruolo.
 */
public class Bardo extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UN; }
	public String getADS() { return Misc.IL; }
	public String getAIP() { return Misc.ALCUNI; }
	public String getADP() { return Misc.I; }
	public String getDeS() { return Misc.DEL; }
	public String getDeP() { return Misc.DEI; }
	public String getDa() { return Misc.DA_UN; }

	public String getNomeSingolare() { return "Bardo"; }
	public String getNomePlurale() { return "Bardi"; }
	public String getPronome() { return Misc.EGLI; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.MASCHIO; }

	public Bardo(int livello) {
		super(TipoPersonaggio.BARDO, livello);
	}

	public Bardo(String nome, int livello) {
		super(nome, TipoPersonaggio.BARDO, livello);
	}

	/**
	 * Per chi ha le caratteristiche del bardo sotto un'altra classe (vedi Viandante).
	 */
	protected Bardo(String nome, TipoPersonaggio classe, int livello) {
		super(nome, classe, livello);
	}
	
	@Override
	protected void impostaValoriDiPartenza(Function<Integer, Integer> funzione) {
		setCorrompibile(true);
		setAmichevole(true);

		md.setForza(funzione.apply(getAttributoAdeguatoALivello(Costanti.BARDO_MAX_FORZA)));
		md.setDestrezza(funzione.apply(getAttributoAdeguatoALivello(Costanti.BARDO_MAX_DESTREZZA)));
		md.setCostituzione(funzione.apply(getAttributoAdeguatoALivello(Costanti.BARDO_MAX_COSTITUZIONE)));
		md.setIntelligenza(funzione.apply(getAttributoAdeguatoALivello(Costanti.BARDO_MAX_INTELLIGENZA)));
		md.setSaggezza(funzione.apply(getAttributoAdeguatoALivello(Costanti.BARDO_MAX_SAGGEZZA)));
		md.setCarisma(funzione.apply(getAttributoAdeguatoALivello(Costanti.BARDO_MAX_CARISMA)));
		md.setFortuna(funzione.apply(getAttributoAdeguatoALivello(Costanti.BARDO_MAX_FORTUNA)));

		setQuantitaMassima(Costanti.BARDO_MAX_NUMERO);
	}

	@Override
	public int getPuntiEsperienza() {
		return Costanti.BARDO_PUNTI_ESPERIENZA;
	}

	@Override
	public double getSaluteBase() {
		return Costanti.BARDO_SALUTE_BASE;
	}

	@Override
	public double getMagiaBase() {
		return Costanti.BARDO_MAGIA_BASE;
	}

	@Override
	public double getLivellamentoMagia() {
		return Costanti.BARDO_LIVELLAMENTO_MAGIA;
	}

	@Override
	public double getMoltiplicatoreCarico() {
		return Costanti.BARDO_MOLTIPLICATORE_CARICO;
	}


	@Override
	public double getMoltiplicatoreCritico() {
		return Costanti.BARDO_MOLTIPLICATORE_CRITICO;
	}


	@Override
	public double getMoltiplicatorePrecisione() {
		return Costanti.BARDO_MOLTIPLICATORE_PRECISIONE;
	}


	@Override
	public double getMoltiplicatoreVelocita() {
		return Costanti.BARDO_MOLTIPLICATORE_VELOCITA;
	}


	@Override
	public double getMoltiplicatoreFurtivita() {
		return Costanti.BARDO_MOLTIPLICATORE_FURTIVITA;
	}


	@Override
	public double getMoltiplicatoreParata() {
		return Costanti.BARDO_MOLTIPLICATORE_PARATA;
	}


	@Override
	public double getMoltiplicatoreResistenzaMagica() {
		return Costanti.BARDO_MOLTIPLICATORE_RESISTENZA_MAGICA;
	}


	@Override
	public double getMoltiplicatorePercezione() {
		return Costanti.BARDO_MOLTIPLICATORE_PERCEZIONE;
	}


	@Override
	public double getMoltiplicatoreSoggezione() {
		return Costanti.BARDO_MOLTIPLICATORE_SOGGEZIONE;
	}


	@Override
	public double getMoltiplicatoreFuria() {
		return Costanti.BARDO_MOLTIPLICATORE_FURIA;
	}


	@Override
	public double getMoltiplicatoreCoraggio() {
		return Costanti.BARDO_MOLTIPLICATORE_CORAGGIO;
	}


	@Override
	public double getMoltiplicatoreValore() {
		return Costanti.BARDO_MOLTIPLICATORE_VALORE;
	}


	@Override
	public double getMoltiplicatoreContrattazione() {
		return Costanti.BARDO_MOLTIPLICATORE_CONTRATTAZIONE;
	}


	@Override
	public double getMoltiplicatoreNumeroBersagli() {
		return Costanti.BARDO_MOLTIPLICATORE_NUMERO_BERSAGLI;
	}


	@Override
	public double getMoltiplicatoreStanchezza() {
		return Costanti.BARDO_MOLTIPLICATORE_STANCHEZZA;
	}


	@Override
	public double getMoltiplicatoreDanniMagici() {
		return Costanti.BARDO_MOLTIPLICATORE_DANNI_MAGICI;
	}


	@Override
	public double getMoltiplicatoreDanniFisici() {
		return Costanti.BARDO_MOLTIPLICATORE_DANNI_FISICI;
	}


	@Override
	public double getMoltiplicatoreRecuperoMagico() {
		return Costanti.BARDO_MOLTIPLICATORE_RECUPERO_MAGICO;
	}


	@Override
	public double getMoltiplicatoreRecuperoFisico() {
		return Costanti.BARDO_MOLTIPLICATORE_RECUPERO_FISICO;
	}


	@Override
	public double getMaxStatistica(TipoAttributo tipoAttributo) {
		switch (tipoAttributo) {
			case FORZA:
				return Costanti.BARDO_MAX_FORZA;
			case DESTREZZA:
				return Costanti.BARDO_MAX_DESTREZZA;
			case COSTITUZIONE:
				return Costanti.BARDO_MAX_COSTITUZIONE;
			case INTELLIGENZA:
				return Costanti.BARDO_MAX_INTELLIGENZA;
			case SAGGEZZA:
				return Costanti.BARDO_MAX_SAGGEZZA;
			case CARISMA:
				return Costanti.BARDO_MAX_CARISMA;
			case FORTUNA:
				return Costanti.BARDO_MAX_FORTUNA;
			default:
				throw new IllegalArgumentException(tipoAttributo + " non è un tipo primario");
		}
	}
}
