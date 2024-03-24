package com.threeamigos.foresta.personaggi;

import java.awt.image.BufferedImage;
import java.util.List;

import com.threeamigos.foresta.incantesimi.ClassiIncantesimo;
import com.threeamigos.foresta.incantesimi.Incantesimo;
import com.threeamigos.foresta.incantesimi.PortataIncantesimo;
import com.threeamigos.foresta.incantesimi.TipoIncantesimo;
import com.threeamigos.foresta.motore.Comando;
import com.threeamigos.foresta.motore.Gruppo;
import com.threeamigos.foresta.motore.GruppoAvversario;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.Logger;
import com.threeamigos.foresta.motore.modellodati.ArtefattoMD;
import com.threeamigos.foresta.motore.modellodati.PersonaggioMD;
import com.threeamigos.foresta.offerte.ClassiOfferta;
import com.threeamigos.foresta.offerte.Offerta;
import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.tools.Misc;
import com.threeamigos.foresta.tools.Random;
import com.threeamigos.foresta.ui.BufferedImageBuilder;
import com.threeamigos.foresta.ui.ImageCache;
import com.threeamigos.foresta.ui.UI;

/*
 * I personaggi originali della Foresta su ZX Spectrum:
 * Kloin l'elfo
 * Korleth il nano
 * Ankus il gigante
 */

public abstract class PersonaggioBase implements Personaggio {

	private PersonaggioMD md = new PersonaggioMD();

	private int ordinale;
	private boolean png;
	private boolean corrompibile;
	private boolean amichevole;
	
	private String nomeImmagine;
	private String nomeIcona;
	private int quantitaMassima = 1;

	public PersonaggioBase(PersonaggioMD personaggioMD) {
		this.md = personaggioMD;
	}
	
	public PersonaggioBase(ClassiPersonaggio classe) {
		md.setClasse(classe);
		png = true;
		md.setVivo(true);
		impostaValori();
		classe.setQuantitaMassima(quantitaMassima);
		if (isParteConValoriMassimi()) {
			md.setForza(md.getForzaMassima());
			md.setMagia(md.getMagiaMassima());
			md.setStanchezza(0);
		} else {
			md.setForza(md.getForzaMassima() / 2 + Random.getInt(md.getForzaMassima() / 2));
			md.setMagia(md.getMagiaMassima() / 2 + Random.getInt(md.getMagiaMassima() / 2));
			md.setValore(md.getValore() / 2 + Random.getInt(md.getValore() / 2));
			md.setCoraggio(md.getCoraggio() / 2 + Random.getInt(md.getCoraggio() / 2));
			md.setCarisma(md.getCarisma() / 2 + Random.getInt(md.getCarisma() / 2));
			md.setStanchezza(Random.getInt(5));
		}
		Logger.log("Nuovo: " + getNomeSingolare() + " (" + md.getForza() + "/" + md.getForzaMassima() + ")");
	}
	
	/**
	 * Un personaggio giocante (il giocatore o uno dei personaggi che si incontrano
	 * nelle locande)
	 */
	public PersonaggioBase(String nome, ClassiPersonaggio classe) {
		this(classe);
		md.setNome(nome);
		png = false;
	}
	
	/**
	 * Deve impostare forzaMassima, magiaMassima, valore, coraggio, carisma
	 */
	protected abstract void impostaValori();

	public ClassiPersonaggio getClasse() {
		return md.getClasse();
	}
	
	protected void setImmagine(String nomeImmagine) {
		this.nomeImmagine = nomeImmagine;
		if (ImageCache.get(nomeImmagine) == null) {
			ImageCache.set(nomeImmagine, BufferedImageBuilder.buildBufferedImage(nomeImmagine));
		}
	}
	
	public BufferedImage getImmagine() {
		return ImageCache.get(nomeImmagine);
	}

	protected void setIcona(String nomeIcona) {
		this.nomeIcona = nomeIcona;
	}

