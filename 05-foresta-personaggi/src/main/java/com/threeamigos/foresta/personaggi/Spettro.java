package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.tipi.CostantiPersonaggi;

import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * Lo spettro: non morto volante, con le capacità comuni descritte in {@link NonMortoVolante}.
 */
public class Spettro extends NonMortoVolante {

	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.MASCHIO; }

	public Spettro(int livello) {
		super(TipoPersonaggio.SPETTRO, livello);
	}

	@Override
	protected void impostaValoriDiPartenza() {

		setQuantitaMassima(CostantiPersonaggi.SPETTRO_MAX_NUMERO);
	}

	@Override
	public int getPuntiEsperienza() {
		return CostantiPersonaggi.SPETTRO_PUNTI_ESPERIENZA;
	}

	@Override
	public double getSaluteBase() {
		return CostantiPersonaggi.SPETTRO_SALUTE_BASE;
	}

	@Override
	public double getMagiaBase() {
		return CostantiPersonaggi.SPETTRO_MAGIA_BASE;
	}

	@Override
	public double getLivellamentoMagia() {
		return CostantiPersonaggi.SPETTRO_LIVELLAMENTO_MAGIA;
	}

	@Override
	public double getMoltiplicatoreCarico() {
		return CostantiPersonaggi.SPETTRO_MOLTIPLICATORE_CARICO;
	}

	@Override
	public double getMoltiplicatoreCritico() {
		return CostantiPersonaggi.SPETTRO_MOLTIPLICATORE_CRITICO;
	}

	@Override
	public double getMoltiplicatorePrecisione() {
		return CostantiPersonaggi.SPETTRO_MOLTIPLICATORE_PRECISIONE;
	}

	@Override
	public double getMoltiplicatoreVelocita() {
		return CostantiPersonaggi.SPETTRO_MOLTIPLICATORE_VELOCITA;
	}

	@Override
	public double getMoltiplicatoreFurtivita() {
		return CostantiPersonaggi.SPETTRO_MOLTIPLICATORE_FURTIVITA;
	}

	@Override
	public double getMoltiplicatoreParata() {
		return CostantiPersonaggi.SPETTRO_MOLTIPLICATORE_PARATA;
	}

	@Override
	public double getMoltiplicatoreResistenzaMagica() {
		return CostantiPersonaggi.SPETTRO_MOLTIPLICATORE_RESISTENZA_MAGICA;
	}

	@Override
	public double getMoltiplicatorePercezione() {
		return CostantiPersonaggi.SPETTRO_MOLTIPLICATORE_PERCEZIONE;
	}

	@Override
	public double getMoltiplicatoreSoggezione() {
		return CostantiPersonaggi.SPETTRO_MOLTIPLICATORE_SOGGEZIONE;
	}

	@Override
	public double getMoltiplicatoreFuria() {
		return CostantiPersonaggi.SPETTRO_MOLTIPLICATORE_FURIA;
	}

	@Override
	public double getMoltiplicatoreCoraggio() {
		return CostantiPersonaggi.SPETTRO_MOLTIPLICATORE_CORAGGIO;
	}

	@Override
	public double getMoltiplicatoreValore() {
		return CostantiPersonaggi.SPETTRO_MOLTIPLICATORE_VALORE;
	}

	@Override
	public double getMoltiplicatoreNumeroBersagli() {
		return CostantiPersonaggi.SPETTRO_MOLTIPLICATORE_NUMERO_BERSAGLI;
	}

	@Override
	public double getMoltiplicatoreStanchezza() {
		return CostantiPersonaggi.SPETTRO_MOLTIPLICATORE_STANCHEZZA;
	}

	@Override
	public double getMoltiplicatoreDanniMagici() {
		return CostantiPersonaggi.SPETTRO_MOLTIPLICATORE_DANNI_MAGICI;
	}

	@Override
	public double getMoltiplicatoreDanniFisici() {
		return CostantiPersonaggi.SPETTRO_MOLTIPLICATORE_DANNI_FISICI;
	}

	@Override
	public double getMoltiplicatoreRecuperoMagico() {
		return CostantiPersonaggi.SPETTRO_MOLTIPLICATORE_RECUPERO_MAGICO;
	}

	@Override
	public double getMoltiplicatoreRecuperoFisico() {
		return CostantiPersonaggi.SPETTRO_MOLTIPLICATORE_RECUPERO_FISICO;
	}

	@Override
	public double getMaxIntelligenza() {
		return CostantiPersonaggi.SPETTRO_MAX_INTELLIGENZA;
	}
}
