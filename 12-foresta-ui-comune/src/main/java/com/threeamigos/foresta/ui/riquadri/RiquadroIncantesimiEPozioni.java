package com.threeamigos.foresta.ui.riquadri;

import com.threeamigos.foresta.tipi.TipoFinestra;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoCreazioneSpriteATempo;
import com.threeamigos.foresta.eventi.notifiche.NotificaVariazioneDisponibilitaIncantesimi;
import com.threeamigos.foresta.eventi.notifiche.NotificaVariazioneDisponibilitaPozioniAumentoLivello;
import com.threeamigos.foresta.eventi.notifiche.NotificaVariazioneDisponibilitaPozioniMagia;
import com.threeamigos.foresta.eventi.notifiche.NotificaVariazioneDisponibilitaPozioniMagiaGrandi;
import com.threeamigos.foresta.eventi.notifiche.NotificaVariazioneDisponibilitaPozioniPanacea;
import com.threeamigos.foresta.eventi.notifiche.NotificaVariazioneDisponibilitaPozioniSalute;
import com.threeamigos.foresta.eventi.notifiche.NotificaVariazioneDisponibilitaPozioniSaluteGrandi;
import com.threeamigos.foresta.ui.sprite.DescrizioneSpriteATempo;
import com.threeamigos.foresta.interfacce.VistaGruppoGiocatore;
import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.tipi.TipoIncantesimo;
import com.threeamigos.foresta.ui.Area;
import com.threeamigos.foresta.ui.Finestra;
import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.componenti.ScorrimentoSuUnAsse;
import com.threeamigos.foresta.ui.immagini.NomiDelleImmagini;
import com.threeamigos.foresta.ui.testo.DoomdarkColorAlternante;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;
import com.threeamigos.foresta.ui.testo.TestiDellaUI;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;

/**
 * Gli incantesimi e le pozioni del gruppo, senza disegno: una lista che si scorre con la rotella e col trascinamento, una voce per riga, con l'icona, il
 * nome per intero (senza abbreviazioni) e la quantità a destra. Gli incantesimi sono nell'ordine di {@link TipoIncantesimo}, poi le pozioni. Chi disegna
 * (Swing, libGDX) dice quanto è grande un'icona ({@link #misuraDellIcona}), chiama {@link #sincronizzaScorrimento()} e disegna le {@link #righeVisibili()}.
 */
public abstract class RiquadroIncantesimiEPozioni implements Finestra {

	/**
	 * Lo spessore del bordo interno della cornice grande: la lista parte da lì
	 */
	public static final int BORDO_INTERNO = 16;
	// Pixel di scorrimento per ogni scatto della rotella, come per le missioni
	private static final int PASSO_SCORRIMENTO = 2;

	/**
	 * Una riga della lista
	 */
	private static final class Riga {
		private final String icona;
		private final String descrizione;
		private final ToIntFunction<VistaGruppoGiocatore> quantita;

		private Riga(String icona, String descrizione, ToIntFunction<VistaGruppoGiocatore> quantita) {
			this.icona = icona;
			this.descrizione = descrizione;
			this.quantita = quantita;
		}
	}

	/**
	 * Una riga che si vede (almeno in parte): l'icona e il suo posto, il nome, la quantità (allineata a destra in <code>xTotale</code>), il colore
	 */
	public static final class RigaVisibile {
		private final String icona;
		private final int xIcona;
		private final int yIcona;
		private final VoceDiTesto nome;
		private final VoceDiTesto quantita;

		private RigaVisibile(String icona, int xIcona, int yIcona, VoceDiTesto nome, VoceDiTesto quantita) {
			this.icona = icona;
			this.xIcona = xIcona;
			this.yIcona = yIcona;
			this.nome = nome;
			this.quantita = quantita;
		}

		/**
		 * Il nome della risorsa dell'icona
		 */
		public String getIcona() {
			return icona;
		}

		public int getXIcona() {
			return xIcona;
		}

		public int getYIcona() {
			return yIcona;
		}

		public VoceDiTesto getNome() {
			return nome;
		}

		public VoceDiTesto getQuantita() {
			return quantita;
		}
	}

	private final List<Riga> righe = new ArrayList<>();
	private final int altezzaRiga = DoomdarkFont.medio().getHeight();
	private final int topLeftX;
	private int topLeftY;
	private final int larghezzaCornice;
	private final int altezzaCornice;
	private final VistaPartita vistaPartita;
	// Quanto la lista è scorsa: con la rotella e trascinandola (anche col dito, e prosegue per inerzia)
	private final ScorrimentoSuUnAsse scorrimento = new ScorrimentoSuUnAsse(System::nanoTime);
	// Calcolati la prima volta che servono (dopo che chi disegna ha caricato le immagini)
	private boolean pronto;
	private int altezzaVisibile;
	private int iconaX;
	private int nomeX;
	private int totaleX;
	private int listaX;
	private int listaY;
	private int listaLarghezza;

