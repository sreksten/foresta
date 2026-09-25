package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoCreazionePersonaggio;
import com.threeamigos.foresta.eventi.interni.InternoRisultatoValutazionePersonaggioAttaccante;
import com.threeamigos.foresta.eventi.notifiche.*;
import com.threeamigos.foresta.incantesimi.ClasseIncantesimo;
import com.threeamigos.foresta.incantesimi.Incantesimo;
import com.threeamigos.foresta.incantesimi.IncantesimoMalefico;
import com.threeamigos.foresta.incantesimi.TipoIncantesimo;
import com.threeamigos.foresta.motore.*;
import com.threeamigos.foresta.motore.modellodati.*;
import com.threeamigos.foresta.offerte.ClassiOfferta;
import com.threeamigos.foresta.offerte.Offerta;
import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.tools.Misc;

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

	/**
	 * Posizione all'interno del gruppo
	 */
	private int ordinale;
	/**
	 * Personaggio Non Giocante (*solitamente*, un avversario)
	 */
	private boolean png;
	/**
	 * Suscettibile a corruzione da parte del gruppo del giocatore
	 */
	private boolean corrompibile;
	/**
	 * Amichevole nei confronti del gruppo del giocatore
	 */
	private boolean amichevole;
	/**
	 * Quantità massima per locazione
	 */
	private int quantitaMassima = 1;

	public PersonaggioBase(ClassePersonaggio classe, int livello) {
		md.setClasse(classe);
		png = true;

		// I boss partono con i valori impostati al massimo, gli altri personaggi partono con un pool di valori
		// lievemente casuale, dal 75% al 100% dei valori massimi
		Function<Integer, Integer> funzionePerValoriIniziali;
		if (isParteConValoriMassimi()) {
			funzionePerValoriIniziali = val -> val;
		} else {
			funzionePerValoriIniziali = (max) -> Dado.tiraAncheSenzaRange(max * 3 / 4, max);
		}

		// Questo imposta salute, magia e stanchezza
		impostaValoriDiPartenzaGenerali(funzionePerValoriIniziali, livello);

		// Queste impostano il resto - LanciatoreDeiDadi sovrascrive
		impostaValoriDiPartenza(funzionePerValoriIniziali);
		if (!isParteConValoriMassimi()) {
			LanciatoreDeiDadi.tiraDadiPer(classe, getLivello(), md);
		}

		ricalcolaAttributiSecondari();
		classe.setQuantitaMassima(quantitaMassima);
		BusEventi.pubblica(new InternoCreazionePersonaggio(this));
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
	
	public boolean isImmuneAIncantesimo(ClasseIncantesimo classeIncantesimo) {
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
		BusEventi.pubblica(new NotificaVariazioneStatoVitalePersonaggio(this, false));
	}

	public String getCausaTrapasso() {
		return md.getCausaTrapasso();
	}

	public void resuscita() {
		md.set(TipoAttributo.SALUTE, (int)(calcolaSaluteMassima() / 10.0d));
		md.set(TipoAttributo.STANCHEZZA, 9);
		md.setVivo(true);
		BusEventi.pubblica(new NotificaVariazioneStatoVitalePersonaggio(this, true));
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
		double numeroBersagli = getQuantitaModificata(md, 1 * getMoltiplicatoreNumeroBersagli(), TipoAttributo.NUMERO_BERSAGLI);
		// Almeno un bersaglio: con un moltiplicatore sotto 1 il troncamento darebbe 0
		return (int)(Math.max(1, numeroBersagli));
	}

	/**
	 * Prende il valore base dei danni di un incantesimo e lo moltiplica per il moltiplicatore di danni magia
 	 */
	public int getModificaDanniMagia(int danniBase) {
		double danniModificati = getQuantitaModificata(md, danniBase, TipoAttributo.MAGIA);
		return (int)danniModificati;
	}

	/**
	 * Calcola i Punti Vita (HP) rigenerati durante un turno di riposo.
	 */
	public int getRigenerazioneSalute() {
		// Se la creatura è un non-morto o uno spettro, il moltiplicatore è 0.0, quindi guarisce 0
		if (getMoltiplicatoreRecuperoFisico() == 0.0) {
			return 0;
		}
		// 1. Base di partenza mista
		double baseGrezza = getQuantitaModificata(md, 5.0d, TipoAttributo.RIGENERAZIONE_SALUTE) + (getSaluteMassima() * 0.05);
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
	public int getRigenerazioneMagia() {
		// 1. Calcolo del recupero potenziale basato solo sulla capienza massima
		double recuperoGrezzo = getQuantitaModificata(md, 5.0d, TipoAttributo.RIGENERAZIONE_MAGIA) + getMagiaMassima() * 0.05d;
		// 2. Applicazione del moltiplicatore di classe/razza
		double manaRigenerato = recuperoGrezzo * getMoltiplicatoreRecuperoMagico();
		// Arrotondamento a un decimale per l'interfaccia utente (UI)
		return (int)Math.ceil(manaRigenerato);
	}

	//FIXME questo sparirà quando facciamo la schermata statistiche/inventario
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
					.map(a -> a.getTipo().getUtilizzo() + ' ' + a.getNomeCompleto())
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

	//FIXME occorrerebbe implementare dei modificatori temporanei per l'ignominia della fuga,
	// su coraggio valore e carisma. Anche la salute dovrebbe essere aggiustata in base al livello probabilmente
	public void fugge() {
		subSalute(Dado.tira(50, 100), null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.SI);
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
			bersaglio = personaggiPossibili.get(0);
		}
		attacca(bersaglio);
	}

	public void attacca(Personaggio bersaglio) {
		Logger.log(getNome() + " attacca " + bersaglio.getNome());
		Incantesimo incantesimoScelto = scegliIncantesimoContro(bersaglio);
		if (incantesimoScelto != null) {
			incantesimoScelto.formula(this, bersaglio, null);
		} else {
			OpzioniGetNome articoloDaIncludere = GruppoAvversario.getIstanza().getNumeroPersonaggiVivi() == 1 ?
					OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE :
					OpzioniGetNome.INCLUDI_ARTICOLO_INDETERMINATIVO_SINGOLARE;
            String messaggio = getNome(articoloDaIncludere, OpzioniGetNome.INIZIALE_MAIUSCOLA) +
                    " attacca " + bersaglio.getNome(OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE) + '.';
			BusEventi.pubblica(new NotificaTestoFrase(messaggio));

			boolean colpisce = CalcolatoreCombattimento.colpisce(this, bersaglio, SupertipoDanno.FISICO);
			if (colpisce) {
				Arma arma = getArmaEquipaggiata();
                DannoRisultante risultato = CalcolatoreCombattimento.calcolaDannoRisultante(this, bersaglio, arma);
				Logger.log(getNome() + " colpisce " + bersaglio.getNome() + " assegnando " + risultato.getDanno() + " danni");
				bersaglio.applicaRisultatoCombattimento(risultato);
			} else {
				Logger.log(getNome() + " non colpisce " + bersaglio.getNome());
			}
		}
	}

	@Override
	public void applicaRisultatoCombattimento(DannoRisultante risultato) {
		subSalute(risultato.getDanno(), risultato.getAttaccante(), Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.SI);
		for (EffettoDiStato effetto : risultato.getEffettiDiStatoDaAggiungere()) {
			addEffettoDiStato(effetto.getTipoEffettoDiStato(), effetto.getDurata(), effetto.getDanniNelTempo());
		}
		for (TipoEffettoDiStato tipoEffettoDiStato: risultato.getEffettiDiStatoDaRimuovere()) {
			rimuoviEffettoDiStato(tipoEffettoDiStato);
		}
		for (TipoInterazioneElementale interazione : risultato.getInterazioniElementali()) {
			BusEventi.pubblica(new NotificaInterazioneElementalePersonaggio(this, interazione));
		}
	}

	/**
	 * L'arma impugnata nella mano principale (o a due mani); senza, l'arma naturale.
	 */
	@Override
	public Arma getArmaEquipaggiata() {
		return armaInSlot(SlotArtefatto.MANO_PRINCIPALE, SlotArtefatto.ENTRAMBE_LE_MANI)
				.orElseGet(() -> new ArmaNaturale(this));
	}

	/**
	 * L'arma nella mano secondaria, per chi combatte con due armi (Ladro/Ladra, Elfo/Elfa).
	 */
	@Override
	public Optional<Arma> getArmaSecondaria() {
		return armaInSlot(SlotArtefatto.MANO_SECONDARIA);
	}

	private Optional<Arma> armaInSlot(SlotArtefatto... slot) {
		List<SlotArtefatto> slotAmmessi = Arrays.asList(slot);
		return md.getArtefatti().stream()
				.filter(a -> a.getTipo().getSupertipo() == SupertipoArtefatto.ARMA)
				.filter(a -> slotAmmessi.contains(RegoleEquipaggiamento.slotOccupato(a)))
				.findFirst()
				.map(a -> (Arma) Artefatto.di(a));
	}

	private Incantesimo scegliIncantesimoContro(Personaggio personaggioBersaglio) {
		// Se il personaggio non sa usare la magia, non lancio incantesimo
		if (!isMagico()) {
			BusEventi.pubblica(new InternoRisultatoValutazionePersonaggioAttaccante(this, personaggioBersaglio,
					RisultatoValutazioneAttaccante.NON_USA_MAGIA));
			return null;
		}

		// Se il personaggio non ha magia a sua disposizione, non lancio incantesimo
		int magiaCorrente = getMagia();
		if (getMagia() == 0) {
			BusEventi.pubblica(new InternoRisultatoValutazionePersonaggioAttaccante(this, personaggioBersaglio,
					RisultatoValutazioneAttaccante.SENZA_MAGIA_A_DISPOSIZIONE));
			return null;
		}

		List<IncantesimoMalefico> incantesimiDisponibili = Arrays.stream(ClasseIncantesimo.values())
				.filter(i -> i.getCostoLancio() <= magiaCorrente && i.getTipo() == TipoIncantesimo.MALEFICO)
				.map(i -> i.getIstanza(getLivello()))
				.map(IncantesimoMalefico.class::cast)
				.collect(Collectors.toList());

		// Se non ci sono incantesimi possibili, non lancio incantesimo
		if (incantesimiDisponibili.isEmpty()) {
			BusEventi.pubblica(new InternoRisultatoValutazionePersonaggioAttaccante(this, personaggioBersaglio,
					RisultatoValutazioneAttaccante.SENZA_INCANTESIMI_A_DISPOSIZIONE));
			return null;
		}

		// Per qualche motivo suo il mostro potrebbe decidere di non tirare incantesimi
		if (Dado.tira(3) == 1) {
			BusEventi.pubblica(new InternoRisultatoValutazionePersonaggioAttaccante(this, personaggioBersaglio,
					RisultatoValutazioneAttaccante.CASUALMENTE_NON_LANCIA_INCANTESIMO));
			return null;
		}

		int intelligenza = getIntelligenza();
		// Un mostro stupido non sa mai cosa fare, quindi sceglie un incantesimo a caso
		if (intelligenza < 5) {
			Incantesimo incantesimo = incantesimiDisponibili.get(Dado.tiraAncheAUnaFaccia(incantesimiDisponibili.size()) - 1);
			BusEventi.pubblica(new InternoRisultatoValutazionePersonaggioAttaccante(this, personaggioBersaglio,
					RisultatoValutazioneAttaccante.SCEGLIE_A_CASO, incantesimo));
			return incantesimo;
		}

		incantesimiDisponibili.sort(Comparator.comparing(IncantesimoMalefico::getDanni).reversed());
		IncantesimoMalefico piuPotente = incantesimiDisponibili.get(0);
		if (intelligenza < 7) {
			// Usa l'incantesimo più potente a disposizione
			BusEventi.pubblica(new InternoRisultatoValutazionePersonaggioAttaccante(this, personaggioBersaglio,
					RisultatoValutazioneAttaccante.SCEGLIE_IL_PIU_POTENTE, piuPotente));
			return piuPotente;
		}

		// Ancora più intelligente, controlla se farebbe più danni tra fisico e non

		int probabilitaDiColpireMagico = CalcolatoreCombattimento.calcolaProbabilitaDiColpire(
				this, personaggioBersaglio, SupertipoDanno.MAGICO);
		int probabilitaDiColpireFisico = CalcolatoreCombattimento.calcolaProbabilitaDiColpire(
				this, personaggioBersaglio, SupertipoDanno.FISICO);

		int possibiliDanniMagici = CalcolatoreCombattimento.calcolaDannoRisultante(this, personaggioBersaglio, piuPotente).getDanno();

        Arma arma = getArmaEquipaggiata();
		int possibiliDanniFisici = CalcolatoreCombattimento.calcolaDannoRisultante(this, personaggioBersaglio, arma).getDanno();

		if (probabilitaDiColpireMagico * possibiliDanniMagici > probabilitaDiColpireFisico * possibiliDanniFisici) {
			BusEventi.pubblica(new InternoRisultatoValutazionePersonaggioAttaccante(this, personaggioBersaglio,
					RisultatoValutazioneAttaccante.PREFERISCE_ATTACCO_MAGICO, piuPotente,
					probabilitaDiColpireFisico, probabilitaDiColpireMagico, possibiliDanniFisici, possibiliDanniMagici));
			return piuPotente;
		} else {
			BusEventi.pubblica(new InternoRisultatoValutazionePersonaggioAttaccante(this, personaggioBersaglio,
					RisultatoValutazioneAttaccante.PREFERISCE_ATTACCO_FISICO, piuPotente,
					probabilitaDiColpireFisico, probabilitaDiColpireMagico, possibiliDanniFisici, possibiliDanniMagici));
			return null;
		}
	}

	public boolean isATempo() {
		Optional<Double> tempoOpt = md.getOptional(TipoAttributo.TEMPO);
		return tempoOpt.isPresent() && tempoOpt.get() != PersonaggioMD.SENZA_LIMITE;
	}

	public void setTempo(int tempo) {
		double quantitaPrecedente = md.get(TipoAttributo.TEMPO);
		md.set(TipoAttributo.TEMPO, tempo);
		BusEventi.pubblica(new NotificaVariazioneStatistichePersonaggio(this, TipoAttributo.TEMPO, quantitaPrecedente, tempo));
	}

	public int decrementaTempo() {
		double tempo = md.get(TipoAttributo.TEMPO);
		if (!md.isVivo() || !isATempo()) {
			return (int)tempo;
		}
		double quantitaPrecedente = tempo;
		tempo--;
		md.set(TipoAttributo.TEMPO, tempo);
		BusEventi.pubblica(new NotificaVariazioneStatistichePersonaggio(this, TipoAttributo.TEMPO, quantitaPrecedente, tempo));
		return (int)tempo;
	}

	public Offerta getOfferta(Comando azione) {
		ClassiOfferta[] offerte = null;
		if (isAmichevole() && azione == Comando.AMICIZIA) {
			offerte = getOfferteAmicizia();
		} else {
			offerte = getOfferteCorruzione();
		}
		// A gruppo pieno nessuno si puo' unire: le offerte di aiuto non si propongono
		if (GruppoGiocatore.getIstanza().getNumeroPersonaggi() >= Costanti.MAX_PERSONAGGI_GRUPPO_GIOCATORE) {
			offerte = Arrays.stream(offerte)
					.filter(o -> o != ClassiOfferta.AIUTO_GRATUITO && o != ClassiOfferta.AIUTO_MERCENARIO)
					.toArray(ClassiOfferta[]::new);
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
		md.set(TipoAttributo.SALUTE, funzione.apply(getSaluteMassima()));
		md.set(TipoAttributo.MAGIA,funzione.apply(getMagiaMassima()));
		md.set(TipoAttributo.STANCHEZZA,Costanti.MAX_STANCHEZZA - funzione.apply(Costanti.MAX_STANCHEZZA));
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

	public void addPuntiEsperienza(int esperienza) {
		if (isPNG()) {
			return;
		}

		int quantitaPrecedente = md.getEsperienza();
		int nuovaQuantita = quantitaPrecedente + esperienza;
		md.setEsperienza(nuovaQuantita);
		BusEventi.pubblica(new NotificaVariazioneStatistichePersonaggio(this, TipoAttributo.PUNTI_ESPERIENZA,
				quantitaPrecedente, nuovaQuantita));

		// Verifichiamo se i nuovi XP accumulati determinano un salto di livello
		int livelloAttuale = md.getLivello();
		int nuovoLivello = GestoreProgressione.calcolaLivelloDaXp(md.getEsperienza());

		if (nuovoLivello > livelloAttuale) {
			int differenza = nuovoLivello - livelloAttuale;
			md.setLivello(nuovoLivello);
			BusEventi.pubblica(new NotificaAumentoLivelloPersonaggio(this, livelloAttuale, nuovoLivello));
			int puntiAbilitaDisponibili = md.getPuntiAbilitaDisponibili();
			int nuoviPuntiAbilitaDisponibili = puntiAbilitaDisponibili + differenza;
			md.setPuntiAbilitaDisponibili(nuoviPuntiAbilitaDisponibili);
			BusEventi.pubblica(new NotificaVariazioneStatistichePersonaggio(this, TipoAttributo.PUNTI_ABILITA, puntiAbilitaDisponibili, nuoviPuntiAbilitaDisponibili));
		}
		// QUI PUOI AGGANCIARE IL CODICE PRECEDENTE:
		// 1. Ricalcola il nuovo budget di punti primari (con la tolleranza del 5%)
		// 2. Aggiorna le statistiche nel modello md.setForza(...), ecc.
		// 3. Ricalcola i valori derivati come Carico, Critico, Velocità.
	}

	@Override
	public int getPuntiAbilitaDisponibili() {
		return md.getPuntiAbilitaDisponibili();
	}

	@Override
	public void spendiPuntoAbilita(TipoAttributo tipoAttributo) {
		if (!tipoAttributo.isPrimario() || md.getPuntiAbilitaDisponibili() <= 0) {
			return;
		}
		add(tipoAttributo, 1);
		int puntiAbilitaDisponibili = md.getPuntiAbilitaDisponibili();
		int nuoviPuntiAbilitaDisponibili = puntiAbilitaDisponibili - 1;
		md.setPuntiAbilitaDisponibili(nuoviPuntiAbilitaDisponibili);
		// I secondari (precisione, critico, velocita'...) si ricavano dai primari
		ricalcolaAttributiSecondari();
		BusEventi.pubblica(new NotificaVariazioneStatistichePersonaggio(this, TipoAttributo.PUNTI_ABILITA,
				puntiAbilitaDisponibili, nuoviPuntiAbilitaDisponibili));
		BusEventi.pubblica(new NotificaConsumoPuntoAbilitaPersonaggio(this, tipoAttributo));
	}

	// CARICO

	@Override
	public double getCarico() {
		return md.getArtefatti().stream().mapToDouble(ArtefattoMD::getPeso).sum();
	}

	@Override
	public boolean puoPrendere(Artefatto artefatto) {
		return puoPrendere(artefatto.getPeso());
	}

	@Override
	public Optional<MotivoRifiutoEquipaggiamento> puoEquipaggiare(Artefatto artefatto) {
		RegoleEquipaggiamento.Esito esito = RegoleEquipaggiamento.valuta(getClasse(), getLivello(),
				md.getArtefatti(), artefatto.getModelloDati());
		if (esito.getMotivo() != null) {
			return Optional.of(esito.getMotivo());
		}
		if (artefatto.getTipo() == TipoArtefatto.ARMATURA && getForza() < Costanti.ARMATURA_FORZA_MINIMA) {
			return Optional.of(MotivoRifiutoEquipaggiamento.FORZA_INSUFFICIENTE);
		}
		if (!puoPrendere(artefatto)) {
			return Optional.of(MotivoRifiutoEquipaggiamento.TROPPO_CARICO);
		}
		return Optional.empty();
	}

	public boolean puoPrendere(double quantita) {
		return quantita <= calcolaCaricoMassimo(md, this) - getCarico();
	}

	// CARICO MASSIMO

	@Override
	public int getCaricoMassimo() {
		return (int)calcolaCaricoMassimo(md, this);
	}

	/**
	 * Calcola il carico massimo basandosi UNICAMENTE sulle statistiche primarie
	 * e sul moltiplicatore della classe, mantenendo i rendimenti decrescenti.
	 */
	private static double calcolaCaricoMassimo(PersonaggioMD md, Personaggio moltiplicatori) {

		if (moltiplicatori.getMoltiplicatoreCarico() == 0.0) {
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
		double potenzaFisica = PESO_PER_RADICE_FORZA * Math.sqrt(get(md, PersonaggioMD::getForza, TipoAttributo.FORZA));
		double resistenzaFisica = PESO_PER_RADICE_COSTITUZIONE * Math.sqrt(get(md, PersonaggioMD::getCostituzione, TipoAttributo.COSTITUZIONE));

		// Il potenziale di carico totale del corpo
		double potenziale = potenzaFisica + resistenzaFisica;

        return getQuantitaModificata(md, potenziale, TipoAttributo.CARICO_MASSIMO);
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
		int salutePrecedente = (int)md.get(TipoAttributo.SALUTE);
		quantita = limitaEntroMassimi(salutePrecedente, getSaluteMassima(), quantita);
		int saluteCorrente = salutePrecedente + quantita;
		md.set(TipoAttributo.SALUTE, saluteCorrente);
		BusEventi.pubblica(new NotificaVariazioneStatistichePersonaggio(this, TipoAttributo.SALUTE, salutePrecedente, saluteCorrente));
	}

	//FIXME sono convinto che questo metodo sia un po' troppo un pout-pourri. Include sia la morte che la notifica. Andrebbe spezzato
	//perché in caso di danni nel tempo non c'è un attaccante e la morte risulterebbe per troppa codardia. Quantomeno va rivisto.
	public void subSalute(int quantita, Personaggio avversario, Personaggio.NotificaFerite notificaFerite, Personaggio.NotificaMorte notificaMorte) {
		if (quantita <= 0) {
			if (notificaFerite == Personaggio.NotificaFerite.SI) {
				String sb = getNome(OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE, OpzioniGetNome.INIZIALE_MAIUSCOLA) +
						" non ha riportato danni dall'attacco " + avversario.getNome(OpzioniGetNome.INCLUDI_PREPOSIZIONE_ARTICOLATA) + '.';
				BusEventi.pubblica(new NotificaTestoFrase(sb));
			}
			return;
		}

		double saluteOriginale = md.get(TipoAttributo.SALUTE);
		double salute = saluteOriginale - quantita;
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
					BusEventi.pubblica(new NotificaTestoFrase(sb.toString()));
				}
				if (avversario != null) {
					md.setCausaTrapasso("Uccis" + getLetteraFinaleAttributo() + " " + avversario.getDa() + avversario.getNomeSingolare() + ".");
				} else {
					// FIXME se si muore per effetti di stato?
					md.setCausaTrapasso("Mort" + getLetteraFinaleAttributo() + " per troppa codardia.");
				}
				muore(md.getCausaTrapasso());
			}
		} else {
			if (notificaFerite == Personaggio.NotificaFerite.SI) {
				String nome = getNome(OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE, OpzioniGetNome.INIZIALE_MAIUSCOLA);
				String notifica = nome + " ha ancora " + (int)salute + " punt" + (salute == 1 ? 'o' : 'i') +
						" ferita su " + (int)calcolaSaluteMassima() + '.';
				BusEventi.pubblica(new NotificaTestoFrase(notifica));
			}
		}
		md.setSalute(salute);
		BusEventi.pubblica(new NotificaVariazioneStatistichePersonaggio(this, TipoAttributo.SALUTE, saluteOriginale, salute));

		if ((md.getClasse() == ClassePersonaggio.GUERRIERO || md.getClasse() == ClassePersonaggio.GUERRIERA) &&
				getFuria() > 0 && !hasEffettoDiStato(TipoEffettoDiStato.BERSERK)) {
			double sogliaBerserk = calcolaSaluteMassima() / 3.0d;
			if (salute > 0 && salute <= sogliaBerserk) {
				addEffettoDiStato(TipoEffettoDiStato.BERSERK, 1, 0);
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
		saluteMassima = getQuantitaModificata(md, saluteMassima, TipoAttributo.SALUTE);
		return saluteMassima;
	}

	public void addSaluteMassima(int quantita, String note) {
		ModificatoreAttributo modificatore = new ModificatoreAttributo(TipoAttributo.SALUTE, TipoModificatore.AUMENTO_FISSO, quantita, note);
		addModificatore(modificatore);
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
        int quantitaPrecedente = md.getMagia();
		quantita = limitaEntroMassimi(quantitaPrecedente, getMagiaMassima(), quantita);
		int quantitaAttuale = quantitaPrecedente + quantita;
		md.setMagia(quantitaAttuale);
		BusEventi.pubblica(new NotificaVariazioneStatistichePersonaggio(this, TipoAttributo.MAGIA, quantitaPrecedente, quantitaAttuale));
	}

	public void subMagia(int quantita) {
		int quantitaPrecedente = md.getMagia();
		if (quantita > quantitaPrecedente) {
			throw new IllegalStateException("Tentativo di utilizzo di più magia rispetto a quella disponibile");
		}
		int quantitaAttuale = quantitaPrecedente - quantita;
		md.setMagia(quantitaAttuale);
		BusEventi.pubblica(new NotificaVariazioneStatistichePersonaggio(this, TipoAttributo.MAGIA, quantitaPrecedente, quantitaAttuale));
	}

	@Override
	public int getMagiaMassima() {
		return (int)calcolaMagiaMassima();
	}

	@Override
	public void addMagiaMassima(int quantita, String note) {
		ModificatoreAttributo modificatore = new ModificatoreAttributo(TipoAttributo.MAGIA, TipoModificatore.AUMENTO_FISSO, quantita, note);
		addModificatore(modificatore);
	}

	protected double calcolaMagiaMassima() {
		if (getLivellamentoMagia() == 0.0d) {
			return 0.0d;
		}
		double magiaMassima = getMagiaBase() + getLivellamentoMagia() * Math.sqrt(getLivello() - 1);
		magiaMassima = getQuantitaModificata(md, magiaMassima, TipoAttributo.MAGIA);
		return magiaMassima;
	}

	// Funzione per aggiornamento attributi primari. Questo può succedere per esempio quando un personaggio aumenta di livello

	private void add(TipoAttributo tipoAttributo, int quantita) {
		double valorePrecedente = md.get(tipoAttributo);
		double valoreAttuale = valorePrecedente + quantita;
		md.set(tipoAttributo, valoreAttuale);
		BusEventi.pubblica(new NotificaVariazioneStatistichePersonaggio(this, tipoAttributo, valorePrecedente, valoreAttuale));
	}

	// FORZA

	@Override
	public int getForza() {
		return get(md, PersonaggioMD::getForza, TipoAttributo.FORZA);
	}

	public void addForza(int quantita) {
		add(TipoAttributo.FORZA, quantita);
	}

	// DESTREZZA

	@Override
	public int getDestrezza() {
		return get(md, PersonaggioMD::getDestrezza, TipoAttributo.DESTREZZA);
	}

	public void addDestrezza(int quantita) {
		add(TipoAttributo.DESTREZZA, quantita);
	}

	// COSTITUZIONE

	@Override
	public int getCostituzione() {
		return get(md, PersonaggioMD::getCostituzione, TipoAttributo.COSTITUZIONE);
	}

	public void addCostituzione(int quantita) {
		add(TipoAttributo.COSTITUZIONE, quantita);
	}

	// INTELLIGENZA

	@Override
	public int getIntelligenza() {
		return get(md, PersonaggioMD::getIntelligenza, TipoAttributo.INTELLIGENZA);
	}

	public void addIntelligenza(int quantita) {
		add(TipoAttributo.INTELLIGENZA, quantita);
	}

	// SAGGEZZA

	@Override
	public int getSaggezza() {
		return get(md, PersonaggioMD::getSaggezza, TipoAttributo.SAGGEZZA);
	}

	public void addSaggezza(int quantita) {
		add(TipoAttributo.SAGGEZZA, quantita);
	}

	// CARISMA

	@Override
	public int getCarisma() {
		return get(md, PersonaggioMD::getCarisma, TipoAttributo.CARISMA);
	}

	public void addCarisma(int quantita) {
		add(TipoAttributo.CARISMA, quantita);
	}

	public void subCarisma(int quantita) {
		if (quantita > md.getCarisma()) {
			quantita = md.getCarisma();
		}
		if (quantita > 0) {
			add(TipoAttributo.CARISMA, -quantita);
		}
	}

	// FORTUNA

	@Override
	public int getFortuna() {
		return get(md, PersonaggioMD::getFortuna, TipoAttributo.FORTUNA);
	}

	public void addFortuna(int quantita) {
		add(TipoAttributo.FORTUNA, quantita);
	}

	// CRITICO

	@Override
	public int getCritico() {
		return get(md, PersonaggioMD::getCritico, TipoAttributo.CRITICO);
	}

	// PRECISIONE

	@Override
	public int getPrecisione() {
		return get(md, PersonaggioMD::getPrecisione, TipoAttributo.PRECISIONE);
	}

	// VELOCITA

	@Override
	public int getVelocita() {
		return get(md, PersonaggioMD::getVelocita, TipoAttributo.VELOCITA);
	}

	// FURTIVITA

	@Override
	public int getFurtivita() {
		return get(md, PersonaggioMD::getFurtivita, TipoAttributo.FURTIVITA);
	}

	// PARATA

	/**
	 * La PARATA con l'equipaggiamento: scudo, elmo e armatura ne aggiungono una parte fissa più una per ogni
	 * loro livello, mentre chi impugna due armi o un'arma a due mani ha la guardia aperta e ne perde un quarto.
	 */
	@Override
	public int getParata() {
		double parata = get(md, PersonaggioMD::getParata, TipoAttributo.PARATA);
		for (ArtefattoMD artefatto : md.getArtefatti()) {
			parata += parataIntrinseca(artefatto);
		}
		if (haGuardiaAperta()) {
			parata *= Costanti.GUARDIA_APERTA_FATTORE_PARATA;
		}
		return (int) parata;
	}

	/**
	 * La PARATA che un pezzo difensivo dà di suo, oltre ai modificatori scritti sull'artefatto.
	 * La veste non para: dà lo stesso minimo in RESISTENZA_MAGICA (vedi getResistenzaMagica).
	 */
	private static int parataIntrinseca(ArtefattoMD artefatto) {
		int livello = artefatto.getLivello();
		switch (artefatto.getTipo()) {
			case SCUDO:
				return Costanti.SCUDO_PARATA_MINIMA + Costanti.SCUDO_PARATA_PER_LIVELLO * livello;
			case ELMO:
				return Costanti.ELMO_PARATA_MINIMA + Costanti.ELMO_PARATA_PER_LIVELLO * livello;
			case ARMATURA:
				return Costanti.ARMATURA_PARATA_MINIMA + Costanti.ARMATURA_PARATA_PER_LIVELLO * livello;
			default:
				return 0;
		}
	}

	private boolean haGuardiaAperta() {
		return md.getArtefatti().stream()
				.filter(a -> a.getTipo().getSupertipo() == SupertipoArtefatto.ARMA)
				.map(RegoleEquipaggiamento::slotOccupato)
				.anyMatch(slot -> slot == SlotArtefatto.ENTRAMBE_LE_MANI || slot == SlotArtefatto.MANO_SECONDARIA);
	}

	// RESISTENZA MAGICA

	/**
	 * La RESISTENZA_MAGICA con l'equipaggiamento: lo scudo ne aggiunge per ogni suo livello, di più se è raro;
	 * la veste, l'armatura di chi usa la magia, ne dà quanto un'armatura dà di PARATA.
	 */
	@Override
	public int getResistenzaMagica() {
		int resistenza = get(md, PersonaggioMD::getResistenzaMagica, TipoAttributo.RESISTENZA_MAGICA);
		for (ArtefattoMD artefatto : md.getArtefatti()) {
			if (artefatto.getTipo() == TipoArtefatto.SCUDO) {
				int perLivello = artefatto.getRarita() == RaritaArtefatto.COMUNE
						? Costanti.SCUDO_RESISTENZA_MAGICA_PER_LIVELLO
						: Costanti.SCUDO_RARO_RESISTENZA_MAGICA_PER_LIVELLO;
				resistenza += perLivello * artefatto.getLivello();
			} else if (artefatto.getTipo() == TipoArtefatto.VESTE) {
				resistenza += Costanti.ARMATURA_PARATA_MINIMA + Costanti.ARMATURA_PARATA_PER_LIVELLO * artefatto.getLivello();
			}
		}
		return resistenza;
	}

	// PERCEZIONE

	@Override
	public int getPercezione() {
		return get(md, PersonaggioMD::getPercezione, TipoAttributo.PERCEZIONE);
	}

	// SOGGEZIONE

	@Override
	public int getSoggezione() {
		return get(md, PersonaggioMD::getSoggezione, TipoAttributo.SOGGEZIONE);
	}

	// FURIA

	@Override
	public int getFuria() {
		return get(md, PersonaggioMD::getFuria, TipoAttributo.FURIA);
	}

	// CORAGGIO

	@Override
	public int getCoraggio() {
		return get(md, PersonaggioMD::getCoraggio, TipoAttributo.CORAGGIO);
	}

	// VALORE

	@Override
	public int getValore() {
		return get(md, PersonaggioMD::getValore, TipoAttributo.VALORE);
	}

	// CONTRATTAZIONE

	@Override
	public int getContrattazione() {
		return get(md, PersonaggioMD::getContrattazione, TipoAttributo.CONTRATTAZIONE);
	}

	// STANCHEZZA

	@Override
	public int getStanchezza() {
		return get(md, PersonaggioMD::getStanchezza, TipoAttributo.STANCHEZZA);
	}

	public void addStanchezza(int quantita) {
		int quantitaPrecedente = md.getStanchezza();
		quantita = limitaEntroMassimi(quantitaPrecedente, Costanti.MAX_STANCHEZZA, quantita);
		int quantitaAttuale = quantitaPrecedente + quantita;
		md.setStanchezza(md.getStanchezza() + quantita);
		BusEventi.pubblica(new NotificaVariazioneStatistichePersonaggio(this, TipoAttributo.STANCHEZZA, quantitaPrecedente, quantitaAttuale));
	}

	public void subStanchezza(int quantita) {
		int quantitaPrecedente = md.getStanchezza();
		int quantitaAttuale = quantitaPrecedente - quantita;
		md.setStanchezza(Math.max(quantitaAttuale, 0));
		BusEventi.pubblica(new NotificaVariazioneStatistichePersonaggio(this, TipoAttributo.STANCHEZZA, quantitaPrecedente, quantitaAttuale));
	}

	public void addModificatore(ModificatoreAttributo modificatore) {
		int saluteMassimaPrecedente = getSaluteMassima();
		int magiaMassimaPrecedente = getMagiaMassima();

		md.getModificatori().add(modificatore);

		int saluteMassimaRicalcolata = getSaluteMassima();
		if (saluteMassimaPrecedente != saluteMassimaRicalcolata) {
			BusEventi.pubblica(new NotificaVariazioneStatistichePersonaggio(this, TipoAttributo.SALUTE_MASSIMA, saluteMassimaPrecedente, saluteMassimaRicalcolata));
		}

		int magiaMassimaRicalcolata = getMagiaMassima();
		if (magiaMassimaPrecedente != magiaMassimaRicalcolata) {
			BusEventi.pubblica(new NotificaVariazioneStatistichePersonaggio(this, TipoAttributo.MAGIA_MASSIMA, magiaMassimaPrecedente, magiaMassimaRicalcolata));
		}

		ricalcolaAttributiSecondari();
		BusEventi.pubblica(new NotificaAggiuntaModificatorePersonaggio(this, modificatore));
	}

	// Funzioni di calcolo per gli attributi derivati

	/**
	 * Ricalcola gli attributi secondari del personaggio: carico massimo, critico, precisione, velocità, furtività,
	 * parata, resistenza magica, percezione, soggezione, furia, coraggio, valore, contrattazione, numero bersagli
	 */
	protected void ricalcolaAttributiSecondari() {
		ricalcolaAttributiSecondariCore(md, this, this);
	}

	/**
	 * Ricalcola gli attributi secondari (carico massimo, critico, precisione, velocità, furtività, parata,
	 * resistenza magica, percezione, soggezione, furia, coraggio, valore, contrattazione, numero bersagli)
	 * direttamente su {@code md}, usando i moltiplicatori di classe di {@code moltiplicatori}. Non pubblica eventi: da usare
	 * in fase di caricamento, quando non esiste ancora un Personaggio vivo a cui riferirli.
	 */
	public static void ricalcolaAttributiSecondari(PersonaggioMD md, Personaggio moltiplicatori) {
		ricalcolaAttributiSecondariCore(md, moltiplicatori, null);
	}

	private static void ricalcolaAttributiSecondariCore(PersonaggioMD md, Personaggio moltiplicatori, Personaggio sorgenteEvento) {
		// Il carico massimo è un massimo, non un valore: si legge e si scrive con getMassimo/setMassimo
		Optional<Double> valorePrecedente = md.getMassimo(TipoAttributo.CARICO_MASSIMO);
		double valoreAttuale = calcolaCaricoMassimo(md, moltiplicatori);
		md.setMassimo(TipoAttributo.CARICO_MASSIMO, valoreAttuale);
		if (sorgenteEvento != null && valorePrecedente.isPresent() && valorePrecedente.get() != valoreAttuale) {
			BusEventi.pubblica(new NotificaVariazioneStatistichePersonaggio(sorgenteEvento, TipoAttributo.CARICO_MASSIMO, valorePrecedente.get(), valoreAttuale));
		}
		ricalcolaAttributoSecondario(md, sorgenteEvento, TipoAttributo.CRITICO, calcolaCritico(md, moltiplicatori));
		ricalcolaAttributoSecondario(md, sorgenteEvento, TipoAttributo.PRECISIONE, calcolaPrecisione(md, moltiplicatori));
		ricalcolaAttributoSecondario(md, sorgenteEvento, TipoAttributo.VELOCITA, calcolaVelocita(md, moltiplicatori));
		ricalcolaAttributoSecondario(md, sorgenteEvento, TipoAttributo.FURTIVITA, calcolaFurtivita(md, moltiplicatori));
		ricalcolaAttributoSecondario(md, sorgenteEvento, TipoAttributo.PARATA, calcolaParata(md, moltiplicatori));
		ricalcolaAttributoSecondario(md, sorgenteEvento, TipoAttributo.RESISTENZA_MAGICA, calcolaResistenzaMagica(md, moltiplicatori));
		ricalcolaAttributoSecondario(md, sorgenteEvento, TipoAttributo.PERCEZIONE, calcolaPercezione(md, moltiplicatori));
		ricalcolaAttributoSecondario(md, sorgenteEvento, TipoAttributo.SOGGEZIONE, calcolaSoggezione(md, moltiplicatori));
		ricalcolaAttributoSecondario(md, sorgenteEvento, TipoAttributo.FURIA, calcolaFuria(md, moltiplicatori));
		ricalcolaAttributoSecondario(md, sorgenteEvento, TipoAttributo.CORAGGIO, calcolaCoraggio(md, moltiplicatori));
		ricalcolaAttributoSecondario(md, sorgenteEvento, TipoAttributo.VALORE, calcolaValore(md, moltiplicatori));
		ricalcolaAttributoSecondario(md, sorgenteEvento, TipoAttributo.CONTRATTAZIONE, calcolaContrattazione(md, moltiplicatori));
		ricalcolaAttributoSecondario(md, sorgenteEvento, TipoAttributo.NUMERO_BERSAGLI, calcolaNumeroBersagli(md, moltiplicatori));
	}

	private static void ricalcolaAttributoSecondario(PersonaggioMD md, Personaggio sorgenteEvento, TipoAttributo tipoAttributo, double valoreRicalcolato) {
		Optional<Double> valorePrecedente = md.getOptional(tipoAttributo);
		md.set(tipoAttributo, valoreRicalcolato);
		if (sorgenteEvento != null && valorePrecedente.isPresent() && valorePrecedente.get() != valoreRicalcolato) {
			BusEventi.pubblica(new NotificaVariazioneStatistichePersonaggio(sorgenteEvento, tipoAttributo, valorePrecedente.get(), valoreRicalcolato));
		}
	}

	public abstract double getMoltiplicatoreCarico();

	public abstract double getMoltiplicatoreCritico();

	/**
	 * Calcola il critico (0-100) basandosi UNICAMENTE sulle statistiche primarie
	 * e sul moltiplicatore della classe, mantenendo i rendimenti decrescenti.
	 */
	private static int calcolaCritico(PersonaggioMD md, Personaggio moltiplicatori) {
		// Costanti per calibrare la curva (es. con Destrezza 25 e Fortuna 25, il Ladro ha ~15% di critico)
		// Critico deve essere alimentato da due forze distinte:
		// DESTREZZA (Peso Maggiore - 70%): Rappresenta la precisione chirurgica nel colpire i punti vitali scoperti
		// (giugulare, fessure dell'armatura).
		// FORTUNA (Peso Minore - 30%): Rappresenta il fato favorevole che fa deviare il colpo all'ultimo millisecondo
		// nel punto giusto.
		final double COEFFICIENTE_DESTREZZA = 2.0;
		final double COEFFICIENTE_FORTUNA = 0.8;

		// Calcolo della precisione letale grezza con Diminishing Returns
		double precisioneGrezza = (COEFFICIENTE_DESTREZZA * Math.sqrt(get(md, PersonaggioMD::getDestrezza, TipoAttributo.DESTREZZA))) +
				(COEFFICIENTE_FORTUNA * Math.sqrt(get(md, PersonaggioMD::getFortuna, TipoAttributo.FORTUNA)));

		// Applicazione del moltiplicatore di archetipo
		double criticoFinale = precisioneGrezza * moltiplicatori.getMoltiplicatoreCritico();

		// Cap per evitare che superi il 100% (o il 95% se vuoi sempre un margine di fallimento)
		if (criticoFinale > 100.0) {
			criticoFinale = 100.0;
		}

		// Arrotondamento a due decimali
		return (int)criticoFinale;
	}

	public abstract double getMoltiplicatorePrecisione();

	private static int calcolaPrecisione(PersonaggioMD md, Personaggio moltiplicatori) {
		// Per rispecchiare la descrizione ("precisione oculare, stabilità della mano e coordinazione occhio-mano"),
		// la Precisione deve essere alimentata da due forze distinte:
		// DESTREZZA (Peso Maggiore - 80%): La coordinazione motoria fine, i riflessi e la fermezza muscolare.
		// INTELLIGENZA (Peso Minore - 20%): La capacità logica di calcolare la traiettoria del bersaglio, anticiparne
		// i movimenti e non farsi ingannare dalle illusioni.
		final double COEFFICIENTE_DESTREZZA = 0.8;
		final double COEFFICIENTE_INTELLIGENZA = 0.2;

		// Calcolo della precisione grezza con Diminishing Returns
		double precisioneGrezza = (COEFFICIENTE_DESTREZZA * Math.sqrt(get(md, PersonaggioMD::getDestrezza, TipoAttributo.DESTREZZA))) +
				(COEFFICIENTE_INTELLIGENZA * Math.sqrt(get(md, PersonaggioMD::getIntelligenza, TipoAttributo.INTELLIGENZA)));

		// Applicazione del moltiplicatore di archetipo
		double precisioneFinale = precisioneGrezza * moltiplicatori.getMoltiplicatorePrecisione();

		return (int)precisioneFinale;
	}

	public abstract double getMoltiplicatoreVelocita();

	private static int calcolaVelocita(PersonaggioMD md, Personaggio moltiplicatori) {
		// Per rispecchiare la descrizione ("precisione oculare, stabilità della mano e coordinazione occhio-mano"),
		// la Precisione deve essere alimentata da due forze distinte:
		// DESTREZZA (Peso Maggiore - 80%): La coordinazione motoria fine, i riflessi e la fermezza muscolare.
		// INTELLIGENZA (Peso Minore - 20%): La capacità logica di calcolare la traiettoria del bersaglio, anticiparne
		// i movimenti e non farsi ingannare dalle illusioni.
		final double COEFFICIENTE_DESTREZZA = 0.85;
		final double COEFFICIENTE_FORTUNA = 0.15;

		// Calcolo della precisione grezza con Diminishing Returns
		double velocitaGrezza = (COEFFICIENTE_DESTREZZA * Math.sqrt(get(md, PersonaggioMD::getDestrezza, TipoAttributo.DESTREZZA))) +
				(COEFFICIENTE_FORTUNA * Math.sqrt(get(md, PersonaggioMD::getFortuna, TipoAttributo.FORTUNA)));

		// Applicazione del moltiplicatore di archetipo
		double velocitaFinale = velocitaGrezza * moltiplicatori.getMoltiplicatoreVelocita();

		return (int)velocitaFinale;
	}

	public abstract double getMoltiplicatoreFurtivita();

	private static int calcolaFurtivita(PersonaggioMD md, Personaggio moltiplicatori) {
		// Per rispecchiare il concetto di "muoversi senza farsi notare e agire nell'ombra", la Furtività deve
		// attingere a due forze distinte:
		// DESTREZZA (Peso Maggiore - 75%): La grazia nei movimenti, il controllo totale del corpo e la coordinazione
		// millimetrica per non fare rumore (es. evitare rami secchi o passi pesanti).
		// FORTUNA (Peso Minore - 25%): Il tempismo perfetto che fa muovere il personaggio proprio quando la guardia
		// nemica si gira dall'altra parte o un rumore ambientale (es. un tuono o il vento) copre i suoi passi.
		final double COEFFICIENTE_DESTREZZA = 0.75;
		final double COEFFICIENTE_FORTUNA = 0.25;

		// Calcolo della furtivita grezza con Diminishing Returns
		double furtivitaGrezza = (COEFFICIENTE_DESTREZZA * Math.sqrt(get(md, PersonaggioMD::getDestrezza, TipoAttributo.DESTREZZA))) +
				(COEFFICIENTE_FORTUNA * Math.sqrt(get(md, PersonaggioMD::getFortuna, TipoAttributo.FORTUNA)));

		// Applicazione del moltiplicatore di archetipo
		double furtivitaFinale = furtivitaGrezza * moltiplicatori.getMoltiplicatoreFurtivita();

		return (int)furtivitaFinale;
	}

	public abstract double getMoltiplicatoreParata();

	private static int calcolaParata(PersonaggioMD md, Personaggio moltiplicatori) {
		// Per rispecchiare il concetto di "frapporre l'arma o lo scudo tra sé e il colpo nemico", la Parata
		// deve attingere a due forze distinte:
		// FORZA (Peso Maggiore - 70%): La potenza muscolare necessaria a reggere l'impatto di un colpo pesante senza
		// farsi spezzare la guardia.
		// DESTREZZA (Peso Minore - 30%): I riflessi e la coordinazione occhio-mano per posizionare lo scudo o la lama
		// nell'angolo esatto prima dell'impatto.
		final double COEFFICIENTE_FORZA = 0.70;
		final double COEFFICIENTE_DESTREZZA = 0.30;

		// Calcolo della precisione letale grezza con Diminishing Returns
		double parataGrezza = (COEFFICIENTE_FORZA * Math.sqrt(get(md, PersonaggioMD::getForza, TipoAttributo.FORZA))) +
				(COEFFICIENTE_DESTREZZA * Math.sqrt(get(md, PersonaggioMD::getDestrezza, TipoAttributo.DESTREZZA)));

		// Applicazione del moltiplicatore di archetipo
		double parataFinale = parataGrezza * moltiplicatori.getMoltiplicatoreParata();

		return (int)parataFinale;
	}

	public abstract double getMoltiplicatoreResistenzaMagica();

	private static int calcolaResistenzaMagica(PersonaggioMD md, Personaggio moltiplicatori) {
		// Per rispecchiare una difesa basata sul controllo dei flussi energetici e sulla fermezza d'animo, la
		// Resistenza Magica deve attingere a due forze della mente e dello spirito:
		// SAGGEZZA (Peso Maggiore - 70%): La consapevolezza spirituale e la connessione con il divino che agiscono come
		// uno scudo naturale contro le corruzioni dell'anima e gli anatemi.
		// INTELLIGENZA (Peso Minore - 30%): La comprensione logica della struttura degli incantesimi, che permette di
		// "dissipare" o deviare la trama magica prima dell'impatto.
		final double COEFFICIENTE_SAGGEZZA = 0.70;
		final double COEFFICIENTE_INTELLIGENZA = 0.30;

		// Calcolo della resistenza magica grezza con Diminishing Returns
		double resistenzaMagicaGrezza = (COEFFICIENTE_SAGGEZZA * Math.sqrt(get(md, PersonaggioMD::getSaggezza, TipoAttributo.SAGGEZZA))) +
				(COEFFICIENTE_INTELLIGENZA * Math.sqrt(get(md, PersonaggioMD::getIntelligenza, TipoAttributo.INTELLIGENZA)));

		// Applicazione del moltiplicatore di archetipo
		double resistenzaMagicaFinale = resistenzaMagicaGrezza * moltiplicatori.getMoltiplicatoreResistenzaMagica();

		return (int)resistenzaMagicaFinale;
	}

	public abstract double getMoltiplicatorePercezione();

	private static int calcolaPercezione(PersonaggioMD md, Personaggio moltiplicatori) {
		// Per rispecchiare fedelmente il concetto di "sensi acuti, vista sviluppata e udito sopraffino", la Percezione
		// deve attingere a due forze distinte:
		// SAGGEZZA (Peso Maggiore - 75%): La consapevolezza spirituale, l'intuito e la connessione con l'ambiente
		// circostante (il "sesto senso").
		// DESTREZZA (Peso Minore - 25%): La prontezza di riflessi oculari e la rapidità nel volgere lo sguardo o
		// l'orecchio verso uno stimolo improvviso.
		final double COEFFICIENTE_SAGGEZZA = 0.75;
		final double COEFFICIENTE_DESTREZZA = 0.25;

		// Calcolo della precisione letale grezza con Diminishing Returns
		double percezioneGrezza = (COEFFICIENTE_SAGGEZZA * Math.sqrt(get(md, PersonaggioMD::getSaggezza, TipoAttributo.SAGGEZZA))) +
				(COEFFICIENTE_DESTREZZA * Math.sqrt(get(md, PersonaggioMD::getDestrezza, TipoAttributo.DESTREZZA)));

		// Applicazione del moltiplicatore di archetipo
		double percezioneFinale = percezioneGrezza * moltiplicatori.getMoltiplicatorePercezione();

		return (int)percezioneFinale;
	}

	public abstract double getMoltiplicatoreSoggezione();

	private static int calcolaSoggezione(PersonaggioMD md, Personaggio moltiplicatori) {
		// Per rispecchiare il concetto di "forza della personalità combinata all'aura di terrore", la Soggezione
		// deve attingere a due forze distinte:
		// CARISMA (Peso Maggiore - 70%): Il magnetismo, la forza della personalità e la capacità di imporre la propria
		// volontà o presenza sugli altri.
		// FORZA (Peso Minore - 30%): La stazza e la potenza muscolare visibile che intimidiscono fisicamente chiunque
		// si trovi davanti.
		final double COEFFICIENTE_CARISMA = 0.70;
		final double COEFFICIENTE_FORZA = 0.30;

		// Calcolo della precisione letale grezza con Diminishing Returns
		double soggezioneGrezza = (COEFFICIENTE_CARISMA * Math.sqrt(get(md, PersonaggioMD::getCarisma, TipoAttributo.CARISMA))) +
				(COEFFICIENTE_FORZA * Math.sqrt(get(md, PersonaggioMD::getForza, TipoAttributo.FORZA)));

		// Applicazione del moltiplicatore di archetipo
		double soggezioneFinale = soggezioneGrezza * moltiplicatori.getMoltiplicatoreSoggezione();

		return (int)soggezioneFinale;
	}

	public abstract double getMoltiplicatoreFuria();

	private static int calcolaFuria(PersonaggioMD md, Personaggio moltiplicatori) {
		// Per rispecchiare una statistica basata sull'impulso distruttivo e sulla resistenza al dolore, la Furia
		// deve attingere a due forze puramente fisiche ed emotive:
		// FORZA (Peso Maggiore - 75%): La potenza muscolare grezza che alimenta la violenza dei colpi durante lo stato
		// di rabbia.
		// FORTUNA (Peso Minore - 25%): L'elemento caotico e imprevedibile del fato che premia l'audacia di chi attacca
		// alla cieca senza difendersi.
		final double COEFFICIENTE_FORZA = 0.75;
		final double COEFFICIENTE_FORTUNA = 0.25;

		// Calcolo della precisione letale grezza con Diminishing Returns
		double furiaGrezza = (COEFFICIENTE_FORZA * Math.sqrt(get(md, PersonaggioMD::getForza, TipoAttributo.FORZA))) +
				(COEFFICIENTE_FORTUNA * Math.sqrt(get(md, PersonaggioMD::getFortuna, TipoAttributo.FORTUNA)));

		// Applicazione del moltiplicatore di archetipo
		double furiaFinale = furiaGrezza * moltiplicatori.getMoltiplicatoreFuria();

		return (int)furiaFinale;
	}

	public abstract double getMoltiplicatoreCoraggio();

	private static int calcolaCoraggio(PersonaggioMD md, Personaggio moltiplicatori) {
		// Per rispecchiare il concetto di "forza della personalità e forza di volontà", il Coraggio deve attingere a
		// due forze della mente e dell'identità:
		// CARISMA (Peso Maggiore - 75%): La forza dell'ego e la stabilità della personalità, che impediscono al
		// personaggio di farsi intimidire o manipolare.
		// COSTITUZIONE (Peso Minore - 25%): La stabilità biologica (es. controllo del battito cardiaco e
		// dell'adrenalina), che impedisce al corpo di cedere al panico fisico.
		final double COEFFICIENTE_CARISMA = 0.75;
		final double COEFFICIENTE_COSTITUZIONE = 0.25;

		// Calcolo della precisione letale grezza con Diminishing Returns
		double coraggioGrezzo = (COEFFICIENTE_CARISMA * Math.sqrt(get(md, PersonaggioMD::getCarisma, TipoAttributo.CARISMA))) +
				(COEFFICIENTE_COSTITUZIONE * Math.sqrt(get(md, PersonaggioMD::getCostituzione, TipoAttributo.COSTITUZIONE)));

		// Applicazione del moltiplicatore di archetipo
		double coraggioFinale = coraggioGrezzo * moltiplicatori.getMoltiplicatoreCoraggio();

		return (int)coraggioFinale;
	}

	/**
	 * Solo chi può entrare nel gruppo (eroi e i pochi mostri che si uniscono per amicizia o per denaro) tratta con i
	 * mercanti: gli altri non hanno CONTRATTAZIONE e non ridefiniscono questo metodo.
	 */
	@Override
	public double getMoltiplicatoreContrattazione() {
		return 0;
	}

	@Override
	public String getNoteMoltiplicatoreContrattazione() {
		return "Non entra mai nel gruppo: non tratta con i mercanti.";
	}

	private static int calcolaContrattazione(PersonaggioMD md, Personaggio moltiplicatori) {
		// Per rispecchiare il concetto di "strappare un prezzo migliore a chi vende o compra", la Contrattazione
		// deve attingere a due forze distinte:
		// CARISMA (Peso Maggiore - 60%): La parlantina, la faccia tosta e la capacità di convincere il mercante che
		// quell'affare lo sta facendo lui.
		// FORTUNA (Peso Minore - 40%): Beccare il mercante nella giornata buona, notare il difetto sulla merce al
		// momento giusto, arrivare quando ha fretta di liberarsi del magazzino.
		final double COEFFICIENTE_CARISMA = 0.60;
		final double COEFFICIENTE_FORTUNA = 0.40;

		// Calcolo della contrattazione grezza con Diminishing Returns
		double contrattazioneGrezza = (COEFFICIENTE_CARISMA * Math.sqrt(get(md, PersonaggioMD::getCarisma, TipoAttributo.CARISMA))) +
				(COEFFICIENTE_FORTUNA * Math.sqrt(get(md, PersonaggioMD::getFortuna, TipoAttributo.FORTUNA)));

		// Applicazione del moltiplicatore di archetipo
		double contrattazioneFinale = contrattazioneGrezza * moltiplicatori.getMoltiplicatoreContrattazione();

		return (int)contrattazioneFinale;
	}

	public abstract double getMoltiplicatoreValore();

	private static int calcolaValore(PersonaggioMD md, Personaggio moltiplicatori) {
		// Per rispecchiare il concetto di "spirito di sacrificio ed eroismo guidato dalla stabilità biologica", il
		// Valore deve attingere a due forze distinte:
		// SAGGEZZA (Peso Maggiore - 70%): La consapevolezza morale, la connessione spirituale e la rettitudine che
		// spingono a compiere il gesto eroico o a difendere i deboli.
		// COSTITUZIONE (Peso Minore - 30%): La riserva di salute e la tempra fisica necessarie a sopportare l'impatto
		// dei danni mitigati o dei colpi intercettati per gli altri.
		final double COEFFICIENTE_SAGGEZZA = 0.70;
		final double COEFFICIENTE_COSTITUZIONE = 0.30;

		// Calcolo del valore grezzo con Diminishing Returns
		double valoreGrezzo = (COEFFICIENTE_SAGGEZZA * Math.sqrt(get(md, PersonaggioMD::getSaggezza, TipoAttributo.SAGGEZZA))) +
				(COEFFICIENTE_COSTITUZIONE * Math.sqrt(get(md, PersonaggioMD::getCostituzione, TipoAttributo.COSTITUZIONE)));

		// Applicazione del moltiplicatore di archetipo
		double valoreFinale = valoreGrezzo * moltiplicatori.getMoltiplicatoreValore();

		return (int)valoreFinale;
	}

	public abstract double getMoltiplicatoreNumeroBersagli();

	private static int calcolaNumeroBersagli(PersonaggioMD md, Personaggio moltiplicatori) {
		// Recuperiamo la classe per capire qual è la forza trainante del personaggio
		ClassePersonaggio classe = moltiplicatori.getClasse();

		double statPrincipale;
		double statSecondaria;

		// BIVIO DI BILANCIAMENTO IN BASE ALL'ARCHETIPO DI CLASSE
		switch (classe) {
			case MAGO:
			case MAGA:
			case LICH:
			case STREGA:
				// I maghi concatenano minacce con la mente: Intelligenza (70%) + Saggezza (30%)
				statPrincipale = get(md, PersonaggioMD::getIntelligenza, TipoAttributo.INTELLIGENZA);
				statSecondaria = get(md, PersonaggioMD::getSaggezza, TipoAttributo.SAGGEZZA);
				break;

			case BARDO:
			case CANTASTORIE:
				// I bardi ammaliano o spaventano folle intere: Carisma (70%) + Intelligenza (30%)
				statPrincipale = get(md, PersonaggioMD::getCarisma, TipoAttributo.CARISMA);
				statSecondaria = get(md, PersonaggioMD::getIntelligenza, TipoAttributo.INTELLIGENZA);
				break;

			case LADRO:
			case LADRA:
			case ELFO:
			case ELFA:
				// Classi agili (e il Ladro che mena a due mani): Destrezza (70%) + Fortuna/Forza (30%)
				statPrincipale = get(md, PersonaggioMD::getDestrezza, TipoAttributo.DESTREZZA);
				statSecondaria = get(md, PersonaggioMD::getForza, TipoAttributo.FORZA);
				break;

			case GUERRIERO:
			case GUERRIERA:
			case MINOTAURO:
			case MINOTAURO_GIGANTE:
			case GIGANTE:
			case TITANO:
			case DRAGO:
				// I bruti e i tank fisici: Forza (70%) + Costituzione (30%)
				statPrincipale = get(md, PersonaggioMD::getForza, TipoAttributo.FORZA);
				statSecondaria = get(md, PersonaggioMD::getCostituzione, TipoAttributo.COSTITUZIONE);
				break;

			default:
				// Fail-safe per mostri generici ed eremiti (Forza ed equilibrio mentale)
				statPrincipale = get(md, PersonaggioMD::getForza, TipoAttributo.FORZA);
				statSecondaria = get(md, PersonaggioMD::getIntelligenza, TipoAttributo.INTELLIGENZA);
				break;
		}

		// PESI DELLA FORMULA ADATTIVA (70% Potere di Classe, 30% Controllo Secondario)
		final double COEFFICIENTE_PRIMARIO = 0.70;
		final double COEFFICIENTE_SECONDARIO = 0.30;

		// Calcolo del valore grezzo con Diminishing Returns protetto da radice
		double numeroGrezzo = (COEFFICIENTE_PRIMARIO * Math.sqrt(statPrincipale)) +
				(COEFFICIENTE_SECONDARIO * Math.sqrt(statSecondaria));

		// Applicazione del moltiplicatore di archetipo (molto utile per i Boss o le abilità AoE)
		double valoreFinale = numeroGrezzo * moltiplicatori.getMoltiplicatoreNumeroBersagli();

		// Restituisce il numero di bersagli calcolato, garantendo il minimo di 1
		return (int) Math.max(1, Math.floor(valoreFinale));
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

	/**
	 * Riporta l'attributo del personaggio modificato sia dai modificatori locali che quelli degli artefatti
	 */
	private static int get(PersonaggioMD md, Function<PersonaggioMD, Integer> getterAttributo, TipoAttributo tipoAttributo) {
		return (int) getQuantitaModificata(md, getterAttributo.apply(md), tipoAttributo);
	}

	private static double getQuantitaModificata(PersonaggioMD md, double quantitaOriginale, TipoAttributo tipoAttributo) {
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

	public void addEffettoDiStato(TipoEffettoDiStato tipoEffettoDiStato, int durata, int danniNelTempo) {
		if (!isVivo()) {
			return;
		}
		if (durata <= 0) {
			throw new IllegalArgumentException("Valore effetto di stato non valido");
		}
		Collection<EffettoDiStato> effettiDiStato = md.getEffettiDiStato();
		Optional<EffettoDiStato> equivalenteOpt = effettiDiStato
				.stream().
				filter(e -> e.getTipoEffettoDiStato() == tipoEffettoDiStato)
				.findFirst();
		if (equivalenteOpt.isPresent()) {
			EffettoDiStato equivalente = equivalenteOpt.get();
			boolean duraDiPiu = equivalente.getDurata() < durata;
			boolean aumentaDanni = equivalente.getDanniNelTempo() < danniNelTempo;
			if (duraDiPiu || aumentaDanni) {
				if (duraDiPiu) {
					equivalente.setDurata(durata);
				}
				if (aumentaDanni) {
					equivalente.setDanniNelTempo(danniNelTempo);
				}
				BusEventi.pubblica(new NotificaVariazioneEffettoDiStatoPersonaggio(this,
						NotificaVariazioneEffettoDiStatoPersonaggio.TipoVariazione.VARIAZIONE, tipoEffettoDiStato,
						equivalente.getDurata(), equivalente.getDanniNelTempo()));
			}
		} else {
			md.getEffettiDiStato().add(new EffettoDiStato(tipoEffettoDiStato, durata, danniNelTempo));
			BusEventi.pubblica(new NotificaVariazioneEffettoDiStatoPersonaggio(this,
					NotificaVariazioneEffettoDiStatoPersonaggio.TipoVariazione.AGGIUNTA, tipoEffettoDiStato,
					-1, durata));
		}
	}

	@Override
	public void applicaDanniDaEffettiDiStato() {
		// Copia perché subSalute() può a sua volta aggiungere un nuovo effetto di stato
		// (es. BERSERK) alla stessa collezione che stiamo scorrendo.
		for (EffettoDiStato effettoDiStato : new ArrayList<>(getEffettiDiStato())) {
			if (effettoDiStato.getDanniNelTempo() > 0) {
				subSalute(effettoDiStato.getDanniNelTempo(), null,
						Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
			}
		}
	}

	@Override
	public void riduciEffettiDiStato() {
		List<EffettoDiStato> effettiDiStatoDaRimuovere = new ArrayList<>();
		for (EffettoDiStato effettoDiStato : getEffettiDiStato()) {
			int valorePrecedente = effettoDiStato.getDurata();
			int valoreAttuale = valorePrecedente - 1;
			if (valoreAttuale > 0) {
				effettoDiStato.setDurata(valoreAttuale);
				BusEventi.pubblica(new NotificaVariazioneEffettoDiStatoPersonaggio(this,
						NotificaVariazioneEffettoDiStatoPersonaggio.TipoVariazione.VARIAZIONE,
						effettoDiStato.getTipoEffettoDiStato(),
						valorePrecedente, valoreAttuale));
			} else {
				effettiDiStatoDaRimuovere.add(effettoDiStato);
			}
		}
		effettiDiStatoDaRimuovere.forEach(e -> {
			md.getEffettiDiStato().remove(e);
			BusEventi.pubblica(new NotificaVariazioneEffettoDiStatoPersonaggio(this,
					NotificaVariazioneEffettoDiStatoPersonaggio.TipoVariazione.RIMOZIONE,
					e.getTipoEffettoDiStato(), 1, 0));
		});
	}

	@Override
	public void rimuoviTuttiGliEffettiDiStato() {
		rimuoviEffettoDiStato(null);
	}

	@Override
	public void rimuoviEffettoDiStato(TipoEffettoDiStato tipoEffettoDiStato) {
		List<EffettoDiStato> effettiDiStatoDaRimuovere = getEffettiDiStato()
				.stream()
				.filter(e -> tipoEffettoDiStato == null || e.getTipoEffettoDiStato() == tipoEffettoDiStato)
				.collect(Collectors.toList());
		effettiDiStatoDaRimuovere.forEach(effettoDiStato -> {
			md.getEffettiDiStato().remove(effettoDiStato);
			BusEventi.pubblica(new NotificaVariazioneEffettoDiStatoPersonaggio(this,
					NotificaVariazioneEffettoDiStatoPersonaggio.TipoVariazione.RIMOZIONE, tipoEffettoDiStato,
					effettoDiStato.getDurata(), 0));
		});
	}

	@Override
	public boolean hasEffettoDiStato(TipoEffettoDiStato tipoEffettoDiStato) {
		return md.getEffettiDiStato().stream().anyMatch(e -> e.getTipoEffettoDiStato() == tipoEffettoDiStato);
	}

	@Override
	public int getQuantitaEffettoDiStato(TipoEffettoDiStato tipoEffettoDiStato) {
		return md.getEffettiDiStato().stream().filter(e -> e.getTipoEffettoDiStato() == tipoEffettoDiStato).mapToInt(EffettoDiStato::getDurata).sum();
	}

	// Scambiatore Artefatti

	@Override
	public List<Artefatto> getInventario() {
		return md.getArtefatti().stream().map(Artefatto::di).collect(Collectors.toList());
	}

	/**
	 * Aggiunge l'artefatto all'equipaggiamento e ne imposta lo slot occupato. I controlli
	 * ({@link #puoEquipaggiare}) spettano a chi chiama: se l'artefatto non passerebbe le regole,
	 * occupa lo slot del suo tipo.
	 */
	public void addArtefatto(Artefatto a) {
		ArtefattoMD artefattoMD = a.getModelloDati();
		SlotArtefatto slot = RegoleEquipaggiamento.valuta(getClasse(), getLivello(), md.getArtefatti(), artefattoMD).getSlot();
		artefattoMD.setSlotEquipaggiamento(slot != null ? slot : artefattoMD.getTipo().getSlotArtefatto());
		md.getArtefatti().add(artefattoMD);
		// I modificatori dell'artefatto sui primari cambiano anche i secondari che ne derivano
		ricalcolaAttributiSecondari();
		limitaAiMassimi();
	}

	public void removeArtefatto(Artefatto a) {
		md.getArtefatti().remove(a.getModelloDati());
		a.getModelloDati().setSlotEquipaggiamento(null);
		ricalcolaAttributiSecondari();
		limitaAiMassimi();
	}

	/**
	 * Togliendo un artefatto che dava SALUTE o MAGIA massima, i valori attuali possono superare i nuovi massimi: si
	 * riportano subito entro il limite (voluto: chi toglie l'artefatto ne perde il vantaggio).
	 */
	private void limitaAiMassimi() {
		int salute = md.getSalute();
		int saluteMassima = getSaluteMassima();
		if (salute > saluteMassima) {
			md.setSalute(saluteMassima);
			BusEventi.pubblica(new NotificaVariazioneStatistichePersonaggio(this, TipoAttributo.SALUTE, salute, saluteMassima));
		}
		int magia = md.getMagia();
		int magiaMassima = getMagiaMassima();
		if (magia > magiaMassima) {
			md.setMagia(magiaMassima);
			BusEventi.pubblica(new NotificaVariazioneStatistichePersonaggio(this, TipoAttributo.MAGIA, magia, magiaMassima));
		}
	}
}
