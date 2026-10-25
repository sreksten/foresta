package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.tipi.CostantiPersonaggi;

import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.testi.Articoli;
import com.threeamigos.foresta.tipi.Costanti;
import com.threeamigos.foresta.tipi.TipoPersonaggio;


/**
 * L'ombrafiamma: un personaggio segreto, immortale, che parte con salute e magia piene. Ha un carisma che fa riuscire sempre
 * l'amicizia (una faccia in più del dado dell'amicizia), e non partecipa alle sfide a carta, forbici e sasso.
 */
public class OmbraFiamma extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Articoli.UN_APOSTROFO; }
	public String getADS() { return Articoli.L_APOSTROFO; }
	public String getAIP() { return Articoli.ALCUNE; }
	public String getADP() { return Articoli.LE; }
	public String getDeS() { return Articoli.DELL_APOSTROFO; }
	public String getDeP() { return Articoli.DELLE; }
	public String getDa() { return Articoli.DALL_APOSTROFO; }

	public String getNomeSingolare() { return "OmbraFiamma"; }
	public String getNomePlurale() { return "OmbreFiamma"; }
	public String getPronome() { return Articoli.ELLA; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.FEMMINA; }

	public OmbraFiamma(int livello) {
		super(TipoPersonaggio.OMBRAFIAMMA, livello);
	}

	public OmbraFiamma(String nome, int livello) {
		super(nome, TipoPersonaggio.OMBRAFIAMMA, livello);
	}

	// L'ombrafiamma è un personaggio segreto con un carisma che fa riuscire sempre l'amicizia: una faccia in più del dado
	// dell'amicizia (vedi CostantiPersonaggi.FACCE_DEL_DADO_DELL_AMICIZIA)
	@Override
	public int getCarisma() {
		return CostantiPersonaggi.FACCE_DEL_DADO_DELL_AMICIZIA + 1;
	}

	@Override
	protected void impostaValoriDiPartenza() {
		setCorrompibile(true);
		setAmichevole(true);

		setQuantitaMassima(CostantiPersonaggi.OMBRAFIAMMA_MAX_NUMERO);
	}

	@Override
	public int getPuntiEsperienza() {
		return CostantiPersonaggi.OMBRAFIAMMA_PUNTI_ESPERIENZA;
	}

	@Override
	public double getSaluteBase() {
		return CostantiPersonaggi.OMBRAFIAMMA_SALUTE_BASE;
	}

	@Override
	public double getMagiaBase() {
		return CostantiPersonaggi.OMBRAFIAMMA_MAGIA_BASE;
	}

	@Override
	public double getLivellamentoMagia() {
		return CostantiPersonaggi.OMBRAFIAMMA_LIVELLAMENTO_MAGIA;
	}

	@Override
	public double getMoltiplicatoreCarico() {
		return CostantiPersonaggi.OMBRAFIAMMA_MOLTIPLICATORE_CARICO;
	}


	@Override
	public double getMoltiplicatoreCritico() {
		return CostantiPersonaggi.OMBRAFIAMMA_MOLTIPLICATORE_CRITICO;
	}


	@Override
	public double getMoltiplicatorePrecisione() {
		return CostantiPersonaggi.OMBRAFIAMMA_MOLTIPLICATORE_PRECISIONE;
	}


	@Override
	public double getMoltiplicatoreVelocita() {
		return CostantiPersonaggi.OMBRAFIAMMA_MOLTIPLICATORE_VELOCITA;
	}


	@Override
	public double getMoltiplicatoreFurtivita() {
		return CostantiPersonaggi.OMBRAFIAMMA_MOLTIPLICATORE_FURTIVITA;
	}


	@Override
	public double getMoltiplicatoreParata() {
		return CostantiPersonaggi.OMBRAFIAMMA_MOLTIPLICATORE_PARATA;
	}


	@Override
	public double getMoltiplicatoreResistenzaMagica() {
		return CostantiPersonaggi.OMBRAFIAMMA_MOLTIPLICATORE_RESISTENZA_MAGICA;
	}


	@Override
	public double getMoltiplicatorePercezione() {
		return CostantiPersonaggi.OMBRAFIAMMA_MOLTIPLICATORE_PERCEZIONE;
	}


	@Override
	public double getMoltiplicatoreSoggezione() {
		return CostantiPersonaggi.OMBRAFIAMMA_MOLTIPLICATORE_SOGGEZIONE;
	}


	@Override
	public double getMoltiplicatoreFuria() {
		return CostantiPersonaggi.OMBRAFIAMMA_MOLTIPLICATORE_FURIA;
	}


	@Override
	public double getMoltiplicatoreCoraggio() {
		return CostantiPersonaggi.OMBRAFIAMMA_MOLTIPLICATORE_CORAGGIO;
	}


	@Override
	public double getMoltiplicatoreValore() {
		return CostantiPersonaggi.OMBRAFIAMMA_MOLTIPLICATORE_VALORE;
	}


	@Override
	public double getMoltiplicatoreContrattazione() {
		return CostantiPersonaggi.OMBRAFIAMMA_MOLTIPLICATORE_CONTRATTAZIONE;
	}


	@Override
	public double getMoltiplicatoreNumeroBersagli() {
		return CostantiPersonaggi.OMBRAFIAMMA_MOLTIPLICATORE_NUMERO_BERSAGLI;
	}


	@Override
	public double getMoltiplicatoreStanchezza() {
		return CostantiPersonaggi.OMBRAFIAMMA_MOLTIPLICATORE_STANCHEZZA;
	}


	@Override
	public double getMoltiplicatoreDanniMagici() {
		return CostantiPersonaggi.OMBRAFIAMMA_MOLTIPLICATORE_DANNI_MAGICI;
	}


	@Override
	public double getMoltiplicatoreDanniFisici() {
		return CostantiPersonaggi.OMBRAFIAMMA_MOLTIPLICATORE_DANNI_FISICI;
	}


	@Override
	public double getMoltiplicatoreRecuperoMagico() {
		return CostantiPersonaggi.OMBRAFIAMMA_MOLTIPLICATORE_RECUPERO_MAGICO;
	}


	@Override
	public double getMoltiplicatoreRecuperoFisico() {
		return CostantiPersonaggi.OMBRAFIAMMA_MOLTIPLICATORE_RECUPERO_FISICO;
	}


	@Override
	public double getMaxStatistica(TipoAttributo tipoAttributo) {
		switch (tipoAttributo) {
			case FORZA:
				return CostantiPersonaggi.OMBRAFIAMMA_MAX_FORZA;
			case DESTREZZA:
				return CostantiPersonaggi.OMBRAFIAMMA_MAX_DESTREZZA;
			case COSTITUZIONE:
				return CostantiPersonaggi.OMBRAFIAMMA_MAX_COSTITUZIONE;
			case INTELLIGENZA:
				return CostantiPersonaggi.OMBRAFIAMMA_MAX_INTELLIGENZA;
			case SAGGEZZA:
				return CostantiPersonaggi.OMBRAFIAMMA_MAX_SAGGEZZA;
			case CARISMA:
				return CostantiPersonaggi.OMBRAFIAMMA_MAX_CARISMA;
			case FORTUNA:
				return CostantiPersonaggi.OMBRAFIAMMA_MAX_FORTUNA;
			default:
				throw new IllegalArgumentException(tipoAttributo + " non è un tipo primario");
		}
	}

	@Override
	public boolean isParteConValoriMassimi() {
		return true;
	}

	@Override
	public boolean isImmortale() {
		return true;
	}
}