	public BufferedImage getIcona() {
		return ImageCache.get(nomeIcona);
	}

	protected void setQuantitaMassima(int quantitaMassima) {
		this.quantitaMassima = quantitaMassima;
	}
	
	public int getOrdinale() {
		return ordinale;
	}

	public void setOrdinale(int ordinale) {
		this.ordinale = ordinale;
	}

	public boolean isCorrompibile() {
		return corrompibile;
	}
	
	public void setCorrompibile(boolean corrompibile) {
		this.corrompibile = corrompibile;
	}
	
	public boolean isAmichevole() {
		return amichevole;
	}
	
	public void setAmichevole(boolean amichevole) {
		this.amichevole = amichevole;
	}

	public boolean isMagico() {
		return md.getMagiaMassima() > 0;
	}

	public int getRecuperoForza() {
		if (isPNG()) {
			return getForzaMassima() / 10;
		} else {
			return getForzaMassima() / 20;
		}
	}

	public int getRecuperoMagia() {
		return 1;
	}

	protected ClassiOfferta[] getOfferteAmicizia() {
		return new ClassiOfferta[0];
	}

	protected ClassiOfferta[] getOfferteCorruzione() {
		return new ClassiOfferta[0];
	}
	
	public boolean isImmuneAIncantesimo(ClassiIncantesimo classeIncantesimo) {
		return false;
	}
	
	public boolean isParteConValoriMassimi() {
		return false;
	}

	public boolean isPNG() {
		return png;
	}

	public String getNome() {
		return md.getNome();
	}

	public boolean isImmortale() {
		return false;
	}

	public boolean isVivo() {
		return md.isVivo();
	}

	public void muore(String causaTrapasso) {
		if (!isImmortale()) {
			md.setVivo(false);
			md.setCausaTrapasso(causaTrapasso);
		}
		UI.notificaMorte(this);
	}

	public String getCausaTrapasso() {
		return md.getCausaTrapasso();
	}

	public void resuscita() {
		md.setVivo(true);
		md.setCausaTrapasso(null);
		md.setForza(md.getForzaMassima() / 10);
		md.setStanchezza(9);
	}

	public int getForza() {
		int modificaDaArtefatti = 0;
		for (ArtefattoMD artefatto : md.getArtefatti()) {
			modificaDaArtefatti += artefatto.getForza();
		}
		return md.getForza() + modificaDaArtefatti;
	}

	public void addForza(int quantita) {
		int forza = md.getForza() + quantita;
		if (forza > md.getForzaMassima()) {
			forza = md.getForzaMassima();
		}
		md.setForza(forza);
		UI.variaForza(this, quantita);
	}

