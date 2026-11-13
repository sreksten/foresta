package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.tipi.CostantiPersonaggi;

import com.threeamigos.foresta.tipi.TipoOfferta;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * Il minotauro: creatura umanoide e ibrida, corrompibile ma non amichevole. Con la corruzione offre un aiuto a pagamento o la
 * mappa della Foresta. Per la sua versione gigante vedi {@link MinotauroGigante}.
 */
public class Minotauro extends PersonaggioBase implements Personaggio {

	public TipoPersonaggio getClasse() { return TipoPersonaggio.MINOTAURO; }

	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.MASCHIO; }

	public Minotauro(int livello) {
		super(TipoPersonaggio.MINOTAURO, livello);
	}

	@Override
	protected void impostaValoriDiPartenza() {
		setCorrompibile(true);

		setQuantitaMassima(CostantiPersonaggi.MINOTAURO_MAX_NUMERO);
	}

	@Override
	public int getPuntiEsperienza() {
		return CostantiPersonaggi.MINOTAURO_PUNTI_ESPERIENZA;
	}

	@Override
	public TipoOfferta[] getOfferteCorruzione() {
		return new TipoOfferta[]{
				TipoOfferta.AIUTO_MERCENARIO,
				TipoOfferta.MAPPA_FORESTA,
		};
	}

	@Override
	public double getSaluteBase() {
		return CostantiPersonaggi.MINOTAURO_SALUTE_BASE;
	}

	@Override
	public double getMagiaBase() {
		return CostantiPersonaggi.MINOTAURO_MAGIA_BASE;
	}

	@Override
	public double getLivellamentoMagia() {
		return CostantiPersonaggi.MINOTAURO_LIVELLAMENTO_MAGIA;
	}

	@Override
	public double getMoltiplicatoreCarico() {
		return CostantiPersonaggi.MINOTAURO_MOLTIPLICATORE_CARICO;
	}

	@Override
	public double getMoltiplicatoreCritico() {
		return CostantiPersonaggi.MINOTAURO_MOLTIPLICATORE_CRITICO;
	}

	@Override
	public double getMoltiplicatorePrecisione() {
		return CostantiPersonaggi.MINOTAURO_MOLTIPLICATORE_PRECISIONE;
	}

	@Override
	public double getMoltiplicatoreVelocita() {
		return CostantiPersonaggi.MINOTAURO_MOLTIPLICATORE_VELOCITA;
	}

	@Override
	public double getMoltiplicatoreFurtivita() {
		return CostantiPersonaggi.MINOTAURO_MOLTIPLICATORE_FURTIVITA;
	}

	@Override
	public double getMoltiplicatoreParata() {
		return CostantiPersonaggi.MINOTAURO_MOLTIPLICATORE_PARATA;
	}

	@Override
	public double getMoltiplicatoreResistenzaMagica() {
		return CostantiPersonaggi.MINOTAURO_MOLTIPLICATORE_RESISTENZA_MAGICA;
	}

	@Override
	public double getMoltiplicatorePercezione() {
		return CostantiPersonaggi.MINOTAURO_MOLTIPLICATORE_PERCEZIONE;
	}

	@Override
	public double getMoltiplicatoreSoggezione() {
		return CostantiPersonaggi.MINOTAURO_MOLTIPLICATORE_SOGGEZIONE;
	}

	@Override
	public double getMoltiplicatoreFuria() {
		return CostantiPersonaggi.MINOTAURO_MOLTIPLICATORE_FURIA;
	}

	@Override
	public double getMoltiplicatoreCoraggio() {
		return CostantiPersonaggi.MINOTAURO_MOLTIPLICATORE_CORAGGIO;
	}

	@Override
	public double getMoltiplicatoreValore() {
		return CostantiPersonaggi.MINOTAURO_MOLTIPLICATORE_VALORE;
	}

	@Override
	public double getMoltiplicatoreContrattazione() {
		return CostantiPersonaggi.MINOTAURO_MOLTIPLICATORE_CONTRATTAZIONE;
	}

	@Override
	public double getMoltiplicatoreNumeroBersagli() {
		return CostantiPersonaggi.MINOTAURO_MOLTIPLICATORE_NUMERO_BERSAGLI;
	}

	@Override
	public double getMoltiplicatoreStanchezza() {
		return CostantiPersonaggi.MINOTAURO_MOLTIPLICATORE_STANCHEZZA;
	}

	@Override
	public double getMoltiplicatoreDanniMagici() {
		return CostantiPersonaggi.MINOTAURO_MOLTIPLICATORE_DANNI_MAGICI;
	}

	@Override
	public double getMoltiplicatoreDanniFisici() {
		return CostantiPersonaggi.MINOTAURO_MOLTIPLICATORE_DANNI_FISICI;
	}

	@Override
	public double getMoltiplicatoreRecuperoMagico() {
		return CostantiPersonaggi.MINOTAURO_MOLTIPLICATORE_RECUPERO_MAGICO;
	}

	@Override
	public double getMoltiplicatoreRecuperoFisico() {
		return CostantiPersonaggi.MINOTAURO_MOLTIPLICATORE_RECUPERO_FISICO;
	}

	@Override
	public double getMaxIntelligenza() {
		return CostantiPersonaggi.MINOTAURO_MAX_INTELLIGENZA;
	}
}
