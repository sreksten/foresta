package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.tipi.Costanti;
import com.threeamigos.foresta.attributi.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.strumenti.Misc;


/**
 * Il minotauro gigante: creatura umanoide, ibrida e gigantesca, boss del suo castello. Parte con salute e magia piene.
 */
public class MinotauroGigante extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UN; }
	public String getADS() { return Misc.IL; }
	public String getAIP() { return Misc.ALCUNI; }
	public String getADP() { return Misc.I; }
	public String getDeS() { return Misc.DEL; }
	public String getDeP() { return Misc.DEI; }
	public String getDa() { return Misc.DAL; }

	public TipoPersonaggio getClasse() { return TipoPersonaggio.MINOTAURO_GIGANTE; }
	public String getNomeSingolare() { return "Minotauro Gigante"; }
	public String getNomePlurale() { return "Minotauri Giganti"; }
	public String getPronome() { return Misc.ESSO; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.MASCHIO; }

	public MinotauroGigante(int livello) {
		super(TipoPersonaggio.MINOTAURO_GIGANTE, livello);
	}

	@Override
	public boolean isParteConValoriMassimi() {
		return true;
	}

	@Override
	protected void impostaValoriDiPartenza() {

		setQuantitaMassima(Costanti.MINOTAUROGIGANTE_MAX_NUMERO);
	}

	@Override
	public int getPuntiEsperienza() {
		return Costanti.MINOTAUROGIGANTE_PUNTI_ESPERIENZA;
	}

	@Override
	public double getSaluteBase() {
		return Costanti.MINOTAUROGIGANTE_SALUTE_BASE;
	}

	@Override
	public double getMagiaBase() {
		return Costanti.MINOTAUROGIGANTE_MAGIA_BASE;
	}

	@Override
	public double getLivellamentoMagia() {
		return Costanti.MINOTAUROGIGANTE_LIVELLAMENTO_MAGIA;
	}

	@Override
	public double getMoltiplicatoreCarico() {
		return Costanti.MINOTAUROGIGANTE_MOLTIPLICATORE_CARICO;
	}


	@Override
	public double getMoltiplicatoreCritico() {
		return Costanti.MINOTAUROGIGANTE_MOLTIPLICATORE_CRITICO;
	}


	@Override
	public double getMoltiplicatorePrecisione() {
		return Costanti.MINOTAUROGIGANTE_MOLTIPLICATORE_PRECISIONE;
	}


	@Override
	public double getMoltiplicatoreVelocita() {
		return Costanti.MINOTAUROGIGANTE_MOLTIPLICATORE_VELOCITA;
	}


	@Override
	public double getMoltiplicatoreFurtivita() {
		return Costanti.MINOTAUROGIGANTE_MOLTIPLICATORE_FURTIVITA;
	}


	@Override
	public double getMoltiplicatoreParata() {
		return Costanti.MINOTAUROGIGANTE_MOLTIPLICATORE_PARATA;
	}


	@Override
	public double getMoltiplicatoreResistenzaMagica() {
		return Costanti.MINOTAUROGIGANTE_MOLTIPLICATORE_RESISTENZA_MAGICA;
	}


	@Override
	public double getMoltiplicatorePercezione() {
		return Costanti.MINOTAUROGIGANTE_MOLTIPLICATORE_PERCEZIONE;
	}


	@Override
	public double getMoltiplicatoreSoggezione() {
		return Costanti.MINOTAUROGIGANTE_MOLTIPLICATORE_SOGGEZIONE;
	}


	@Override
	public double getMoltiplicatoreFuria() {
		return Costanti.MINOTAUROGIGANTE_MOLTIPLICATORE_FURIA;
	}


	@Override
	public double getMoltiplicatoreCoraggio() {
		return Costanti.MINOTAUROGIGANTE_MOLTIPLICATORE_CORAGGIO;
	}


	@Override
	public double getMoltiplicatoreValore() {
		return Costanti.MINOTAUROGIGANTE_MOLTIPLICATORE_VALORE;
	}


	@Override
	public double getMoltiplicatoreNumeroBersagli() {
		return Costanti.MINOTAUROGIGANTE_MOLTIPLICATORE_NUMERO_BERSAGLI;
	}


	@Override
	public double getMoltiplicatoreStanchezza() {
		return Costanti.MINOTAUROGIGANTE_MOLTIPLICATORE_STANCHEZZA;
	}


	@Override
	public double getMoltiplicatoreDanniMagici() {
		return Costanti.MINOTAUROGIGANTE_MOLTIPLICATORE_DANNI_MAGICI;
	}


	@Override
	public double getMoltiplicatoreDanniFisici() {
		return Costanti.MINOTAUROGIGANTE_MOLTIPLICATORE_DANNI_FISICI;
	}


	@Override
	public double getMoltiplicatoreRecuperoMagico() {
		return Costanti.MINOTAUROGIGANTE_MOLTIPLICATORE_RECUPERO_MAGICO;
	}


	@Override
	public double getMoltiplicatoreRecuperoFisico() {
		return Costanti.MINOTAUROGIGANTE_MOLTIPLICATORE_RECUPERO_FISICO;
	}


	@Override
	public double getMaxStatistica(TipoAttributo tipoAttributo) {
		switch (tipoAttributo) {
			case FORZA:
				return Costanti.MINOTAUROGIGANTE_MAX_FORZA;
			case DESTREZZA:
				return Costanti.MINOTAUROGIGANTE_MAX_DESTREZZA;
			case COSTITUZIONE:
				return Costanti.MINOTAUROGIGANTE_MAX_COSTITUZIONE;
			case INTELLIGENZA:
				return Costanti.MINOTAUROGIGANTE_MAX_INTELLIGENZA;
			case SAGGEZZA:
				return Costanti.MINOTAUROGIGANTE_MAX_SAGGEZZA;
			case CARISMA:
				return Costanti.MINOTAUROGIGANTE_MAX_CARISMA;
			case FORTUNA:
				return Costanti.MINOTAUROGIGANTE_MAX_FORTUNA;
			default:
				throw new IllegalArgumentException(tipoAttributo + " non è un tipo primario");
		}
	}
}
