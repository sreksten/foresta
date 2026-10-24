package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.testi.Articoli;
import com.threeamigos.foresta.attributi.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.*;

import java.util.EnumSet;
import java.util.Set;

/**
 * La chimera drago: bestia ibrida e draconica. Nel suo repertorio ci sono il Fuoco e il Veleno, ed è immune al Fuoco e
 * allo stato bruciato.
 */
public class ChimeraDrago extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Articoli.UNA; }
	public String getADS() { return Articoli.LA; }
	public String getAIP() { return Articoli.ALCUNE; }
	public String getADP() { return Articoli.LE; }
	public String getDeS() { return Articoli.DELLA; }
	public String getDeP() { return Articoli.DELLE; }
	public String getDa() { return Articoli.DA_UNA; }

	public String getNomeSingolare() { return "Chimera-Drago"; }
	public String getNomePlurale() { return "Chimere-Drago"; }
	public String getPronome() { return Articoli.ESSA; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.FEMMINA; }

	public ChimeraDrago(int livello) {
		super(TipoPersonaggio.CHIMERA_DRAGO, livello);
	}

	@Override
	protected void impostaValoriDiPartenza() {

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
	public Set<TipoIncantesimo> getRepertorioIncantesimi() {
		return EnumSet.of(TipoIncantesimo.FUOCO, TipoIncantesimo.VELENO);
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
}
