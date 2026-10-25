package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.tipi.CostantiPersonaggi;

import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.testi.Articoli;
import com.threeamigos.foresta.tipi.Costanti;
import com.threeamigos.foresta.tipi.TipoPersonaggio;


/**
 * Il lich: non morto, boss del suo castello. Parte con salute e magia piene; oltre a ciò che vale per tutti i non morti è
 * immune ad avvelenamento, spavento e silenzio.
 */
public class Lich extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Articoli.UN; }
	public String getADS() { return Articoli.IL; }
	public String getAIP() { return Articoli.ALCUNI; }
	public String getADP() { return Articoli.I; }
	public String getDeS() { return Articoli.DEL; }
	public String getDeP() { return Articoli.DEI; }
	public String getDa() { return Articoli.DAL; }

	public String getNomeSingolare() { return "Lich"; }
	public String getNomePlurale() { return "Lich"; }
	public String getPronome() { return Articoli.ESSO; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.MASCHIO; }

	public Lich(int livello) {
		super(TipoPersonaggio.LICH, livello);
	}

	@Override
	protected void impostaValoriDiPartenza() {

		setQuantitaMassima(CostantiPersonaggi.LICH_MAX_NUMERO);
	}

	@Override
	public int getPuntiEsperienza() {
		return CostantiPersonaggi.LICH_PUNTI_ESPERIENZA;
	}

	@Override
	public double getSaluteBase() {
		return CostantiPersonaggi.LICH_SALUTE_BASE;
	}

	@Override
	public double getMagiaBase() {
		return CostantiPersonaggi.LICH_MAGIA_BASE;
	}

	@Override
	public double getLivellamentoMagia() {
		return CostantiPersonaggi.LICH_LIVELLAMENTO_MAGIA;
	}

	@Override
	public double getMoltiplicatoreCarico() {
		return CostantiPersonaggi.LICH_MOLTIPLICATORE_CARICO;
	}


	@Override
	public double getMoltiplicatoreCritico() {
		return CostantiPersonaggi.LICH_MOLTIPLICATORE_CRITICO;
	}


	@Override
	public double getMoltiplicatorePrecisione() {
		return CostantiPersonaggi.LICH_MOLTIPLICATORE_PRECISIONE;
	}


	@Override
	public double getMoltiplicatoreVelocita() {
		return CostantiPersonaggi.LICH_MOLTIPLICATORE_VELOCITA;
	}


	@Override
	public double getMoltiplicatoreFurtivita() {
		return CostantiPersonaggi.LICH_MOLTIPLICATORE_FURTIVITA;
	}


	@Override
	public double getMoltiplicatoreParata() {
		return CostantiPersonaggi.LICH_MOLTIPLICATORE_PARATA;
	}


	@Override
	public double getMoltiplicatoreResistenzaMagica() {
		return CostantiPersonaggi.LICH_MOLTIPLICATORE_RESISTENZA_MAGICA;
	}


	@Override
	public double getMoltiplicatorePercezione() {
		return CostantiPersonaggi.LICH_MOLTIPLICATORE_PERCEZIONE;
	}


	@Override
	public double getMoltiplicatoreSoggezione() {
		return CostantiPersonaggi.LICH_MOLTIPLICATORE_SOGGEZIONE;
	}


	@Override
	public double getMoltiplicatoreFuria() {
		return CostantiPersonaggi.LICH_MOLTIPLICATORE_FURIA;
	}


	@Override
	public double getMoltiplicatoreCoraggio() {
		return CostantiPersonaggi.LICH_MOLTIPLICATORE_CORAGGIO;
	}


	@Override
	public double getMoltiplicatoreValore() {
		return CostantiPersonaggi.LICH_MOLTIPLICATORE_VALORE;
	}


	@Override
	public double getMoltiplicatoreNumeroBersagli() {
		return CostantiPersonaggi.LICH_MOLTIPLICATORE_NUMERO_BERSAGLI;
	}


	@Override
	public double getMoltiplicatoreStanchezza() {
		return CostantiPersonaggi.LICH_MOLTIPLICATORE_STANCHEZZA;
	}


	@Override
	public double getMoltiplicatoreDanniMagici() {
		return CostantiPersonaggi.LICH_MOLTIPLICATORE_DANNI_MAGICI;
	}


	@Override
	public double getMoltiplicatoreDanniFisici() {
		return CostantiPersonaggi.LICH_MOLTIPLICATORE_DANNI_FISICI;
	}


	@Override
	public double getMoltiplicatoreRecuperoMagico() {
		return CostantiPersonaggi.LICH_MOLTIPLICATORE_RECUPERO_MAGICO;
	}


	@Override
	public double getMoltiplicatoreRecuperoFisico() {
		return CostantiPersonaggi.LICH_MOLTIPLICATORE_RECUPERO_FISICO;
	}


	@Override
	public double getMaxStatistica(TipoAttributo tipoAttributo) {
		switch (tipoAttributo) {
			case FORZA:
				return CostantiPersonaggi.LICH_MAX_FORZA;
			case DESTREZZA:
				return CostantiPersonaggi.LICH_MAX_DESTREZZA;
			case COSTITUZIONE:
				return CostantiPersonaggi.LICH_MAX_COSTITUZIONE;
			case INTELLIGENZA:
				return CostantiPersonaggi.LICH_MAX_INTELLIGENZA;
			case SAGGEZZA:
				return CostantiPersonaggi.LICH_MAX_SAGGEZZA;
			case CARISMA:
				return CostantiPersonaggi.LICH_MAX_CARISMA;
			case FORTUNA:
				return CostantiPersonaggi.LICH_MAX_FORTUNA;
			default:
				throw new IllegalArgumentException(tipoAttributo + " non è un tipo primario");
		}
	}

	@Override
	public boolean isParteConValoriMassimi() {
		return true;
	}
}
