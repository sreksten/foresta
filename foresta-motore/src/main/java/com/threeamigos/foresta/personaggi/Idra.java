package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.strumenti.Misc;
import com.threeamigos.foresta.tipi.*;


/**
 * L'idra: bestia gigantesca, boss del suo castello. Parte con salute e magia piene; è immune agli incantesimi di Terra, Acqua e
 * Aria e agli stati confuso e stordito.
 */
public class Idra extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UN_APOSTROFO; }
	public String getADS() { return Misc.L_APOSTROFO; }
	public String getAIP() { return Misc.ALCUNE; }
	public String getADP() { return Misc.LE; }
	public String getDeS() { return Misc.DELL_APOSTROFO; }
	public String getDeP() { return Misc.DELLE; }
	public String getDa() { return Misc.DA_UN_APOSTROFO; }

	public TipoPersonaggio getClasse() { return TipoPersonaggio.IDRA; }
	public String getNomeSingolare() { return "Idra"; }
	public String getNomePlurale() { return "Idre"; }
	public String getPronome() { return Misc.ESSA; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.FEMMINA; }

	public Idra(int livello) {
		super(TipoPersonaggio.IDRA, livello);
	}

	@Override
	protected void impostaValoriDiPartenza() {

		setQuantitaMassima(Costanti.IDRA_MAX_NUMERO);
	}

	@Override
	public int getPuntiEsperienza() {
		return Costanti.IDRA_PUNTI_ESPERIENZA;
	}

	@Override
	public double getSaluteBase() {
		return Costanti.IDRA_SALUTE_BASE;
	}

	@Override
	public double getMagiaBase() {
		return Costanti.IDRA_MAGIA_BASE;
	}

	@Override
	public double getLivellamentoMagia() {
		return Costanti.IDRA_LIVELLAMENTO_MAGIA;
	}

	@Override
	public double getMoltiplicatoreCarico() {
		return Costanti.IDRA_MOLTIPLICATORE_CARICO;
	}


	@Override
	public double getMoltiplicatoreCritico() {
		return Costanti.IDRA_MOLTIPLICATORE_CRITICO;
	}


	@Override
	public double getMoltiplicatorePrecisione() {
		return Costanti.IDRA_MOLTIPLICATORE_PRECISIONE;
	}


	@Override
	public double getMoltiplicatoreVelocita() {
		return Costanti.IDRA_MOLTIPLICATORE_VELOCITA;
	}


	@Override
	public double getMoltiplicatoreFurtivita() {
		return Costanti.IDRA_MOLTIPLICATORE_FURTIVITA;
	}


	@Override
	public double getMoltiplicatoreParata() {
		return Costanti.IDRA_MOLTIPLICATORE_PARATA;
	}


	@Override
	public double getMoltiplicatoreResistenzaMagica() {
		return Costanti.IDRA_MOLTIPLICATORE_RESISTENZA_MAGICA;
	}


	@Override
	public double getMoltiplicatorePercezione() {
		return Costanti.IDRA_MOLTIPLICATORE_PERCEZIONE;
	}


	@Override
	public double getMoltiplicatoreSoggezione() {
		return Costanti.IDRA_MOLTIPLICATORE_SOGGEZIONE;
	}


	@Override
	public double getMoltiplicatoreFuria() {
		return Costanti.IDRA_MOLTIPLICATORE_FURIA;
	}


	@Override
	public double getMoltiplicatoreCoraggio() {
		return Costanti.IDRA_MOLTIPLICATORE_CORAGGIO;
	}


	@Override
	public double getMoltiplicatoreValore() {
		return Costanti.IDRA_MOLTIPLICATORE_VALORE;
	}


	@Override
	public double getMoltiplicatoreNumeroBersagli() {
		return Costanti.IDRA_MOLTIPLICATORE_NUMERO_BERSAGLI;
	}


	@Override
	public double getMoltiplicatoreStanchezza() {
		return Costanti.IDRA_MOLTIPLICATORE_STANCHEZZA;
	}


	@Override
	public double getMoltiplicatoreDanniMagici() {
		return Costanti.IDRA_MOLTIPLICATORE_DANNI_MAGICI;
	}


	@Override
	public double getMoltiplicatoreDanniFisici() {
		return Costanti.IDRA_MOLTIPLICATORE_DANNI_FISICI;
	}


	@Override
	public double getMoltiplicatoreRecuperoMagico() {
		return Costanti.IDRA_MOLTIPLICATORE_RECUPERO_MAGICO;
	}


	@Override
	public double getMoltiplicatoreRecuperoFisico() {
		return Costanti.IDRA_MOLTIPLICATORE_RECUPERO_FISICO;
	}


	@Override
	public double getMaxStatistica(TipoAttributo tipoAttributo) {
		switch (tipoAttributo) {
			case FORZA:
				return Costanti.IDRA_MAX_FORZA;
			case DESTREZZA:
				return Costanti.IDRA_MAX_DESTREZZA;
			case COSTITUZIONE:
				return Costanti.IDRA_MAX_COSTITUZIONE;
			case INTELLIGENZA:
				return Costanti.IDRA_MAX_INTELLIGENZA;
			case SAGGEZZA:
				return Costanti.IDRA_MAX_SAGGEZZA;
			case CARISMA:
				return Costanti.IDRA_MAX_CARISMA;
			case FORTUNA:
				return Costanti.IDRA_MAX_FORTUNA;
			default:
				throw new IllegalArgumentException(tipoAttributo + " non è un tipo primario");
		}
	}

	@Override
	public boolean isParteConValoriMassimi() {
		return true;
	}
}
