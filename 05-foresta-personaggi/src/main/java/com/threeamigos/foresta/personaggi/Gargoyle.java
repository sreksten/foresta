package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.tipi.CostantiPersonaggi;

import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * Il gargoyle: costrutto volante. È immune a tutti gli incantesimi e agli stati sanguinamento, infezione, avvelenamento e
 * immobilizzazione.
 */
public class Gargoyle extends PersonaggioBase implements Personaggio {

	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.MASCHIO; }

	public Gargoyle(int livello) {
		super(TipoPersonaggio.GARGOYLE, livello);
	}

	@Override
	protected void impostaValoriDiPartenza() {

		setQuantitaMassima(CostantiPersonaggi.GARGOYLE_MAX_NUMERO);
	}

	@Override
	public int getPuntiEsperienza() {
		return CostantiPersonaggi.GARGOYLE_PUNTI_ESPERIENZA;
	}

	@Override
	public double getSaluteBase() {
		return CostantiPersonaggi.GARGOYLE_SALUTE_BASE;
	}

	@Override
	public double getMagiaBase() {
		return CostantiPersonaggi.GARGOYLE_MAGIA_BASE;
	}

	@Override
	public double getLivellamentoMagia() {
		return CostantiPersonaggi.GARGOYLE_LIVELLAMENTO_MAGIA;
	}

	@Override
	public double getMoltiplicatoreCarico() {
		return CostantiPersonaggi.GARGOYLE_MOLTIPLICATORE_CARICO;
	}

	@Override
	public double getMoltiplicatoreCritico() {
		return CostantiPersonaggi.GARGOYLE_MOLTIPLICATORE_CRITICO;
	}

	@Override
	public double getMoltiplicatorePrecisione() {
		return CostantiPersonaggi.GARGOYLE_MOLTIPLICATORE_PRECISIONE;
	}

	@Override
	public double getMoltiplicatoreVelocita() {
		return CostantiPersonaggi.GARGOYLE_MOLTIPLICATORE_VELOCITA;
	}

	@Override
	public double getMoltiplicatoreFurtivita() {
		return CostantiPersonaggi.GARGOYLE_MOLTIPLICATORE_FURTIVITA;
	}

	@Override
	public double getMoltiplicatoreParata() {
		return CostantiPersonaggi.GARGOYLE_MOLTIPLICATORE_PARATA;
	}

	@Override
	public double getMoltiplicatoreResistenzaMagica() {
		return CostantiPersonaggi.GARGOYLE_MOLTIPLICATORE_RESISTENZA_MAGICA;
	}

	@Override
	public double getMoltiplicatorePercezione() {
		return CostantiPersonaggi.GARGOYLE_MOLTIPLICATORE_PERCEZIONE;
	}

	@Override
	public double getMoltiplicatoreSoggezione() {
		return CostantiPersonaggi.GARGOYLE_MOLTIPLICATORE_SOGGEZIONE;
	}

	@Override
	public double getMoltiplicatoreFuria() {
		return CostantiPersonaggi.GARGOYLE_MOLTIPLICATORE_FURIA;
	}

	@Override
	public double getMoltiplicatoreCoraggio() {
		return CostantiPersonaggi.GARGOYLE_MOLTIPLICATORE_CORAGGIO;
	}

	@Override
	public double getMoltiplicatoreValore() {
		return CostantiPersonaggi.GARGOYLE_MOLTIPLICATORE_VALORE;
	}

	@Override
	public double getMoltiplicatoreNumeroBersagli() {
		return CostantiPersonaggi.GARGOYLE_MOLTIPLICATORE_NUMERO_BERSAGLI;
	}

	@Override
	public double getMoltiplicatoreStanchezza() {
		return CostantiPersonaggi.GARGOYLE_MOLTIPLICATORE_STANCHEZZA;
	}

	@Override
	public double getMoltiplicatoreDanniMagici() {
		return CostantiPersonaggi.GARGOYLE_MOLTIPLICATORE_DANNI_MAGICI;
	}

	@Override
	public double getMoltiplicatoreDanniFisici() {
		return CostantiPersonaggi.GARGOYLE_MOLTIPLICATORE_DANNI_FISICI;
	}

	@Override
	public double getMoltiplicatoreRecuperoMagico() {
		return CostantiPersonaggi.GARGOYLE_MOLTIPLICATORE_RECUPERO_MAGICO;
	}

	@Override
	public double getMoltiplicatoreRecuperoFisico() {
		return CostantiPersonaggi.GARGOYLE_MOLTIPLICATORE_RECUPERO_FISICO;
	}

	@Override
	public double getMaxIntelligenza() {
		return CostantiPersonaggi.GARGOYLE_MAX_INTELLIGENZA;
	}
}
