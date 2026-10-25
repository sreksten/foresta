package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.tipi.CostantiPersonaggi;

import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.testi.Articoli;
import com.threeamigos.foresta.tipi.Costanti;
import com.threeamigos.foresta.tipi.TipoPersonaggio;


/**
 * La druida: il Druido al femminile, con lo stesso ruolo e le stesse capacità (vedi {@link Druido}).
 */
public class Druida extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Articoli.UNA; }
	public String getADS() { return Articoli.LA; }
	public String getAIP() { return Articoli.ALCUNE; }
	public String getADP() { return Articoli.LE; }
	public String getDeS() { return Articoli.DELLA; }
	public String getDeP() { return Articoli.DELLE; }
	public String getDa() { return Articoli.DA_UNA; }

	public String getNomeSingolare() { return "Druida"; }
	public String getNomePlurale() { return "Druide"; }
	public String getPronome() { return Articoli.ELLA; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.FEMMINA; }

	public Druida(int livello) {
		super(TipoPersonaggio.DRUIDA, livello);
	}

	public Druida(String nome, int livello) {
		super(nome, TipoPersonaggio.DRUIDA, livello);
	}
	
	@Override
	protected void impostaValoriDiPartenza() {
		setCorrompibile(true);
		setAmichevole(true);

		setQuantitaMassima(CostantiPersonaggi.DRUIDA_MAX_NUMERO);
	}

	@Override
	public int getPuntiEsperienza() {
		return CostantiPersonaggi.DRUIDA_PUNTI_ESPERIENZA;
	}

	@Override
	public double getSaluteBase() {
		return CostantiPersonaggi.DRUIDA_SALUTE_BASE;
	}

	@Override
	public double getMagiaBase() {
		return CostantiPersonaggi.DRUIDA_MAGIA_BASE;
	}

	@Override
	public double getLivellamentoMagia() {
		return CostantiPersonaggi.DRUIDA_LIVELLAMENTO_MAGIA;
	}

	@Override
	public double getMoltiplicatoreCarico() {
		return CostantiPersonaggi.DRUIDA_MOLTIPLICATORE_CARICO;
	}


	@Override
	public double getMoltiplicatoreCritico() {
		return CostantiPersonaggi.DRUIDA_MOLTIPLICATORE_CRITICO;
	}


	@Override
	public double getMoltiplicatorePrecisione() {
		return CostantiPersonaggi.DRUIDA_MOLTIPLICATORE_PRECISIONE;
	}


	@Override
	public double getMoltiplicatoreVelocita() {
		return CostantiPersonaggi.DRUIDA_MOLTIPLICATORE_VELOCITA;
	}


	@Override
	public double getMoltiplicatoreFurtivita() {
		return CostantiPersonaggi.DRUIDA_MOLTIPLICATORE_FURTIVITA;
	}


	@Override
	public double getMoltiplicatoreParata() {
		return CostantiPersonaggi.DRUIDA_MOLTIPLICATORE_PARATA;
	}


	@Override
	public double getMoltiplicatoreResistenzaMagica() {
		return CostantiPersonaggi.DRUIDA_MOLTIPLICATORE_RESISTENZA_MAGICA;
	}


	@Override
	public double getMoltiplicatorePercezione() {
		return CostantiPersonaggi.DRUIDA_MOLTIPLICATORE_PERCEZIONE;
	}


	@Override
	public double getMoltiplicatoreSoggezione() {
		return CostantiPersonaggi.DRUIDA_MOLTIPLICATORE_SOGGEZIONE;
	}


	@Override
	public double getMoltiplicatoreFuria() {
		return CostantiPersonaggi.DRUIDA_MOLTIPLICATORE_FURIA;
	}


	@Override
	public double getMoltiplicatoreCoraggio() {
		return CostantiPersonaggi.DRUIDA_MOLTIPLICATORE_CORAGGIO;
	}


	@Override
	public double getMoltiplicatoreValore() {
		return CostantiPersonaggi.DRUIDA_MOLTIPLICATORE_VALORE;
	}


	@Override
	public double getMoltiplicatoreContrattazione() {
		return CostantiPersonaggi.DRUIDA_MOLTIPLICATORE_CONTRATTAZIONE;
	}


	@Override
	public double getMoltiplicatoreNumeroBersagli() {
		return CostantiPersonaggi.DRUIDA_MOLTIPLICATORE_NUMERO_BERSAGLI;
	}


	@Override
	public double getMoltiplicatoreStanchezza() {
		return CostantiPersonaggi.DRUIDA_MOLTIPLICATORE_STANCHEZZA;
	}


	@Override
	public double getMoltiplicatoreDanniMagici() {
		return CostantiPersonaggi.DRUIDA_MOLTIPLICATORE_DANNI_MAGICI;
	}


	@Override
	public double getMoltiplicatoreDanniFisici() {
		return CostantiPersonaggi.DRUIDA_MOLTIPLICATORE_DANNI_FISICI;
	}


	@Override
	public double getMoltiplicatoreRecuperoMagico() {
		return CostantiPersonaggi.DRUIDA_MOLTIPLICATORE_RECUPERO_MAGICO;
	}


	@Override
	public double getMoltiplicatoreRecuperoFisico() {
		return CostantiPersonaggi.DRUIDA_MOLTIPLICATORE_RECUPERO_FISICO;
	}


	@Override
	public double getMaxStatistica(TipoAttributo tipoAttributo) {
		switch (tipoAttributo) {
			case FORZA:
				return CostantiPersonaggi.DRUIDA_MAX_FORZA;
			case DESTREZZA:
				return CostantiPersonaggi.DRUIDA_MAX_DESTREZZA;
			case COSTITUZIONE:
				return CostantiPersonaggi.DRUIDA_MAX_COSTITUZIONE;
			case INTELLIGENZA:
				return CostantiPersonaggi.DRUIDA_MAX_INTELLIGENZA;
			case SAGGEZZA:
				return CostantiPersonaggi.DRUIDA_MAX_SAGGEZZA;
			case CARISMA:
				return CostantiPersonaggi.DRUIDA_MAX_CARISMA;
			case FORTUNA:
				return CostantiPersonaggi.DRUIDA_MAX_FORTUNA;
			default:
				throw new IllegalArgumentException(tipoAttributo + " non è un tipo primario");
		}
	}
}
