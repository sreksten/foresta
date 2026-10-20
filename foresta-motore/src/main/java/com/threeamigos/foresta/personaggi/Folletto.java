package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.strumenti.Misc;
import com.threeamigos.foresta.tipi.*;

import java.util.EnumSet;
import java.util.Set;

/**
 * Il folletto: creatura umanoide e volante, corrompibile e amichevole. Nel suo repertorio ci sono Acqua, Gelo e Aria; con
 * l'amicizia offre incantesimi, informazioni e un pasto.
 */
public class Folletto extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UN; }
	public String getADS() { return Misc.IL; }
	public String getAIP() { return Misc.ALCUNI; }
	public String getADP() { return Misc.I; }
	public String getDeS() { return Misc.DEL; }
	public String getDeP() { return Misc.DEI; }
	public String getDa() { return Misc.DA_UN; }

	public String getNomeSingolare() { return "Folletto"; }
	public String getNomePlurale() { return "Folletti"; }
	public String getPronome() { return Misc.EGLI; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.MASCHIO; }

	public Folletto(int livello) {
		super(TipoPersonaggio.FOLLETTO, livello);
	}

	@Override
	protected void impostaValoriDiPartenza() {
		setCorrompibile(true);
		setAmichevole(true);

		setQuantitaMassima(Costanti.FOLLETTO_MAX_NUMERO);
	}

	@Override
	public int getPuntiEsperienza() {
		return Costanti.FOLLETTO_PUNTI_ESPERIENZA;
	}

	@Override
	public double getSaluteBase() {
		return Costanti.FOLLETTO_SALUTE_BASE;
	}

	@Override
	public double getMagiaBase() {
		return Costanti.FOLLETTO_MAGIA_BASE;
	}

	@Override
	public double getLivellamentoMagia() {
		return Costanti.FOLLETTO_LIVELLAMENTO_MAGIA;
	}

	@Override
	public Set<TipoIncantesimo> getRepertorioIncantesimi() {
		return EnumSet.of(TipoIncantesimo.ACQUA, TipoIncantesimo.GELO, TipoIncantesimo.ARIA);
	}

	@Override
	public double getMoltiplicatoreCarico() {
		return Costanti.FOLLETTO_MOLTIPLICATORE_CARICO;
	}


	@Override
	public double getMoltiplicatoreCritico() {
		return Costanti.FOLLETTO_MOLTIPLICATORE_CRITICO;
	}


	@Override
	public double getMoltiplicatorePrecisione() {
		return Costanti.FOLLETTO_MOLTIPLICATORE_PRECISIONE;
	}


	@Override
	public double getMoltiplicatoreVelocita() {
		return Costanti.FOLLETTO_MOLTIPLICATORE_VELOCITA;
	}


	@Override
	public double getMoltiplicatoreFurtivita() {
		return Costanti.FOLLETTO_MOLTIPLICATORE_FURTIVITA;
	}


	@Override
	public double getMoltiplicatoreParata() {
		return Costanti.FOLLETTO_MOLTIPLICATORE_PARATA;
	}


	@Override
	public double getMoltiplicatoreResistenzaMagica() {
		return Costanti.FOLLETTO_MOLTIPLICATORE_RESISTENZA_MAGICA;
	}


	@Override
	public double getMoltiplicatorePercezione() {
		return Costanti.FOLLETTO_MOLTIPLICATORE_PERCEZIONE;
	}


	@Override
	public double getMoltiplicatoreSoggezione() {
		return Costanti.FOLLETTO_MOLTIPLICATORE_SOGGEZIONE;
	}


	@Override
	public double getMoltiplicatoreFuria() {
		return Costanti.FOLLETTO_MOLTIPLICATORE_FURIA;
	}


	@Override
	public double getMoltiplicatoreCoraggio() {
		return Costanti.FOLLETTO_MOLTIPLICATORE_CORAGGIO;
	}


	@Override
	public double getMoltiplicatoreValore() {
		return Costanti.FOLLETTO_MOLTIPLICATORE_VALORE;
	}


	@Override
	public double getMoltiplicatoreNumeroBersagli() {
		return Costanti.FOLLETTO_MOLTIPLICATORE_NUMERO_BERSAGLI;
	}


	@Override
	public double getMoltiplicatoreStanchezza() {
		return Costanti.FOLLETTO_MOLTIPLICATORE_STANCHEZZA;
	}


	@Override
	public double getMoltiplicatoreDanniMagici() {
		return Costanti.FOLLETTO_MOLTIPLICATORE_DANNI_MAGICI;
	}


	@Override
	public double getMoltiplicatoreDanniFisici() {
		return Costanti.FOLLETTO_MOLTIPLICATORE_DANNI_FISICI;
	}


	@Override
	public double getMoltiplicatoreRecuperoMagico() {
		return Costanti.FOLLETTO_MOLTIPLICATORE_RECUPERO_MAGICO;
	}


	@Override
	public double getMoltiplicatoreRecuperoFisico() {
		return Costanti.FOLLETTO_MOLTIPLICATORE_RECUPERO_FISICO;
	}


	@Override
	public double getMaxStatistica(TipoAttributo tipoAttributo) {
		switch (tipoAttributo) {
			case FORZA:
				return Costanti.FOLLETTO_MAX_FORZA;
			case DESTREZZA:
				return Costanti.FOLLETTO_MAX_DESTREZZA;
			case COSTITUZIONE:
				return Costanti.FOLLETTO_MAX_COSTITUZIONE;
			case INTELLIGENZA:
				return Costanti.FOLLETTO_MAX_INTELLIGENZA;
			case SAGGEZZA:
				return Costanti.FOLLETTO_MAX_SAGGEZZA;
			case CARISMA:
				return Costanti.FOLLETTO_MAX_CARISMA;
			case FORTUNA:
				return Costanti.FOLLETTO_MAX_FORTUNA;
			default:
				throw new IllegalArgumentException(tipoAttributo + " non è un tipo primario");
		}
	}

	@Override
	public TipoOfferta[] getOfferteAmicizia() {
		return new TipoOfferta[] {
				TipoOfferta.INCANTESIMI,
				TipoOfferta.INFORMAZIONI,
				TipoOfferta.PASTO
		};
	}
}
