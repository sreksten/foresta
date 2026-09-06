package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.EventoMortePersonaggio;
import com.threeamigos.foresta.eventi.EventoVariazioneStatistichePersonaggio;
import com.threeamigos.foresta.incantesimi.ClassiIncantesimo;
import com.threeamigos.foresta.incantesimi.Incantesimo;
import com.threeamigos.foresta.incantesimi.PortataIncantesimo;
import com.threeamigos.foresta.incantesimi.TipoIncantesimo;
import com.threeamigos.foresta.motore.*;
import com.threeamigos.foresta.motore.modellodati.*;
import com.threeamigos.foresta.offerte.ClassiOfferta;
import com.threeamigos.foresta.offerte.Offerta;
import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.tools.CostruttoreArtefatto;
import com.threeamigos.foresta.tools.Misc;
import com.threeamigos.foresta.ui.BufferedImageBuilder;
import com.threeamigos.foresta.ui.ImageCache;
import com.threeamigos.foresta.ui.UI;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/*
 * I personaggi originali della Foresta su ZX Spectrum:
 * Kloin l'elfo
 * Korleth il nano
 * Ankus il gigante
 */

public abstract class PersonaggioBase implements Personaggio {

	protected PersonaggioMD md = new PersonaggioMD();

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

	public PersonaggioBase(ClassePersonaggio classe) {
		this(classe, 1);
	}

	public PersonaggioBase(ClassePersonaggio classe, int livello) {
		md.setClasse(classe);
		png = true;
		Function<Integer, Integer> funzionePerValoriIniziali;
		if (isParteConValoriMassimi()) {
			funzionePerValoriIniziali = val -> val;
		} else {
			funzionePerValoriIniziali = (max) -> Dado.tiraAncheSenzaRange(max * 3 / 4, max);
		}
		//Questo imposta salure e magia
		impostaValoriDiPartenzaGenerali(funzionePerValoriIniziali, livello);

		//Queste impostano il resto - LanciatoreDeiDadi sovrascrive
		impostaValoriDiPartenza(funzionePerValoriIniziali);
		if (!isParteConValoriMassimi()) {
			LanciatoreDeiDadi.tiraDadiPer(classe, getLivello(), md);
		}

		ricalcolaAttributiSecondari();
		classe.setQuantitaMassima(quantitaMassima);
		Logger.log(String.format("Nuovo: %17s - Livello: %2d, Salute: %3d/%3d; Magia: %3d/%3d; Forza: %3d; Destrezza: %3d; Costituzione: %3d; Intelligenza: %3d; Saggezza: %3d; Carisma: %3d; Fortuna: %3d",
				getNomeSingolare(), getLivello(), getSalute(), getSaluteMassima(), getMagia(), getMagiaMassima(),
				getForza(), getDestrezza(), getCostituzione(), getIntelligenza(), getSaggezza(), getCarisma(), getFortuna()));
	}

