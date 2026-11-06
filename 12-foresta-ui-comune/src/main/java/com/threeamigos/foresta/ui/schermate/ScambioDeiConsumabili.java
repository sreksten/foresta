package com.threeamigos.foresta.ui.schermate;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoAcquistoConsumabile;
import com.threeamigos.foresta.eventi.interni.InternoNotificaViaFumettoATempo;
import com.threeamigos.foresta.eventi.notifiche.NotificaApprovazioneAcquistoConsumabile;
import com.threeamigos.foresta.eventi.notifiche.NotificaRifiutoAcquistoConsumabile;
import com.threeamigos.foresta.interfacce.VistaGruppoGiocatore;
import com.threeamigos.foresta.interfacce.VistaOffertaConsumabile;
import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.interfacce.VistaPersonaggio;
import com.threeamigos.foresta.tipi.TipoConsumabile;
import com.threeamigos.foresta.tipi.TipoIncantesimo;
import com.threeamigos.foresta.ui.Finestra;
import com.threeamigos.foresta.ui.Finestra.Tasto;
import com.threeamigos.foresta.ui.Punto;
import com.threeamigos.foresta.ui.componenti.ElencoScorrevole;
import com.threeamigos.foresta.ui.componenti.ElencoScorrevole.TestoDelNodo;
import com.threeamigos.foresta.ui.immagini.NomiDelleImmagini;
import com.threeamigos.foresta.ui.sprite.DescrizioneSpriteATempo;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;
import com.threeamigos.foresta.ui.testo.TestiDellAiuto;
import com.threeamigos.foresta.ui.testo.TestiDellaUI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Lo scambio di consumabili (incantesimi e pozioni) fra il gruppo, a sinistra, e l'alchimista, a destra, senza disegno: le due liste come alberi da far
 * scorrere ({@link ElencoScorrevole}) con il separatore fra incantesimi e pozioni, la voce sotto un punto, le righe dell'aiuto, il click che apre e chiude la
 * descrizione e il doppio click che compra. Reagisce alle notifiche di acquisto e di rifiuto con un fumetto e gli sprite (le monete spese e, per un
 * incantesimo, l'icona che sale dalla lista del gruppo). Il listino lo manda il motore con l'apertura della bottega: nomi, descrizioni e costi sono suoi.
 * <p>
 * Non è una finestra: lo tiene una schermata di scambio ({@link SchermataDiScambio}) a cui chiede le zone, il mouse e gli scorrimenti.
 */
public final class ScambioDeiConsumabili {

	/**
	 * Una voce di una delle due liste: quel che il gruppo ha, o quel che l'alchimista vende
	 */
	public static final class Voce {
		private final TipoConsumabile tipo;
		private final String nome;
		private final String descrizione;
		private final int quantita;
		private final TipoIncantesimo tipoIncantesimo;
		private final VistaPersonaggio personaggio;
		private final int costo;
		private final String icona;

		private Voce(TipoConsumabile tipo, String nome, String descrizione, int quantita, TipoIncantesimo tipoIncantesimo, VistaPersonaggio personaggio,
					 int costo, String icona) {
			this.tipo = tipo;
			this.nome = nome;
			this.descrizione = descrizione;
			this.quantita = quantita;
			this.tipoIncantesimo = tipoIncantesimo;
			this.personaggio = personaggio;
			this.costo = costo;
			this.icona = icona;
		}

		public TipoConsumabile getTipo() {
			return tipo;
		}

		public String getNome() {
			return nome;
		}

		public int getQuantita() {
			return quantita;
		}

		public TipoIncantesimo getTipoIncantesimo() {
			return tipoIncantesimo;
		}
	}

	private static final int INDENTAZIONE = 10;
	private static final int INTERLINEA = 1;

	private final SchermataDiScambio schermata;
	private final VistaPartita vistaPartita;
	private final MisureDelleImmagini misure;
	private final Consumer<DescrizioneSpriteATempo> spriteLocali;

	// Il listino che il motore ha mandato con l'apertura della bottega
	private List<? extends VistaOffertaConsumabile> offerte = Collections.emptyList();
	// Quali descrizioni sono aperte: TipoConsumabile da solo non basta come chiave (INCANTESIMO è lo stesso per tutti gli incantesimi, e AUMENTO_MAGIA_SINGOLO
	// per ogni personaggio), quindi si usa, quando c'è, il tratto che distingue le voci dello stesso tipo
	private final Map<Object, Boolean> descrizioniAperte = new HashMap<>();

	/**
	 * @param spriteLocali dove mandare gli sprite che restano confinati nella schermata (le monete spese, l'incantesimo comprato)
	 */
	public ScambioDeiConsumabili(SchermataDiScambio schermata, VistaPartita vistaPartita, MisureDelleImmagini misure, Consumer<DescrizioneSpriteATempo> spriteLocali) {
		this.schermata = schermata;
		this.vistaPartita = vistaPartita;
		this.misure = misure;
		this.spriteLocali = spriteLocali;
		BusEventi.iscriviti(NotificaApprovazioneAcquistoConsumabile.class, this::gestisciApprovazioneAcquisto);
		BusEventi.iscriviti(NotificaRifiutoAcquistoConsumabile.class, this::gestisciRifiutoAcquisto);
	}

	/**
	 * Imposta le offerte da mostrare.
	 */
	public void impostaOfferte(List<? extends VistaOffertaConsumabile> offerte) {
		this.offerte = offerte;
	}

	private void gestisciApprovazioneAcquisto(NotificaApprovazioneAcquistoConsumabile notifica) {
		BusEventi.pubblica(new InternoNotificaViaFumettoATempo(TestiDellaUI.get("CONSUMABILI_GRAZIE_ACQUISTO"), schermata.getCoordinateFumetto()));
		ComandoAcquistoConsumabile comando = notifica.getEventoRichiestaAcquistoConsumabile();
		String moneta = NomiDelleImmagini.SPRITE_MONETA;
		spriteLocali.accept(DescrizioneSpriteATempo.conVariazione(moneta, misure.immagine(moneta), -notifica.getPrezzoPagato(), schermata.font,
				schermata.xMassimaZonaCentrale, schermata.yRigaMonete(), false, "Monete spese"));
		if (comando.getTipoConsumabile() == TipoConsumabile.INCANTESIMO) {
			TipoIncantesimo classe = comando.getClasseIncantesimo();
			String icona = NomiDelleImmagini.iconaDelIncantesimo(classe);
			String descrizione = TestiDellaUI.get("INCANTESIMO_ACQUISTATO", classe.getNomeSingolare());
			// L'icona sale dal punto della lista del gruppo dove sta quell'incantesimo, se si vede; altrimenti dalla riga delle monete
			Punto posizione = posizioneIncantesimoNellElencoSinistro(classe);
			int x = posizione != null ? posizione.x : schermata.xMinimaZonaCentrale;
			int y = posizione != null ? posizione.y : schermata.yRigaMonete();
			spriteLocali.accept(DescrizioneSpriteATempo.conVariazione(icona, misure.immagine(icona), 1, schermata.font, x, y, true, descrizione));
		}
	}

	private void gestisciRifiutoAcquisto(NotificaRifiutoAcquistoConsumabile notifica) {
		BusEventi.pubblica(new InternoNotificaViaFumettoATempo(TestiDellaUI.get("CONSUMABILI_NON_ABBASTANZA_MONETE"), schermata.getCoordinateFumetto()));
	}

	/**
	 * Dove si vede, in coordinate della finestra, l'icona di quell'incantesimo nella lista di sinistra (l'inventario del gruppo), o null se non c'è o è
	 * fuori dalla porzione visibile.
	 */
	private Punto posizioneIncantesimoNellElencoSinistro(TipoIncantesimo tipoIncantesimo) {
		Punto posizione = costruisciElenco(getElencoGruppo(), null, false).posizioneTitolo(v -> v.tipoIncantesimo == tipoIncantesimo);
		if (posizione == null) {
			return null;
		}
		int relativo = posizione.y - schermata.offsetYZonaSinistra();
		if (relativo < 0 || relativo >= schermata.altezzaDisponibileInRiquadroInventario()) {
			return null;
		}
		return new Punto(schermata.xMinimaZonaSinistra + Finestra.SPACING + posizione.x,
				SchermataDiScambio.DIMENSIONE_BORDO_INTERNO + 2 * Finestra.SPACING + relativo);
	}

	/**
	 * Quel che il gruppo ha: si descrive come la voce del listino che lo vende (stesso nome, stessa descrizione)
	 */
	public List<Voce> getElencoGruppo() {
		List<Voce> elenco = new ArrayList<>();
		VistaGruppoGiocatore gruppo = vistaPartita.getGruppoGiocatore();
		for (TipoIncantesimo tipoIncantesimo : TipoIncantesimo.values()) {
			aggiungiSePosseduto(elenco, TipoConsumabile.INCANTESIMO, tipoIncantesimo, gruppo.getIncantesimi(tipoIncantesimo));
		}
		aggiungiSePosseduto(elenco, TipoConsumabile.POZIONE_SALUTE, null, gruppo.getPozioniSalute());
		aggiungiSePosseduto(elenco, TipoConsumabile.POZIONE_SALUTE_GRANDE, null, gruppo.getPozioniSaluteGrande());
		aggiungiSePosseduto(elenco, TipoConsumabile.POZIONE_MAGIA, null, gruppo.getPozioniMagia());
		aggiungiSePosseduto(elenco, TipoConsumabile.POZIONE_MAGIA_GRANDE, null, gruppo.getPozioniMagiaGrande());
		aggiungiSePosseduto(elenco, TipoConsumabile.POZIONE_PANACEA, null, gruppo.getPozioniPanacea());
		return elenco;
	}

	private void aggiungiSePosseduto(List<Voce> elenco, TipoConsumabile tipo, TipoIncantesimo tipoIncantesimo, int quantita) {
		if (quantita <= 0) {
			return;
		}
		for (VistaOffertaConsumabile offerta : offerte) {
			if (offerta.getTipo() == tipo && offerta.getClasseIncantesimo() == tipoIncantesimo) {
				elenco.add(daOfferta(offerta, quantita));
				return;
			}
		}
	}

	/**
	 * Quel che vende l'alchimista
	 */
	public List<Voce> getElencoVenditore() {
		List<Voce> elenco = new ArrayList<>();
		for (VistaOffertaConsumabile offerta : offerte) {
			elenco.add(daOfferta(offerta, 1));
		}
		return elenco;
	}

	private Voce daOfferta(VistaOffertaConsumabile offerta, int quantita) {
		return new Voce(offerta.getTipo(), offerta.getNome(), offerta.getDescrizione(), quantita, offerta.getClasseIncantesimo(), offerta.getPersonaggio(),
				offerta.getCosto(), iconaDi(offerta));
	}

	private static String iconaDi(VistaOffertaConsumabile offerta) {
		switch (offerta.getTipo()) {
			case INCANTESIMO:
				return NomiDelleImmagini.iconaDelIncantesimo(offerta.getClasseIncantesimo());
			case POZIONE_SALUTE:
				return NomiDelleImmagini.POZIONE_SALUTE;
			case POZIONE_SALUTE_GRANDE:
				return NomiDelleImmagini.POZIONE_SALUTE_GRANDE;
			case POZIONE_MAGIA:
				return NomiDelleImmagini.POZIONE_MAGIA;
			case POZIONE_MAGIA_GRANDE:
				return NomiDelleImmagini.POZIONE_MAGIA_GRANDE;
			case POZIONE_PANACEA:
				return NomiDelleImmagini.POZIONE_PANACEA;
			case AUMENTO_MAGIA_SINGOLO:
				return NomiDelleImmagini.iconaDelPersonaggio(offerta.getPersonaggio().getClasse());
			case AUMENTO_MAGIA_GRUPPO:
				return NomiDelleImmagini.SPRITE_GRUPPO;
			default:
				return NomiDelleImmagini.SPRITE_MAPPA;
		}
	}

	private static String separatoreDelTipo(TipoConsumabile tipo) {
		switch (tipo) {
			case INCANTESIMO:
				return NomiDelleImmagini.SEPARATORE_INCANTESIMI;
			case POZIONE_SALUTE:
				return NomiDelleImmagini.SEPARATORE_POZIONI;
			default:
				return null;
		}
	}

	private Object chiaveDellaDescrizione(Voce voce) {
		if (voce.tipoIncantesimo != null) {
			return voce.tipoIncantesimo;
		}
		if (voce.personaggio != null) {
			return voce.personaggio;
		}
		return voce.tipo;
	}

	private boolean isDescrizioneAperta(Voce voce) {
		return descrizioniAperte.getOrDefault(chiaveDellaDescrizione(voce), Boolean.TRUE);
	}

	/**
	 * L'albero di una delle due liste, con un separatore quando cambia il tipo. La voce passata in evidenziato (se non null) è disegnata in bianco invece che in
	 * grigio chiaro.
	 *
	 * @param mostraCosto vero per la lista dell'alchimista (accanto al nome il prezzo, in giallo), falso per quella del gruppo (la quantità)
	 */
	public ElencoScorrevole<Voce> costruisciElenco(List<Voce> voci, Voce evidenziata, boolean mostraCosto) {
		DoomdarkFont font = schermata.font;
		DoomdarkFont fontSmall = schermata.fontSmall;
		ElencoScorrevole<Voce> elenco = new ElencoScorrevole<>(schermata.larghezzaDisponibileInRiquadroInventario(), INDENTAZIONE, INTERLINEA);
		TipoConsumabile tipoPrecedente = null;
		for (Voce voce : voci) {
			if (tipoPrecedente != voce.tipo) {
				tipoPrecedente = voce.tipo;
				String separatore = separatoreDelTipo(tipoPrecedente);
				if (separatore != null) {
					elenco.creaSeparatore(ElencoDegliArtefatti.immagine(separatore, misure));
				}
			}
			DoomdarkColorModel.Color colore = voce == evidenziata ? DoomdarkColorModel.Color.WHITE : DoomdarkColorModel.Color.LIGHT_GRAY;
			String valore = String.valueOf(mostraCosto ? vistaPartita.prezzoAcquisto(voce.costo) : voce.quantita);
			ElencoScorrevole<Voce>.Nodo nodo = elenco.creaNodo(new TestoDelNodo(voce.nome, font, colore),
					new TestoDelNodo(valore, fontSmall, mostraCosto ? DoomdarkColorModel.Color.YELLOW : DoomdarkColorModel.Color.LIGHT_GRAY), null,
					new TestoDelNodo(voce.descrizione, fontSmall, colore), ElencoDegliArtefatti.immagine(voce.icona, misure), voce);
			nodo.setFigliVisibili(isDescrizioneAperta(voce));
		}
		return elenco;
	}

	/**
	 * La voce disegnata alla posizione (x, y) in coordinate della finestra, o null se il punto non cade sull'elenco o non su un titolo.
	 */
	public Voce trovaVoce(List<Voce> voci, int boxX, int offset, int x, int y, boolean mostraCosto) {
		int xInterno = x - (boxX + Finestra.SPACING);
		int yInterno = y - (SchermataDiScambio.DIMENSIONE_BORDO_INTERNO + 2 * Finestra.SPACING);
		if (xInterno < 0 || xInterno >= schermata.larghezzaDisponibileInRiquadroInventario()
				|| yInterno < 0 || yInterno >= schermata.altezzaDisponibileInRiquadroInventario()) {
			return null;
		}
		return costruisciElenco(voci, null, mostraCosto).riferimentoTitoloAllaQuota(yInterno + offset);
	}

	/**
	 * La voce sotto il mouse nella lista del gruppo (null se nessuna): serve a evidenziarla
	 */
	public Voce voceDelGruppoSottoIlMouse(List<Voce> voci, int offset) {
		return trovaVoce(voci, schermata.xMinimaZonaSinistra, offset, schermata.mouseX, schermata.mouseY, false);
	}

	/**
	 * La voce sotto il mouse nella lista dell'alchimista
	 */
	public Voce voceDelVenditoreSottoIlMouse(List<Voce> voci, int offset) {
		return trovaVoce(voci, schermata.xMinimaZonaDestra, offset, schermata.mouseX, schermata.mouseY, true);
	}

	/**
	 * Le righe di aiuto per l'elenco sotto il mouse: su un consumabile il click apre e chiude la sua descrizione, e quelli dell'alchimista, a destra, si
	 * comprano con il doppio click (i consumabili del gruppo non si vendono); ovunque nell'elenco, se si scorre, la rotella
	 */
	public List<String> righeAiuto() {
		List<String> righe = new ArrayList<>();
		if (schermata.dentroElenco(schermata.xMinimaZonaSinistra, schermata.mouseX, schermata.mouseY)) {
			List<Voce> gruppo = getElencoGruppo();
			Voce voce = voceDelGruppoSottoIlMouse(gruppo, schermata.offsetYZonaSinistra());
			if (voce != null) {
				righe.add(TestiDellAiuto.click(isDescrizioneAperta(voce), TestiDellaUI.get("COSA_DESCRIZIONE")));
			}
			aggiungiAiutoRotella(righe, gruppo, false);
		} else if (schermata.dentroElenco(schermata.xMinimaZonaDestra, schermata.mouseX, schermata.mouseY)) {
			List<Voce> venditore = getElencoVenditore();
			Voce voce = voceDelVenditoreSottoIlMouse(venditore, schermata.offsetYZonaDestra());
			if (voce != null) {
				righe.add(TestiDellAiuto.click(isDescrizioneAperta(voce), TestiDellaUI.get("COSA_DESCRIZIONE")));
				righe.add(TestiDellaUI.get("AIUTO_DOPPIO_CLICK_ACQUISTA"));
			}
			aggiungiAiutoRotella(righe, venditore, true);
		}
		return righe;
	}

	private void aggiungiAiutoRotella(List<String> righe, List<Voce> voci, boolean mostraCosto) {
		if (costruisciElenco(voci, null, mostraCosto).isScorrevole(schermata.altezzaDisponibileInRiquadroInventario())) {
			righe.add(TestiDellAiuto.rotella());
		}
	}

	/**
	 * Un click sul titolo di una voce apre o chiude la sua descrizione
	 */
	public void processaClick(int x, int y, Tasto tasto) {
		if (tasto != Tasto.SINISTRO || schermata.isGestoTrascinato()) {
			return;
		}
		Voce voce = trovaVoce(getElencoGruppo(), schermata.xMinimaZonaSinistra, schermata.offsetYZonaSinistra(), x, y, false);
		if (voce == null) {
			voce = trovaVoce(getElencoVenditore(), schermata.xMinimaZonaDestra, schermata.offsetYZonaDestra(), x, y, true);
		}
		if (voce != null) {
			descrizioniAperte.put(chiaveDellaDescrizione(voce), !isDescrizioneAperta(voce));
		}
	}

	/**
	 * Un doppio click su una voce dell'alchimista la compra (solo con il tasto sinistro: un doppio click destro o centrale non compra)
	 */
	public void processaDoppioClick(int x, int y, Tasto tasto) {
		if (tasto != Tasto.SINISTRO || schermata.isGestoTrascinato()) {
			return;
		}
		Voce voce = trovaVoce(getElencoVenditore(), schermata.xMinimaZonaDestra, schermata.offsetYZonaDestra(), x, y, true);
		if (voce != null) {
			BusEventi.pubblica(new ComandoAcquistoConsumabile(voce.tipo, voce.tipoIncantesimo, voce.personaggio == null ? null : voce.personaggio.getUuid()));
		}
	}
}
