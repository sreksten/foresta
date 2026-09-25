package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.motore.modellodati.TipoAttributo;
import com.threeamigos.foresta.offerte.ClassiOfferta;
import com.threeamigos.foresta.tools.Misc;

import java.util.function.Function;

public class Goblin extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UN; }
	public String getADS() { return Misc.IL; }
	public String getAIP() { return Misc.ALCUNI; }
	public String getADP() { return Misc.I; }
	public String getDeS() { return Misc.DEL; }
	public String getDeP() { return Misc.DEI; }
	public String getDa() { return Misc.DA_UN; }

	public String getNomeSingolare() { return "Goblin"; }
	public String getNomePlurale() { return "Goblin"; }
	public String getPronome() { return Misc.ESSO; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.MASCHIO; }

	public Goblin(int livello) {
		super(ClassePersonaggio.GOBLIN, livello);
	}

	@Override
	protected void impostaValoriDiPartenza(Function<Integer, Integer> funzione) {
		setCorrompibile(true);

		md.setForza(funzione.apply(getAttributoAdeguatoALivello(Costanti.GOBLIN_MAX_FORZA)));
		md.setDestrezza(funzione.apply(getAttributoAdeguatoALivello(Costanti.GOBLIN_MAX_DESTREZZA)));
		md.setCostituzione(funzione.apply(getAttributoAdeguatoALivello(Costanti.GOBLIN_MAX_COSTITUZIONE)));
		md.setIntelligenza(funzione.apply(getAttributoAdeguatoALivello(Costanti.GOBLIN_MAX_INTELLIGENZA)));
		md.setSaggezza(funzione.apply(getAttributoAdeguatoALivello(Costanti.GOBLIN_MAX_SAGGEZZA)));
		md.setCarisma(funzione.apply(getAttributoAdeguatoALivello(Costanti.GOBLIN_MAX_CARISMA)));
		md.setFortuna(funzione.apply(getAttributoAdeguatoALivello(Costanti.GOBLIN_MAX_FORTUNA)));

		setQuantitaMassima(Costanti.GOBLIN_MAX_NUMERO);
	}

	@Override
	public int getPuntiEsperienza() {
		return Costanti.GOBLIN_PUNTI_ESPERIENZA;
	}

	@Override
	public double getSaluteBase() {
		return Costanti.GOBLIN_SALUTE_BASE;
	}

	@Override
	public double getLivellamentoSalute() {
		return Costanti.GOBLIN_LIVELLAMENTO_SALUTE;
	}

	@Override
	public double getMagiaBase() {
		return Costanti.GOBLIN_MAGIA_BASE;
	}

	@Override
	public double getLivellamentoMagia() {
		return Costanti.GOBLIN_LIVELLAMENTO_MAGIA;
	}

	@Override
	public double getMoltiplicatoreCarico() {
		return Costanti.GOBLIN_MOLTIPLICATORE_CARICO;
	}

	@Override
	public String getNoteMoltiplicatoreCarico() {
		return Costanti.GOBLIN_MOLTIPLICATORE_CARICO_NOTA;
	}

	@Override
	public double getMoltiplicatoreCritico() {
		return Costanti.GOBLIN_MOLTIPLICATORE_CRITICO;
	}

	@Override
	public String getNoteMoltiplicatoreCritico() {
		return Costanti.GOBLIN_MOLTIPLICATORE_CRITICO_NOTA;
	}

	@Override
	public double getMoltiplicatorePrecisione() {
		return Costanti.GOBLIN_MOLTIPLICATORE_PRECISIONE;
	}

	@Override
	public String getNoteMoltiplicatorePrecisione() {
		return Costanti.GOBLIN_MOLTIPLICATORE_PRECISIONE_NOTA;
	}

	@Override
	public double getMoltiplicatoreVelocita() {
		return Costanti.GOBLIN_MOLTIPLICATORE_VELOCITA;
	}

	@Override
	public String getNoteMoltiplicatoreVelocita() {
		return Costanti.GOBLIN_MOLTIPLICATORE_VELOCITA_NOTA;
	}

	@Override
	public double getMoltiplicatoreFurtivita() {
		return Costanti.GOBLIN_MOLTIPLICATORE_FURTIVITA;
	}

	@Override
	public String getNoteMoltiplicatoreFurtivita() {
		return Costanti.GOBLIN_MOLTIPLICATORE_FURTIVITA_NOTA;
	}

	@Override
	public double getMoltiplicatoreParata() {
		return Costanti.GOBLIN_MOLTIPLICATORE_PARATA;
	}

	@Override
	public String getNoteMoltiplicatoreParata() {
		return Costanti.GOBLIN_MOLTIPLICATORE_PARATA_NOTA;
	}

	@Override
	public double getMoltiplicatoreResistenzaMagica() {
		return Costanti.GOBLIN_MOLTIPLICATORE_RESISTENZA_MAGICA;
	}

	@Override
	public String getNoteMoltiplicatoreResistenzaMagica() {
		return Costanti.GOBLIN_MOLTIPLICATORE_RESISTENZA_MAGICA_NOTA;
	}

	@Override
	public double getMoltiplicatorePercezione() {
		return Costanti.GOBLIN_MOLTIPLICATORE_PERCEZIONE;
	}

	@Override
	public String getNoteMoltiplicatorePercezione() {
		return Costanti.GOBLIN_MOLTIPLICATORE_PERCEZIONE_NOTA;
	}

	@Override
	public double getMoltiplicatoreSoggezione() {
		return Costanti.GOBLIN_MOLTIPLICATORE_SOGGEZIONE;
	}

	@Override
	public String getNoteMoltiplicatoreSoggezione() {
		return Costanti.GOBLIN_MOLTIPLICATORE_SOGGEZIONE_NOTA;
	}

	@Override
	public double getMoltiplicatoreFuria() {
		return Costanti.GOBLIN_MOLTIPLICATORE_FURIA;
	}

	@Override
	public String getNoteMoltiplicatoreFuria() {
		return Costanti.GOBLIN_MOLTIPLICATORE_FURIA_NOTA;
	}

	@Override
	public double getMoltiplicatoreCoraggio() {
		return Costanti.GOBLIN_MOLTIPLICATORE_CORAGGIO;
	}

	@Override
	public String getNoteMoltiplicatoreCoraggio() {
		return Costanti.GOBLIN_MOLTIPLICATORE_CORAGGIO_NOTA;
	}

	@Override
	public double getMoltiplicatoreValore() {
		return Costanti.GOBLIN_MOLTIPLICATORE_VALORE;
	}

	@Override
	public String getNoteMoltiplicatoreValore() {
		return Costanti.GOBLIN_MOLTIPLICATORE_VALORE_NOTA;
	}

	@Override
	public double getMoltiplicatoreContrattazione() {
		return Costanti.GOBLIN_MOLTIPLICATORE_CONTRATTAZIONE;
	}

	@Override
	public String getNoteMoltiplicatoreContrattazione() {
		return Costanti.GOBLIN_MOLTIPLICATORE_CONTRATTAZIONE_NOTA;
	}

	@Override
	public double getMoltiplicatoreNumeroBersagli() {
		return Costanti.GOBLIN_MOLTIPLICATORE_NUMERO_BERSAGLI;
	}

	@Override
	public String getNoteMoltiplicatoreNumeroBersagli() {
		return Costanti.GOBLIN_MOLTIPLICATORE_NUMERO_BERSAGLI_NOTA;
	}

	@Override
	public double getMoltiplicatoreStanchezza() {
		return Costanti.GOBLIN_MOLTIPLICATORE_STANCHEZZA;
	}

	@Override
	public String getNoteMoltiplicatoreStanchezza() {
		return Costanti.GOBLIN_MOLTIPLICATORE_STANCHEZZA_NOTA;
	}

	@Override
	public double getMoltiplicatoreDanniMagici() {
		return Costanti.GOBLIN_MOLTIPLICATORE_DANNI_MAGICI;
	}

	@Override
	public String getNoteMoltiplicatoreDanniMagici() {
		return Costanti.GOBLIN_MOLTIPLICATORE_DANNI_MAGICI_NOTA;
	}

	@Override
	public double getMoltiplicatoreDanniFisici() {
		return Costanti.GOBLIN_MOLTIPLICATORE_DANNI_FISICI;
	}

	@Override
	public String getNoteMoltiplicatoreDanniFisici() {
		return Costanti.GOBLIN_MOLTIPLICATORE_DANNI_FISICI_NOTA;
	}

	@Override
	public double getMoltiplicatoreRecuperoMagico() {
		return Costanti.GOBLIN_MOLTIPLICATORE_RECUPERO_MAGICO;
	}

	@Override
	public String getNoteMoltiplicatoreRecuperoMagico() {
		return Costanti.GOBLIN_MOLTIPLICATORE_RECUPERO_MAGICO_NOTA;
	}

	@Override
	public double getMoltiplicatoreRecuperoFisico() {
		return Costanti.GOBLIN_MOLTIPLICATORE_RECUPERO_FISICO;
	}

	@Override
	public String getNoteMoltiplicatoreRecuperoFisico() {
		return Costanti.GOBLIN_MOLTIPLICATORE_RECUPERO_FISICO_NOTA;
	}

	@Override
	public double getMaxStatistica(TipoAttributo tipoAttributo) {
		switch (tipoAttributo) {
			case FORZA:
				return Costanti.GOBLIN_MAX_FORZA;
			case DESTREZZA:
				return Costanti.GOBLIN_MAX_DESTREZZA;
			case COSTITUZIONE:
				return Costanti.GOBLIN_MAX_COSTITUZIONE;
			case INTELLIGENZA:
				return Costanti.GOBLIN_MAX_INTELLIGENZA;
			case SAGGEZZA:
				return Costanti.GOBLIN_MAX_SAGGEZZA;
			case CARISMA:
				return Costanti.GOBLIN_MAX_CARISMA;
			case FORTUNA:
				return Costanti.GOBLIN_MAX_FORTUNA;
			default:
				throw new IllegalArgumentException(tipoAttributo + " non è un tipo primario");
		}
	}

	@Override
	public ClassiOfferta[] getOfferteCorruzione() {
		return new ClassiOfferta[] {
				ClassiOfferta.AIUTO_MERCENARIO,
				ClassiOfferta.MAPPA_FORESTA,
		};
	}
}
