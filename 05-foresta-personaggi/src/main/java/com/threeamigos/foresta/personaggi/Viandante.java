package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.tipi.CostantiPersonaggi;

import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * Chi viaggia con il gruppo come ospite perché una missione lo deve scortare (vedi MissioneAPassi.prendiInScorta):
 * un mercante, un pellegrino... Non è un mostro né una classe giocante, non si incontra nelle locazioni e non si
 * recluta. È un personaggio a sé, con le sue caratteristiche (CostantiPersonaggi.VIANDANTE_*) e per ora la stessa immagine del
 * bardo; non è un Bardo, e nessuna capacità del Bardo gli arriva per eredità.
 */
public class Viandante extends PersonaggioBase implements Personaggio {

	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.MASCHIO; }

	public Viandante(int livello) {
		this(null, livello);
	}

	public Viandante(String nome, int livello) {
		super(nome, TipoPersonaggio.VIANDANTE, livello);
	}

	@Override
	protected void impostaValoriDiPartenza() {
		setCorrompibile(true);
		setAmichevole(true);

		setQuantitaMassima(CostantiPersonaggi.VIANDANTE_MAX_NUMERO);
	}

	@Override
	public int getPuntiEsperienza() {
		return CostantiPersonaggi.VIANDANTE_PUNTI_ESPERIENZA;
	}

	@Override
	public double getSaluteBase() {
		return CostantiPersonaggi.VIANDANTE_SALUTE_BASE;
	}

	@Override
	public double getMagiaBase() {
		return CostantiPersonaggi.VIANDANTE_MAGIA_BASE;
	}

	@Override
	public double getLivellamentoMagia() {
		return CostantiPersonaggi.VIANDANTE_LIVELLAMENTO_MAGIA;
	}

	@Override
	public double getMoltiplicatoreCarico() {
		return CostantiPersonaggi.VIANDANTE_MOLTIPLICATORE_CARICO;
	}

	@Override
	public double getMoltiplicatoreCritico() {
		return CostantiPersonaggi.VIANDANTE_MOLTIPLICATORE_CRITICO;
	}

	@Override
	public double getMoltiplicatorePrecisione() {
		return CostantiPersonaggi.VIANDANTE_MOLTIPLICATORE_PRECISIONE;
	}

	@Override
	public double getMoltiplicatoreVelocita() {
		return CostantiPersonaggi.VIANDANTE_MOLTIPLICATORE_VELOCITA;
	}

	@Override
	public double getMoltiplicatoreFurtivita() {
		return CostantiPersonaggi.VIANDANTE_MOLTIPLICATORE_FURTIVITA;
	}

	@Override
	public double getMoltiplicatoreParata() {
		return CostantiPersonaggi.VIANDANTE_MOLTIPLICATORE_PARATA;
	}

	@Override
	public double getMoltiplicatoreResistenzaMagica() {
		return CostantiPersonaggi.VIANDANTE_MOLTIPLICATORE_RESISTENZA_MAGICA;
	}

	@Override
	public double getMoltiplicatorePercezione() {
		return CostantiPersonaggi.VIANDANTE_MOLTIPLICATORE_PERCEZIONE;
	}

	@Override
	public double getMoltiplicatoreSoggezione() {
		return CostantiPersonaggi.VIANDANTE_MOLTIPLICATORE_SOGGEZIONE;
	}

	@Override
	public double getMoltiplicatoreFuria() {
		return CostantiPersonaggi.VIANDANTE_MOLTIPLICATORE_FURIA;
	}

	@Override
	public double getMoltiplicatoreCoraggio() {
		return CostantiPersonaggi.VIANDANTE_MOLTIPLICATORE_CORAGGIO;
	}

	@Override
	public double getMoltiplicatoreValore() {
		return CostantiPersonaggi.VIANDANTE_MOLTIPLICATORE_VALORE;
	}

	@Override
	public double getMoltiplicatoreContrattazione() {
		return CostantiPersonaggi.VIANDANTE_MOLTIPLICATORE_CONTRATTAZIONE;
	}

	@Override
	public double getMoltiplicatoreNumeroBersagli() {
		return CostantiPersonaggi.VIANDANTE_MOLTIPLICATORE_NUMERO_BERSAGLI;
	}

	@Override
	public double getMoltiplicatoreStanchezza() {
		return CostantiPersonaggi.VIANDANTE_MOLTIPLICATORE_STANCHEZZA;
	}

	@Override
	public double getMoltiplicatoreDanniMagici() {
		return CostantiPersonaggi.VIANDANTE_MOLTIPLICATORE_DANNI_MAGICI;
	}

	@Override
	public double getMoltiplicatoreDanniFisici() {
		return CostantiPersonaggi.VIANDANTE_MOLTIPLICATORE_DANNI_FISICI;
	}

	@Override
	public double getMoltiplicatoreRecuperoMagico() {
		return CostantiPersonaggi.VIANDANTE_MOLTIPLICATORE_RECUPERO_MAGICO;
	}

	@Override
	public double getMoltiplicatoreRecuperoFisico() {
		return CostantiPersonaggi.VIANDANTE_MOLTIPLICATORE_RECUPERO_FISICO;
	}

	@Override
	public double getMaxIntelligenza() {
		return CostantiPersonaggi.VIANDANTE_MAX_INTELLIGENZA;
	}
}
