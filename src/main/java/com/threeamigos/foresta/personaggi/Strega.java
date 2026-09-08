package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.incantesimi.ClasseIncantesimo;
import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.tools.Misc;

import java.util.function.Function;

public class Strega extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UNA; }
	public String getADS() { return Misc.LA; }
	public String getAIP() { return Misc.ALCUNE; }
	public String getADP() { return Misc.LE; }
	public String getDeS() { return Misc.DELLA; }
	public String getDeP() { return Misc.DELLE; }
	public String getDa() { return Misc.DA_UNA; }

	public String getNomeSingolare() { return "Strega"; }
	public String getNomePlurale() { return "Streghe"; }
	public String getPronome() { return Misc.ELLA; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.FEMMINA; }

	public Strega(int livello) {
		super(ClassePersonaggio.STREGA, livello);
	}

	@Override
	protected void impostaValoriDiPartenza(Function<Integer, Integer> funzione) {
		setImmagine("personaggi/Strega.gif");

		md.setForza(funzione.apply(getAttributoAdeguatoALivello(Costanti.STREGA_MAX_FORZA)));
		md.setDestrezza(funzione.apply(getAttributoAdeguatoALivello(Costanti.STREGA_MAX_DESTREZZA)));
		md.setCostituzione(funzione.apply(getAttributoAdeguatoALivello(Costanti.STREGA_MAX_COSTITUZIONE)));
		md.setIntelligenza(funzione.apply(getAttributoAdeguatoALivello(Costanti.STREGA_MAX_INTELLIGENZA)));
		md.setSaggezza(funzione.apply(getAttributoAdeguatoALivello(Costanti.STREGA_MAX_SAGGEZZA)));
		md.setCarisma(funzione.apply(getAttributoAdeguatoALivello(Costanti.STREGA_MAX_CARISMA)));
		md.setFortuna(funzione.apply(getAttributoAdeguatoALivello(Costanti.STREGA_MAX_FORTUNA)));

		setQuantitaMassima(Costanti.STREGA_MAX_NUMERO);
	}

	@Override
	public int getPuntiEsperienza() {
		return Costanti.STREGA_PUNTI_ESPERIENZA;
	}

	@Override
	public double getSaluteBase() {
		return Costanti.STREGA_SALUTE_BASE;
	}

	@Override
	public double getLivellamentoSalute() {
		return Costanti.STREGA_LIVELLAMENTO_SALUTE;
	}

	@Override
	public double getMagiaBase() {
		return Costanti.STREGA_MAGIA_BASE;
	}

	@Override
	public double getLivellamentoMagia() {
		return Costanti.STREGA_LIVELLAMENTO_MAGIA;
	}

	@Override
	public double getMoltiplicatoreCarico() {
		return Costanti.STREGA_MOLTIPLICATORE_CARICO;
	}

	@Override
	public String getNoteMoltiplicatoreCarico() {
		return Costanti.STREGA_MOLTIPLICATORE_CARICO_NOTA;
	}

	@Override
	public double getMoltiplicatoreCritico() {
		return Costanti.STREGA_MOLTIPLICATORE_CRITICO;
	}

	@Override
	public String getNoteMoltiplicatoreCritico() {
		return Costanti.STREGA_MOLTIPLICATORE_CRITICO_NOTA;
	}

	@Override
	public double getMoltiplicatorePrecisione() {
		return Costanti.STREGA_MOLTIPLICATORE_PRECISIONE;
	}

	@Override
	public String getNoteMoltiplicatorePrecisione() {
		return Costanti.STREGA_MOLTIPLICATORE_PRECISIONE_NOTA;
	}

	@Override
	public double getMoltiplicatoreVelocita() {
		return Costanti.STREGA_MOLTIPLICATORE_VELOCITA;
	}

	@Override
	public String getNoteMoltiplicatoreVelocita() {
		return Costanti.STREGA_MOLTIPLICATORE_VELOCITA_NOTA;
	}

	@Override
	public double getMoltiplicatoreFurtivita() {
		return Costanti.STREGA_MOLTIPLICATORE_FURTIVITA;
	}

	@Override
	public String getNoteMoltiplicatoreFurtivita() {
		return Costanti.STREGA_MOLTIPLICATORE_FURTIVITA_NOTA;
	}

	@Override
	public double getMoltiplicatoreParata() {
		return Costanti.STREGA_MOLTIPLICATORE_PARATA;
	}

	@Override
	public String getNoteMoltiplicatoreParata() {
		return Costanti.STREGA_MOLTIPLICATORE_PARATA_NOTA;
	}

	@Override
	public double getMoltiplicatoreResistenzaMagica() {
		return Costanti.STREGA_MOLTIPLICATORE_RESISTENZA_MAGICA;
	}

	@Override
	public String getNoteMoltiplicatoreResistenzaMagica() {
		return Costanti.STREGA_MOLTIPLICATORE_RESISTENZA_MAGICA_NOTA;
	}

	@Override
	public double getMoltiplicatorePercezione() {
		return Costanti.STREGA_MOLTIPLICATORE_PERCEZIONE;
	}

	@Override
	public String getNoteMoltiplicatorePercezione() {
		return Costanti.STREGA_MOLTIPLICATORE_PERCEZIONE_NOTA;
	}

	@Override
	public double getMoltiplicatoreSoggezione() {
		return Costanti.STREGA_MOLTIPLICATORE_SOGGEZIONE;
	}

	@Override
	public String getNoteMoltiplicatoreSoggezione() {
		return Costanti.STREGA_MOLTIPLICATORE_SOGGEZIONE_NOTA;
	}

	@Override
	public double getMoltiplicatoreFuria() {
		return Costanti.STREGA_MOLTIPLICATORE_FURIA;
	}

	@Override
	public String getNoteMoltiplicatoreFuria() {
		return Costanti.STREGA_MOLTIPLICATORE_FURIA_NOTA;
	}

	@Override
	public double getMoltiplicatoreCoraggio() {
		return Costanti.STREGA_MOLTIPLICATORE_CORAGGIO;
	}

	@Override
	public String getNoteMoltiplicatoreCoraggio() {
		return Costanti.STREGA_MOLTIPLICATORE_CORAGGIO_NOTA;
	}

	@Override
	public double getMoltiplicatoreValore() {
		return Costanti.STREGA_MOLTIPLICATORE_VALORE;
	}

	@Override
	public String getNoteMoltiplicatoreValore() {
		return Costanti.STREGA_MOLTIPLICATORE_VALORE_NOTA;
	}

	@Override
	public double getMoltiplicatoreNumeroBersagli() {
		return Costanti.STREGA_MOLTIPLICATORE_NUMERO_BERSAGLI;
	}

	@Override
	public String getNoteMoltiplicatoreNumeroBersagli() {
		return Costanti.STREGA_MOLTIPLICATORE_NUMERO_BERSAGLI_NOTA;
	}

	@Override
	public double getMoltiplicatoreStanchezza() {
		return Costanti.STREGA_MOLTIPLICATORE_STANCHEZZA;
	}

	@Override
	public String getNoteMoltiplicatoreStanchezza() {
		return Costanti.STREGA_MOLTIPLICATORE_STANCHEZZA_NOTA;
	}

	@Override
	public double getMoltiplicatoreDanniMagici() {
		return Costanti.STREGA_MOLTIPLICATORE_DANNI_MAGICI;
	}

	@Override
	public String getNoteMoltiplicatoreDanniMagici() {
		return Costanti.STREGA_MOLTIPLICATORE_DANNI_MAGICI_NOTA;
	}

	@Override
	public double getMoltiplicatoreDanniFisici() {
		return Costanti.STREGA_MOLTIPLICATORE_DANNI_FISICI;
	}

	@Override
	public String getNoteMoltiplicatoreDanniFisici() {
		return Costanti.STREGA_MOLTIPLICATORE_DANNI_FISICI_NOTA;
	}

	@Override
	public double getMoltiplicatoreRecuperoMagico() {
		return Costanti.STREGA_MOLTIPLICATORE_RECUPERO_MAGICO;
	}

	@Override
	public String getNoteMoltiplicatoreRecuperoMagico() {
		return Costanti.STREGA_MOLTIPLICATORE_RECUPERO_MAGICO_NOTA;
	}

	@Override
	public double getMoltiplicatoreRecuperoFisico() {
		return Costanti.STREGA_MOLTIPLICATORE_RECUPERO_FISICO;
	}

	@Override
	public String getNoteMoltiplicatoreRecuperoFisico() {
		return Costanti.STREGA_MOLTIPLICATORE_RECUPERO_FISICO_NOTA;
	}

	@Override
	public boolean isParteConValoriMassimi() {
		return true;
	}

	@Override
	public boolean isImmuneAIncantesimo(ClasseIncantesimo classeIncantesimo) {
		return classeIncantesimo == ClasseIncantesimo.ACQUA ||
				classeIncantesimo == ClasseIncantesimo.ARIA ||
				classeIncantesimo == ClasseIncantesimo.FUOCO ||
				classeIncantesimo == ClasseIncantesimo.TERRA;
	}
}