	public void subForza(int quantita, Personaggio avversario, Personaggio.NotificaFerite notificaFerite, Personaggio.NotificaMorte notificaMorte) {
		int modificaDaArtefatti = 0;
		for (ArtefattoMD artefatto : md.getArtefatti()) {
			modificaDaArtefatti += artefatto.getProtezione();
		}
		quantita -= modificaDaArtefatti;
		if (quantita <= 0) {
			if (notificaFerite == Personaggio.NotificaFerite.SI) {
				StringBuilder sb = new StringBuilder();
				if (md.getNome() == null) {
					String ads = getADS();
					sb.append(Character.toUpperCase(ads.charAt(0)));
					sb.append(ads.substring(1));
					sb.append(getNomeSingolare());
				} else {
					sb.append(md.getNome());
				}
				sb.append(" non ha riportato danni dall'attacco ");
				if (avversario.getNome() == null) {
					sb.append(avversario.getDeS());
					sb.append(avversario.getNomeSingolare());
				} else {
					sb.append("di ");
					sb.append(avversario.getNome());
				}
				sb.append('.');
				UI.notifica(sb.toString());
			}
			return;
		}

		int forza = md.getForza() - quantita;
		if (forza <= 0) {
			forza = 0;
			if (!isImmortale()) {
				md.setVivo(false);
				StringBuilder sb;
				if (notificaMorte == Personaggio.NotificaMorte.SI) {
					sb = new StringBuilder();
					if (md.getNome() == null) {
						boolean isGruppoAvversario;
						Gruppo gruppo;
						if (GruppoGiocatore.getIstanza().contiene(this)) {
							isGruppoAvversario = false;
							gruppo = GruppoGiocatore.getIstanza();
						} else {
							isGruppoAvversario = true;
							gruppo = GruppoAvversario.getIstanza();
						}
						if (gruppo.getNumeroPersonaggi() > 1 && isGruppoAvversario) {
							if (getSesso() == Personaggio.Sesso.MASCHIO)
								sb.append(Misc.getOrdinaleM(ordinale, true));
							else
								sb.append(Misc.getOrdinaleF(ordinale, true));
							sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
							sb.append(' ');
						} else {
							String ads = getADS();
							sb.append(Character.toUpperCase(ads.charAt(0)));
							sb.append(ads.substring(1));
						}
						sb.append(getNomeSingolare());
						sb.append(' ');
					} else {
						sb.append(md.getNome());
					}
					//sb.append(" non ce l'ha fatta, ed e' mort");
					sb.append(" e' mort");
					if (getSesso() == Personaggio.Sesso.MASCHIO)
						sb.append('o');
					else
						sb.append('a');
					sb.append(" per le ferite riportate.");
					UI.notifica(sb.toString());
					Logger.log("Notificata morte del personaggio");
				}
				if (avversario != null) {
					sb = new StringBuilder(getSesso() == Personaggio.Sesso.MASCHIO ? "Ucciso " : "Uccisa ");
					sb.append(avversario.getDa()).append(avversario.getNomeSingolare()).append('.');
					md.setCausaTrapasso(sb.toString());
				} else {
					md.setCausaTrapasso((getSesso() == Personaggio.Sesso.MASCHIO ? "Morto" : "Morta") + " per troppa codardia.");
				}
				muore(md.getCausaTrapasso());
			}
		} else {
			if (notificaFerite == Personaggio.NotificaFerite.SI) {
				StringBuilder sb = new StringBuilder();
				if (md.getNome() == null) {
					String ads = getADS();
					sb.append(Character.toUpperCase(ads.charAt(0)));
					sb.append(ads.substring(1));
					sb.append(getNomeSingolare());
				} else {
					sb.append(md.getNome());
				}
				sb.append(" ha ancora ").append(forza).append(" punt").append(forza == 1 ? 'o' : 'i')
						.append(" ferita su ").append(md.getForzaMassima()).append('.');
				UI.notifica(sb.toString());
			}
		}
		md.setForza(forza);
		UI.variaForza(this, -quantita);
	}

	protected void setForzaMassima(int forzaMassima) {
		md.setForzaMassima(forzaMassima);
	}

	public int getForzaMassima() {
		return md.getForzaMassima();
	}

	public void addForzaMassima(int quantita) {
		md.setForzaMassima(md.getForzaMassima() + quantita);
		UI.variaForzaMassima(this, quantita);
	}

	public int getDanniInCombattimento() {
		int danni;
		/*
		 * if (isPNG()) { // per i personaggi non giocanti facciamo una media // tra la
		 * forza e la forza massima danni = (getForza() + getForzaMassima()) / 10; }
		 * else {
		 */
		danni = (getForza() + getValore() + getCoraggio()) / 10 - getStanchezza() - Random.getInt(10);
		/*
		 * }
		 */
		Logger.log((getNome() == null ? getNomeSingolare() : getNome()) + "(" + getForza() + "/"
				+ getForzaMassima() + ") fa " + danni + " danni.");
		if (danni < 0) {
			return 0;
		} else {
			return danni * getModificaDanniForza();
		}
	}

	public int getModificaDanniForza() {
		return 1;
	}

	public int getMagia() {
		return md.getMagia();
	}