	/**
	 * @param larghezzaCornice larghezza della cornice grande, già con lo zoom
	 * @param altezzaCornice   altezza della cornice grande, già con lo zoom
	 */
	protected RiquadroIncantesimiEPozioni(int topLeftX, int topLeftY, int larghezzaCornice, int altezzaCornice, VistaPartita vistaPartita) {
		this.vistaPartita = vistaPartita;
		this.topLeftX = topLeftX;
		this.topLeftY = topLeftY;
		this.larghezzaCornice = larghezzaCornice;
		this.altezzaCornice = altezzaCornice;
		for (TipoIncantesimo tipo : TipoIncantesimo.values()) {
			righe.add(new Riga(NomiDelleImmagini.iconaDelIncantesimo(tipo), tipo.getNome(), g -> g.getIncantesimi(tipo)));
		}
		righe.add(new Riga(NomiDelleImmagini.POZIONE_SALUTE, TestiDellaUI.get("POZIONE_SALUTE"), VistaGruppoGiocatore::getPozioniSalute));
		righe.add(new Riga(NomiDelleImmagini.POZIONE_SALUTE_GRANDE, TestiDellaUI.get("POZIONE_SALUTE_GRANDE"), VistaGruppoGiocatore::getPozioniSaluteGrande));
		righe.add(new Riga(NomiDelleImmagini.POZIONE_MAGIA, TestiDellaUI.get("POZIONE_MAGIA"), VistaGruppoGiocatore::getPozioniMagia));
		righe.add(new Riga(NomiDelleImmagini.POZIONE_MAGIA_GRANDE, TestiDellaUI.get("POZIONE_MAGIA_GRANDE"), VistaGruppoGiocatore::getPozioniMagiaGrande));
		righe.add(new Riga(NomiDelleImmagini.POZIONE_PANACEA, TestiDellaUI.get("POZIONE_PANACEA"), VistaGruppoGiocatore::getPozioniPanacea));
		righe.add(new Riga(NomiDelleImmagini.POZIONE_AUMENTO_LIVELLO, TestiDellaUI.get("POZIONE_AUMENTO_LIVELLO"), VistaGruppoGiocatore::getPozioniAumentoLivello));
	}

	/**
	 * Quanto è grande l'icona (<code>icone/Fuoco.png</code>, <code>icone/ComponenteScorrevole-FrecciaSu.png</code>…), come la disegna chi disegna
	 */
	protected abstract Misura misuraDellIcona(String risorsa);

