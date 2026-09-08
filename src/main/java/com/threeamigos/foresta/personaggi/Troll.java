package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.incantesimi.ClasseIncantesimo;
import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.tools.Misc;

import java.util.function.Function;

public class Troll extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UN; }
	public String getADS() { return Misc.IL; }
	public String getAIP() { return Misc.ALCUNI; }
	public String getADP() { return Misc.I; }
	public String getDeS() { return Misc.DEL; }
	public String getDeP() { return Misc.DEI; }
	public String getDa() { return Misc.DAL; }

	public String getNomeSingolare() { return "Troll"; }
	public String getNomePlurale() { return "Troll"; }
	public String getPronome() { return Misc.ESSO; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.MASCHIO; }

	public Troll(int livello) {
		super(ClassePersonaggio.TROLL, livello);
	}

	@Override
	protected void impostaValoriDiPartenza(Function<Integer, Integer> funzione) {
		setImmagine("personaggi/Troll.gif");
		setCorrompibile(true);

		md.setForza(funzione.apply(getAttributoAdeguatoALivello(Costanti.TROLL_MAX_FORZA)));
		md.setDestrezza(funzione.apply(getAttributoAdeguatoALivello(Costanti.TROLL_MAX_DESTREZZA)));
		md.setCostituzione(funzione.apply(getAttributoAdeguatoALivello(Costanti.TROLL_MAX_COSTITUZIONE)));
		md.setIntelligenza(funzione.apply(getAttributoAdeguatoALivello(Costanti.TROLL_MAX_INTELLIGENZA)));
		md.setSaggezza(funzione.apply(getAttributoAdeguatoALivello(Costanti.TROLL_MAX_SAGGEZZA)));
		md.setCarisma(funzione.apply(getAttributoAdeguatoALivello(Costanti.TROLL_MAX_CARISMA)));
		md.setFortuna(funzione.apply(getAttributoAdeguatoALivello(Costanti.TROLL_MAX_FORTUNA)));

		setQuantitaMassima(Costanti.TROLL_MAX_NUMERO);
	}

	@Override
	public int getPuntiEsperienza() {
		return Costanti.TROLL_PUNTI_ESPERIENZA;
	}

	@Override
	public double getSaluteBase() {
		return Costanti.TROLL_SALUTE_BASE;
	}

	@Override
	public double getLivellamentoSalute() {
		return Costanti.TROLL_LIVELLAMENTO_SALUTE;
	}

	@Override
	public double getMagiaBase() {
		return Costanti.TROLL_MAGIA_BASE;
	}

	@Override
	public double getLivellamentoMagia() {
		return Costanti.TROLL_LIVELLAMENTO_MAGIA;
	}

	@Override
	public double getMoltiplicatoreCarico() {
		return Costanti.TROLL_MOLTIPLICATORE_CARICO;
	}

	@Override
	public String getNoteMoltiplicatoreCarico() {
		return Costanti.TROLL_MOLTIPLICATORE_CARICO_NOTA;
	}

	@Override
	public double getMoltiplicatoreCritico() {
		return Costanti.TROLL_MOLTIPLICATORE_CRITICO;
	}

	@Override
	public String getNoteMoltiplicatoreCritico() {
		return Costanti.TROLL_MOLTIPLICATORE_CRITICO_NOTA;
	}

	@Override
	public double getMoltiplicatorePrecisione() {
		return Costanti.TROLL_MOLTIPLICATORE_PRECISIONE;
	}

	@Override
	public String getNoteMoltiplicatorePrecisione() {
		return Costanti.TROLL_MOLTIPLICATORE_PRECISIONE_NOTA;
	}

	@Override
	public double getMoltiplicatoreVelocita() {
		return Costanti.TROLL_MOLTIPLICATORE_VELOCITA;
	}

	@Override
	public String getNoteMoltiplicatoreVelocita() {
		return Costanti.TROLL_MOLTIPLICATORE_VELOCITA_NOTA;
	}

	@Override
	public double getMoltiplicatoreFurtivita() {
		return Costanti.TROLL_MOLTIPLICATORE_FURTIVITA;
	}

	@Override
	public String getNoteMoltiplicatoreFurtivita() {
		return Costanti.TROLL_MOLTIPLICATORE_FURTIVITA_NOTA;
	}

	@Override
	public double getMoltiplicatoreParata() {
		return Costanti.TROLL_MOLTIPLICATORE_PARATA;
	}

	@Override
	public String getNoteMoltiplicatoreParata() {
		return Costanti.TROLL_MOLTIPLICATORE_PARATA_NOTA;
	}

	@Override
	public double getMoltiplicatoreResistenzaMagica() {
		return Costanti.TROLL_MOLTIPLICATORE_RESISTENZA_MAGICA;
	}

	@Override
	public String getNoteMoltiplicatoreResistenzaMagica() {
		return Costanti.TROLL_MOLTIPLICATORE_RESISTENZA_MAGICA_NOTA;
	}

	@Override
	public double getMoltiplicatorePercezione() {
		return Costanti.TROLL_MOLTIPLICATORE_PERCEZIONE;
	}

	@Override
	public String getNoteMoltiplicatorePercezione() {
		return Costanti.TROLL_MOLTIPLICATORE_PERCEZIONE_NOTA;
	}

	@Override
	public double getMoltiplicatoreSoggezione() {
		return Costanti.TROLL_MOLTIPLICATORE_SOGGEZIONE;
	}

	@Override
	public String getNoteMoltiplicatoreSoggezione() {
		return Costanti.TROLL_MOLTIPLICATORE_SOGGEZIONE_NOTA;
	}

	@Override
	public double getMoltiplicatoreFuria() {
		return Costanti.TROLL_MOLTIPLICATORE_FURIA;
	}

	@Override
	public String getNoteMoltiplicatoreFuria() {
		return Costanti.TROLL_MOLTIPLICATORE_FURIA_NOTA;
	}

	@Override
	public double getMoltiplicatoreCoraggio() {
		return Costanti.TROLL_MOLTIPLICATORE_CORAGGIO;
	}

	@Override
	public String getNoteMoltiplicatoreCoraggio() {
		return Costanti.TROLL_MOLTIPLICATORE_CORAGGIO_NOTA;
	}

	@Override
	public double getMoltiplicatoreValore() {
		return Costanti.TROLL_MOLTIPLICATORE_VALORE;
	}

	@Override
	public String getNoteMoltiplicatoreValore() {
		return Costanti.TROLL_MOLTIPLICATORE_VALORE_NOTA;
	}

	@Override
	public double getMoltiplicatoreNumeroBersagli() {
		return Costanti.TROLL_MOLTIPLICATORE_NUMERO_BERSAGLI;
	}

	@Override
	public String getNoteMoltiplicatoreNumeroBersagli() {
		return Costanti.TROLL_MOLTIPLICATORE_NUMERO_BERSAGLI_NOTA;
	}

	@Override
	public double getMoltiplicatoreStanchezza() {
		return Costanti.TROLL_MOLTIPLICATORE_STANCHEZZA;
	}

	@Override
	public String getNoteMoltiplicatoreStanchezza() {
		return Costanti.TROLL_MOLTIPLICATORE_STANCHEZZA_NOTA;
	}

	@Override
	public double getMoltiplicatoreDanniMagici() {
		return Costanti.TROLL_MOLTIPLICATORE_DANNI_MAGICI;
	}

	@Override
	public String getNoteMoltiplicatoreDanniMagici() {
		return Costanti.TROLL_MOLTIPLICATORE_DANNI_MAGICI_NOTA;
	}

	@Override
	public double getMoltiplicatoreDanniFisici() {
		return Costanti.TROLL_MOLTIPLICATORE_DANNI_FISICI;
	}

	@Override
	public String getNoteMoltiplicatoreDanniFisici() {
		return Costanti.TROLL_MOLTIPLICATORE_DANNI_FISICI_NOTA;
	}

	@Override
	public double getMoltiplicatoreRecuperoMagico() {
		return Costanti.TROLL_MOLTIPLICATORE_RECUPERO_MAGICO;
	}

	@Override
	public String getNoteMoltiplicatoreRecuperoMagico() {
		return Costanti.TROLL_MOLTIPLICATORE_RECUPERO_MAGICO_NOTA;
	}

	@Override
	public double getMoltiplicatoreRecuperoFisico() {
		return Costanti.TROLL_MOLTIPLICATORE_RECUPERO_FISICO;
	}

	@Override
	public String getNoteMoltiplicatoreRecuperoFisico() {
		return Costanti.TROLL_MOLTIPLICATORE_RECUPERO_FISICO_NOTA;
	}

	@Override
	public boolean isImmuneAIncantesimo(ClasseIncantesimo classeIncantesimo) {
		return classeIncantesimo == ClasseIncantesimo.TERRA;
	}
}
