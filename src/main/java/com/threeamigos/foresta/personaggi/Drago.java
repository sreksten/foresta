package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.incantesimi.ClasseIncantesimo;
import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.tools.Misc;

import java.util.function.Function;

public class Drago extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UN; }
	public String getADS() { return Misc.IL; }
	public String getAIP() { return Misc.ALCUNI; }
	public String getADP() { return Misc.I; }
	public String getDeS() { return Misc.DEL; }
	public String getDeP() { return Misc.DEI; }
	public String getDa() { return Misc.DAL; }

	public String getNomeSingolare() { return "Drago"; }
	public String getNomePlurale() { return "Draghi"; }
	public String getPronome() { return Misc.ESSO; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.MASCHIO; }

	public Drago(int livello) {
		super(ClassePersonaggio.DRAGO, livello);
	}

	@Override
	protected void impostaValoriDiPartenza(Function<Integer, Integer> funzione) {
		setImmagine("personaggi/Drago.gif");

		md.setForza(funzione.apply(getAttributoAdeguatoALivello(Costanti.DRAGO_MAX_FORZA)));
		md.setDestrezza(funzione.apply(getAttributoAdeguatoALivello(Costanti.DRAGO_MAX_DESTREZZA)));
		md.setCostituzione(funzione.apply(getAttributoAdeguatoALivello(Costanti.DRAGO_MAX_COSTITUZIONE)));
		md.setIntelligenza(funzione.apply(getAttributoAdeguatoALivello(Costanti.DRAGO_MAX_INTELLIGENZA)));
		md.setSaggezza(funzione.apply(getAttributoAdeguatoALivello(Costanti.DRAGO_MAX_SAGGEZZA)));
		md.setCarisma(funzione.apply(getAttributoAdeguatoALivello(Costanti.DRAGO_MAX_CARISMA)));
		md.setFortuna(funzione.apply(getAttributoAdeguatoALivello(Costanti.DRAGO_MAX_FORTUNA)));

		setQuantitaMassima(Costanti.DRAGO_MAX_NUMERO);
	}

	@Override
	public int getPuntiEsperienza() {
		return Costanti.DRAGO_PUNTI_ESPERIENZA;
	}

	@Override
	public double getSaluteBase() {
		return Costanti.DRAGO_SALUTE_BASE;
	}

	@Override
	public double getLivellamentoSalute() {
		return Costanti.DRAGO_LIVELLAMENTO_SALUTE;
	}

	@Override
	public double getMagiaBase() {
		return Costanti.DRAGO_MAGIA_BASE;
	}

	@Override
	public double getLivellamentoMagia() {
		return Costanti.DRAGO_LIVELLAMENTO_MAGIA;
	}

	@Override
	public double getMoltiplicatoreCarico() {
		return Costanti.DRAGO_MOLTIPLICATORE_CARICO;
	}

	@Override
	public String getNoteMoltiplicatoreCarico() {
		return Costanti.DRAGO_MOLTIPLICATORE_CARICO_NOTA;
	}

	@Override
	public double getMoltiplicatoreCritico() {
		return Costanti.DRAGO_MOLTIPLICATORE_CRITICO;
	}

	@Override
	public String getNoteMoltiplicatoreCritico() {
		return Costanti.DRAGO_MOLTIPLICATORE_CRITICO_NOTA;
	}

	@Override
	public double getMoltiplicatorePrecisione() {
		return Costanti.DRAGO_MOLTIPLICATORE_PRECISIONE;
	}

	@Override
	public String getNoteMoltiplicatorePrecisione() {
		return Costanti.DRAGO_MOLTIPLICATORE_PRECISIONE_NOTA;
	}

	@Override
	public double getMoltiplicatoreVelocita() {
		return Costanti.DRAGO_MOLTIPLICATORE_VELOCITA;
	}

	@Override
	public String getNoteMoltiplicatoreVelocita() {
		return Costanti.DRAGO_MOLTIPLICATORE_VELOCITA_NOTA;
	}

	@Override
	public double getMoltiplicatoreFurtivita() {
		return Costanti.DRAGO_MOLTIPLICATORE_FURTIVITA;
	}

	@Override
	public String getNoteMoltiplicatoreFurtivita() {
		return Costanti.DRAGO_MOLTIPLICATORE_FURTIVITA_NOTA;
	}

	@Override
	public double getMoltiplicatoreParata() {
		return Costanti.DRAGO_MOLTIPLICATORE_PARATA;
	}

	@Override
	public String getNoteMoltiplicatoreParata() {
		return Costanti.DRAGO_MOLTIPLICATORE_PARATA_NOTA;
	}

	@Override
	public double getMoltiplicatoreResistenzaMagica() {
		return Costanti.DRAGO_MOLTIPLICATORE_RESISTENZA_MAGICA;
	}

	@Override
	public String getNoteMoltiplicatoreResistenzaMagica() {
		return Costanti.DRAGO_MOLTIPLICATORE_RESISTENZA_MAGICA_NOTA;
	}

	@Override
	public double getMoltiplicatorePercezione() {
		return Costanti.DRAGO_MOLTIPLICATORE_PERCEZIONE;
	}

	@Override
	public String getNoteMoltiplicatorePercezione() {
		return Costanti.DRAGO_MOLTIPLICATORE_PERCEZIONE_NOTA;
	}

	@Override
	public double getMoltiplicatoreSoggezione() {
		return Costanti.DRAGO_MOLTIPLICATORE_SOGGEZIONE;
	}

	@Override
	public String getNoteMoltiplicatoreSoggezione() {
		return Costanti.DRAGO_MOLTIPLICATORE_SOGGEZIONE_NOTA;
	}

	@Override
	public double getMoltiplicatoreFuria() {
		return Costanti.DRAGO_MOLTIPLICATORE_FURIA;
	}

	@Override
	public String getNoteMoltiplicatoreFuria() {
		return Costanti.DRAGO_MOLTIPLICATORE_FURIA_NOTA;
	}

	@Override
	public double getMoltiplicatoreCoraggio() {
		return Costanti.DRAGO_MOLTIPLICATORE_CORAGGIO;
	}

	@Override
	public String getNoteMoltiplicatoreCoraggio() {
		return Costanti.DRAGO_MOLTIPLICATORE_CORAGGIO_NOTA;
	}

	@Override
	public double getMoltiplicatoreValore() {
		return Costanti.DRAGO_MOLTIPLICATORE_VALORE;
	}

	@Override
	public String getNoteMoltiplicatoreValore() {
		return Costanti.DRAGO_MOLTIPLICATORE_VALORE_NOTA;
	}

	@Override
	public double getMoltiplicatoreNumeroBersagli() {
		return Costanti.DRAGO_MOLTIPLICATORE_NUMERO_BERSAGLI;
	}

	@Override
	public String getNoteMoltiplicatoreNumeroBersagli() {
		return Costanti.DRAGO_MOLTIPLICATORE_NUMERO_BERSAGLI_NOTA;
	}

	@Override
	public double getMoltiplicatoreStanchezza() {
		return Costanti.DRAGO_MOLTIPLICATORE_STANCHEZZA;
	}

	@Override
	public String getNoteMoltiplicatoreStanchezza() {
		return Costanti.DRAGO_MOLTIPLICATORE_STANCHEZZA_NOTA;
	}

	@Override
	public double getMoltiplicatoreDanniMagici() {
		return Costanti.DRAGO_MOLTIPLICATORE_DANNI_MAGICI;
	}

	@Override
	public String getNoteMoltiplicatoreDanniMagici() {
		return Costanti.DRAGO_MOLTIPLICATORE_DANNI_MAGICI_NOTA;
	}

	@Override
	public double getMoltiplicatoreDanniFisici() {
		return Costanti.DRAGO_MOLTIPLICATORE_DANNI_FISICI;
	}

	@Override
	public String getNoteMoltiplicatoreDanniFisici() {
		return Costanti.DRAGO_MOLTIPLICATORE_DANNI_FISICI_NOTA;
	}

	@Override
	public double getMoltiplicatoreRecuperoMagico() {
		return Costanti.DRAGO_MOLTIPLICATORE_RECUPERO_MAGICO;
	}

	@Override
	public String getNoteMoltiplicatoreRecuperoMagico() {
		return Costanti.DRAGO_MOLTIPLICATORE_RECUPERO_MAGICO_NOTA;
	}

	@Override
	public double getMoltiplicatoreRecuperoFisico() {
		return Costanti.DRAGO_MOLTIPLICATORE_RECUPERO_FISICO;
	}

	@Override
	public String getNoteMoltiplicatoreRecuperoFisico() {
		return Costanti.DRAGO_MOLTIPLICATORE_RECUPERO_FISICO_NOTA;
	}

	@Override
	public boolean isParteConValoriMassimi() {
		return true;
	}

	@Override
	public boolean isImmuneAIncantesimo(ClasseIncantesimo classeIncantesimo) {
		return classeIncantesimo == ClasseIncantesimo.FUOCO;
	}
}