	public void addMagia(int quantita) {
		md.setMagia(Math.min(md.getMagia() + quantita, md.getMagiaMassima()));
		UI.variaMagia(this, quantita);
	}

	public void subMagia(int quantita) {
		md.setMagia(Math.max(md.getMagia() - quantita, 0));
		UI.variaMagia(this, -quantita);
	}

	protected void setMagiaMassima(int magiaMassima) {
		md.setMagiaMassima(magiaMassima);
	}
	
	public int getMagiaMassima() {
		return md.getMagiaMassima();
	}

	//TODO esiste un massimo per la magia?
	public void addMagiaMassima(int quantita) {
		md.setMagiaMassima(md.getMagiaMassima() + quantita);
		UI.variaMagiaMassima(this, quantita);
	}

	public int getBersagliPerIncantesimo() {
		int modificaDaArtefatti = 1;
		for (ArtefattoMD artefatto : md.getArtefatti()) {
			modificaDaArtefatti += artefatto.getBersagli();
		}
		return 1 + modificaDaArtefatti;
	}

	public int getModificaDanniMagia(int danniBase) {
		return danniBase;
	}
	
	protected void setCoraggio(int coraggio) {
		md.setCoraggio(coraggio);
	}

	public int getCoraggio() {
		int modificaDaArtefatti = 0;
		for (ArtefattoMD artefatto : md.getArtefatti()) {
			modificaDaArtefatti += artefatto.getForza();
		}
		return md.getCoraggio() + modificaDaArtefatti;
	}

	public void addCoraggio(int quantita) {
		md.setCoraggio(Math.min(md.getCoraggio() + quantita, 99));
		UI.variaCoraggio(this, quantita);
	}

	public void subCoraggio(int quantita) {
		md.setCoraggio(Math.max(md.getCoraggio() - quantita, 0));
		UI.variaCoraggio(this, -quantita);
	}

	protected void setValore(int valore) {
		md.setValore(valore);
	}
	
	public int getValore() {
		int modificaDaArtefatti = 0;
		for (ArtefattoMD artefatto : md.getArtefatti()) {
			modificaDaArtefatti += artefatto.getValore();
		}
		return md.getValore() + modificaDaArtefatti;
	}

	public void addValore(int quantita) {
		md.setValore(Math.min(md.getValore() + quantita, 99));
		UI.variaValore(this, quantita);
	}

	public void subValore(int quantita) {
		md.setValore(Math.max(md.getValore() - quantita, 0));
		UI.variaValore(this, -quantita);
	}

	public int getStanchezza() {
		int modificaDaArtefatti = 0;
		for (ArtefattoMD artefatto : md.getArtefatti()) {
			modificaDaArtefatti += artefatto.getStanchezza();
		}
		return Math.max(md.getStanchezza() - modificaDaArtefatti, 0);
	}

	public void addStanchezza(int quantita) {
		md.setStanchezza(Math.min(md.getStanchezza() + quantita, 9));
		UI.variaStanchezza(this, quantita);
	}

	public void subStanchezza(int quantita) {
		md.setStanchezza(Math.max(md.getStanchezza() - quantita, 0));
		UI.variaStanchezza(this, -quantita);
	}

	protected void setCarisma(int carisma) {
		md.setCarisma(carisma);
	}
	
	public int getCarisma() {
		return md.getCarisma();
	}

	public void addCarisma(int quantita) {
		md.setCarisma(Math.min(md.getCarisma() + quantita, 10));
		UI.variaCarisma(this, quantita);
	}

	public void subCarisma(int quantita) {
		md.setCarisma(Math.max(md.getCarisma() - quantita, 0));
		UI.variaCarisma(this, -quantita);
	}

	public int getBersagli() {
		int modificaDaArtefatti = 0;
		for (ArtefattoMD artefatto : md.getArtefatti()) {
			modificaDaArtefatti += artefatto.getBersagli();
		}
		return 1 + modificaDaArtefatti;
	}