	/**
	 * Un personaggio giocante (il giocatore o uno dei personaggi che si incontrano
	 * nelle locande)
	 */
	public PersonaggioBase(String nome, ClassePersonaggio classe, int livello) {
		this(classe, livello);
		md.setNome(nome);
		png = false;
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
		return getLivellamentoMagia() > 0.0d;
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

	public boolean isImmortale() {
		return false;
	}

	public boolean isVivo() {
		return md.isVivo();
	}

	public void muore(String causaTrapasso) {
		if (isImmortale()) {
			return;
		}
		md.setVivo(false);
		md.setCausaTrapasso(causaTrapasso);
		BusEventi.pubblica(new EventoMortePersonaggio(this, causaTrapasso));
		//FIXME da levare quando si passa alla notifica via bus
		UI.notificaMorte(this);
	}

	public String getCausaTrapasso() {
		return md.getCausaTrapasso();
	}

	public void resuscita() {
		md.setVivo(true);
		md.setCausaTrapasso(null);
		md.setSalute((int)(calcolaSaluteMassima() / 10.0d));
		md.setStanchezza(9);
		BusEventi.pubblica(new EventoVariazioneStatistichePersonaggio(this, TipoAttributo.SALUTE, getSalute()));
		BusEventi.pubblica(new EventoVariazioneStatistichePersonaggio(this, TipoAttributo.STANCHEZZA, getStanchezza()));
	}

	//FIXME metodo da rimuovere quando passiamo al nuovo motore di combattimento
	public int getDanniInCombattimento() {

		double danni = Math.max(0, (getSalute() + getCoraggio()) / 10 + getValore() - getStanchezza() - Dado.tira(-5, +5));
		danni = danni * getMoltiplicatoreDanniFisici();

		Logger.log((getNome(OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE, OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE)) + " (" + getSalute() + "/"
				+ getSaluteMassima() + ") fa " + danni + " danni.");
		return (int)danni;
	}

	public int getBersagli() {
		double numeroBersagli = getQuantitaModificata(1 * getMoltiplicatoreNumeroBersagli(), TipoAttributo.NUMERO_BERSAGLI);
		return (int)(Math.min(1, numeroBersagli));
	}

	/**
	 * Prende il valore base dei danni di un incantesimo e lo moltiplica per il moltiplicatore di danni magia
 	 */
	public int getModificaDanniMagia(int danniBase) {
		double danniModificati = getQuantitaModificata(danniBase, TipoAttributo.MAGIA);
		return (int)danniModificati;
	}

	/**
	 * Calcola i Punti Vita (HP) rigenerati durante un turno di riposo.
	 */
	public int getRecuperoSalute() {

		/* Metodologia precedente:
		if (isPNG()) {
			return getSaluteMassima() / 10 + getModificaDaArtefatti(TipoAttributo.SALUTE);
		} else {
			return getSaluteMassima() / 20;
		}
		 */

		// Se la creatura è un non-morto o uno spettro, il moltiplicatore è 0.0, quindi guarisce 0
		if (getMoltiplicatoreRecuperoFisico() == 0.0) {
			return 0;
		}

		// 1. Base di partenza mista
		double baseGrezza = getQuantitaModificata(5.0d, TipoAttributo.RIGENERAZIONE_SALUTE) + (getSaluteMassima() * 0.05);

		// 2. Impatto dell'attributo Costituzione con Diminishing Returns
		double bonusCostituzione = 1.0 + (Math.sqrt(getCostituzione()) / 10.0);

		// 3. Calcolo finale combinato con il moltiplicatore di archetipo
		double saluteFinale = (baseGrezza * bonusCostituzione) * getMoltiplicatoreRecuperoFisico();

		// Arrotondamento a un decimale per la UI
		return (int)Math.ceil(saluteFinale);
	}

	/**
	 * Calcola il mana rigenerato durante un turno di riposo.
	 */
	public int getRecuperoMagia() {
		// 1. Calcolo del recupero potenziale basato solo sulla capienza massima
		double recuperoGrezzo = getQuantitaModificata(5.0d, TipoAttributo.RIGENERAZIONE_MAGIA) + getMagiaMassima() * 0.05d;

		// 2. Applicazione del moltiplicatore di classe/razza
		double manaRigenerato = recuperoGrezzo * getMoltiplicatoreRecuperoMagico();

		// Arrotondamento a un decimale per l'interfaccia utente (UI)
		return (int)Math.ceil(manaRigenerato);
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
		int salute = getSalute();
		if (salute < 20) {
			sb.append("è molto debole");
		} else if (salute < 40) {
			sb.append("è debole");
		} else if (salute < 80) {
			sb.append("non è molto in salute");
		} else if (salute < 100) {
			sb.append("è in salute");
		} else if (salute < 200) {
			sb.append("è assolutamente in salute");
		} else if (salute < 400) {
			sb.append("è davvero in salute");
		} else {
			sb.append("è san").append(getLetteraFinaleAttributo()).append(" come un pesce");
		}
		sb.append(", ");
		int coraggio = getCoraggio();
		if (coraggio < 30) {
			sb.append("non ha molto coraggio");
		} else if (coraggio < 60) {
			sb.append("ha coraggio");
		} else {
			sb.append("ha coraggio da vendere");
		}
		sb.append(", nei combattimenti ");
		int valore = getValore();
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
		Collection<ArtefattoMD> artefatti = md.getArtefatti();
		if (!artefatti.isEmpty()) {
			sb.append(' ');
			sb.append(getPronome());
			sb.append(' ');
			sb.append(artefatti.stream()
					.map(a -> a.getTipo().getUtilizzo() + ' ' + a.getNome() + ", " + a.getDescrizione())
					.collect(Collectors.joining(", ")));
			sb.append('.');
		}
		return sb.toString();
	}

	@Override
	public void riposa(int ore, TipoRiposo tipoRiposo) {
		RisultatoRiposo risultatoRiposo = CalcolatoreRiposo.calcolaRiposo(this, ore, tipoRiposo);
		addSalute(risultatoRiposo.getRipristinoSalute());
		addMagia(risultatoRiposo.getRipristinoMagia());
		subStanchezza(risultatoRiposo.getAbbassamentoStanchezza());
	}

	public void fugge() {
		subCoraggio(Dado.tira(10, 20));
		subSalute(Dado.tira(50, 100), null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.SI);
		subCarisma(1);
	}

	public void attacca(Personaggio bersaglio) {
		Logger.log(getNome() + " attacca " + bersaglio.getNome());
		Incantesimo incantesimoScelto = null;
		if (isMagico() && getMagia() > 0) {
			Logger.log("Avversario magico, scelgo incantesimo");
			for (ClassiIncantesimo classeIncantesimo : ClassiIncantesimo.values()) {
				Incantesimo incantesimoCorrente = classeIncantesimo.getIstanza();
				if (getMagia() >= incantesimoCorrente.getCostoLancio() && incantesimoCorrente.getTipo() == TipoIncantesimo.MALEFICO && (incantesimoScelto == null || Dado.tira(2) == 1)) {
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
				sb.append(bersaglio.getNome(OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE));
			}
			sb.append('.');
			UI.notifica(sb.toString());
			incantesimoScelto.formula(this, bersaglio, null);
		} else {
			OpzioniGetNome articoloDaIncludere = GruppoAvversario.getIstanza().getNumeroPersonaggiVivi() == 1 ?
					OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE :
					OpzioniGetNome.INCLUDI_ARTICOLO_INDETERMINATIVO_SINGOLARE;
            String sb = getNome(articoloDaIncludere, OpzioniGetNome.INIZIALE_MAIUSCOLA) +
                    " attacca " + bersaglio.getNome(OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE) + '.';
			UI.notifica(sb);
			int danno = getDanniInCombattimento();

			// Test per nuovo motore combattimento

			Logger.log("---------- NUOVO MOTORE ----------");
			Logger.log("Valutazione danno originale: " + danno);
			boolean colpirebbe = CalcolatoreCombattimento.colpisce(this, bersaglio);
			if (colpirebbe) {
				Artefatto arma = CostruttoreArtefatto.istanza()
						.setTipo(TipoArtefatto.ASCIA)
						.setNome("il budello di tu' ma' vestito da spada leggendaria")
						.setDescrizione("si presta bene a picchiare")
						.setLivello(1)
						.setDanniBase(5)
						.setCostoAcquisto(15)
						.setPeso(2)
						.setModificatore(TipoAttributo.FORZA, TipoModificatore.AUMENTO_PERCENTUALE, 5)
						.costruisci();
                RisultatoCombattimento risultato = CalcolatoreCombattimento.calcolaDannoFinale(this, bersaglio, TipoDanno.TAGLIENTE, arma);
				UI.notifica("Con nuovo motore colpirebbe assegnando " + risultato.getDannoTotale() + " danni");
			} else {
				UI.notifica("Con nuovo motore " + getNome() + " non colpisce " + bersaglio.getNome());
			}

			bersaglio.subSalute(danno, this, Personaggio.NotificaFerite.SI, Personaggio.NotificaMorte.SI);
		}
	}

	public void attacca(Gruppo gruppoBersaglio) {
		List<Personaggio> personaggiPossibili = gruppoBersaglio.getPersonaggiVivi();
		Personaggio bersaglio = null;
		for (Personaggio personaggio : personaggiPossibili) {
			if (personaggio.getClasse() == ClassePersonaggio.MAGA || personaggio.getClasse() == ClassePersonaggio.MAGO) {
				bersaglio = personaggio;
				break;
			}
		}
		if (bersaglio == null) {
			for (Personaggio personaggio : personaggiPossibili) {
				if (personaggio.getClasse() == ClassePersonaggio.ELFA || personaggio.getClasse() == ClassePersonaggio.ELFO) {
					bersaglio = personaggio;
					break;
				}
			}
		}
		if (bersaglio == null) {
			for (Personaggio personaggio : personaggiPossibili) {
				if (bersaglio == null || Dado.tira(2) == 1) {
					bersaglio = personaggio;
				}
			}
		}
		if (bersaglio == null) {
			Logger.log(getNome() + " non ha nemici da attaccare!");
			return;
		}
		attacca(bersaglio);
	}

	public boolean isATempo() {
		return md.getTempo() != PersonaggioMD.SENZA_LIMITE;
	}

	public void setTempo(int tempo) {
		md.setTempo(tempo);
	}

	public int decrementaTempo() {
		int tempo = md.getTempo();
		if (!md.isVivo() || !isATempo()) {
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
			int indice = Dado.tiraAncheAUnaFaccia(offerte.length) - 1;
			return offerte[indice].getIstanza();
		}
		return null;
	}
	
	public PersonaggioMD getModelloDati() {
		return md;
	}
	
	public void setModelloDati(PersonaggioMD personaggioMD) {
		this.md = personaggioMD;
		png = false;
		ricalcolaAttributiSecondari();
	}

	// Statistiche del personaggio

	// CLASSE
	@Override
	public ClassePersonaggio getClasse() {
		return md.getClasse();
	}

	// NOME

	@Override
	public Optional<String> getNomeProprio() {
		return Optional.ofNullable(md.getNome());
	}

	@Override
	public String getNome(Personaggio.OpzioniGetNome... opzioni) {
		boolean includiArticoloDeterminativoSingolare = false;
		boolean includiArticoloIndeterminativoSingolare = false;
		boolean includiPreposizioneArticolata = false;
		boolean inizialeMaiuscola = false;
		for (Personaggio.OpzioniGetNome opzione : opzioni) {
			if (opzione == Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE) {
				includiArticoloDeterminativoSingolare = true;
			} else if (opzione == Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_INDETERMINATIVO_SINGOLARE) {
				includiArticoloIndeterminativoSingolare = true;
			} else if (opzione == Personaggio.OpzioniGetNome.INCLUDI_PREPOSIZIONE_ARTICOLATA) {
				includiPreposizioneArticolata = true;
			} else if (opzione == Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA) {
				inizialeMaiuscola = true;
			}
		}
		String nome = md.getNome();
		if (nome != null) {
			return includiPreposizioneArticolata ? " di " + nome : nome;
		} else if (!isPNG()) {
			throw new IllegalStateException("Personaggio giocante senza nome!");
		}
		StringBuilder sb = new StringBuilder();
		if (includiArticoloDeterminativoSingolare) {
			sb.append(getADS());
		} else if (includiArticoloIndeterminativoSingolare) {
			sb.append(getAIS());
		} else if (includiPreposizioneArticolata) {
			sb.append(getDeS());
		}
		sb.append(getNomeSingolare());
		if (inizialeMaiuscola) {
			sb.replace(0, 1, sb.substring(0, 1).toUpperCase());
		}
		return sb.toString();
	}

	@Override
	public String getLetteraFinaleAttributo() {
		return getSesso() == Personaggio.Sesso.MASCHIO ? "o" : "a";
	}

	/**
	 * Da chiamare durante l'inizializzazione di un personaggio. Imposta il personaggio a vivo, il livello a 1
	 * e l'esperienza e il carico a 0. Inoltre in base ai valori massimi degli attributi primari imposta quelli
	 * correnti per gli attributi primari e calcola gli attributi secondari.
	 * @param funzione una funzione che determina il valore per un attributo. O il massimo valore possibile
	 *                    o un valore scelto a caso in un dato intervallo.
	 */
	private void impostaValoriDiPartenzaGenerali(Function<Integer, Integer> funzione, int livello) {
		md.setVivo(true);
		md.setLivello(livello);
		md.setEsperienza(0);
		md.setSalute(funzione.apply(getSaluteMassima()));
		md.setMagia(funzione.apply(getMagiaMassima()));
		md.setStanchezza(Costanti.MAX_STANCHEZZA - funzione.apply(Costanti.MAX_STANCHEZZA));
	}

	protected abstract void impostaValoriDiPartenza(Function<Integer, Integer> funzione);

	protected int getAttributoAdeguatoALivello(double valoreBase) {
		/*
		 * Applichiamo il metodo Curva con radice e spostamento.
		 * valore = valoreBase + moltiplicatore * (radice(livello + 3) - 2)
		 */
		double moltiplicatore = valoreBase / 2.0d;

		return (int)(valoreBase + moltiplicatore * (Math.sqrt(getLivello() + 3) - 2));
	}

	/**
	 * Funzione che limita il valore di un attributo al suo massimo (valore base più modifica da artefatti)
	 */
	private int limitaEntroMassimi(int valoreAttuale, int valoreMassimo, int quantitaDaAggiungere) {
		if (valoreMassimo == PersonaggioMD.SENZA_LIMITE) {
			return quantitaDaAggiungere;
		}
		if (valoreAttuale + quantitaDaAggiungere <= valoreMassimo) {
			return quantitaDaAggiungere;
		}
		return valoreMassimo - valoreAttuale;
	}

	// LIVELLO - non è un attributo con un massimo

	public int getLivello() {
		return md.getLivello();
	}

	// ESPERIENZA - non è un attributo con un massimo

	public int getEsperienza() {
		return md.getEsperienza();
	}

	//FIXME aggiungendo esperienza si dovrebbe poter salire di livello e aumentare alcune statistiche
	public void addPuntiEsperienza(int esperienza) {
		md.setEsperienza(md.getEsperienza() + esperienza);

		// Verifichiamo se i nuovi XP accumulati determinano un salto di livello
		int livelloAttuale = md.getLivello();
		int nuovoLivello = GestoreProgressione.calcolaLivelloDaXp(md.getEsperienza());

		if (nuovoLivello > livelloAttuale) {
			int differenza = nuovoLivello - livelloAttuale;
			md.setLivello(nuovoLivello);
			UI.notifica("LEVELED UP! Ora " + getNome(OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE) + " è al livello " + nuovoLivello + "!");
			UI.variaLivello(this, differenza);
			// QUI PUOI AGGANCIARE IL CODICE PRECEDENTE:
			// 1. Ricalcola il nuovo budget di punti primari (con la tolleranza del 5%)
			// 2. Aggiorna le statistiche nel modello md.setForza(...), ecc.
			// 3. Ricalcola i valori derivati come Carico, Critico, Velocità.
		}
	}

	@Override
	public int getPuntiAbilitaDisponibili() {
		return md.getPuntiAbilitaDisponibili();
	}

	// CARICO

	@Override
	public double getCarico() {
		return md.getArtefatti().stream().mapToDouble(ArtefattoMD::getPeso).sum();
	}

	public boolean puoPrendere(Artefatto artefatto) {
		return puoPrendere(artefatto.getPeso());
	}

	public boolean puoPrendere(double quantita) {
		return quantita <= calcolaCaricoMassimo() - getCarico();
	}

	// CARICO MASSIMO

	@Override
	public int getCaricoMassimo() {
		return (int)calcolaCaricoMassimo();
	}

	/**
	 * Calcola il carico massimo basandosi UNICAMENTE sulle statistiche primarie
	 * e sul moltiplicatore della classe, mantenendo i rendimenti decrescenti.
	 */
	private double calcolaCaricoMassimo() {

		if (getMoltiplicatoreCarico() == 0.0) {
			// Fantasmi vari
			return 0.0;
		}

		// Per calcolare il CARICO massimo trasportabile in modo realistico, si attinge a due attributi primari fisici:
		// FORZA (Peso Maggiore): La potenza muscolare determina la capacità di sollevare oggetti pesanti.
		// COSTITUZIONE (Peso Minore): La struttura fisica e la tempra determinano la tolleranza a camminare a lungo
		// sotto sforzo senza affaticarsi.
		final double PESO_PER_RADICE_FORZA = 12.0;
		final double PESO_PER_RADICE_COSTITUZIONE = 6.0;

		// Applichiamo i diminishing returns grezzi tramite radice quadrata
		double potenzaFisica = PESO_PER_RADICE_FORZA * Math.sqrt(getForza());
		double resistenzaFisica = PESO_PER_RADICE_COSTITUZIONE * Math.sqrt(getCostituzione());

		// Il potenziale di carico totale del corpo
		double potenziale = potenzaFisica + resistenzaFisica;

        return getQuantitaModificata(potenziale, TipoAttributo.CARICO_MASSIMO);
	}

	// SALUTE

	@Override
	public int getSalute() {
		return md.getSalute();
	}

	/**
	 * La quantità che si può aggiungere è limitata dal valore massimo di base accresciuto da bonus da artefatti
	 */
	public void addSalute(int quantita) {
		quantita = limitaEntroMassimi(md.getSalute(), getSaluteMassima(), quantita);
		md.setSalute(md.getSalute() + quantita);
		UI.variaSalute(this, quantita);
	}

	//FIXME sono convinto che questo metodo sia un po' troppo un pout-pourri
	public void subSalute(int quantita, Personaggio avversario, Personaggio.NotificaFerite notificaFerite, Personaggio.NotificaMorte notificaMorte) {
		if (quantita <= 0) {
			if (notificaFerite == Personaggio.NotificaFerite.SI) {
				String sb = getNome(OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE, OpzioniGetNome.INIZIALE_MAIUSCOLA) +
						" non ha riportato danni dall'attacco " +
						avversario.getNome(OpzioniGetNome.INCLUDI_PREPOSIZIONE_ARTICOLATA) +
						'.';
				UI.notifica(sb);
			}
			return;
		}

		int salute = md.getSalute() - quantita;
		if (salute <= 0) {
			salute = 0;
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
							if (getSesso() == Personaggio.Sesso.MASCHIO) {
								sb.append(Misc.getOrdinaleM(ordinale, true));
							} else {
								sb.append(Misc.getOrdinaleF(ordinale, true));
							}
							sb.append(" ");
							sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
						} else {
							String ads = getADS();
							sb.append(Character.toUpperCase(ads.charAt(0)));
							sb.append(ads.substring(1));
						}
						sb.append(getNomeSingolare());
					} else {
						sb.append(md.getNome());
					}
					sb.append(" è mort");
					sb.append(getLetteraFinaleAttributo());
					sb.append(" per le ferite riportate.");
					UI.notifica(sb.toString());
					Logger.log("Notificata morte del personaggio");
				}
				if (avversario != null) {
					md.setCausaTrapasso("Uccis" + getLetteraFinaleAttributo() + " " + avversario.getDa() + avversario.getNomeSingolare() + ".");
				} else {
					md.setCausaTrapasso("Mort" + getLetteraFinaleAttributo() + " per troppa codardia.");
				}
				muore(md.getCausaTrapasso());
			}
		} else {
			if (notificaFerite == Personaggio.NotificaFerite.SI) {
				String nome = getNome(OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE, OpzioniGetNome.INIZIALE_MAIUSCOLA);
				String notifica = nome + " ha ancora " + salute + " punt" + (salute == 1 ? 'o' : 'i') +
						" ferita su " + (int)calcolaSaluteMassima() + '.';
				UI.notifica(notifica);
			}
		}
		md.setSalute(salute);
		UI.variaSalute(this, -quantita);

		if ((md.getClasse() == ClassePersonaggio.GUERRIERO || md.getClasse() == ClassePersonaggio.GUERRIERA) &&
				getFuria() > 0 && !hasEffettoDiStato(TipoEffettoDiStato.BERSERK)) {
			double sogliaBerserk = calcolaSaluteMassima() / 3.0d;
			if (salute > 0 && salute <= sogliaBerserk) {
				addEffettoDiStato(TipoEffettoDiStato.BERSERK, 1);
			}
		}
	}

	// SALUTE MASSIMA

	/**
	 * La quantità di base accresciuta da bonus da artefatti e altri modificatori
	 */
	@Override
	public int getSaluteMassima() {
		return (int)calcolaSaluteMassima();
	}

	protected double calcolaSaluteMassima() {
		double saluteMassima = getSaluteBase() + getLivellamentoSalute() * Math.sqrt(getLivello() - 1);
		saluteMassima = getQuantitaModificata(saluteMassima, TipoAttributo.SALUTE);
		return saluteMassima;
	}

	public void addSaluteMassima(int quantita, String note) {
		md.getModificatori().add(new ModificatoreAttributo(TipoAttributo.SALUTE, TipoModificatore.AUMENTO_FISSO, quantita, note));
		UI.variaSaluteMassima(this, quantita);
	}

	// MAGIA

	@Override
	public int getMagia() {
		return md.getMagia();
	}

	/**
	 * La quantità che si può aggiungere è limitata dal valore massimo di base accresciuto da bonus da artefatti
	 */
	public void addMagia(int quantita) {
		quantita = limitaEntroMassimi(md.getMagia(), getMagiaMassima(), quantita);
		md.setMagia(md.getMagia() + quantita);
		UI.variaMagia(this, quantita);
	}

	public void subMagia(int quantita) {
		if (quantita > md.getMagia()) {
			throw new IllegalStateException("Tentativo di utilizzo di più magia rispetto a quella disponibile");
		}
		md.setMagia(md.getMagia() - quantita);
		UI.variaMagia(this, -quantita);
	}

	@Override
	public int getMagiaMassima() {
		return (int)calcolaMagiaMassima();
	}

	@Override
	public void addMagiaMassima(int quantita, String note) {
		md.getModificatori().add(new ModificatoreAttributo(TipoAttributo.MAGIA, TipoModificatore.AUMENTO_FISSO, quantita, note));
		UI.variaMagiaMassima(this, quantita);
	}

	protected double calcolaMagiaMassima() {
		if (getLivellamentoMagia() == 0.0d) {
			return 0.0d;
		}
		double magiaMassima = getMagiaBase() + getLivellamentoMagia() * Math.sqrt(getLivello() - 1);
		magiaMassima = getQuantitaModificata(magiaMassima, TipoAttributo.MAGIA);
		return magiaMassima;
	}

	// FORZA

	@Override
	public int getForza() {
		return get(PersonaggioMD::getForza, TipoAttributo.FORZA);
	}

	// DESTREZZA

	@Override
	public int getDestrezza() {
		return get(PersonaggioMD::getDestrezza, TipoAttributo.DESTREZZA);
	}

	public void addDestrezza(int quantita) {
		md.setDestrezza(md.getDestrezza() + quantita);
	}

	// COSTITUZIONE

	@Override
	public int getCostituzione() {
		return get(PersonaggioMD::getCostituzione, TipoAttributo.COSTITUZIONE);
	}

	public void addCostituzione(int quantita) {
		md.setCostituzione(md.getCostituzione() + quantita);
	}

	// INTELLIGENZA

	@Override
	public int getIntelligenza() {
		return get(PersonaggioMD::getIntelligenza, TipoAttributo.INTELLIGENZA);
	}

	public void addIntelligenza(int quantita) {
		md.setIntelligenza(md.getIntelligenza() + quantita);
	}

	// SAGGEZZA

	@Override
	public int getSaggezza() {
		return get(PersonaggioMD::getSaggezza, TipoAttributo.SAGGEZZA);
	}

	public void addSaggezza(int quantita) {
		md.setSaggezza(md.getSaggezza() + quantita);
	}

	// CARISMA

	@Override
	public int getCarisma() {
		return get(PersonaggioMD::getCarisma, TipoAttributo.CARISMA);
	}

	public void addCarisma(int quantita) {
		md.setCarisma(md.getCarisma() + quantita);
		UI.variaCarisma(this, quantita);
	}

	public void subCarisma(int quantita) {
		if (quantita > md.getCarisma()) {
			quantita = md.getCarisma();
		}
		md.setCarisma(md.getCarisma() - quantita);
		UI.variaCarisma(this, -quantita);
	}

	// FORTUNA

	@Override
	public int getFortuna() {
		return get(PersonaggioMD::getFortuna, TipoAttributo.FORTUNA);
	}

	public void addFortuna(int quantita) {
		md.setFortuna(md.getFortuna() + quantita);
	}

	// CRITICO

	@Override
	public int getCritico() {
		return get(PersonaggioMD::getCritico, TipoAttributo.CRITICO);
	}

	public void addCritico(int quantita) {
		md.setCritico(md.getCritico() + quantita);
	}

	// PRECISIONE

	@Override
	public int getPrecisione() {
		return get(PersonaggioMD::getPrecisione, TipoAttributo.PRECISIONE);
	}

	public void addPrecisione(int quantita) {
		md.setPrecisione(md.getPrecisione() + quantita);
	}

	// VELOCITA

	@Override
	public int getVelocita() {
		return get(PersonaggioMD::getVelocita, TipoAttributo.VELOCITA);
	}

	public void addVelocita(int quantita) {
		md.setVelocita(md.getVelocita() + quantita);
	}

	// FURTIVITA

	@Override
	public int getFurtivita() {
		return get(PersonaggioMD::getFurtivita, TipoAttributo.FURTIVITA);
	}

	public void addFurtivita(int quantita) {
		md.setFurtivita(md.getFurtivita() + quantita);
	}

	// PARATA

	@Override
	public int getParata() {
		return get(PersonaggioMD::getParata, TipoAttributo.PARATA);
	}

	public void addParata(int quantita) {
		md.setParata(md.getParata() + quantita);
	}

	// RESISTENZA MAGICA

	@Override
	public int getResistenzaMagica() {
		return get(PersonaggioMD::getResistenzaMagica, TipoAttributo.RESISTENZA_MAGICA);
	}

	public void addResistenzaMagica(int quantita) {
		md.setResistenzaMagica(md.getResistenzaMagica() + quantita);
	}

	// PERCEZIONE

	@Override
	public int getPercezione() {
		return get(PersonaggioMD::getPercezione, TipoAttributo.PERCEZIONE);
	}

	public void addPercezione(int quantita) {
		md.setPercezione(md.getPercezione() + quantita);
	}

	// SOGGEZIONE

	@Override
	public int getSoggezione() {
		return get(PersonaggioMD::getSoggezione, TipoAttributo.SOGGEZIONE);
	}

	public void addSoggezione(int quantita) {
		md.setSoggezione(md.getSoggezione() + quantita);
	}

	// FURIA

	@Override
	public int getFuria() {
		return get(PersonaggioMD::getFuria, TipoAttributo.FURIA);
	}

	public void addFuria(int quantita) {
		md.setFuria(md.getFuria() + quantita);
	}

	// CORAGGIO

	@Override
	public int getCoraggio() {
		return get(PersonaggioMD::getCoraggio, TipoAttributo.CORAGGIO);
	}

	public void addCoraggio(int quantita) {
		md.setCoraggio(md.getCoraggio() + quantita);
		UI.variaCoraggio(this, quantita);
	}

	// FIXME il coraggio si resetterà quando si ricalcolano i valori secondari
	public void subCoraggio(int quantita) {
		if (quantita > md.getCoraggio()) {
			quantita = md.getCoraggio();
		}
		md.setCoraggio(md.getCoraggio() - quantita);
		UI.variaCoraggio(this, -quantita);
	}

	// VALORE

	@Override
	public int getValore() {
		return get(PersonaggioMD::getValore, TipoAttributo.VALORE);
	}

	// FIXME il valore si resetterà quando si ricalcolano i valori secondari
	public void addValore(int quantita) {
		md.setValore(md.getValore() + quantita);
		UI.variaValore(this, quantita);
	}

	// FIXME il valore si resetterà quando si ricalcolano i valori secondari
	public void subValore(int quantita) {
		md.setValore(Math.max(md.getValore() - quantita, 0));
		UI.variaValore(this, -quantita);
	}

	// STANCHEZZA

	@Override
	public int getStanchezza() {
		return get(PersonaggioMD::getStanchezza, TipoAttributo.STANCHEZZA);
	}

	public void addStanchezza(int quantita) {
		quantita = limitaEntroMassimi(md.getStanchezza(), Costanti.MAX_STANCHEZZA, quantita);
		md.setStanchezza(md.getStanchezza() + quantita);
		UI.variaStanchezza(this, quantita);
	}

	public void subStanchezza(int quantita) {
		md.setStanchezza(Math.max(md.getStanchezza() - quantita, 0));
		UI.variaStanchezza(this, -quantita);
	}

	// Funzioni di calcolo per gli attributi derivati

	protected void ricalcolaAttributiSecondari() {
		md.setCaricoMassimo(calcolaCaricoMassimo());
		md.setCritico(calcolaCritico());
		md.setPrecisione(calcolaPrecisione());
		md.setVelocita(calcolaVelocita());
		md.setFurtivita(calcolaFurtivita());
		md.setParata(calcolaParata());
		md.setResistenzaMagica(calcolaResistenzaMagica());
		md.setPercezione(calcolaPercezione());
		md.setSoggezione(calcolaSoggezione());
		md.setFuria(calcolaFuria());
		md.setCoraggio(calcolaCoraggio());
		md.setValore(calcolaValore());
		md.setNumeroBersagli(calcolaNumeroBersagli());
	}

	public abstract double getMoltiplicatoreCarico();

	public abstract double getMoltiplicatoreCritico();

	/**
	 * Calcola il critico (0-100) basandosi UNICAMENTE sulle statistiche primarie
	 * e sul moltiplicatore della classe, mantenendo i rendimenti decrescenti.
	 */
	private int calcolaCritico() {
		// Costanti per calibrare la curva (es. con Destrezza 25 e Fortuna 25, il Ladro ha ~15% di critico)
		// Critico deve essere alimentato da due forze distinte:
		// DESTREZZA (Peso Maggiore - 70%): Rappresenta la precisione chirurgica nel colpire i punti vitali scoperti
		// (giugulare, fessure dell'armatura).
		// FORTUNA (Peso Minore - 30%): Rappresenta il fato favorevole che fa deviare il colpo all'ultimo millisecondo
		// nel punto giusto.
		final double COEFFICIENTE_DESTREZZA = 2.0;
		final double COEFFICIENTE_FORTUNA = 0.8;

		// Calcolo della precisione letale grezza con Diminishing Returns
		double precisioneGrezza = (COEFFICIENTE_DESTREZZA * Math.sqrt(getDestrezza())) +
				(COEFFICIENTE_FORTUNA * Math.sqrt(getFortuna()));

		// Applicazione del moltiplicatore di archetipo
		double criticoFinale = precisioneGrezza * getMoltiplicatoreCritico();

		// Cap per evitare che superi il 100% (o il 95% se vuoi sempre un margine di fallimento)
		if (criticoFinale > 100.0) {
			criticoFinale = 100.0;
		}

		// Arrotondamento a due decimali
		return (int)criticoFinale;
	}

	public abstract double getMoltiplicatorePrecisione();

	private int calcolaPrecisione() {
		// Per rispecchiare la descrizione ("precisione oculare, stabilità della mano e coordinazione occhio-mano"),
		// la Precisione deve essere alimentata da due forze distinte:
		// DESTREZZA (Peso Maggiore - 80%): La coordinazione motoria fine, i riflessi e la fermezza muscolare.
		// INTELLIGENZA (Peso Minore - 20%): La capacità logica di calcolare la traiettoria del bersaglio, anticiparne
		// i movimenti e non farsi ingannare dalle illusioni.
		final double COEFFICIENTE_DESTREZZA = 0.8;
		final double COEFFICIENTE_INTELLIGENZA = 0.2;

		// Calcolo della precisione grezza con Diminishing Returns
		double precisioneGrezza = (COEFFICIENTE_DESTREZZA * Math.sqrt(getDestrezza())) +
				(COEFFICIENTE_INTELLIGENZA * Math.sqrt(getIntelligenza()));

		// Applicazione del moltiplicatore di archetipo
		double precisioneFinale = precisioneGrezza * getMoltiplicatorePrecisione();

		return (int)precisioneFinale;
	}

	public abstract double getMoltiplicatoreVelocita();

	private int calcolaVelocita() {
		// Per rispecchiare la descrizione ("precisione oculare, stabilità della mano e coordinazione occhio-mano"),
		// la Precisione deve essere alimentata da due forze distinte:
		// DESTREZZA (Peso Maggiore - 80%): La coordinazione motoria fine, i riflessi e la fermezza muscolare.
		// INTELLIGENZA (Peso Minore - 20%): La capacità logica di calcolare la traiettoria del bersaglio, anticiparne
		// i movimenti e non farsi ingannare dalle illusioni.
		final double COEFFICIENTE_DESTREZZA = 0.85;
		final double COEFFICIENTE_FORTUNA = 0.15;

		// Calcolo della precisione grezza con Diminishing Returns
		double velocitaGrezza = (COEFFICIENTE_DESTREZZA * Math.sqrt(getDestrezza())) +
				(COEFFICIENTE_FORTUNA * Math.sqrt(getFortuna()));

		// Applicazione del moltiplicatore di archetipo
		double velocitaFinale = velocitaGrezza * getMoltiplicatoreVelocita();

		return (int)velocitaFinale;
	}

	public abstract double getMoltiplicatoreFurtivita();

	private int calcolaFurtivita() {
		// Per rispecchiare il concetto di "muoversi senza farsi notare e agire nell'ombra", la Furtività deve
		// attingere a due forze distinte:
		// DESTREZZA (Peso Maggiore - 75%): La grazia nei movimenti, il controllo totale del corpo e la coordinazione
		// millimetrica per non fare rumore (es. evitare rami secchi o passi pesanti).
		// FORTUNA (Peso Minore - 25%): Il tempismo perfetto che fa muovere il personaggio proprio quando la guardia
		// nemica si gira dall'altra parte o un rumore ambientale (es. un tuono o il vento) copre i suoi passi.
		final double COEFFICIENTE_DESTREZZA = 0.75;
		final double COEFFICIENTE_FORTUNA = 0.25;

		// Calcolo della furtivita grezza con Diminishing Returns
		double furtivitaGrezza = (COEFFICIENTE_DESTREZZA * Math.sqrt(getDestrezza())) +
				(COEFFICIENTE_FORTUNA * Math.sqrt(getFortuna()));

		// Applicazione del moltiplicatore di archetipo
		double furtivitaFinale = furtivitaGrezza * getMoltiplicatoreFurtivita();

		return (int)furtivitaFinale;
	}

	public abstract double getMoltiplicatoreParata();

	private int calcolaParata() {
		// Per rispecchiare il concetto di "frapporre l'arma o lo scudo tra sé e il colpo nemico", la Parata
		// deve attingere a due forze distinte:
		// FORZA (Peso Maggiore - 70%): La potenza muscolare necessaria a reggere l'impatto di un colpo pesante senza
		// farsi spezzare la guardia.
		// DESTREZZA (Peso Minore - 30%): I riflessi e la coordinazione occhio-mano per posizionare lo scudo o la lama
		// nell'angolo esatto prima dell'impatto.
		final double COEFFICIENTE_FORZA = 0.70;
		final double COEFFICIENTE_DESTREZZA = 0.30;

		// Calcolo della precisione letale grezza con Diminishing Returns
		double parataGrezza = (COEFFICIENTE_FORZA * Math.sqrt(getForza())) +
				(COEFFICIENTE_DESTREZZA * Math.sqrt(getDestrezza()));

		// Applicazione del moltiplicatore di archetipo
		double parataFinale = parataGrezza * getMoltiplicatoreParata();

		return (int)parataFinale;
	}

	public abstract double getMoltiplicatoreResistenzaMagica();

	private int calcolaResistenzaMagica() {
		// Per rispecchiare una difesa basata sul controllo dei flussi energetici e sulla fermezza d'animo, la
		// Resistenza Magica deve attingere a due forze della mente e dello spirito:
		// SAGGEZZA (Peso Maggiore - 70%): La consapevolezza spirituale e la connessione con il divino che agiscono come
		// uno scudo naturale contro le corruzioni dell'anima e gli anatemi.
		// INTELLIGENZA (Peso Minore - 30%): La comprensione logica della struttura degli incantesimi, che permette di
		// "dissipare" o deviare la trama magica prima dell'impatto.
		final double COEFFICIENTE_SAGGEZZA = 0.70;
		final double COEFFICIENTE_INTELLIGENZA = 0.30;

		// Calcolo della resistenza magica grezza con Diminishing Returns
		double resistenzaMagicaGrezza = (COEFFICIENTE_SAGGEZZA * Math.sqrt(getSaggezza())) +
				(COEFFICIENTE_INTELLIGENZA * Math.sqrt(getIntelligenza()));

		// Applicazione del moltiplicatore di archetipo
		double resistenzaMagicaFinale = resistenzaMagicaGrezza * getMoltiplicatoreResistenzaMagica();

		return (int)resistenzaMagicaFinale;
	}

	public abstract double getMoltiplicatorePercezione();

	private int calcolaPercezione() {
		// Per rispecchiare fedelmente il concetto di "sensi acuti, vista sviluppata e udito sopraffino", la Percezione
		// deve attingere a due forze distinte:
		// SAGGEZZA (Peso Maggiore - 75%): La consapevolezza spirituale, l'intuito e la connessione con l'ambiente
		// circostante (il "sesto senso").
		// DESTREZZA (Peso Minore - 25%): La prontezza di riflessi oculari e la rapidità nel volgere lo sguardo o
		// l'orecchio verso uno stimolo improvviso.
		final double COEFFICIENTE_SAGGEZZA = 0.75;
		final double COEFFICIENTE_DESTREZZA = 0.25;

		// Calcolo della precisione letale grezza con Diminishing Returns
		double percezioneGrezza = (COEFFICIENTE_SAGGEZZA * Math.sqrt(getSaggezza())) +
				(COEFFICIENTE_DESTREZZA * Math.sqrt(getDestrezza()));

		// Applicazione del moltiplicatore di archetipo
		double percezioneFinale = percezioneGrezza * getMoltiplicatorePercezione();

		return (int)percezioneFinale;
	}

	public abstract double getMoltiplicatoreSoggezione();

	private int calcolaSoggezione() {
		// Per rispecchiare il concetto di "forza della personalità combinata all'aura di terrore", la Soggezione
		// deve attingere a due forze distinte:
		// CARISMA (Peso Maggiore - 70%): Il magnetismo, la forza della personalità e la capacità di imporre la propria
		// volontà o presenza sugli altri.
		// FORZA (Peso Minore - 30%): La stazza e la potenza muscolare visibile che intimidiscono fisicamente chiunque
		// si trovi davanti.
		final double COEFFICIENTE_CARISMA = 0.70;
		final double COEFFICIENTE_FORZA = 0.30;

		// Calcolo della precisione letale grezza con Diminishing Returns
		double soggezioneGrezza = (COEFFICIENTE_CARISMA * Math.sqrt(getCarisma())) +
				(COEFFICIENTE_FORZA * Math.sqrt(getForza()));

		// Applicazione del moltiplicatore di archetipo
		double soggezioneFinale = soggezioneGrezza * getMoltiplicatoreSoggezione();

		return (int)soggezioneFinale;
	}

	public abstract double getMoltiplicatoreFuria();

	private int calcolaFuria() {
		// Per rispecchiare una statistica basata sull'impulso distruttivo e sulla resistenza al dolore, la Furia
		// deve attingere a due forze puramente fisiche ed emotive:
		// FORZA (Peso Maggiore - 75%): La potenza muscolare grezza che alimenta la violenza dei colpi durante lo stato
		// di rabbia.
		// FORTUNA (Peso Minore - 25%): L'elemento caotico e imprevedibile del fato che premia l'audacia di chi attacca
		// alla cieca senza difendersi.
		final double COEFFICIENTE_FORZA = 0.75;
		final double COEFFICIENTE_FORTUNA = 0.25;

		// Calcolo della precisione letale grezza con Diminishing Returns
		double furiaGrezza = (COEFFICIENTE_FORZA * Math.sqrt(getForza())) +
				(COEFFICIENTE_FORTUNA * Math.sqrt(getFortuna()));

		// Applicazione del moltiplicatore di archetipo
		double furiaFinale = furiaGrezza * getMoltiplicatoreFuria();

		return (int)furiaFinale;
	}

	public abstract double getMoltiplicatoreCoraggio();

	private int calcolaCoraggio() {
		// Per rispecchiare il concetto di "forza della personalità e forza di volontà", il Coraggio deve attingere a
		// due forze della mente e dell'identità:
		// CARISMA (Peso Maggiore - 75%): La forza dell'ego e la stabilità della personalità, che impediscono al
		// personaggio di farsi intimidire o manipolare.
		// COSTITUZIONE (Peso Minore - 25%): La stabilità biologica (es. controllo del battito cardiaco e
		// dell'adrenalina), che impedisce al corpo di cedere al panico fisico.
		final double COEFFICIENTE_CARISMA = 0.75;
		final double COEFFICIENTE_COSTITUZIONE = 0.25;

		// Calcolo della precisione letale grezza con Diminishing Returns
		double coraggioGrezzo = (COEFFICIENTE_CARISMA * Math.sqrt(getCarisma())) +
				(COEFFICIENTE_COSTITUZIONE * Math.sqrt(getCostituzione()));

		// Applicazione del moltiplicatore di archetipo
		double coraggioFinale = coraggioGrezzo * getMoltiplicatoreCoraggio();

		return (int)coraggioFinale;
	}

	public abstract double getMoltiplicatoreValore();

	private int calcolaValore() {
		// Per rispecchiare il concetto di "spirito di sacrificio ed eroismo guidato dalla stabilità biologica", il
		// Valore deve attingere a due forze distinte:
		// SAGGEZZA (Peso Maggiore - 70%): La consapevolezza morale, la connessione spirituale e la rettitudine che
		// spingono a compiere il gesto eroico o a difendere i deboli.
		// COSTITUZIONE (Peso Minore - 30%): La riserva di salute e la tempra fisica necessarie a sopportare l'impatto
		// dei danni mitigati o dei colpi intercettati per gli altri.
		final double COEFFICIENTE_SAGGEZZA = 0.70;
		final double COEFFICIENTE_COSTITUZIONE = 0.30;

		// Calcolo del valore grezzo con Diminishing Returns
		double valoreGrezzo = (COEFFICIENTE_SAGGEZZA * Math.sqrt(getSaggezza())) +
				(COEFFICIENTE_COSTITUZIONE * Math.sqrt(getCostituzione()));

		// Applicazione del moltiplicatore di archetipo
		double valoreFinale = valoreGrezzo * getMoltiplicatoreValore();

		return (int)valoreFinale;
	}

	public abstract double getMoltiplicatoreNumeroBersagli();

	private int calcolaNumeroBersagli() {
		// Per rispecchiare sia la capacità fisica di spazzare un'area con la massa corporea sia il controllo mentale
		// per gestire più minacce contemporaneamente, il Numero di Bersagli deve attingere a queste due forze:
		// FORZA (Peso Maggiore - 70%): La potenza fisica e la stazza. Più si è forti e grandi, più le armi impugnate
		// sono lunghe (spadoni, clave monumentali, colpi di coda), coprendo un arco di attacco più ampio.
		// INTELLIGENZA (Peso Minore - 30%): La concentrazione mentale e il calcolo tattico, necessari sia per i maghi
		// che concatenano incantesimi su più bersagli, sia per i guerrieri che mantengono il controllo su più
		// nemici ingaggiati.
		final double COEFFICIENTE_FORZA = 0.70;
		final double COEFFICIENTE_INTELLIGENZA = 0.30;

		// Calcolo del valore grezzo con Diminishing Returns
		double numeroGrezzo = (COEFFICIENTE_FORZA * Math.sqrt(getSaggezza())) +
				(COEFFICIENTE_INTELLIGENZA * Math.sqrt(getIntelligenza()));

		// Applicazione del moltiplicatore di archetipo
		double valoreFinale = numeroGrezzo * getMoltiplicatoreNumeroBersagli();

		return (int)Math.max(1, Math.floor(valoreFinale));
	}

	public abstract double getMoltiplicatoreStanchezza();

	public double calcolaStanchezza(Comando comando) {
		// Per calcolare quanta stanchezza accumula un personaggio alla fine di un turno di combattimento, dobbiamo
		// guardare alla sua capacità di tollerare lo sforzo:
		// COSTITUZIONE (Peso Maggiore - 80%): La tempra biologica e la riserva di salute. Più è alta, più il corpo
		// recupera rapidamente e resiste alla fatica.
		// FORZA (Peso Minore - 20%): La potenza muscolare. Un corpo forte fa meno fatica a compiere movimenti
		// atletici pesanti.
		final double COEFFICIENTE_COSTITUZIONE = 0.80;
		final double COEFFICIENTE_FORZA = 0.20;

		// Calcolo del valore grezzo con Diminishing Returns
		double numeroGrezzo = (COEFFICIENTE_COSTITUZIONE * Math.sqrt(getCostituzione())) +
				(COEFFICIENTE_FORZA * Math.sqrt(getForza()));

		//FIXME
		/*
		1) Turno di Combattimento Fisico Base: 5.0
		Rappresenta lo standard. Un fendente di spada, una parata reattiva o uno scatto di posizionamento. È lo sforzo muscolare regolare a cui un guerriero è addestrato.
		2) Lancio di un Incantesimo Base (Utility / Trucchetto): 3.0
		Piccole magie che richiedono pochissima concentrazione (es. accendere una luce, un piccolo dardo magico, una cura minore). Stanca meno di un turno di legnate fisiche.
		3) Lancio di un Incantesimo Complesso (Medio / Avanzato): 8.0 - 10.0
		Palle di fuoco, evocazioni o barriere mistiche. Canalizzare queste forze richiede di trattenere il fiato, sforzare la mente e subire il contraccolpo arcano.
		 Stanca circa il doppio rispetto a un attacco fisico.
		4) Lancio di una Magia Suprema (Ultimate / Cataclisma): 15.0 - 20.0
		Tempeste di fulmini o incantesimi che alterano il tempo. Questo sforzo svuota quasi completamente le riserve fisiche del lanciatore, rischiando di portarlo in Sfinimento in due o tre turni se non gestito.
		 */
		double costoAzione = 1;

		// Applicazione del moltiplicatore di archetipo
		double valoreFinale = costoAzione / numeroGrezzo * getMoltiplicatoreStanchezza();

		return (int)Math.max(1, Math.floor(valoreFinale));
	}

	// -- funzioni per calcolo modificatori

	private int get(Function<PersonaggioMD, Integer> getterAttributo, TipoAttributo tipoAttributo) {
		return (int) getQuantitaModificata(getterAttributo.apply(md), tipoAttributo);
	}

	private double getQuantitaModificata(double quantitaOriginale, TipoAttributo tipoAttributo) {
		List<ModificatoreAttributo> modificatoriLocali = new ArrayList<>();

		md.getModificatori()
				.stream()
				.filter(m -> m.getTipoAttributo() == tipoAttributo)
				.forEach(modificatoriLocali::add);

		md.getArtefatti()
				.stream()
				.flatMap(a -> a.getModificatori().stream())
				.filter(m -> m.getTipoAttributo() == tipoAttributo)
				.forEach(modificatoriLocali::add);

		OptionalDouble modificatoreAssoluto = modificatoriLocali
				.stream()
				.filter(m -> m.getTipoModificatoreAttributo() == TipoModificatore.QUANTITA_ASSOLUTA)
				.mapToDouble(ModificatoreAttributo::getQuantita).min();

		if (modificatoreAssoluto.isPresent())
			return modificatoreAssoluto.getAsDouble();

		double quantitaFisse = modificatoriLocali.stream()
				.filter(m -> m.getTipoModificatoreAttributo() == TipoModificatore.AUMENTO_FISSO)
				.mapToDouble(ModificatoreAttributo::getQuantita).sum();

		quantitaOriginale += quantitaFisse;

		double quantitaPercentuali = modificatoriLocali.stream()
				.filter(m -> m.getTipoModificatoreAttributo() == TipoModificatore.AUMENTO_PERCENTUALE)
				.mapToDouble(ModificatoreAttributo::getQuantita).sum();

		return quantitaOriginale * (1 + quantitaPercentuali / 100);
	}

	// EFFETTI DI STATO

	public Collection<EffettoDiStato> getEffettiDiStato() {
		return md.getEffettiDiStato();
	}

	public void addEffettoDiStato(TipoEffettoDiStato tipoEffettoDiStato, int valore) {
		md.getEffettiDiStato().add(new EffettoDiStato(tipoEffettoDiStato, valore));
	}

	public void removeEffettoDiStato(TipoEffettoDiStato tipoEffettoDiStato) {
		md.getEffettiDiStato().removeIf(e -> e.getTipoModificatoreAttributo() == tipoEffettoDiStato);
	}

	public boolean hasEffettoDiStato(TipoEffettoDiStato tipoEffettoDiStato) {
		return md.getEffettiDiStato().stream().anyMatch(e -> e.getTipoModificatoreAttributo() == tipoEffettoDiStato);
	}

	public int getQuantitaEffettoDiStato(TipoEffettoDiStato tipoEffettoDiStato) {
		return md.getEffettiDiStato().stream().filter(e -> e.getTipoModificatoreAttributo() == tipoEffettoDiStato).mapToInt(EffettoDiStato::getValore).sum();
	}

	// Artefatti

	//FIXME così fa un po' ribrezzo ma intanto facciamolo compilare. Per poterlo eliminare del tutto occorre gestire correttamente l'inventario del gruppo.
	@Override
	public List<Artefatto> getInventario() {
		return md.getArtefatti().stream().map(Artefatto::new).collect(Collectors.toList());
	}

	public void addArtefatto(Artefatto a) {
		md.getArtefatti().add(a.getModelloDati());
	}

	public void removeArtefatto(Artefatto a) {
		md.getArtefatti().remove(a.getModelloDati());
	}
}
