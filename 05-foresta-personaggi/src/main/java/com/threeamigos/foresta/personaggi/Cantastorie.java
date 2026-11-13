package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.tipi.CostantiPersonaggi;

import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * La Cantastorie: il Bardo al femminile, con lo stesso ruolo e le stesse capacità (vedi {@link Bardo}).
 */
public class Cantastorie extends PersonaggioBase implements Personaggio {

	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.FEMMINA; }

	public Cantastorie(int livello) {
		super(TipoPersonaggio.CANTASTORIE, livello);
	}

	public Cantastorie(String nome, int livello) {
		super(nome, TipoPersonaggio.CANTASTORIE, livello);
	}
	
	@Override
	protected void impostaValoriDiPartenza() {
		setCorrompibile(true);
		setAmichevole(true);

		setQuantitaMassima(CostantiPersonaggi.CANTASTORIE_MAX_NUMERO);
	}

	@Override
	public int getPuntiEsperienza() {
		return CostantiPersonaggi.CANTASTORIE_PUNTI_ESPERIENZA;
	}

	@Override
	public double getSaluteBase() {
		return CostantiPersonaggi.CANTASTORIE_SALUTE_BASE;
	}

	@Override
	public double getMagiaBase() {
		return CostantiPersonaggi.CANTASTORIE_MAGIA_BASE;
	}

	@Override
	public double getLivellamentoMagia() {
		return CostantiPersonaggi.CANTASTORIE_LIVELLAMENTO_MAGIA;
	}

	@Override
	public double getMoltiplicatoreCarico() {
		return CostantiPersonaggi.CANTASTORIE_MOLTIPLICATORE_CARICO;
	}

	@Override
	public double getMoltiplicatoreCritico() {
		return CostantiPersonaggi.CANTASTORIE_MOLTIPLICATORE_CRITICO;
	}

	@Override
	public double getMoltiplicatorePrecisione() {
		return CostantiPersonaggi.CANTASTORIE_MOLTIPLICATORE_PRECISIONE;
	}

	@Override
	public double getMoltiplicatoreVelocita() {
		return CostantiPersonaggi.CANTASTORIE_MOLTIPLICATORE_VELOCITA;
	}

	@Override
	public double getMoltiplicatoreFurtivita() {
		return CostantiPersonaggi.CANTASTORIE_MOLTIPLICATORE_FURTIVITA;
	}

	@Override
	public double getMoltiplicatoreParata() {
		return CostantiPersonaggi.CANTASTORIE_MOLTIPLICATORE_PARATA;
	}

	@Override
	public double getMoltiplicatoreResistenzaMagica() {
		return CostantiPersonaggi.CANTASTORIE_MOLTIPLICATORE_RESISTENZA_MAGICA;
	}

	@Override
	public double getMoltiplicatorePercezione() {
		return CostantiPersonaggi.CANTASTORIE_MOLTIPLICATORE_PERCEZIONE;
	}

	@Override
	public double getMoltiplicatoreSoggezione() {
		return CostantiPersonaggi.CANTASTORIE_MOLTIPLICATORE_SOGGEZIONE;
	}

	@Override
	public double getMoltiplicatoreFuria() {
		return CostantiPersonaggi.CANTASTORIE_MOLTIPLICATORE_FURIA;
	}

	@Override
	public double getMoltiplicatoreCoraggio() {
		return CostantiPersonaggi.CANTASTORIE_MOLTIPLICATORE_CORAGGIO;
	}

	@Override
	public double getMoltiplicatoreValore() {
		return CostantiPersonaggi.CANTASTORIE_MOLTIPLICATORE_VALORE;
	}

	@Override
	public double getMoltiplicatoreContrattazione() {
		return CostantiPersonaggi.CANTASTORIE_MOLTIPLICATORE_CONTRATTAZIONE;
	}

	@Override
	public double getMoltiplicatoreNumeroBersagli() {
		return CostantiPersonaggi.CANTASTORIE_MOLTIPLICATORE_NUMERO_BERSAGLI;
	}

	@Override
	public double getMoltiplicatoreStanchezza() {
		return CostantiPersonaggi.CANTASTORIE_MOLTIPLICATORE_STANCHEZZA;
	}

	@Override
	public double getMoltiplicatoreDanniMagici() {
		return CostantiPersonaggi.CANTASTORIE_MOLTIPLICATORE_DANNI_MAGICI;
	}

	@Override
	public double getMoltiplicatoreDanniFisici() {
		return CostantiPersonaggi.CANTASTORIE_MOLTIPLICATORE_DANNI_FISICI;
	}

	@Override
	public double getMoltiplicatoreRecuperoMagico() {
		return CostantiPersonaggi.CANTASTORIE_MOLTIPLICATORE_RECUPERO_MAGICO;
	}

	@Override
	public double getMoltiplicatoreRecuperoFisico() {
		return CostantiPersonaggi.CANTASTORIE_MOLTIPLICATORE_RECUPERO_FISICO;
	}

	@Override
	public double getMaxIntelligenza() {
		return CostantiPersonaggi.CANTASTORIE_MAX_INTELLIGENZA;
	}
}