	public String getDescrizione() {
		StringBuilder sb = new StringBuilder();
		if (md.getNome() != null) {
			sb.append(md.getNome());
		} else {
			String s = getADS();
			sb.append(Character.toUpperCase(s.charAt(0)));
			sb.append(s.substring(1));
			sb.append(getNomeSingolare());
		}
		sb.append(' ');
		int forza = md.getForza();
		if (forza < 20) {
			sb.append("e' molto debole");
		} else if (forza < 40) {
			sb.append("e' debole");
		} else if (forza < 80) {
			sb.append("non e' molto forte");
		} else if (forza < 100) {
			sb.append("e' forte");
		} else if (forza < 200) {
			sb.append("e' molto forte");
		} else if (forza < 400) {
			sb.append("e' davvero forte");
		} else {
			sb.append("ha una forza enorme");
		}
		sb.append(", ");
		int coraggio = md.getCoraggio();
		if (coraggio < 30) {
			sb.append("non ha molto coraggio");
		} else if (coraggio < 60) {
			sb.append("ha coraggio");
		} else {
			sb.append("ha coraggio da vendere");
		}
		sb.append(", nei combattimenti ");
		int valore = md.getValore();
		if (valore < 30) {
			sb.append("non e' che se la cavi egregiamente");
		} else if (valore < 60) {
			sb.append("se la cava bene");
		} else {
			sb.append("puo' arrecare gravi danni");
		}
		sb.append(" e ");
		int carisma = md.getCarisma();
		if (carisma < 3) {
			sb.append("non ha molto carisma");
		} else if (carisma < 6) {
			sb.append("ha abbastanza carisma");
		} else {
			sb.append("ha molto carisma");
		}
		sb.append('.');
		List<ArtefattoMD> artefatti = md.getArtefatti();
		if (!artefatti.isEmpty()) {
			sb.append(' ');
			sb.append(getPronome());
			sb.append(' ');
			ArtefattoMD a;
			for (int i = 0; i < artefatti.size(); i++) {
				if (i > 0) {
					sb.append(',');
				}
				a = artefatti.get(i);
				sb.append(a.getUtilizzo()).append(' ').append(a.getNome()).append(", ").append(a.getDescrizione());
			}
			sb.append('.');
		}
		return sb.toString();
	}

	public void riposa(int ore, boolean alCoperto) {
		int modifica;
		// via la stanchezza
		if (alCoperto) {
			md.setStanchezza(0);
		} else {
			modifica = md.getStanchezza() / 2;
			for (ArtefattoMD artefatto : md.getArtefatti()) {
				modifica += artefatto.getStanchezza();
			}
			subStanchezza(modifica);
		}
		// accresce la forza
		modifica = 0;
		for (ArtefattoMD artefatto : md.getArtefatti()) {
			modifica += artefatto.getForza();
		}
		if (ore < 4) {
			if (alCoperto) {
				addForza(getRecuperoForza() * ore + modifica);
			}
		} else {
			if (alCoperto) {
				addForza(100 + modifica);
			} else {
				int i = getRecuperoForza() * ore;
				if (i > 100) {
					i = 100;
				}
				addForza(i + modifica);
			}
		}
		// torna la magia
		modifica = 0;
		for (ArtefattoMD artefatto : md.getArtefatti()) {
			modifica += artefatto.getMagia();
		}
		addMagia(getRecuperoMagia() * ore + modifica);
	}

	public void fugge() {
		subCoraggio(Random.getInt(10) + 10);
		subForza(Random.getInt(50) + 50, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.SI);
		subCarisma(1);
	}

	public void addArtefatto(Artefatto a) {
		md.getArtefatti().add(a.getModelloDati());
	}