	private void prepara() {
		if (pronto) {
			return;
		}
		int maxIconWidth = 0;
		for (Riga riga : righe) {
			maxIconWidth = Math.max(maxIconWidth, misuraDellIcona(riga.icona).larghezza);
		}
		altezzaVisibile = altezzaCornice - 2 * BORDO_INTERNO;
		listaX = topLeftX + BORDO_INTERNO;
		listaY = topLeftY + BORDO_INTERNO;
		listaLarghezza = larghezzaCornice - 2 * BORDO_INTERNO;
		iconaX = listaX;
		nomeX = iconaX + maxIconWidth + 2;
		// Le quantità stanno a sinistra delle frecce, che si disegnano sopra la lista
		totaleX = listaX + listaLarghezza - misuraDellIcona(NomiDelleImmagini.FRECCIA_SU).larghezza - 2 * Finestra.SPACING;
		pronto = true;
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

	/**
	 * La parte del riquadro in cui sta la lista (dentro il bordo): fuori da lì il disegno si ritaglia
	 */
	public Area getAreaLista() {
		prepara();
		return new Area(listaX, listaY, listaLarghezza, altezzaVisibile);
	}

	/**
	 * Dove si allinea a destra la quantità (sinistra delle frecce)
	 */
	protected int getTotaleX() {
		prepara();
		return totaleX;
	}

	/**
	 * Sposta il riquadro in verticale: topLeftY è la nuova quota del bordo superiore della cornice
	 */
	public void spostaInVerticale(int topLeftY) {
		this.topLeftY = topLeftY;
		listaY = topLeftY + BORDO_INTERNO;
	}

	private int offsetMassimo() {
		return Math.max(0, righe.size() * altezzaRiga - altezzaVisibile);
	}

	/**
	 * Di quanti pixel la lista è scorsa
	 */
	private int offsetY() {
		return (int) Math.round(scorrimento.getScorrimento());
	}

	public boolean isScorribileSu() {
		return offsetY() > 0;
	}

	public boolean isScorribileGiu() {
		return offsetY() < offsetMassimo();
	}

	/**
	 * L'elenco può essere cambiato dall'ultimo scorrimento: il limite va aggiornato e lo scorrimento rimesso dentro. Da chiamare prima di ogni disegno.
	 */
	protected void sincronizzaScorrimento() {
		prepara();
		scorrimento.imposta(offsetMassimo() > 0, offsetMassimo());
		scorrimento.aggiorna();
	}

	/**
	 * La quota a cui sta una riga della lista, tenendo conto dello scorrimento (può essere fuori dalla lista)
	 */
	protected int yDellaRiga(int indice) {
		prepara();
		return listaY + indice * altezzaRiga - offsetY();
	}

	/**
	 * L'icona della riga (per lo sprite della variazione)
	 */
	protected String getIconaDellaRiga(int indice) {
		return righe.get(indice).icona;
	}

	protected static int indicePozione(int numeroPozione) {
		return TipoIncantesimo.values().length + numeroPozione;
	}

	/**
	 * Le righe che si vedono con la lista nella posizione corrente, con i valori di adesso: l'icona (centrata sul corpo del testo) e due scritte, il nome e
	 * la quantità
	 */
	public List<RigaVisibile> righeVisibili() {
		prepara();
		DoomdarkFont fontMedium = DoomdarkFont.medio();
		VistaGruppoGiocatore g = vistaPartita.getGruppoGiocatore();
		DoomdarkColorAlternante color = new DoomdarkColorAlternante();
		List<RigaVisibile> visibili = new ArrayList<>();
		// Si alternano i colori su tutte le righe, anche quelle fuori vista, perché scorrendo non cambino
		for (int i = 0; i < righe.size(); i++) {
			DoomdarkColorModel.Color colore = color.getColor();
			int y = yDellaRiga(i);
			if (y + altezzaRiga > listaY && y < listaY + altezzaVisibile) {
				Riga riga = righe.get(i);
				Misura icona = misuraDellIcona(riga.icona);
				// L'icona sta centrata sul corpo del testo; il testo sale delle righe in più per gli accenti
				int yIcona = y - (icona.altezza - fontMedium.getAltezzaDelCorpo()) / 2;
				int yTesto = y - fontMedium.getRigheInCima();
				visibili.add(new RigaVisibile(riga.icona, iconaX, yIcona,
						new VoceDiTesto(riga.descrizione, nomeX, yTesto, colore, false, 0),
						new VoceDiTesto(String.valueOf(riga.quantita.applyAsInt(g)), totaleX, yTesto, colore, true, 0)));
			}
		}
		return visibili;
	}

	// ---- lo scorrimento ----

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

	/**
	 * Aggiorna gli incantesimi disponibili e pubblica lo sprite della variazione.
	 */
	public void gestisciEventoVariazioneIncantesimi(NotificaVariazioneDisponibilitaIncantesimi evento) {
		pubblica(spriteDellaVariazione(evento.getClasseIncantesimo().ordinal(), evento.getNuovoValore() - evento.getValorePrecedente(),
				"Incantesimo " + evento.getClasseIncantesimo() + " variato"));
	}

	public void gestisciEventoVariazionePozioniSalute(NotificaVariazioneDisponibilitaPozioniSalute evento) {
		pubblica(spriteDellaVariazione(indicePozione(0), evento.getNuovoValore() - evento.getValorePrecedente(), "Pozioni salute variate"));
	}

	public void gestisciEventoVariazionePozioniSaluteGrandi(NotificaVariazioneDisponibilitaPozioniSaluteGrandi evento) {
		pubblica(spriteDellaVariazione(indicePozione(1), evento.getNuovoValore() - evento.getValorePrecedente(), "Pozioni salute grande variate"));
	}

	public void gestisciEventoVariazionePozioniMagia(NotificaVariazioneDisponibilitaPozioniMagia evento) {
		pubblica(spriteDellaVariazione(indicePozione(2), evento.getNuovoValore() - evento.getValorePrecedente(), "Pozioni magia variate"));
	}

	public void gestisciEventoVariazionePozioniMagiaGrandi(NotificaVariazioneDisponibilitaPozioniMagiaGrandi evento) {
		pubblica(spriteDellaVariazione(indicePozione(3), evento.getNuovoValore() - evento.getValorePrecedente(), "Pozioni magia grande variate"));
	}

	public void gestisciEventoVariazionePozioniPanacea(NotificaVariazioneDisponibilitaPozioniPanacea evento) {
		pubblica(spriteDellaVariazione(indicePozione(4), evento.getNuovoValore() - evento.getValorePrecedente(), "Panacee variate"));
	}

	public void gestisciEventoVariazionePozioniAumentoLivello(NotificaVariazioneDisponibilitaPozioniAumentoLivello evento) {
		pubblica(spriteDellaVariazione(indicePozione(5), evento.getNuovoValore() - evento.getValorePrecedente(), "Pozioni aumento livello variate"));
	}

	private static void pubblica(DescrizioneSpriteATempo sprite) {
		if (sprite != null) {
			BusEventi.pubblica(new InternoCreazioneSpriteATempo(sprite.delRiquadro(TipoFinestra.INCANTESIMI_E_POZIONI)));
		}
	}

	/**
	 * Lo sprite della variazione parte dalla riga indicata, anche se non si vede tutta (o per niente) perché la lista è scorsa: si vede solo la parte dentro
	 * la lista. Nessuno sprite se non è cambiato niente.
	 */
	private DescrizioneSpriteATempo spriteDellaVariazione(int indiceRiga, int variazione, String descrizione) {
		if (variazione == 0) {
			return null;
		}
		String icona = getIconaDellaRiga(indiceRiga);
		return DescrizioneSpriteATempo.conVariazione(icona, misuraDellIcona(icona), variazione, DoomdarkFont.medio(), getTotaleX(), yDellaRiga(indiceRiga),
				false, descrizione).conRitaglio(getAreaLista());
	}
}
