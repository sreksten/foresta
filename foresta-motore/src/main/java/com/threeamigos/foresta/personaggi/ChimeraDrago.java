package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.tipi.Costanti;
import com.threeamigos.foresta.tipi.ClasseIncantesimo;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.strumenti.Misc;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.Function;

/**
 * La chimera drago: bestia ibrida e draconica. Nel suo repertorio ci sono il Fuoco e il Veleno, ed è immune al Fuoco e
 * allo stato bruciato.
 */
public class ChimeraDrago extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UNA; }
	public String getADS() { return Misc.LA; }
	public String getAIP() { return Misc.ALCUNE; }
	public String getADP() { return Misc.LE; }
	public String getDeS() { return Misc.DELLA; }
	public String getDeP() { return Misc.DELLE; }
	public String getDa() { return Misc.DA_UNA; }

	public String getNomeSingolare() { return "Chimera-Drago"; }
	public String getNomePlurale() { return "Chimere-Drago"; }
	public String getPronome() { return Misc.ESSA; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.FEMMINA; }

	public ChimeraDrago(int livello) {
		super(TipoPersonaggio.CHIMERA_DRAGO, livello);
	}

	@Override
	protected void impostaValoriDiPartenza(Function<Integer, Integer> funzione) {
		md.setForza(funzione.apply(getAttributoAdeguatoALivello(Costanti.CHIMERADRAGO_MAX_FORZA)));
		md.setDestrezza(funzione.apply(getAttributoAdeguatoALivello(Costanti.CHIMERADRAGO_MAX_DESTREZZA)));
		md.setCostituzione(funzione.apply(getAttributoAdeguatoALivello(Costanti.CHIMERADRAGO_MAX_COSTITUZIONE)));
		md.setIntelligenza(funzione.apply(getAttributoAdeguatoALivello(Costanti.CHIMERADRAGO_MAX_INTELLIGENZA)));
		md.setSaggezza(funzione.apply(getAttributoAdeguatoALivello(Costanti.CHIMERADRAGO_MAX_SAGGEZZA)));
		md.setCarisma(funzione.apply(getAttributoAdeguatoALivello(Costanti.CHIMERADRAGO_MAX_CARISMA)));
		md.setFortuna(funzione.apply(getAttributoAdeguatoALivello(Costanti.CHIMERADRAGO_MAX_FORTUNA)));

		setQuantitaMassima(Costanti.CHIMERADRAGO_MAX_NUMERO);
	}

	@Override
	public int getPuntiEsperienza() {
		return Costanti.CHIMERADRAGO_PUNTI_ESPERIENZA;
	}

	@Override
	public double getSaluteBase() {
		return Costanti.CHIMERADRAGO_SALUTE_BASE;
	}

	@Override
	public double getMagiaBase() {
		return Costanti.CHIMERADRAGO_MAGIA_BASE;
	}

	@Override
	public double getLivellamentoMagia() {
		return Costanti.CHIMERADRAGO_LIVELLAMENTO_MAGIA;
	}

	@Override
	public Set<ClasseIncantesimo> getRepertorioIncantesimi() {
		return EnumSet.of(ClasseIncantesimo.FUOCO, ClasseIncantesimo.VELENO);
	}

	@Override
	public double getMoltiplicatoreCarico() {
		return Costanti.CHIMERADRAGO_MOLTIPLICATORE_CARICO;
	}


	@Override
	public double getMoltiplicatoreCritico() {
		return Costanti.CHIMERADRAGO_MOLTIPLICATORE_CRITICO;
	}


	@Override
	public double getMoltiplicatorePrecisione() {
		return Costanti.CHIMERADRAGO_MOLTIPLICATORE_PRECISIONE;
	}


	@Override
	public double getMoltiplicatoreVelocita() {
		return Costanti.CHIMERADRAGO_MOLTIPLICATORE_VELOCITA;
	}


	@Override
	public double getMoltiplicatoreFurtivita() {
		return Costanti.CHIMERADRAGO_MOLTIPLICATORE_FURTIVITA;
	}


	@Override
	public double getMoltiplicatoreParata() {
		return Costanti.CHIMERADRAGO_MOLTIPLICATORE_PARATA;
	}


	@Override
	public double getMoltiplicatoreResistenzaMagica() {
		return Costanti.CHIMERADRAGO_MOLTIPLICATORE_RESISTENZA_MAGICA;
	}


	@Override
	public double getMoltiplicatorePercezione() {
		return Costanti.CHIMERADRAGO_MOLTIPLICATORE_PERCEZIONE;
	}


	@Override
	public double getMoltiplicatoreSoggezione() {
		return Costanti.CHIMERADRAGO_MOLTIPLICATORE_SOGGEZIONE;
	}


	@Override
	public double getMoltiplicatoreFuria() {
		return Costanti.CHIMERADRAGO_MOLTIPLICATORE_FURIA;
	}


	@Override
	public double getMoltiplicatoreCoraggio() {
		return Costanti.CHIMERADRAGO_MOLTIPLICATORE_CORAGGIO;
	}


	@Override
	public double getMoltiplicatoreValore() {
		return Costanti.CHIMERADRAGO_MOLTIPLICATORE_VALORE;
	}


	@Override
	public double getMoltiplicatoreNumeroBersagli() {
		return Costanti.CHIMERADRAGO_MOLTIPLICATORE_NUMERO_BERSAGLI;
	}


	@Override
	public double getMoltiplicatoreStanchezza() {
		return Costanti.CHIMERADRAGO_MOLTIPLICATORE_STANCHEZZA;
	}


	@Override
	public double getMoltiplicatoreDanniMagici() {
		return Costanti.CHIMERADRAGO_MOLTIPLICATORE_DANNI_MAGICI;
	}


	@Override
	public double getMoltiplicatoreDanniFisici() {
		return Costanti.CHIMERADRAGO_MOLTIPLICATORE_DANNI_FISICI;
	}


	@Override
	public double getMoltiplicatoreRecuperoMagico() {
		return Costanti.CHIMERADRAGO_MOLTIPLICATORE_RECUPERO_MAGICO;
	}


	@Override
	public double getMoltiplicatoreRecuperoFisico() {
		return Costanti.CHIMERADRAGO_MOLTIPLICATORE_RECUPERO_FISICO;
	}


	@Override
	public double getMaxStatistica(TipoAttributo tipoAttributo) {
		switch (tipoAttributo) {
			case FORZA:
				return Costanti.CHIMERADRAGO_MAX_FORZA;
			case DESTREZZA:
				return Costanti.CHIMERADRAGO_MAX_DESTREZZA;
			case COSTITUZIONE:
				return Costanti.CHIMERADRAGO_MAX_COSTITUZIONE;
			case INTELLIGENZA:
				return Costanti.CHIMERADRAGO_MAX_INTELLIGENZA;
			case SAGGEZZA:
				return Costanti.CHIMERADRAGO_MAX_SAGGEZZA;
			case CARISMA:
				return Costanti.CHIMERADRAGO_MAX_CARISMA;
			case FORTUNA:
				return Costanti.CHIMERADRAGO_MAX_FORTUNA;
			default:
				throw new IllegalArgumentException(tipoAttributo + " non è un tipo primario");
		}
	}

	@Override
	public boolean isImmuneAIncantesimo(ClasseIncantesimo classeIncantesimo) {
		return classeIncantesimo == ClasseIncantesimo.FUOCO;
	}

	@Override
	public boolean isImmuneAEffetto(TipoEffettoDiStato effettoStato) {
		return effettoStato == TipoEffettoDiStato.BRUCIATO;
	}
}
