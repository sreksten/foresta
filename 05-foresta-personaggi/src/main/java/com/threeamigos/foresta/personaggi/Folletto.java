package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.tipi.CostantiPersonaggi;

import com.threeamigos.foresta.tipi.TipoIncantesimo;
import com.threeamigos.foresta.tipi.TipoOfferta;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

import java.util.EnumSet;
import java.util.Set;

/**
 * Il folletto: creatura umanoide e volante, corrompibile e amichevole. Nel suo repertorio ci sono Acqua, Gelo e Aria; con
 * l'amicizia offre incantesimi, informazioni e un pasto.
 */
public class Folletto extends PersonaggioBase implements Personaggio {

	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.MASCHIO; }

	public Folletto(int livello) {
		super(TipoPersonaggio.FOLLETTO, livello);
	}

	@Override
	protected void impostaValoriDiPartenza() {
		setCorrompibile(true);
		setAmichevole(true);

		setQuantitaMassima(CostantiPersonaggi.FOLLETTO_MAX_NUMERO);
	}

	@Override
	public int getPuntiEsperienza() {
		return CostantiPersonaggi.FOLLETTO_PUNTI_ESPERIENZA;
	}

	@Override
	public double getSaluteBase() {
		return CostantiPersonaggi.FOLLETTO_SALUTE_BASE;
	}

	@Override
	public double getMagiaBase() {
		return CostantiPersonaggi.FOLLETTO_MAGIA_BASE;
	}

	@Override
	public double getLivellamentoMagia() {
		return CostantiPersonaggi.FOLLETTO_LIVELLAMENTO_MAGIA;
	}

	@Override
	public Set<TipoIncantesimo> getRepertorioIncantesimi() {
		return EnumSet.of(TipoIncantesimo.ACQUA, TipoIncantesimo.GELO, TipoIncantesimo.ARIA);
	}

	@Override
	public double getMoltiplicatoreCarico() {
		return CostantiPersonaggi.FOLLETTO_MOLTIPLICATORE_CARICO;
	}

	@Override
	public double getMoltiplicatoreCritico() {
		return CostantiPersonaggi.FOLLETTO_MOLTIPLICATORE_CRITICO;
	}

	@Override
	public double getMoltiplicatorePrecisione() {
		return CostantiPersonaggi.FOLLETTO_MOLTIPLICATORE_PRECISIONE;
	}

	@Override
	public double getMoltiplicatoreVelocita() {
		return CostantiPersonaggi.FOLLETTO_MOLTIPLICATORE_VELOCITA;
	}

	@Override
	public double getMoltiplicatoreFurtivita() {
		return CostantiPersonaggi.FOLLETTO_MOLTIPLICATORE_FURTIVITA;
	}

	@Override
	public double getMoltiplicatoreParata() {
		return CostantiPersonaggi.FOLLETTO_MOLTIPLICATORE_PARATA;
	}

	@Override
	public double getMoltiplicatoreResistenzaMagica() {
		return CostantiPersonaggi.FOLLETTO_MOLTIPLICATORE_RESISTENZA_MAGICA;
	}

	@Override
	public double getMoltiplicatorePercezione() {
		return CostantiPersonaggi.FOLLETTO_MOLTIPLICATORE_PERCEZIONE;
	}

	@Override
	public double getMoltiplicatoreSoggezione() {
		return CostantiPersonaggi.FOLLETTO_MOLTIPLICATORE_SOGGEZIONE;
	}

	@Override
	public double getMoltiplicatoreFuria() {
		return CostantiPersonaggi.FOLLETTO_MOLTIPLICATORE_FURIA;
	}

	@Override
	public double getMoltiplicatoreCoraggio() {
		return CostantiPersonaggi.FOLLETTO_MOLTIPLICATORE_CORAGGIO;
	}

	@Override
	public double getMoltiplicatoreValore() {
		return CostantiPersonaggi.FOLLETTO_MOLTIPLICATORE_VALORE;
	}

	@Override
	public double getMoltiplicatoreNumeroBersagli() {
		return CostantiPersonaggi.FOLLETTO_MOLTIPLICATORE_NUMERO_BERSAGLI;
	}

	@Override
	public double getMoltiplicatoreStanchezza() {
		return CostantiPersonaggi.FOLLETTO_MOLTIPLICATORE_STANCHEZZA;
	}

	@Override
	public double getMoltiplicatoreDanniMagici() {
		return CostantiPersonaggi.FOLLETTO_MOLTIPLICATORE_DANNI_MAGICI;
	}

	@Override
	public double getMoltiplicatoreDanniFisici() {
		return CostantiPersonaggi.FOLLETTO_MOLTIPLICATORE_DANNI_FISICI;
	}

	@Override
	public double getMoltiplicatoreRecuperoMagico() {
		return CostantiPersonaggi.FOLLETTO_MOLTIPLICATORE_RECUPERO_MAGICO;
	}

	@Override
	public double getMoltiplicatoreRecuperoFisico() {
		return CostantiPersonaggi.FOLLETTO_MOLTIPLICATORE_RECUPERO_FISICO;
	}

	@Override
	public double getMaxIntelligenza() {
		return CostantiPersonaggi.FOLLETTO_MAX_INTELLIGENZA;
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
