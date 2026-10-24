package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.testi.Articoli;
import com.threeamigos.foresta.tipi.Costanti;
import com.threeamigos.foresta.attributi.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoPersonaggio;


/**
 * Il necromante: avversario umano oscuro, che risveglia i morti e li manda avanti a sé (vedi le missioni dei morti che camminano). Umanoide,
 * non si corrompe e non diventa amico. Combatte con l'energia necrotica (la dissipazione dell'anima, vedi ArmaNaturale) e conosce le magie dei maghi
 * nemici; subisce meno danno necrotico e più danno sacro (vedi TipoPersonaggio).
 */
public class Necromante extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Articoli.UN; }
	public String getADS() { return Articoli.IL; }
	public String getAIP() { return Articoli.ALCUNI; }
	public String getADP() { return Articoli.I; }
	public String getDeS() { return Articoli.DEL; }
	public String getDeP() { return Articoli.DEI; }
	public String getDa() { return Articoli.DAL; }

	public String getNomeSingolare() { return "Necromante"; }
	public String getNomePlurale() { return "Necromanti"; }
	public String getPronome() { return Articoli.EGLI; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.MASCHIO; }

	public Necromante(int livello) {
		super(TipoPersonaggio.NECROMANTE, livello);
	}

	public Necromante(String nome, int livello) {
		super(nome, TipoPersonaggio.NECROMANTE, livello);
	}

	@Override
	protected void impostaValoriDiPartenza() {
		setQuantitaMassima(Costanti.NECROMANTE_MAX_NUMERO);
	}

	@Override
	public int getPuntiEsperienza() {
		return Costanti.NECROMANTE_PUNTI_ESPERIENZA;
	}

	@Override
	public double getSaluteBase() {
		return Costanti.NECROMANTE_SALUTE_BASE;
	}

	@Override
	public double getMagiaBase() {
		return Costanti.NECROMANTE_MAGIA_BASE;
	}

	@Override
	public double getLivellamentoMagia() {
		return Costanti.NECROMANTE_LIVELLAMENTO_MAGIA;
	}

	@Override
	public double getMoltiplicatoreCarico() {
		return Costanti.NECROMANTE_MOLTIPLICATORE_CARICO;
	}


	@Override
	public double getMoltiplicatoreCritico() {
		return Costanti.NECROMANTE_MOLTIPLICATORE_CRITICO;
	}


	@Override
	public double getMoltiplicatorePrecisione() {
		return Costanti.NECROMANTE_MOLTIPLICATORE_PRECISIONE;
	}


	@Override
	public double getMoltiplicatoreVelocita() {
		return Costanti.NECROMANTE_MOLTIPLICATORE_VELOCITA;
	}


	@Override
	public double getMoltiplicatoreFurtivita() {
		return Costanti.NECROMANTE_MOLTIPLICATORE_FURTIVITA;
	}


	@Override
	public double getMoltiplicatoreParata() {
		return Costanti.NECROMANTE_MOLTIPLICATORE_PARATA;
	}


	@Override
	public double getMoltiplicatoreResistenzaMagica() {
		return Costanti.NECROMANTE_MOLTIPLICATORE_RESISTENZA_MAGICA;
	}


	@Override
	public double getMoltiplicatorePercezione() {
		return Costanti.NECROMANTE_MOLTIPLICATORE_PERCEZIONE;
	}


	@Override
	public double getMoltiplicatoreSoggezione() {
		return Costanti.NECROMANTE_MOLTIPLICATORE_SOGGEZIONE;
	}


	@Override
	public double getMoltiplicatoreFuria() {
		return Costanti.NECROMANTE_MOLTIPLICATORE_FURIA;
	}


	@Override
	public double getMoltiplicatoreCoraggio() {
		return Costanti.NECROMANTE_MOLTIPLICATORE_CORAGGIO;
	}


	@Override
	public double getMoltiplicatoreValore() {
		return Costanti.NECROMANTE_MOLTIPLICATORE_VALORE;
	}


	@Override
	public double getMoltiplicatoreContrattazione() {
		return Costanti.NECROMANTE_MOLTIPLICATORE_CONTRATTAZIONE;
	}


	@Override
	public double getMoltiplicatoreNumeroBersagli() {
		return Costanti.NECROMANTE_MOLTIPLICATORE_NUMERO_BERSAGLI;
	}


	@Override
	public double getMoltiplicatoreStanchezza() {
		return Costanti.NECROMANTE_MOLTIPLICATORE_STANCHEZZA;
	}


	@Override
	public double getMoltiplicatoreDanniMagici() {
		return Costanti.NECROMANTE_MOLTIPLICATORE_DANNI_MAGICI;
	}


	@Override
	public double getMoltiplicatoreDanniFisici() {
		return Costanti.NECROMANTE_MOLTIPLICATORE_DANNI_FISICI;
	}


	@Override
	public double getMoltiplicatoreRecuperoMagico() {
		return Costanti.NECROMANTE_MOLTIPLICATORE_RECUPERO_MAGICO;
	}


	@Override
	public double getMoltiplicatoreRecuperoFisico() {
		return Costanti.NECROMANTE_MOLTIPLICATORE_RECUPERO_FISICO;
	}


	@Override
	public double getMaxStatistica(TipoAttributo tipoAttributo) {
		switch (tipoAttributo) {
			case FORZA:
				return Costanti.NECROMANTE_MAX_FORZA;
			case DESTREZZA:
				return Costanti.NECROMANTE_MAX_DESTREZZA;
			case COSTITUZIONE:
				return Costanti.NECROMANTE_MAX_COSTITUZIONE;
			case INTELLIGENZA:
				return Costanti.NECROMANTE_MAX_INTELLIGENZA;
			case SAGGEZZA:
				return Costanti.NECROMANTE_MAX_SAGGEZZA;
			case CARISMA:
				return Costanti.NECROMANTE_MAX_CARISMA;
			case FORTUNA:
				return Costanti.NECROMANTE_MAX_FORTUNA;
			default:
				throw new IllegalArgumentException(tipoAttributo + " non è un tipo primario");
		}
	}
}
