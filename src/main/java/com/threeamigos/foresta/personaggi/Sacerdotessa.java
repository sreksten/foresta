package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.strumenti.Misc;

import java.util.function.Function;

/**
 * Come il Maga, ma con Saggezza e robustezza al posto della sola Intelligenza, e senza dardo arcano: il suo incantesimo
 * innato è l'alba sacra, che non consuma pergamene (vedi AlbaSacra.puoLanciarlaInnata). Usa il bastone magico e la mazza.
 */
public class Sacerdotessa extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UNA; }
	public String getADS() { return Misc.LA; }
	public String getAIP() { return Misc.ALCUNE; }
	public String getADP() { return Misc.LE; }
	public String getDeS() { return Misc.DELLA; }
	public String getDeP() { return Misc.DELLE; }
	public String getDa() { return Misc.DA_UNA; }

	public String getNomeSingolare() { return "Sacerdotessa"; }
	public String getNomePlurale() { return "Sacerdotesse"; }
	public String getPronome() { return Misc.ELLA; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.FEMMINA; }

	public Sacerdotessa(int livello) {
		super(TipoPersonaggio.SACERDOTESSA, livello);
	}

	public Sacerdotessa(String nome, int livello) {
		super(nome, TipoPersonaggio.SACERDOTESSA, livello);
	}

	@Override
	protected void impostaValoriDiPartenza(Function<Integer, Integer> funzione) {
		setCorrompibile(true);
		setAmichevole(true);

		md.setForza(funzione.apply(getAttributoAdeguatoALivello(Costanti.SACERDOTESSA_MAX_FORZA)));
		md.setDestrezza(funzione.apply(getAttributoAdeguatoALivello(Costanti.SACERDOTESSA_MAX_DESTREZZA)));
		md.setCostituzione(funzione.apply(getAttributoAdeguatoALivello(Costanti.SACERDOTESSA_MAX_COSTITUZIONE)));
		md.setIntelligenza(funzione.apply(getAttributoAdeguatoALivello(Costanti.SACERDOTESSA_MAX_INTELLIGENZA)));
		md.setSaggezza(funzione.apply(getAttributoAdeguatoALivello(Costanti.SACERDOTESSA_MAX_SAGGEZZA)));
		md.setCarisma(funzione.apply(getAttributoAdeguatoALivello(Costanti.SACERDOTESSA_MAX_CARISMA)));
		md.setFortuna(funzione.apply(getAttributoAdeguatoALivello(Costanti.SACERDOTESSA_MAX_FORTUNA)));

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
	public String getNoteMoltiplicatoreCarico() {
		return Costanti.SACERDOTESSA_MOLTIPLICATORE_CARICO_NOTA;
	}

	@Override
	public double getMoltiplicatoreCritico() {
		return Costanti.SACERDOTESSA_MOLTIPLICATORE_CRITICO;
	}

	@Override
	public String getNoteMoltiplicatoreCritico() {
		return Costanti.SACERDOTESSA_MOLTIPLICATORE_CRITICO_NOTA;
	}

	@Override
	public double getMoltiplicatorePrecisione() {
		return Costanti.SACERDOTESSA_MOLTIPLICATORE_PRECISIONE;
	}

	@Override
	public String getNoteMoltiplicatorePrecisione() {
		return Costanti.SACERDOTESSA_MOLTIPLICATORE_PRECISIONE_NOTA;
	}

	@Override
	public double getMoltiplicatoreVelocita() {
		return Costanti.SACERDOTESSA_MOLTIPLICATORE_VELOCITA;
	}

	@Override
	public String getNoteMoltiplicatoreVelocita() {
		return Costanti.SACERDOTESSA_MOLTIPLICATORE_VELOCITA_NOTA;
	}

	@Override
	public double getMoltiplicatoreFurtivita() {
		return Costanti.SACERDOTESSA_MOLTIPLICATORE_FURTIVITA;
	}

	@Override
	public String getNoteMoltiplicatoreFurtivita() {
		return Costanti.SACERDOTESSA_MOLTIPLICATORE_FURTIVITA_NOTA;
	}

	@Override
	public double getMoltiplicatoreParata() {
		return Costanti.SACERDOTESSA_MOLTIPLICATORE_PARATA;
	}

	@Override
	public String getNoteMoltiplicatoreParata() {
		return Costanti.SACERDOTESSA_MOLTIPLICATORE_PARATA_NOTA;
	}

	@Override
	public double getMoltiplicatoreResistenzaMagica() {
		return Costanti.SACERDOTESSA_MOLTIPLICATORE_RESISTENZA_MAGICA;
	}

	@Override
	public String getNoteMoltiplicatoreResistenzaMagica() {
		return Costanti.SACERDOTESSA_MOLTIPLICATORE_RESISTENZA_MAGICA_NOTA;
	}

	@Override
	public double getMoltiplicatorePercezione() {
		return Costanti.SACERDOTESSA_MOLTIPLICATORE_PERCEZIONE;
	}

	@Override
	public String getNoteMoltiplicatorePercezione() {
		return Costanti.SACERDOTESSA_MOLTIPLICATORE_PERCEZIONE_NOTA;
	}

	@Override
	public double getMoltiplicatoreSoggezione() {
		return Costanti.SACERDOTESSA_MOLTIPLICATORE_SOGGEZIONE;
	}

	@Override
	public String getNoteMoltiplicatoreSoggezione() {
		return Costanti.SACERDOTESSA_MOLTIPLICATORE_SOGGEZIONE_NOTA;
	}

	@Override
	public double getMoltiplicatoreFuria() {
		return Costanti.SACERDOTESSA_MOLTIPLICATORE_FURIA;
	}

	@Override
	public String getNoteMoltiplicatoreFuria() {
		return Costanti.SACERDOTESSA_MOLTIPLICATORE_FURIA_NOTA;
	}

	@Override
	public double getMoltiplicatoreCoraggio() {
		return Costanti.SACERDOTESSA_MOLTIPLICATORE_CORAGGIO;
	}

	@Override
	public String getNoteMoltiplicatoreCoraggio() {
		return Costanti.SACERDOTESSA_MOLTIPLICATORE_CORAGGIO_NOTA;
	}

	@Override
	public double getMoltiplicatoreValore() {
		return Costanti.SACERDOTESSA_MOLTIPLICATORE_VALORE;
	}

	@Override
	public String getNoteMoltiplicatoreValore() {
		return Costanti.SACERDOTESSA_MOLTIPLICATORE_VALORE_NOTA;
	}

	@Override
	public double getMoltiplicatoreContrattazione() {
		return Costanti.SACERDOTESSA_MOLTIPLICATORE_CONTRATTAZIONE;
	}

	@Override
	public String getNoteMoltiplicatoreContrattazione() {
		return Costanti.SACERDOTESSA_MOLTIPLICATORE_CONTRATTAZIONE_NOTA;
	}

	@Override
	public double getMoltiplicatoreNumeroBersagli() {
		return Costanti.SACERDOTESSA_MOLTIPLICATORE_NUMERO_BERSAGLI;
	}

	@Override
	public String getNoteMoltiplicatoreNumeroBersagli() {
		return Costanti.SACERDOTESSA_MOLTIPLICATORE_NUMERO_BERSAGLI_NOTA;
	}

	@Override
	public double getMoltiplicatoreStanchezza() {
		return Costanti.SACERDOTESSA_MOLTIPLICATORE_STANCHEZZA;
	}

	@Override
	public String getNoteMoltiplicatoreStanchezza() {
		return Costanti.SACERDOTESSA_MOLTIPLICATORE_STANCHEZZA_NOTA;
	}

	@Override
	public double getMoltiplicatoreDanniMagici() {
		return Costanti.SACERDOTESSA_MOLTIPLICATORE_DANNI_MAGICI;
	}

	@Override
	public String getNoteMoltiplicatoreDanniMagici() {
		return Costanti.SACERDOTESSA_MOLTIPLICATORE_DANNI_MAGICI_NOTA;
	}

	@Override
	public double getMoltiplicatoreDanniFisici() {
		return Costanti.SACERDOTESSA_MOLTIPLICATORE_DANNI_FISICI;
	}

	@Override
	public String getNoteMoltiplicatoreDanniFisici() {
		return Costanti.SACERDOTESSA_MOLTIPLICATORE_DANNI_FISICI_NOTA;
	}

	@Override
	public double getMoltiplicatoreRecuperoMagico() {
		return Costanti.SACERDOTESSA_MOLTIPLICATORE_RECUPERO_MAGICO;
	}

	@Override
	public String getNoteMoltiplicatoreRecuperoMagico() {
		return Costanti.SACERDOTESSA_MOLTIPLICATORE_RECUPERO_MAGICO_NOTA;
	}

	@Override
	public double getMoltiplicatoreRecuperoFisico() {
		return Costanti.SACERDOTESSA_MOLTIPLICATORE_RECUPERO_FISICO;
	}

	@Override
	public String getNoteMoltiplicatoreRecuperoFisico() {
		return Costanti.SACERDOTESSA_MOLTIPLICATORE_RECUPERO_FISICO_NOTA;
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
