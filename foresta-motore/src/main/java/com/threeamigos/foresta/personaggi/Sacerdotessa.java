package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.testi.Articoli;
import com.threeamigos.foresta.tipi.Costanti;
import com.threeamigos.foresta.attributi.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoPersonaggio;


/**
 * Come il Maga, ma con Saggezza e robustezza al posto della sola Intelligenza, e senza dardo arcano: il suo incantesimo
 * innato sono l'alba sacra e la luce divina, che non consumano pergamene (vedi AlbaSacra.puoLanciarlaInnata e LuceDivina.puoLanciarlaInnata). Usa il bastone magico e la mazza.
 */
public class Sacerdotessa extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Articoli.UNA; }
	public String getADS() { return Articoli.LA; }
	public String getAIP() { return Articoli.ALCUNE; }
	public String getADP() { return Articoli.LE; }
	public String getDeS() { return Articoli.DELLA; }
	public String getDeP() { return Articoli.DELLE; }
	public String getDa() { return Articoli.DA_UNA; }

	public String getNomeSingolare() { return "Sacerdotessa"; }
	public String getNomePlurale() { return "Sacerdotesse"; }
	public String getPronome() { return Articoli.ELLA; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.FEMMINA; }

	public Sacerdotessa(int livello) {
		super(TipoPersonaggio.SACERDOTESSA, livello);
	}

	public Sacerdotessa(String nome, int livello) {
		super(nome, TipoPersonaggio.SACERDOTESSA, livello);
	}

	@Override
	protected void impostaValoriDiPartenza() {
		setCorrompibile(true);
		setAmichevole(true);

		setQuantitaMassima(Costanti.SACERDOTESSA_MAX_NUMERO);
	}

	@Override
	public int getPuntiEsperienza() {
		return Costanti.SACERDOTESSA_PUNTI_ESPERIENZA;
	}

	@Override
	public double getSaluteBase() {
		return Costanti.SACERDOTESSA_SALUTE_BASE;
	}

	@Override
	public double getMagiaBase() {
		return Costanti.SACERDOTESSA_MAGIA_BASE;
	}

	@Override
	public double getLivellamentoMagia() {
		return Costanti.SACERDOTESSA_LIVELLAMENTO_MAGIA;
	}

	@Override
	public double getMoltiplicatoreCarico() {
		return Costanti.SACERDOTESSA_MOLTIPLICATORE_CARICO;
	}


	@Override
	public double getMoltiplicatoreCritico() {
		return Costanti.SACERDOTESSA_MOLTIPLICATORE_CRITICO;
	}


	@Override
	public double getMoltiplicatorePrecisione() {
		return Costanti.SACERDOTESSA_MOLTIPLICATORE_PRECISIONE;
	}


	@Override
	public double getMoltiplicatoreVelocita() {
		return Costanti.SACERDOTESSA_MOLTIPLICATORE_VELOCITA;
	}


	@Override
	public double getMoltiplicatoreFurtivita() {
		return Costanti.SACERDOTESSA_MOLTIPLICATORE_FURTIVITA;
	}


	@Override
	public double getMoltiplicatoreParata() {
		return Costanti.SACERDOTESSA_MOLTIPLICATORE_PARATA;
	}


	@Override
	public double getMoltiplicatoreResistenzaMagica() {
		return Costanti.SACERDOTESSA_MOLTIPLICATORE_RESISTENZA_MAGICA;
	}


	@Override
	public double getMoltiplicatorePercezione() {
		return Costanti.SACERDOTESSA_MOLTIPLICATORE_PERCEZIONE;
	}


	@Override
	public double getMoltiplicatoreSoggezione() {
		return Costanti.SACERDOTESSA_MOLTIPLICATORE_SOGGEZIONE;
	}


	@Override
	public double getMoltiplicatoreFuria() {
		return Costanti.SACERDOTESSA_MOLTIPLICATORE_FURIA;
	}


	@Override
	public double getMoltiplicatoreCoraggio() {
		return Costanti.SACERDOTESSA_MOLTIPLICATORE_CORAGGIO;
	}


	@Override
	public double getMoltiplicatoreValore() {
		return Costanti.SACERDOTESSA_MOLTIPLICATORE_VALORE;
	}


	@Override
	public double getMoltiplicatoreContrattazione() {
		return Costanti.SACERDOTESSA_MOLTIPLICATORE_CONTRATTAZIONE;
	}


	@Override
	public double getMoltiplicatoreNumeroBersagli() {
		return Costanti.SACERDOTESSA_MOLTIPLICATORE_NUMERO_BERSAGLI;
	}


	@Override
	public double getMoltiplicatoreStanchezza() {
		return Costanti.SACERDOTESSA_MOLTIPLICATORE_STANCHEZZA;
	}


	@Override
	public double getMoltiplicatoreDanniMagici() {
		return Costanti.SACERDOTESSA_MOLTIPLICATORE_DANNI_MAGICI;
	}


	@Override
	public double getMoltiplicatoreDanniFisici() {
		return Costanti.SACERDOTESSA_MOLTIPLICATORE_DANNI_FISICI;
	}


	@Override
	public double getMoltiplicatoreRecuperoMagico() {
		return Costanti.SACERDOTESSA_MOLTIPLICATORE_RECUPERO_MAGICO;
	}


	@Override
	public double getMoltiplicatoreRecuperoFisico() {
		return Costanti.SACERDOTESSA_MOLTIPLICATORE_RECUPERO_FISICO;
	}


	@Override
	public double getMaxStatistica(TipoAttributo tipoAttributo) {
		switch (tipoAttributo) {
			case FORZA:
				return Costanti.SACERDOTESSA_MAX_FORZA;
			case DESTREZZA:
				return Costanti.SACERDOTESSA_MAX_DESTREZZA;
			case COSTITUZIONE:
				return Costanti.SACERDOTESSA_MAX_COSTITUZIONE;
			case INTELLIGENZA:
				return Costanti.SACERDOTESSA_MAX_INTELLIGENZA;
			case SAGGEZZA:
				return Costanti.SACERDOTESSA_MAX_SAGGEZZA;
			case CARISMA:
				return Costanti.SACERDOTESSA_MAX_CARISMA;
			case FORTUNA:
				return Costanti.SACERDOTESSA_MAX_FORTUNA;
			default:
				throw new IllegalArgumentException(tipoAttributo + " non è un tipo primario");
		}
	}
}