	public void attacca(Personaggio bersaglio) {
		Logger.log("Contrattacco avversario");
		Incantesimo incantesimoScelto = null;
		if (isMagico() && getMagia() > 0) {
			Logger.log("Avversario magico, scelgo incantesimo");
			for (ClassiIncantesimo classeIncantesimo : ClassiIncantesimo.values()) {
				Incantesimo incantesimoCorrente = classeIncantesimo.getIstanza();
				if (getMagia() >= incantesimoCorrente.getCostoLancio() && incantesimoCorrente.getTipo() == TipoIncantesimo.MALEFICO && (incantesimoScelto == null || Random.getInt(2) == 1)) {
					incantesimoScelto = incantesimoCorrente;
				}
			}
		}
		if (incantesimoScelto != null) {
			StringBuilder sb = new StringBuilder("Un ").append(incantesimoScelto.getNomeSingolare())
					.append(" viene formulato contro ");
			if (incantesimoScelto.getPortata() == PortataIncantesimo.GRUPPO && GruppoGiocatore.getIstanza().getNumeroPersonaggiVivi() > 1) {
				sb.append("il gruppo");
			} else {
				if (bersaglio.isPNG()) {
					sb.append(bersaglio.getADS()).append(bersaglio.getNomeSingolare());				
				} else {
					sb.append(bersaglio.getNome());
				}
			}
			sb.append('.');
			UI.notifica(sb.toString());
			incantesimoScelto.formula(this, bersaglio, null);
		} else {
			String s;
			StringBuilder sb = new StringBuilder();
			s = getNome();
			if (s == null) {
				if (GruppoAvversario.getIstanza().getNumeroPersonaggiVivi() == 1) {
					s = getADS();
				} else {
					s = getAIS();
				}
				sb.append(Character.toUpperCase(s.charAt(0))).append(s.substring(1)).append(getNomeSingolare());
			} else {
				sb.append(s);
			}
			sb.append(" attacca ").append(bersaglio.getNome()).append('.');
			UI.notifica(sb.toString());
			int danno = getDanniInCombattimento();
			bersaglio.subForza(danno, this, Personaggio.NotificaFerite.SI, Personaggio.NotificaMorte.SI);
		}
	}

	public void attacca(Gruppo gruppoBersaglio) {
		List<Personaggio> personaggiPossibili = gruppoBersaglio.getPersonaggiVivi();
		Personaggio bersaglio = null;
		for (Personaggio personaggio : personaggiPossibili) {
			if (personaggio.getClasse() == ClassiPersonaggio.MAGA || personaggio.getClasse() == ClassiPersonaggio.MAGO) {
				bersaglio = personaggio;
				break;
			}
		}
		if (bersaglio == null) {
			for (Personaggio personaggio : personaggiPossibili) {
				if (personaggio.getClasse() == ClassiPersonaggio.ELFA || personaggio.getClasse() == ClassiPersonaggio.ELFO) {
					bersaglio = personaggio;
					break;
				}
			}
		}
		if (bersaglio == null) {
			for (Personaggio personaggio : personaggiPossibili) {
				if (bersaglio == null || Random.getInt(2) == 1) {
					bersaglio = personaggio;
				}
			}
		}
		attacca(bersaglio);
	}

	public void setTempo(int tempo) {
		md.setTempo(tempo);
	}

	public int decrementaTempo() {
		int tempo = md.getTempo();
		if (!md.isVivo() || tempo == Personaggio.NO_TEMPO) {
			return tempo;
		}
		tempo--;
		md.setTempo(tempo);
		UI.variaTempo(this, -1);
		return tempo;
	}

	public Offerta getOfferta(Comando azione) {
		ClassiOfferta[] offerte = null;
		if (isAmichevole() && azione == Comando.AMICIZIA) {
			offerte = getOfferteAmicizia();
		} else {
			offerte = getOfferteCorruzione();
		}
		if (offerte.length > 0) {
			return offerte[Random.getInt(offerte.length)].getIstanza();
		}
		return null;
	}
	
	public PersonaggioMD getModelloDati() {
		return md;
	}
	
	public void setModelloDati(PersonaggioMD personaggioMD) {
		this.md = personaggioMD;
		png = false;
	}
}
