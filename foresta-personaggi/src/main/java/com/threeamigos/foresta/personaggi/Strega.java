package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.tipi.CostantiPersonaggi;

import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.testi.Articoli;
import com.threeamigos.foresta.tipi.Costanti;
import com.threeamigos.foresta.tipi.TipoPersonaggio;


/**
 * La strega: creatura umanoide, boss del suo castello. Parte con salute e magia piene ed è immune agli incantesimi di Acqua,
 * Aria, Fuoco e Terra.
 */
public class Strega extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Articoli.UNA; }
	public String getADS() { return Articoli.LA; }
	public String getAIP() { return Articoli.ALCUNE; }
	public String getADP() { return Articoli.LE; }
	public String getDeS() { return Articoli.DELLA; }
	public String getDeP() { return Articoli.DELLE; }
	public String getDa() { return Articoli.DA_UNA; }

	public String getNomeSingolare() { return "Strega"; }
	public String getNomePlurale() { return "Streghe"; }
	public String getPronome() { return Articoli.ELLA; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.FEMMINA; }

	public Strega(int livello) {
		super(TipoPersonaggio.STREGA, livello);
	}

	@Override
	protected void impostaValoriDiPartenza() {

		setQuantitaMassima(CostantiPersonaggi.STREGA_MAX_NUMERO);
	}

	@Override
	public int getPuntiEsperienza() {
		return CostantiPersonaggi.STREGA_PUNTI_ESPERIENZA;
	}

	@Override
	public double getSaluteBase() {
		return CostantiPersonaggi.STREGA_SALUTE_BASE;
	}

	@Override
	public double getMagiaBase() {
		return CostantiPersonaggi.STREGA_MAGIA_BASE;
	}

	@Override
	public double getLivellamentoMagia() {
		return CostantiPersonaggi.STREGA_LIVELLAMENTO_MAGIA;
	}

	@Override
	public double getMoltiplicatoreCarico() {
		return CostantiPersonaggi.STREGA_MOLTIPLICATORE_CARICO;
	}


	@Override
	public double getMoltiplicatoreCritico() {
		return CostantiPersonaggi.STREGA_MOLTIPLICATORE_CRITICO;
	}


	@Override
	public double getMoltiplicatorePrecisione() {
		return CostantiPersonaggi.STREGA_MOLTIPLICATORE_PRECISIONE;
	}


	@Override
	public double getMoltiplicatoreVelocita() {
		return CostantiPersonaggi.STREGA_MOLTIPLICATORE_VELOCITA;
	}


	@Override
	public double getMoltiplicatoreFurtivita() {
		return CostantiPersonaggi.STREGA_MOLTIPLICATORE_FURTIVITA;
	}


	@Override
	public double getMoltiplicatoreParata() {
		return CostantiPersonaggi.STREGA_MOLTIPLICATORE_PARATA;
	}


	@Override
	public double getMoltiplicatoreResistenzaMagica() {
		return CostantiPersonaggi.STREGA_MOLTIPLICATORE_RESISTENZA_MAGICA;
	}


	@Override
	public double getMoltiplicatorePercezione() {
		return CostantiPersonaggi.STREGA_MOLTIPLICATORE_PERCEZIONE;
	}


	@Override
	public double getMoltiplicatoreSoggezione() {
		return CostantiPersonaggi.STREGA_MOLTIPLICATORE_SOGGEZIONE;
	}


	@Override
	public double getMoltiplicatoreFuria() {
		return CostantiPersonaggi.STREGA_MOLTIPLICATORE_FURIA;
	}


	@Override
	public double getMoltiplicatoreCoraggio() {
		return CostantiPersonaggi.STREGA_MOLTIPLICATORE_CORAGGIO;
	}


	@Override
	public double getMoltiplicatoreValore() {
		return CostantiPersonaggi.STREGA_MOLTIPLICATORE_VALORE;
	}


	@Override
	public double getMoltiplicatoreNumeroBersagli() {
		return CostantiPersonaggi.STREGA_MOLTIPLICATORE_NUMERO_BERSAGLI;
	}


	@Override
	public double getMoltiplicatoreStanchezza() {
		return CostantiPersonaggi.STREGA_MOLTIPLICATORE_STANCHEZZA;
	}


	@Override
	public double getMoltiplicatoreDanniMagici() {
		return CostantiPersonaggi.STREGA_MOLTIPLICATORE_DANNI_MAGICI;
	}


	@Override
	public double getMoltiplicatoreDanniFisici() {
		return CostantiPersonaggi.STREGA_MOLTIPLICATORE_DANNI_FISICI;
	}


	@Override
	public double getMoltiplicatoreRecuperoMagico() {
		return CostantiPersonaggi.STREGA_MOLTIPLICATORE_RECUPERO_MAGICO;
	}


	@Override
	public double getMoltiplicatoreRecuperoFisico() {
		return CostantiPersonaggi.STREGA_MOLTIPLICATORE_RECUPERO_FISICO;
	}


	@Override
	public double getMaxStatistica(TipoAttributo tipoAttributo) {
		switch (tipoAttributo) {
			case FORZA:
				return CostantiPersonaggi.STREGA_MAX_FORZA;
			case DESTREZZA:
				return CostantiPersonaggi.STREGA_MAX_DESTREZZA;
			case COSTITUZIONE:
				return CostantiPersonaggi.STREGA_MAX_COSTITUZIONE;
			case INTELLIGENZA:
				return CostantiPersonaggi.STREGA_MAX_INTELLIGENZA;
			case SAGGEZZA:
				return CostantiPersonaggi.STREGA_MAX_SAGGEZZA;
			case CARISMA:
				return CostantiPersonaggi.STREGA_MAX_CARISMA;
			case FORTUNA:
				return CostantiPersonaggi.STREGA_MAX_FORTUNA;
			default:
				throw new IllegalArgumentException(tipoAttributo + " non è un tipo primario");
		}
	}

	@Override
	public boolean isParteConValoriMassimi() {
		return true;
	}
}
