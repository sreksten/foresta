package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.tipi.Costanti;
import com.threeamigos.foresta.tipi.ClasseIncantesimo;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.strumenti.Misc;

import java.util.function.Function;

/**
 * Il gargoyle: costrutto volante. È immune a tutti gli incantesimi e agli stati sanguinamento, infezione, avvelenamento e
 * immobilizzazione.
 */
public class Gargoyle extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UN; }
	public String getADS() { return Misc.IL; }
	public String getAIP() { return Misc.ALCUNI; }
	public String getADP() { return Misc.I; }
	public String getDeS() { return Misc.DEL; }
	public String getDeP() { return Misc.DEI; }
	public String getDa() { return Misc.DA_UN; }

	public String getNomeSingolare() { return "Gargoyle"; }
	public String getNomePlurale() { return "Gargoyle"; }
	public String getPronome() { return Misc.ESSO; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.MASCHIO; }

	public Gargoyle(int livello) {
		super(TipoPersonaggio.GARGOYLE, livello);
	}

	@Override
	protected void impostaValoriDiPartenza(Function<Integer, Integer> funzione) {
		md.setForza(funzione.apply(getAttributoAdeguatoALivello(Costanti.GARGOYLE_MAX_FORZA)));
		md.setDestrezza(funzione.apply(getAttributoAdeguatoALivello(Costanti.GARGOYLE_MAX_DESTREZZA)));
		md.setCostituzione(funzione.apply(getAttributoAdeguatoALivello(Costanti.GARGOYLE_MAX_COSTITUZIONE)));
		md.setIntelligenza(funzione.apply(getAttributoAdeguatoALivello(Costanti.GARGOYLE_MAX_INTELLIGENZA)));
		md.setSaggezza(funzione.apply(getAttributoAdeguatoALivello(Costanti.GARGOYLE_MAX_SAGGEZZA)));
		md.setCarisma(funzione.apply(getAttributoAdeguatoALivello(Costanti.GARGOYLE_MAX_CARISMA)));
		md.setFortuna(funzione.apply(getAttributoAdeguatoALivello(Costanti.GARGOYLE_MAX_FORTUNA)));

		setQuantitaMassima(Costanti.GARGOYLE_MAX_NUMERO);
	}

	@Override
	public int getPuntiEsperienza() {
		return Costanti.GARGOYLE_PUNTI_ESPERIENZA;
	}

	@Override
	public double getSaluteBase() {
		return Costanti.GARGOYLE_SALUTE_BASE;
	}

	@Override
	public double getMagiaBase() {
		return Costanti.GARGOYLE_MAGIA_BASE;
	}

	@Override
	public double getLivellamentoMagia() {
		return Costanti.GARGOYLE_LIVELLAMENTO_MAGIA;
	}

	@Override
	public double getMoltiplicatoreCarico() {
		return Costanti.GARGOYLE_MOLTIPLICATORE_CARICO;
	}


	@Override
	public double getMoltiplicatoreCritico() {
		return Costanti.GARGOYLE_MOLTIPLICATORE_CRITICO;
	}


	@Override
	public double getMoltiplicatorePrecisione() {
		return Costanti.GARGOYLE_MOLTIPLICATORE_PRECISIONE;
	}


	@Override
	public double getMoltiplicatoreVelocita() {
		return Costanti.GARGOYLE_MOLTIPLICATORE_VELOCITA;
	}


	@Override
	public double getMoltiplicatoreFurtivita() {
		return Costanti.GARGOYLE_MOLTIPLICATORE_FURTIVITA;
	}


	@Override
	public double getMoltiplicatoreParata() {
		return Costanti.GARGOYLE_MOLTIPLICATORE_PARATA;
	}


	@Override
	public double getMoltiplicatoreResistenzaMagica() {
		return Costanti.GARGOYLE_MOLTIPLICATORE_RESISTENZA_MAGICA;
	}


	@Override
	public double getMoltiplicatorePercezione() {
		return Costanti.GARGOYLE_MOLTIPLICATORE_PERCEZIONE;
	}


	@Override
	public double getMoltiplicatoreSoggezione() {
		return Costanti.GARGOYLE_MOLTIPLICATORE_SOGGEZIONE;
	}


	@Override
	public double getMoltiplicatoreFuria() {
		return Costanti.GARGOYLE_MOLTIPLICATORE_FURIA;
	}


	@Override
	public double getMoltiplicatoreCoraggio() {
		return Costanti.GARGOYLE_MOLTIPLICATORE_CORAGGIO;
	}


	@Override
	public double getMoltiplicatoreValore() {
		return Costanti.GARGOYLE_MOLTIPLICATORE_VALORE;
	}


	@Override
	public double getMoltiplicatoreNumeroBersagli() {
		return Costanti.GARGOYLE_MOLTIPLICATORE_NUMERO_BERSAGLI;
	}


	@Override
	public double getMoltiplicatoreStanchezza() {
		return Costanti.GARGOYLE_MOLTIPLICATORE_STANCHEZZA;
	}


	@Override
	public double getMoltiplicatoreDanniMagici() {
		return Costanti.GARGOYLE_MOLTIPLICATORE_DANNI_MAGICI;
	}


	@Override
	public double getMoltiplicatoreDanniFisici() {
		return Costanti.GARGOYLE_MOLTIPLICATORE_DANNI_FISICI;
	}


	@Override
	public double getMoltiplicatoreRecuperoMagico() {
		return Costanti.GARGOYLE_MOLTIPLICATORE_RECUPERO_MAGICO;
	}


	@Override
	public double getMoltiplicatoreRecuperoFisico() {
		return Costanti.GARGOYLE_MOLTIPLICATORE_RECUPERO_FISICO;
	}


	@Override
	public double getMaxStatistica(TipoAttributo tipoAttributo) {
		switch (tipoAttributo) {
			case FORZA:
				return Costanti.GARGOYLE_MAX_FORZA;
			case DESTREZZA:
				return Costanti.GARGOYLE_MAX_DESTREZZA;
			case COSTITUZIONE:
				return Costanti.GARGOYLE_MAX_COSTITUZIONE;
			case INTELLIGENZA:
				return Costanti.GARGOYLE_MAX_INTELLIGENZA;
			case SAGGEZZA:
				return Costanti.GARGOYLE_MAX_SAGGEZZA;
			case CARISMA:
				return Costanti.GARGOYLE_MAX_CARISMA;
			case FORTUNA:
				return Costanti.GARGOYLE_MAX_FORTUNA;
			default:
				throw new IllegalArgumentException(tipoAttributo + " non è un tipo primario");
		}
	}

	@Override
	public boolean isImmuneAIncantesimo(ClasseIncantesimo classeIncantesimo) {
		return true;
	}

	@Override
	public boolean isImmuneAEffetto(TipoEffettoDiStato effettoStato) {
		return effettoStato == TipoEffettoDiStato.SANGUINAMENTO ||
				effettoStato == TipoEffettoDiStato.INFETTATO ||
				effettoStato == TipoEffettoDiStato.AVVELENATO ||
				effettoStato == TipoEffettoDiStato.IMMOBILIZZATO;
	}
}
