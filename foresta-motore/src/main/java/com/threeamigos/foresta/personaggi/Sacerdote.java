package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.attributi.tipi.TipoAttributo;
import com.threeamigos.foresta.testi.Articoli;
import com.threeamigos.foresta.tipi.Costanti;
import com.threeamigos.foresta.tipi.TipoPersonaggio;


/**
 * Come il Mago, ma con Saggezza e robustezza al posto della sola Intelligenza, e senza dardo arcano: le sue abilità
 * innate sono l'alba sacra e la luce divina (vedi abilita.AlbaSacra e abilita.LuceDivina), che costano MAGIA e non consumano pergamene. Usa il bastone magico e la mazza.
 */
public class Sacerdote extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Articoli.UN; }
	public String getADS() { return Articoli.IL; }
	public String getAIP() { return Articoli.ALCUNI; }
	public String getADP() { return Articoli.I; }
	public String getDeS() { return Articoli.DEL; }
	public String getDeP() { return Articoli.DEI; }
	public String getDa() { return Articoli.DAL; }

	public String getNomeSingolare() { return "Sacerdote"; }
	public String getNomePlurale() { return "Sacerdoti"; }
	public String getPronome() { return Articoli.EGLI; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.MASCHIO; }

	public Sacerdote(int livello) {
		super(TipoPersonaggio.SACERDOTE, livello);
	}

	public Sacerdote(String nome, int livello) {
		super(nome, TipoPersonaggio.SACERDOTE, livello);
	}

	@Override
	protected void impostaValoriDiPartenza() {
		setCorrompibile(true);
		setAmichevole(true);

		setQuantitaMassima(Costanti.SACERDOTE_MAX_NUMERO);
	}

	@Override
	public int getPuntiEsperienza() {
		return Costanti.SACERDOTE_PUNTI_ESPERIENZA;
	}

	@Override
	public double getSaluteBase() {
		return Costanti.SACERDOTE_SALUTE_BASE;
	}

	@Override
	public double getMagiaBase() {
		return Costanti.SACERDOTE_MAGIA_BASE;
	}

	@Override
	public double getLivellamentoMagia() {
		return Costanti.SACERDOTE_LIVELLAMENTO_MAGIA;
	}

	@Override
	public double getMoltiplicatoreCarico() {
		return Costanti.SACERDOTE_MOLTIPLICATORE_CARICO;
	}


	@Override
	public double getMoltiplicatoreCritico() {
		return Costanti.SACERDOTE_MOLTIPLICATORE_CRITICO;
	}


	@Override
	public double getMoltiplicatorePrecisione() {
		return Costanti.SACERDOTE_MOLTIPLICATORE_PRECISIONE;
	}


	@Override
	public double getMoltiplicatoreVelocita() {
		return Costanti.SACERDOTE_MOLTIPLICATORE_VELOCITA;
	}


	@Override
	public double getMoltiplicatoreFurtivita() {
		return Costanti.SACERDOTE_MOLTIPLICATORE_FURTIVITA;
	}


	@Override
	public double getMoltiplicatoreParata() {
		return Costanti.SACERDOTE_MOLTIPLICATORE_PARATA;
	}


	@Override
	public double getMoltiplicatoreResistenzaMagica() {
		return Costanti.SACERDOTE_MOLTIPLICATORE_RESISTENZA_MAGICA;
	}


	@Override
	public double getMoltiplicatorePercezione() {
		return Costanti.SACERDOTE_MOLTIPLICATORE_PERCEZIONE;
	}


	@Override
	public double getMoltiplicatoreSoggezione() {
		return Costanti.SACERDOTE_MOLTIPLICATORE_SOGGEZIONE;
	}


	@Override
	public double getMoltiplicatoreFuria() {
		return Costanti.SACERDOTE_MOLTIPLICATORE_FURIA;
	}


	@Override
	public double getMoltiplicatoreCoraggio() {
		return Costanti.SACERDOTE_MOLTIPLICATORE_CORAGGIO;
	}


	@Override
	public double getMoltiplicatoreValore() {
		return Costanti.SACERDOTE_MOLTIPLICATORE_VALORE;
	}


	@Override
	public double getMoltiplicatoreContrattazione() {
		return Costanti.SACERDOTE_MOLTIPLICATORE_CONTRATTAZIONE;
	}


	@Override
	public double getMoltiplicatoreNumeroBersagli() {
		return Costanti.SACERDOTE_MOLTIPLICATORE_NUMERO_BERSAGLI;
	}


	@Override
	public double getMoltiplicatoreStanchezza() {
		return Costanti.SACERDOTE_MOLTIPLICATORE_STANCHEZZA;
	}


	@Override
	public double getMoltiplicatoreDanniMagici() {
		return Costanti.SACERDOTE_MOLTIPLICATORE_DANNI_MAGICI;
	}


	@Override
	public double getMoltiplicatoreDanniFisici() {
		return Costanti.SACERDOTE_MOLTIPLICATORE_DANNI_FISICI;
	}


	@Override
	public double getMoltiplicatoreRecuperoMagico() {
		return Costanti.SACERDOTE_MOLTIPLICATORE_RECUPERO_MAGICO;
	}


	@Override
	public double getMoltiplicatoreRecuperoFisico() {
		return Costanti.SACERDOTE_MOLTIPLICATORE_RECUPERO_FISICO;
	}


	@Override
	public double getMaxStatistica(TipoAttributo tipoAttributo) {
		switch (tipoAttributo) {
			case FORZA:
				return Costanti.SACERDOTE_MAX_FORZA;
			case DESTREZZA:
				return Costanti.SACERDOTE_MAX_DESTREZZA;
			case COSTITUZIONE:
				return Costanti.SACERDOTE_MAX_COSTITUZIONE;
			case INTELLIGENZA:
				return Costanti.SACERDOTE_MAX_INTELLIGENZA;
			case SAGGEZZA:
				return Costanti.SACERDOTE_MAX_SAGGEZZA;
			case CARISMA:
				return Costanti.SACERDOTE_MAX_CARISMA;
			case FORTUNA:
				return Costanti.SACERDOTE_MAX_FORTUNA;
			default:
				throw new IllegalArgumentException(tipoAttributo + " non è un tipo primario");
		}
	}
}
