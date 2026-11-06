package com.threeamigos.foresta.ui.schermate;

import com.threeamigos.foresta.tipi.TipoBottega;
import com.threeamigos.foresta.interfacce.VistaOffertaConsumabile;
import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.ui.barra.Cartiglio;
import com.threeamigos.foresta.ui.componenti.ComponenteScorrevole;
import com.threeamigos.foresta.ui.componenti.ScorrimentoSuUnAsse;
import com.threeamigos.foresta.ui.schermate.ScambioDeiConsumabili.Voce;
import com.threeamigos.foresta.ui.sprite.SpriteATempo;

import java.awt.*;
import java.util.List;

/**
 * La schermata dell'alchimista in Swing: elenca le offerte di pozioni e altri consumabili e permette di comprarle per il personaggio scelto. Le due liste, le
 * notifiche di acquisto, l'aiuto e i click sono di {@link ScambioDeiConsumabili} (nel modulo comune); qui c'è il disegno.
 *
 * @author Stefano Reksten
 */
public class DisplayableCanvasScambiatoreConsumabili extends DisplayableCanvasScambiatore {

    private final ScambioDeiConsumabili logica;

    public DisplayableCanvasScambiatoreConsumabili(int width, int height, VistaPartita vistaPartita) {
        super(width, height, vistaPartita);
        this.logica = new ScambioDeiConsumabili(this, vistaPartita, new MisureDelleImmaginiAwt(),
                descrizione -> aggiungiSpriteLocale(new SpriteATempo(descrizione)));
    }

    /**
     * Imposta le offerte da mostrare.
     */
    public void impostaOfferte(List<? extends VistaOffertaConsumabile> offerte) {
        logica.impostaOfferte(offerte);
    }

    @Override
    public void disegnaIntestazioniInventario(Graphics2D graphics) {
        disegnaIntestazioniInventarioImpl(graphics, TipoBottega.ALCHIMISTA.getIntestazioneSinistra(), TipoBottega.ALCHIMISTA.getIntestazioneDestra());
    }

    @Override
    public TipoPersonaggio getMestiereAlCentro() {
        return TipoBottega.ALCHIMISTA.getMestiere();
    }

    @Override
    public String getEtichettaMestiere() {
        return TipoBottega.ALCHIMISTA.getEtichettaDelMestiere();
    }

    private final DisegnoDiContenutoAwt disegno = new DisegnoDiContenutoAwt();

    /**
     * Disegna la colonna centrale: l'alchimista e le monete (vedi SchermataDiScambio.colonnaDelNegoziante, nel modulo comune).
     */
    public void disegnaColonnaPersonaggio(Graphics2D graphics) {
        disegno.disegna(graphics, colonnaDelNegoziante(getMestiereAlCentro()), 0, 0);
    }

    /**
     * Disegna l'elenco delle offerte.
     */
    public void disegnaInventario(Graphics2D graphics) {

        super.disegnaInventario(graphics);

        disegnaColonnaPersonaggio(graphics);

        // Inventario del gruppo
        disegnaElenco(graphics, logica.getElencoGruppo(), xMinimaZonaSinistra, scorrimentoSinistra, false);
        // Offerte dell'alchimista
        disegnaElenco(graphics, logica.getElencoVenditore(), xMinimaZonaDestra, scorrimentoDestra, true);

        disegnaIntestazioniInventario(graphics);

        disegnaSpriteLocali(graphics);

        disegnaAiuto(graphics, logica.righeAiuto());
    }

    private void disegnaElenco(Graphics2D graphics, List<Voce> voci, int x, ScorrimentoSuUnAsse scorrimento, boolean mostraCosto) {
        scorrimento.aggiorna();
        int offset = (int) Math.round(scorrimento.getScorrimento());
        Voce evidenziata = logica.trovaVoce(voci, x, offset, mouseX, mouseY, mostraCosto);
        ComponenteScorrevole<Voce> componenteScorrevole = new ComponenteScorrevole<>(logica.costruisciElenco(voci, evidenziata, mostraCosto),
                larghezzaDisponibileInRiquadroInventario(), 10, 1, DisplayableCanvasScambiatoreArtefatti::immagineDelNodo);

        Image image = componenteScorrevole.produci(altezzaDisponibileInRiquadroInventario(), scorrimento);
        graphics.drawImage(image, x + SPACING, DIMENSIONE_BORDO_INTERNO + 2 * SPACING, null);
    }

    @Override
    public void processaClick(int x, int y, Tasto tasto) {
        logica.processaClick(x, y, tasto);
    }

    @Override
    public void processaDoppioClick(int x, int y, Tasto tasto) {
        logica.processaDoppioClick(x, y, tasto);
    }
}
