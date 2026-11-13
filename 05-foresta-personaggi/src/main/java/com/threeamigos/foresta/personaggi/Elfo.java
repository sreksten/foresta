package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.tipi.CostantiPersonaggi;

import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * L'elfo giocante, al maschile: umanoide, corrompibile e amichevole. Come il Mago ha il dardo arcano come incantesimo
 * innato e, come il Ladro, può impugnare una seconda arma. Il femminile è l'{@link Elfa}.
 */
public class Elfo extends PersonaggioBase implements Personaggio {

	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.MASCHIO; }

	public Elfo(int livello) {
		super(TipoPersonaggio.ELFO, livello);
	}

	public Elfo(String nome, int livello) {
		super(nome, TipoPersonaggio.ELFO, livello);
	}

	@Override
	protected void impostaValoriDiPartenza() {
		setCorrompibile(true);
		setAmichevole(true);

		setQuantitaMassima(CostantiPersonaggi.ELFO_MAX_NUMERO);
	}

	@Override
	public int getPuntiEsperienza() {
		return CostantiPersonaggi.ELFO_PUNTI_ESPERIENZA;
	}

	@Override
	public double getSaluteBase() {
		return CostantiPersonaggi.ELFO_SALUTE_BASE;
	}

	@Override
	public double getMagiaBase() {
		return CostantiPersonaggi.ELFO_MAGIA_BASE;
	}

	@Override
	public double getLivellamentoMagia() {
		return CostantiPersonaggi.ELFO_LIVELLAMENTO_MAGIA;
	}

	@Override
	public double getMoltiplicatoreCarico() {
		return CostantiPersonaggi.ELFO_MOLTIPLICATORE_CARICO;
	}

	@Override
	public double getMoltiplicatoreCritico() {
		return CostantiPersonaggi.ELFO_MOLTIPLICATORE_CRITICO;
	}

	@Override
	public double getMoltiplicatorePrecisione() {
		return CostantiPersonaggi.ELFO_MOLTIPLICATORE_PRECISIONE;
	}

	@Override
	public double getMoltiplicatoreVelocita() {
		return CostantiPersonaggi.ELFO_MOLTIPLICATORE_VELOCITA;
	}

	@Override
	public double getMoltiplicatoreFurtivita() {
		return CostantiPersonaggi.ELFO_MOLTIPLICATORE_FURTIVITA;
	}

	@Override
	public double getMoltiplicatoreParata() {
		return CostantiPersonaggi.ELFO_MOLTIPLICATORE_PARATA;
	}

	@Override
	public double getMoltiplicatoreResistenzaMagica() {
		return CostantiPersonaggi.ELFO_MOLTIPLICATORE_RESISTENZA_MAGICA;
	}

	@Override
	public double getMoltiplicatorePercezione() {
		return CostantiPersonaggi.ELFO_MOLTIPLICATORE_PERCEZIONE;
	}

	@Override
	public double getMoltiplicatoreSoggezione() {
		return CostantiPersonaggi.ELFO_MOLTIPLICATORE_SOGGEZIONE;
	}

	@Override
	public double getMoltiplicatoreFuria() {
		return CostantiPersonaggi.ELFO_MOLTIPLICATORE_FURIA;
	}

	@Override
	public double getMoltiplicatoreCoraggio() {
		return CostantiPersonaggi.ELFO_MOLTIPLICATORE_CORAGGIO;
	}

	@Override
	public double getMoltiplicatoreValore() {
		return CostantiPersonaggi.ELFO_MOLTIPLICATORE_VALORE;
	}

	@Override
	public double getMoltiplicatoreContrattazione() {
		return CostantiPersonaggi.ELFO_MOLTIPLICATORE_CONTRATTAZIONE;
	}

	@Override
	public double getMoltiplicatoreNumeroBersagli() {
		return CostantiPersonaggi.ELFO_MOLTIPLICATORE_NUMERO_BERSAGLI;
	}

	@Override
	public double getMoltiplicatoreStanchezza() {
		return CostantiPersonaggi.ELFO_MOLTIPLICATORE_STANCHEZZA;
	}

	@Override
	public double getMoltiplicatoreDanniMagici() {
		return CostantiPersonaggi.ELFO_MOLTIPLICATORE_DANNI_MAGICI;
	}

	@Override
	public double getMoltiplicatoreDanniFisici() {
		return CostantiPersonaggi.ELFO_MOLTIPLICATORE_DANNI_FISICI;
	}

	@Override
	public double getMoltiplicatoreRecuperoMagico() {
		return CostantiPersonaggi.ELFO_MOLTIPLICATORE_RECUPERO_MAGICO;
	}

	@Override
	public double getMoltiplicatoreRecuperoFisico() {
		return CostantiPersonaggi.ELFO_MOLTIPLICATORE_RECUPERO_FISICO;
	}

	@Override
	public double getMaxIntelligenza() {
		return CostantiPersonaggi.ELFO_MAX_INTELLIGENZA;
	}
}
