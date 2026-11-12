package com.threeamigos.foresta.ui.schermate;

import com.threeamigos.foresta.tipi.TipoBottega;
import com.threeamigos.foresta.artefatti.VistaArtefatto;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoSpesaPuntoAbilita;
import com.threeamigos.foresta.eventi.interni.InternoNotificaViaFumettoATempo;
import com.threeamigos.foresta.eventi.notifiche.NotificaRifiutoPrelievoArtefatto;
import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.interfacce.VistaPersonaggio;
import com.threeamigos.foresta.artefatti.VistaScambio;
import com.threeamigos.foresta.ui.componenti.ComponenteScorrevole;
import com.threeamigos.foresta.ui.componenti.ContenutoDiColonna;
import com.threeamigos.foresta.ui.immagini.ImageCache;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * La schermata dell'inventario: a sinistra gli artefatti del personaggio scelto, a destra quelli del gruppo, in mezzo la colonna del personaggio con
 * equipaggiamento e caratteristiche.
 *
 * @author Stefano Reksten
 */
public class DisplayableCanvasInventario extends DisplayableCanvasScambiatoreArtefatti {

    // La colonna del personaggio (testata, attributi, aiuto, click), nel modulo comune; il personaggio è la parte attiva dello scambio
    private final ColonnaDelPersonaggio colonna = new ColonnaDelPersonaggio(this);
    private VistaPersonaggio personaggio;
    // Verde o rosso: il personaggio può prendere l'artefatto del gruppo o no (modulo comune)
    private final ColoriDellInventario colori = new ColoriDellInventario(() -> personaggio);

    public DisplayableCanvasInventario(int width, int height, VistaPartita vistaPartita) {
        super(width, height, vistaPartita);
        BusEventi.iscriviti(NotificaRifiutoPrelievoArtefatto.class, this::onEventoRifiutoPrelievo);
    }

    /**
     * @param scambio fra l'inventario del personaggio (parte attiva) e quello del gruppo (parte remota)
     */
    public void impostaScambio(VistaScambio scambio, VistaPersonaggio personaggio) {
        impostaScambio(scambio);
        this.personaggio = personaggio;
        colonna.impostaPersonaggio(personaggio);
    }

    void onEventoRifiutoPrelievo(NotificaRifiutoPrelievoArtefatto evento) {
        BusEventi.pubblica(new InternoNotificaViaFumettoATempo(evento.getMotivo(), getCoordinateFumetto()));
    }

    @Override
    protected String aiutoDoppioClickSinistra() {
        return TipoBottega.INVENTARIO.getAiutoDoppioClickSinistra();
    }

    @Override
    protected String aiutoDoppioClickDestra() {
        return TipoBottega.INVENTARIO.getAiutoDoppioClickDestra();
    }

    @Override
    public void disegnaIntestazioniInventario(Graphics2D graphics) {
        disegnaIntestazioniInventarioImpl(graphics, TipoBottega.INVENTARIO.getChiaveIntestazioneSinistra(), TipoBottega.INVENTARIO.getChiaveIntestazioneDestra());
    }

    private final DisegnoDiContenutoAwt disegno = new DisegnoDiContenutoAwt();

    /**
     * Disegna la colonna centrale con il personaggio e le sue caratteristiche.
     */
    public void disegnaColonnaPersonaggio(Graphics2D graphics) {

        // La testata (nome, immagine, livello, esperienza, punti abilità) e gli attributi vengono dal modulo comune
        ContenutoDiColonna testata = colonna.testata();
        disegno.disegna(graphics, testata, 0, 0);
        int y = colonna.getYAttributi();
        yAttributi = y;
        scorrimentoCentrale.aggiorna();
        ComponenteScorrevole<Object> componenteScorrevole = new ComponenteScorrevole<>(colonna.elenco(), colonna.getLarghezzaAttributi(), 10, 1,
                oggetto -> (Image) oggetto);

        Image image = componenteScorrevole.produci(height - y, scorrimentoCentrale);
        graphics.drawImage(image, colonna.getXAttributi(), y, null);
    }

    /**
     * Gestisce un click nella colonna del personaggio; dice se l'ha consumato.
     */
    protected boolean processaClickPersonaggio(int x, int y, Tasto tasto) {
        return colonna.processaClick(x, y);
    }

    /**
     * Negli artefatti del gruppo il livello dice se il personaggio può prenderli adesso (verde) o no (rosso), per
     * qualunque motivo: livello, classe, peso, forza, posto occupato. Quelli che ha già restano grigi.
     */
    @Override
    protected DoomdarkColorModel.Color coloreLivello(VistaArtefatto artefatto, boolean parteAttiva) {
        return colori.coloreLivello(artefatto, parteAttiva);
    }

    /**
     * Sopra un attributo, i cartigli che spiegano il colore (vedi ColonnaDelPersonaggio.righeAiuto).
     */
    @Override
    protected List<String> righeAiutoColonnaPersonaggio() {
        return colonna.righeAiuto();
    }

    /**
     * Gestisce un doppio click nella colonna del personaggio; dice se l'ha consumato.
     */
    protected boolean processaDoppioClickPersonaggio(int x, int y, Tasto tasto) {
        return colonna.processaDoppioClick(x, y);
    }

}
