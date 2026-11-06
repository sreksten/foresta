package com.threeamigos.foresta.ui.riquadri;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoCreazioneSpriteATempo;
import com.threeamigos.foresta.eventi.notifiche.NotificaAumentoLivelloPersonaggio;
import com.threeamigos.foresta.eventi.notifiche.NotificaVariazioneStatistichePersonaggio;
import com.threeamigos.foresta.interfacce.VistaGruppoGiocatore;
import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.interfacce.VistaPersonaggio;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.ui.Area;
import com.threeamigos.foresta.ui.Finestra;
import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.immagini.NomiDelleImmagini;
import com.threeamigos.foresta.ui.sprite.DescrizioneSpriteATempo;
import com.threeamigos.foresta.ui.componenti.ScorrimentoSuUnAsse;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;
import com.threeamigos.foresta.ui.testo.TestiDellAiuto;
import com.threeamigos.foresta.ui.testo.TestiDellaUI;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Il riquadro con lo stato dei personaggi del gruppo (nome, salute, magia, livello e le altre statistiche; gli ospiti solo col nome e la salute), senza
 * disegno: dove sta ogni valore, di che colore, quali blocchi si vedono con la lista scorsa, e lo scorrimento (rotella e trascinamento con inerzia ed
 * elastico). Chi disegna (Swing, libGDX) si fa dare da {@link #voci()} le scritte da mettere, già con posizione, colore e allineamento, dopo aver chiamato
 * {@link #sincronizzaScorrimento()}.
 * <p>
 * Ogni personaggio (o ospite) occupa tre righe di testo del font medio. La lista sta dentro la cornice grande, a {@link #BORDO_INTERNO} pixel dal bordo.
 */
public abstract class RiquadroGruppo implements Finestra {

	/**
	 * Lo spessore del bordo interno della cornice grande: la lista parte da lì
	 */
	public static final int BORDO_INTERNO = 16;

	// Pixel di scorrimento per ogni scatto della rotella, come per le missioni
	private static final int PASSO_SCORRIMENTO = 2;

	private final int topLeftX;
	private int topLeftY;
	private final int larghezzaCornice;
	private final int innerWidth;
	protected final DoomdarkFont fontMedium = DoomdarkFont.medio();

	// Le colonne dei valori: l'etichetta parte a sinistra, il valore si allinea a destra
	protected final int leftXOffsetLabelSalute;
	protected final int rightXOffsetSalute;
	protected final int leftXOffsetSeparatoreSalute;
	protected final int rightXOffsetSaluteMassima;
	protected final int leftXOffsetLabelMagia;
	protected final int rightXOffsetMagia;
	protected final int leftXOffsetSeparatoreMagia;
	protected final int rightXOffsetMagiaMassima;
	protected final int leftXOffsetLabelLivello;
	protected final int rightXOffsetLivello;
	protected final int leftXOffsetLabelCoraggio;
	protected final int rightXOffsetCoraggio;
	protected final int leftXOffsetLabelValore;
	protected final int rightXOffsetValore;
	protected final int leftXOffsetLabelStanchezza;
	protected final int rightXOffsetStanchezza;
	protected final int leftXOffsetLabelCarisma;
	protected final int rightXOffsetCarisma;

	// Ogni personaggio (o ospite) occupa tre righe
	private final int altezzaBlocco;
	private final int listaX;
	private int listaY;
	private final int listaLarghezza;
	private final int listaAltezza;
	// Quanto la lista è scorsa: con la rotella e trascinandola (anche col dito, e prosegue per inerzia)
	private final ScorrimentoSuUnAsse scorrimento = new ScorrimentoSuUnAsse(System::nanoTime);

	// Dove sta il mouse, relativo al riquadro, per l'aiuto (-1 fuori)
	private int mouseX = -1;
	private int mouseY = -1;

	protected final VistaPartita vistaPartita;

	/**
	 * @param larghezzaCornice larghezza della cornice grande, già con lo zoom (il riquadro occupa tutta la cornice)
	 * @param altezzaCornice   altezza della cornice grande, già con lo zoom
	 */
	public RiquadroGruppo(int topLeftX, int topLeftY, int larghezzaCornice, int altezzaCornice, VistaPartita vistaPartita) {
		this.vistaPartita = vistaPartita;
		this.topLeftX = topLeftX;
		this.topLeftY = topLeftY;
		this.larghezzaCornice = larghezzaCornice;
		innerWidth = larghezzaCornice - (BORDO_INTERNO << 1);

		int glyph9Width = fontMedium.getGlyphWidth('9');

		// Fr:999/999 Mg:99/99 Lv: 1
		leftXOffsetLabelSalute = topLeftX + BORDO_INTERNO;
		rightXOffsetSalute = topLeftX + BORDO_INTERNO + glyph9Width * 6;
		leftXOffsetSeparatoreSalute = rightXOffsetSalute;
		rightXOffsetSaluteMassima = topLeftX + BORDO_INTERNO + glyph9Width * 10;
		leftXOffsetLabelMagia = topLeftX + BORDO_INTERNO + glyph9Width * 11;
		rightXOffsetMagia = topLeftX + BORDO_INTERNO + glyph9Width * 16;
		leftXOffsetSeparatoreMagia = rightXOffsetMagia;
		rightXOffsetMagiaMassima = topLeftX + BORDO_INTERNO + glyph9Width * 19;
		leftXOffsetLabelLivello = topLeftX + BORDO_INTERNO + glyph9Width * 20;
		rightXOffsetLivello = topLeftX + BORDO_INTERNO + glyph9Width * 24;

		// Cr:99 Vl:99 St:99 Ca:99
		leftXOffsetLabelCoraggio = topLeftX + BORDO_INTERNO;
		rightXOffsetCoraggio = topLeftX + BORDO_INTERNO + glyph9Width * 5;
		leftXOffsetLabelValore = topLeftX + BORDO_INTERNO + glyph9Width * 6;
		rightXOffsetValore = topLeftX + BORDO_INTERNO + glyph9Width * 11;
		leftXOffsetLabelStanchezza = topLeftX + BORDO_INTERNO + glyph9Width * 12;
		rightXOffsetStanchezza = topLeftX + BORDO_INTERNO + glyph9Width * 17;
		leftXOffsetLabelCarisma = topLeftX + BORDO_INTERNO + glyph9Width * 18;
		rightXOffsetCarisma = topLeftX + BORDO_INTERNO + glyph9Width * 23;

		altezzaBlocco = fontMedium.getHeight() * 3;
		listaX = topLeftX + BORDO_INTERNO;
		listaY = topLeftY + BORDO_INTERNO;
		listaLarghezza = innerWidth;
		listaAltezza = altezzaCornice - (BORDO_INTERNO << 1);
	}

	public int getTopLeftX() {
		return topLeftX;
	}

	public int getTopLeftY() {
		return topLeftY;
	}

	public int getLarghezzaCornice() {
		return larghezzaCornice;
	}

	public int getMouseX() {
		return mouseX;
	}

	public int getMouseY() {
		return mouseY;
	}

	/**
	 * La parte del riquadro in cui sta la lista dei personaggi (dentro il bordo): fuori da lì il testo si ritaglia
	 */
	public Area getAreaLista() {
		return new Area(listaX, listaY, listaLarghezza, listaAltezza);
	}

	/**
	 * Sposta il riquadro in verticale: topLeftY è la nuova quota del bordo superiore della cornice
	 */
	public void spostaInVerticale(int topLeftY) {
		this.topLeftY = topLeftY;
		listaY = topLeftY + BORDO_INTERNO;
	}

	private int numeroBlocchi() {
		VistaGruppoGiocatore g = vistaPartita.getGruppoGiocatore();
		return g.getNumeroPersonaggi() + g.getOspiti().size();
	}

	/**
	 * Di quanti pixel si può scorrere al massimo la lista
	 */
	protected int offsetMassimo() {
		return Math.max(0, numeroBlocchi() * altezzaBlocco - listaAltezza);
	}

	/**
	 * Di quanti pixel la lista è scorsa
	 */
	protected int offsetY() {
		return (int) Math.round(scorrimento.getScorrimento());
	}

	public boolean isScorribileSu() {
		return offsetY() > 0;
	}

	public boolean isScorribileGiu() {
		return offsetY() < offsetMassimo();
	}

	/**
	 * Se i personaggi (ospiti compresi) non ci stanno tutti e la rotella serve
	 */
	/**
	 * Ad aiuto acceso, le righe del cartiglio accanto al mouse: la rotella, se la lista è più alta del riquadro. Vuota se non c'è
	 * niente da dire; il cartiglio va accanto al mouse ({@link #getMouseX()}, {@link #getMouseY()}, relativi al riquadro).
	 */
	public List<String> righeDellAiuto() {
		List<String> righe = new ArrayList<>();
		if (vistaPartita.isAiutoAbilitato() && mouseX >= 0 && isScorrevole()) {
			righe.add(TestiDellAiuto.rotella());
		}
		return righe;
	}

	public boolean isScorrevole() {
		return offsetMassimo() > 0;
	}

	/**
	 * L'elenco può essere cambiato dall'ultimo scorrimento: l'offset va rimesso nei limiti e il movimento aggiornato. Da chiamare prima di ogni
	 * disegno.
	 */
	protected void sincronizzaScorrimento() {
		scorrimento.imposta(offsetMassimo() > 0, offsetMassimo());
		scorrimento.aggiorna();
	}

	/**
	 * La riga del blocco di un personaggio in cui è scritto un valore: 0 il nome, 1 salute, magia e livello, 2 coraggio, valore, stanchezza e
	 * carisma (vedi {@link #voci()}). Serve a mettere lo sprite di una variazione sopra il valore che cambia.
	 */
	static int rigaDelValore(TipoAttributo attributo) {
		switch (attributo) {
			case SALUTE:
			case SALUTE_MASSIMA:
			case MAGIA:
			case MAGIA_MASSIMA:
			case LIVELLO:
				return 1;
			case CORAGGIO:
			case VALORE:
			case STANCHEZZA:
			case CARISMA:
				return 2;
			default:
				throw new IllegalArgumentException("Il riquadro del gruppo non mostra " + attributo);
		}
	}

	/**
	 * Quanto è grande l'icona di uno sprite (<code>icone/Combattimento.png</code>…), come la tiene chi disegna
	 */
	protected abstract Misura misuraDellIcona(String risorsa);

	/**
	 * Segnala sul riquadro che un personaggio è salito di livello: pubblica lo sprite della variazione
	 */
	public void gestisciEventoAumentoLivelloPersonaggio(NotificaAumentoLivelloPersonaggio evento) {
		pubblica(spriteDellaVariazione(evento.getPersonaggio(), evento.getLivelloAttuale() - evento.getLivelloPrecedente(), TipoAttributo.LIVELLO,
				NomiDelleImmagini.POZIONE_AUMENTO_LIVELLO, rightXOffsetLivello, null, "Livello variato"));
	}

	/**
	 * Segnala le variazioni delle statistiche di un personaggio del gruppo con lo sprite sopra il valore che cambia (gli attributi che questo riquadro non
	 * mostra non fanno niente)
	 */
	public void gestisciEventoVariazioneStatistichePersonaggio(NotificaVariazioneStatistichePersonaggio evento) {
		if (!vistaPartita.getGruppoGiocatore().contiene(evento.getPersonaggio())) {
			return;
		}
		VistaPersonaggio personaggio = evento.getPersonaggio();
		int variazione = (int) (evento.getNuovoValore() - evento.getValorePrecedente());
		switch (evento.getTipoAttributo()) {
			case SALUTE:
				pubblica(spriteDellaVariazione(personaggio, variazione, TipoAttributo.SALUTE, NomiDelleImmagini.SPRITE_COMBATTIMENTO, rightXOffsetSalute, null,
						"Salute variata"));
				break;
			case SALUTE_MASSIMA:
				pubblica(spriteDellaVariazione(personaggio, variazione, TipoAttributo.SALUTE_MASSIMA, NomiDelleImmagini.SPRITE_COMBATTIMENTO,
						rightXOffsetSaluteMassima, null, "Salute massima variata"));
				break;
			case MAGIA:
				pubblica(spriteDellaVariazione(personaggio, variazione, TipoAttributo.MAGIA, NomiDelleImmagini.SPRITE_MAGIA, rightXOffsetMagia, null,
						"Magia variata"));
				break;
			case MAGIA_MASSIMA:
				pubblica(spriteDellaVariazione(personaggio, variazione, TipoAttributo.MAGIA_MASSIMA, NomiDelleImmagini.SPRITE_MAGIA, rightXOffsetMagiaMassima,
						null, "Magia massima variata"));
				break;
			case CORAGGIO:
				pubblica(spriteDellaVariazione(personaggio, variazione, TipoAttributo.CORAGGIO, NomiDelleImmagini.SPRITE_COMBATTIMENTO, rightXOffsetCoraggio,
						null, "Coraggio variato"));
				break;
			case VALORE:
				pubblica(spriteDellaVariazione(personaggio, variazione, TipoAttributo.VALORE, NomiDelleImmagini.SPRITE_COMBATTIMENTO, rightXOffsetValore, null,
						"Valore variato"));
				break;
			case CARISMA:
				pubblica(spriteDellaVariazione(personaggio, variazione, TipoAttributo.CARISMA, NomiDelleImmagini.SPRITE_AMICIZIA, rightXOffsetCarisma, null,
						"Carisma variato"));
				break;
			case STANCHEZZA:
				// Più stanchezza è peggio: i colori sono al contrario
				pubblica(spriteDellaVariazione(personaggio, variazione, TipoAttributo.STANCHEZZA, NomiDelleImmagini.SPRITE_COMBATTIMENTO,
						rightXOffsetStanchezza, variazione < 0 ? DoomdarkColorModel.Color.GREEN : DoomdarkColorModel.Color.RED, "Stanchezza variata"));
				break;
			case TEMPO:
				pubblica(spriteDelTempo(personaggio, variazione));
				break;
			default:
				// Altri attributi non sono gestiti da questa finestra
				break;
		}
	}

	private static void pubblica(DescrizioneSpriteATempo sprite) {
		if (sprite != null) {
			BusEventi.pubblica(new InternoCreazioneSpriteATempo(sprite));
		}
	}

	/**
	 * Lo sprite della variazione di un attributo, sopra il suo valore (che sta alla riga {@link #rigaDelValore}, col bordo destro in xDestra): parte dalla quota
	 * giusta anche se il blocco non si vede tutto (o per niente) perché la lista è scorsa, e si vede solo la parte dentro la lista. Nessuno sprite se non è
	 * cambiato niente o se il personaggio non è nella lista.
	 *
	 * @param colore il colore del testo, o null per verde se la variazione non è negativa e rosso se lo è
	 */
	private DescrizioneSpriteATempo spriteDellaVariazione(VistaPersonaggio personaggio, int variazione, TipoAttributo attributo, String icona, int xDestra,
														  DoomdarkColorModel.Color colore, String descrizione) {
		if (variazione == 0) {
			return null;
		}
		int riga = getOrdinalePersonaggio(personaggio);
		if (riga == -1) {
			return null;
		}
		int y = yDelBlocco(riga) + fontMedium.getHeight() * rigaDelValore(attributo);
		DescrizioneSpriteATempo sprite = colore == null
				? DescrizioneSpriteATempo.conVariazione(icona, misuraDellIcona(icona), variazione, fontMedium, xDestra, y, false, descrizione)
				: DescrizioneSpriteATempo.conVariazione(icona, misuraDellIcona(icona), variazione, fontMedium, colore, xDestra, y, false, descrizione);
		return sprite.conRitaglio(getAreaLista());
	}

	/**
	 * Lo sprite della variazione del tempo di un personaggio: sta in cima al suo blocco, a metà della lista
	 */
	private DescrizioneSpriteATempo spriteDelTempo(VistaPersonaggio personaggio, int variazione) {
		if (variazione == 0) {
			return null;
		}
		int ordinale = getOrdinalePersonaggio(personaggio);
		if (ordinale == -1) {
			return null;
		}
		String icona = NomiDelleImmagini.SPRITE_TEMPO;
		return DescrizioneSpriteATempo.conVariazione(icona, misuraDellIcona(icona), variazione, fontMedium,
				getTopLeftX() + ((getLarghezzaCornice() - (BORDO_INTERNO << 1)) >> 1), yDelBlocco(ordinale), false, "Tempo variato")
				.conRitaglio(getAreaLista());
	}

	/**
	 * La quota a cui è disegnato il blocco di un personaggio (o ospite), tenendo conto dello scorrimento: può essere fuori dalla lista
	 */
	protected int yDelBlocco(int indice) {
		// Il corpo del testo parte dove partiva: le righe in più per gli accenti salgono sopra
		return listaY + indice * altezzaBlocco - offsetY() - fontMedium.getRigheInCima();
	}

	private boolean bloccoVisibile(int indice) {
		int y = yDelBlocco(indice);
		return y + altezzaBlocco > listaY && y < listaY + listaAltezza;
	}

	/**
	 * Il colore di un valore: giallo se un modificatore porta l'attributo a un valore fisso, verde o rosso se i modificatori temporanei lo alzano o lo
	 * abbassano (i dettagli sono nella scheda del personaggio)
	 */
	private static DoomdarkColorModel.Color colorePerAttributo(VistaPersonaggio p, TipoAttributo attributo, DoomdarkColorModel.Color colore) {
		if (p.getFissaggio(attributo).isPresent()) {
			return DoomdarkColorModel.Color.YELLOW;
		}
		double variazione = p.getVariazioneDaTemporanei(attributo);
		return variazione > 0 ? DoomdarkColorModel.Color.GREEN : variazione < 0 ? DoomdarkColorModel.Color.RED : colore;
	}

	protected int getOrdinalePersonaggio(VistaPersonaggio personaggio) {
		VistaGruppoGiocatore gruppo = vistaPartita.getGruppoGiocatore();
		for (int i = 0; i < gruppo.getNumeroPersonaggi(); i++) {
			if (gruppo.getPersonaggio(i).equals(personaggio)) {
				return i;
			}
		}
		return -1;
	}

	private static String nomeCompleto(VistaPersonaggio p) {
		Optional<String> nomeProprio = p.getNomeProprio();
		return nomeProprio.isPresent() ? nomeProprio.get() + "-" + p.getNomeSingolare() : p.getNomeSingolare();
	}

	private void aggiungi(List<VoceDiTesto> voci, String testo, int x, int y, DoomdarkColorModel.Color colore) {
		voci.add(new VoceDiTesto(testo, x, y, colore, false, 0));
	}

	private void aggiungiADestra(List<VoceDiTesto> voci, String testo, int xDestra, int y, DoomdarkColorModel.Color colore) {
		voci.add(new VoceDiTesto(testo, xDestra, y, colore, true, 0));
	}

	/**
	 * Le scritte da disegnare con la lista nella posizione di scorrimento corrente: solo quelle dei blocchi che si vedono, almeno in parte. Il
	 * ritaglio al rettangolo della lista lo fa chi disegna ({@link #getAreaLista()}).
	 */
	public List<VoceDiTesto> voci() {
		List<VoceDiTesto> voci = new ArrayList<>();
		VistaGruppoGiocatore g = vistaPartita.getGruppoGiocatore();
		int altezzaRiga = fontMedium.getHeight();
		int x = listaX;
		int l = g.getNumeroPersonaggi();
		List<? extends VistaPersonaggio> ospiti = g.getOspiti();
		for (int i = 0; i < l; i++) {
			if (!bloccoVisibile(i)) {
				continue;
			}
			int y = yDelBlocco(i);
			VistaPersonaggio p = g.getPersonaggio(i);
			String nome = nomeCompleto(p);
			if (!p.isVivo()) {
				aggiungi(voci, nome, x, y, DoomdarkColorModel.Color.DARK_GRAY);
				voci.add(new VoceDiTesto(p.getCausaTrapasso(), x, y + altezzaRiga, DoomdarkColorModel.Color.DARK_GRAY, false, innerWidth));
				continue;
			}
			DoomdarkColorModel.Color color = i % 2 == 0 ? DoomdarkColorModel.Color.MEDIUM_GRAY : DoomdarkColorModel.Color.LIGHT_GRAY;
			aggiungi(voci, nome, x, y, color);

			y += altezzaRiga;
			aggiungi(voci, TestiDellaUI.get("SIGLA_SALUTE"), leftXOffsetLabelSalute, y, color);
			aggiungiADestra(voci, String.valueOf(p.getSalute()), rightXOffsetSalute, y, color);
			aggiungi(voci, "/", leftXOffsetSeparatoreSalute, y, color);
			aggiungiADestra(voci, String.valueOf(p.getSaluteMassima()), rightXOffsetSaluteMassima, y, colorePerAttributo(p, TipoAttributo.SALUTE, color));

			aggiungi(voci, TestiDellaUI.get("SIGLA_MAGIA"), leftXOffsetLabelMagia, y, color);
			aggiungiADestra(voci, String.valueOf(p.getMagia()), rightXOffsetMagia, y, color);
			aggiungi(voci, "/", leftXOffsetSeparatoreMagia, y, color);
			aggiungiADestra(voci, String.valueOf(p.getMagiaMassima()), rightXOffsetMagiaMassima, y, colorePerAttributo(p, TipoAttributo.MAGIA, color));

			aggiungi(voci, TestiDellaUI.get("SIGLA_LIVELLO"), leftXOffsetLabelLivello, y, color);
			aggiungiADestra(voci, String.valueOf(p.getLivello()), rightXOffsetLivello, y, color);

			y += altezzaRiga;
			aggiungi(voci, TestiDellaUI.get("SIGLA_CORAGGIO"), leftXOffsetLabelCoraggio, y, color);
			aggiungiADestra(voci, String.valueOf(p.get(TipoAttributo.CORAGGIO)), rightXOffsetCoraggio, y, colorePerAttributo(p, TipoAttributo.CORAGGIO, color));

			aggiungi(voci, TestiDellaUI.get("SIGLA_VALORE"), leftXOffsetLabelValore, y, color);
			aggiungiADestra(voci, String.valueOf(p.get(TipoAttributo.VALORE)), rightXOffsetValore, y, colorePerAttributo(p, TipoAttributo.VALORE, color));

			aggiungi(voci, TestiDellaUI.get("SIGLA_STANCHEZZA"), leftXOffsetLabelStanchezza, y, color);
			aggiungiADestra(voci, String.valueOf(p.get(TipoAttributo.STANCHEZZA)), rightXOffsetStanchezza, y, color);

			aggiungi(voci, TestiDellaUI.get("SIGLA_CARISMA"), leftXOffsetLabelCarisma, y, color);
			aggiungiADestra(voci, String.valueOf(p.get(TipoAttributo.CARISMA)), rightXOffsetCarisma, y, colorePerAttributo(p, TipoAttributo.CARISMA, color));
		}
		// Dopo i personaggi, gli ospiti: solo il nome, perché non combattono
		for (int i = 0; i < ospiti.size(); i++) {
			if (!bloccoVisibile(l + i)) {
				continue;
			}
			int y = yDelBlocco(l + i);
			VistaPersonaggio ospite = ospiti.get(i);
			DoomdarkColorModel.Color colore = ospite.isVivo() ? DoomdarkColorModel.Color.MEDIUM_GRAY : DoomdarkColorModel.Color.DARK_GRAY;
			aggiungi(voci, nomeCompleto(ospite), x, y, colore);
			// Di un ospite che si può ferire si vede la salute
			String stato;
			if (!ospite.isVivo()) {
				stato = ospite.getCausaTrapasso();
			} else if (g.isOspiteVulnerabile(ospite)) {
				stato = TestiDellaUI.get("OSPITE_SALUTE", ospite.getSalute(), ospite.getSaluteMassima());
			} else {
				stato = TestiDellaUI.get("OSPITE_DEL_GRUPPO");
			}
			voci.add(new VoceDiTesto(stato, x, y + altezzaRiga, DoomdarkColorModel.Color.DARK_GRAY, false, innerWidth));
		}
		return voci;
	}

	// ---- il mouse e lo scorrimento ----

	@Override
	public void processaMovimento(int x, int y) {
		mouseX = x;
		mouseY = y;
	}

	@Override
	public void processaUscita(int x, int y) {
		mouseX = -1;
		mouseY = -1;
	}

	/**
	 * Il gesto che comincia sul riquadro prosegue anche se il puntatore ne esce, finché non si lascia
	 */
	@Override
	public boolean catturaIlTrascinamento() {
		return true;
	}

	@Override
	public void processaPressione(int x, int y, Tasto tasto) {
		if (tasto == Tasto.SINISTRO) {
			scorrimento.premi(y, true);
		}
	}

	@Override
	public void processaTrascinamento(int x, int y) {
		scorrimento.trascina(y);
	}

	@Override
	public void processaRilascio(int x, int y, Tasto tasto) {
		if (tasto == Tasto.SINISTRO) {
			scorrimento.rilascia();
		}
	}

	@Override
	public int passiPerScattoDellaRotella() {
		return PASSI_PER_SCATTO_DELLE_LISTE;
	}

	@Override
	public void processaRotella(int x, int y, int numeroRotazioni, MovimentoRotella movimentoRotella) {
		int pixel = numeroRotazioni * PASSO_SCORRIMENTO;
		scorrimento.scorriDi(movimentoRotella == MovimentoRotella.SU ? -pixel : pixel);
	}
}
