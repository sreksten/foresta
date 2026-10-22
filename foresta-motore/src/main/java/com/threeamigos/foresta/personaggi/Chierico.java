package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.tipi.Costanti;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.strumenti.Misc;


/**
 * Il chierico giocante, al maschile: il sacerdote che combatte in prima linea, con mazza, scudo e armatura. Non ha l'alba
 * sacra e la luce divina innate (sono del Sacerdote): sa scacciare i non morti (vedi {@code ScacciareINonMorti}) e imporre
 * le mani ({@code ImposizioneDelleMani}). Il femminile è la {@link Chierica}.
 */
public class Chierico extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UN; }
	public String getADS() { return Misc.IL; }
	public String getAIP() { return Misc.ALCUNI; }
	public String getADP() { return Misc.I; }
	public String getDeS() { return Misc.DEL; }
	public String getDeP() { return Misc.DEI; }
	public String getDa() { return Misc.DA_UN; }

	public String getNomeSingolare() { return "Chierico"; }
	public String getNomePlurale() { return "Chierici"; }
	public String getPronome() { return Misc.EGLI; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.MASCHIO; }

	public Chierico(int livello) {
		super(TipoPersonaggio.CHIERICO, livello);
	}

	public Chierico(String nome, int livello) {
		super(nome, TipoPersonaggio.CHIERICO, livello);
	}


	@Override
	protected void impostaValoriDiPartenza() {
		setCorrompibile(true);
		setAmichevole(true);

		setQuantitaMassima(Costanti.CHIERICO_MAX_NUMERO);
	}

	@Override
	public int getPuntiEsperienza() {
		return Costanti.CHIERICO_PUNTI_ESPERIENZA;
	}

	@Override
	public double getSaluteBase() {
		return Costanti.CHIERICO_SALUTE_BASE;
	}

	@Override
	public double getMagiaBase() {
		return Costanti.CHIERICO_MAGIA_BASE;
	}

	@Override
	public double getLivellamentoMagia() {
		return Costanti.CHIERICO_LIVELLAMENTO_MAGIA;
	}

	@Override
	public double getMoltiplicatoreCarico() {
		return Costanti.CHIERICO_MOLTIPLICATORE_CARICO;
	}


	@Override
	public double getMoltiplicatoreCritico() {
		return Costanti.CHIERICO_MOLTIPLICATORE_CRITICO;
	}


	@Override
	public double getMoltiplicatorePrecisione() {
		return Costanti.CHIERICO_MOLTIPLICATORE_PRECISIONE;
	}


	@Override
	public double getMoltiplicatoreVelocita() {
		return Costanti.CHIERICO_MOLTIPLICATORE_VELOCITA;
	}


	@Override
	public double getMoltiplicatoreFurtivita() {
		return Costanti.CHIERICO_MOLTIPLICATORE_FURTIVITA;
	}


	@Override
	public double getMoltiplicatoreParata() {
		return Costanti.CHIERICO_MOLTIPLICATORE_PARATA;
	}


	@Override
	public double getMoltiplicatoreResistenzaMagica() {
		return Costanti.CHIERICO_MOLTIPLICATORE_RESISTENZA_MAGICA;
	}


	@Override
	public double getMoltiplicatorePercezione() {
		return Costanti.CHIERICO_MOLTIPLICATORE_PERCEZIONE;
	}


	@Override
	public double getMoltiplicatoreSoggezione() {
		return Costanti.CHIERICO_MOLTIPLICATORE_SOGGEZIONE;
	}


	@Override
	public double getMoltiplicatoreFuria() {
		return Costanti.CHIERICO_MOLTIPLICATORE_FURIA;
	}


	@Override
	public double getMoltiplicatoreCoraggio() {
		return Costanti.CHIERICO_MOLTIPLICATORE_CORAGGIO;
	}


	@Override
	public double getMoltiplicatoreValore() {
		return Costanti.CHIERICO_MOLTIPLICATORE_VALORE;
	}


	@Override
	public double getMoltiplicatoreContrattazione() {
		return Costanti.CHIERICO_MOLTIPLICATORE_CONTRATTAZIONE;
	}


	@Override
	public double getMoltiplicatoreNumeroBersagli() {
		return Costanti.CHIERICO_MOLTIPLICATORE_NUMERO_BERSAGLI;
	}


	@Override
	public double getMoltiplicatoreStanchezza() {
		return Costanti.CHIERICO_MOLTIPLICATORE_STANCHEZZA;
	}


	@Override
	public double getMoltiplicatoreDanniMagici() {
		return Costanti.CHIERICO_MOLTIPLICATORE_DANNI_MAGICI;
	}


	@Override
	public double getMoltiplicatoreDanniFisici() {
		return Costanti.CHIERICO_MOLTIPLICATORE_DANNI_FISICI;
	}


	@Override
	public double getMoltiplicatoreRecuperoMagico() {
		return Costanti.CHIERICO_MOLTIPLICATORE_RECUPERO_MAGICO;
	}


	@Override
	public double getMoltiplicatoreRecuperoFisico() {
		return Costanti.CHIERICO_MOLTIPLICATORE_RECUPERO_FISICO;
	}


	@Override
	public double getMaxStatistica(TipoAttributo tipoAttributo) {
		switch (tipoAttributo) {
			case FORZA:
				return Costanti.CHIERICO_MAX_FORZA;
			case DESTREZZA:
				return Costanti.CHIERICO_MAX_DESTREZZA;
			case COSTITUZIONE:
				return Costanti.CHIERICO_MAX_COSTITUZIONE;
			case INTELLIGENZA:
				return Costanti.CHIERICO_MAX_INTELLIGENZA;
			case SAGGEZZA:
				return Costanti.CHIERICO_MAX_SAGGEZZA;
			case CARISMA:
				return Costanti.CHIERICO_MAX_CARISMA;
			case FORTUNA:
				return Costanti.CHIERICO_MAX_FORTUNA;
			default:
				throw new IllegalArgumentException(tipoAttributo + " non è un tipo primario");
		}
	}

}
