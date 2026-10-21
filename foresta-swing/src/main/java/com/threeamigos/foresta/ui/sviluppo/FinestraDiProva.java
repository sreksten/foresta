package com.threeamigos.foresta.ui.sviluppo;

import com.threeamigos.foresta.interfacce.StrumentiDiSviluppo;
import com.threeamigos.foresta.interfacce.StrumentiDiSviluppo.DovePartono;
import com.threeamigos.foresta.interfacce.StrumentiDiSviluppo.Scorta;
import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.interfacce.VistaPersonaggio;
import com.threeamigos.foresta.strumenti.ContestoLancio;
import com.threeamigos.foresta.strumenti.TrucchiDiSviluppo;
import com.threeamigos.foresta.tipi.MossaCartaForbiciSasso;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoIncantesimo;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * La finestra per chi sviluppa, che si apre solo in modalità di prova: una finestra a parte, con un tab per ogni
 * personaggio del gruppo (attributi, vivo o morto), uno per il gruppo (monete, incantesimi, pozioni, esperienza), uno per le
 * statistiche (il livello del mondo), uno per le missioni che si offrono in locanda, uno per quelle che si
 * offrono in città e uno per truccare i dadi (carta, forbici e sasso, mostri, cofani).
 * <p>
 * Non tocca il motore se non tramite {@link StrumentiDiSviluppo} e {@link TrucchiDiSviluppo}, e la partita solo in
 * lettura tramite {@link VistaPartita}. Si rinfresca da sola ogni mezzo secondo, perché il gioco cambia sotto di lei.
 *
 * @author Stefano Reksten
 */
public class FinestraDiProva {

    private static final int INTERVALLO_RINFRESCO_MS = 500;
    private static final int MARGINE = 6;
    private static final int LARGHEZZA_DESCRIZIONE = 260;

    private final VistaPartita partita;
    private final StrumentiDiSviluppo strumenti;
    private final TrucchiDiSviluppo trucchi = TrucchiDiSviluppo.istanza();

    private final JFrame finestra = new JFrame("Foresta - strumenti di prova");
    private final JTabbedPane tab = new JTabbedPane();
    private final List<PannelloPersonaggio> pannelliPersonaggi = new ArrayList<>();
    private final List<PannelloMissioni> pannelliMissioni = new ArrayList<>();
    private final PannelloGruppo pannelloGruppo = new PannelloGruppo();
    private final PannelloStatistiche pannelloStatistiche = new PannelloStatistiche();
    private final Timer timer;
    // Gli identificativi dei personaggi per cui ci sono i tab, per accorgersi che il gruppo è cambiato
    private List<String> uuidMostrati = new ArrayList<>();
    private boolean aggiornando;

    public FinestraDiProva(VistaPartita partita, StrumentiDiSviluppo strumenti) {
        this.partita = partita;
        this.strumenti = strumenti;
        finestra.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        tab.setTabPlacement(JTabbedPane.LEFT);
        finestra.getContentPane().add(tab, BorderLayout.CENTER);
        pannelliMissioni.add(new PannelloMissioni(DovePartono.LOCANDA));
        pannelliMissioni.add(new PannelloMissioni(DovePartono.CITTA));
        pannelliMissioni.add(new PannelloMissioni(DovePartono.BOSCO));
        finestra.setSize(520, 560);
        finestra.setLocation(40, 40);
        timer = new Timer(INTERVALLO_RINFRESCO_MS, e -> rinfresca());
    }

    /**
     * Apre la finestra e comincia a rinfrescarla.
     */
    public void mostra() {
        ricostruisciITab();
        finestra.setVisible(true);
        timer.start();
    }

    private void rinfresca() {
        if (finestra.isVisible()) {
            aggiornaOra();
        }
    }

    /**
     * Rifà i tab se il gruppo è cambiato e aggiorna i valori mostrati, anche a finestra chiusa (lo fa il timer solo quando
     * la finestra si vede).
     */
    public void aggiornaOra() {
        List<String> uuidAttuali = uuidDelGruppo();
        if (!uuidAttuali.equals(uuidMostrati)) {
            ricostruisciITab();
            return;
        }
        aggiornando = true;
        try {
            pannelliPersonaggi.forEach(PannelloPersonaggio::aggiorna);
            pannelloGruppo.aggiorna();
            pannelloStatistiche.aggiorna();
            pannelliMissioni.forEach(PannelloMissioni::aggiorna);
        } finally {
            aggiornando = false;
        }
    }

    /**
     * I titoli dei tab, nell'ordine.
     */
    public List<String> getTitoliDeiTab() {
        List<String> titoli = new ArrayList<>();
        for (int i = 0; i < tab.getTabCount(); i++) {
            titoli.add(tab.getTitleAt(i));
        }
        return titoli;
    }

    private List<String> uuidDelGruppo() {
        return partita.getGruppoGiocatore().getPersonaggi().stream()
                .map(VistaPersonaggio::getUuid).collect(Collectors.toList());
    }

    /**
     * Un tab per ogni personaggio del gruppo, poi le missioni e i dadi. Si rifà quando cambia il gruppo; il tab scelto resta.
     */
    private void ricostruisciITab() {
        int scelto = Math.max(0, tab.getSelectedIndex());
        tab.removeAll();
        pannelliPersonaggi.clear();
        uuidMostrati = uuidDelGruppo();
        for (VistaPersonaggio personaggio : partita.getGruppoGiocatore().getPersonaggi()) {
            PannelloPersonaggio pannello = new PannelloPersonaggio(personaggio.getUuid());
            pannelliPersonaggi.add(pannello);
            tab.addTab(personaggio.getNome(), new JScrollPane(pannello));
        }
        tab.addTab("Gruppo", new JScrollPane(pannelloGruppo));
        tab.addTab("Statistiche", new JScrollPane(pannelloStatistiche));
        for (PannelloMissioni pannello : pannelliMissioni) {
            tab.addTab("Missioni " + pannello.dove.getDescrizione().toLowerCase(Locale.ROOT), new JScrollPane(pannello));
        }
        tab.addTab("Dadi", new JScrollPane(new PannelloDadi()));
        tab.setSelectedIndex(Math.min(scelto, tab.getTabCount() - 1));
        aggiornaOra();
    }

    private VistaPersonaggio personaggio(String uuid) {
        for (VistaPersonaggio personaggio : partita.getGruppoGiocatore().getPersonaggi()) {
            if (personaggio.getUuid().equals(uuid)) {
                return personaggio;
            }
        }
        return null;
    }

    /**
     * Fa stare il contenuto di un pannello a griglia sul lato sinistro: l'avanzo di larghezza va a una colonna vuota in fondo.
     */
    private static void allineaASinistra(JPanel pannello, int colonne) {
        GridBagLayout griglia = (GridBagLayout) pannello.getLayout();
        double[] pesi = new double[colonne + 1];
        pesi[colonne] = 1;
        griglia.columnWeights = pesi;
        GridBagConstraints c = cella(colonne, 0);
        c.weightx = 1;
        pannello.add(Box.createHorizontalGlue(), c);
    }

    private static GridBagConstraints cella(int colonna, int riga) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = colonna;
        c.gridy = riga;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(MARGINE / 2, MARGINE, MARGINE / 2, MARGINE);
        return c;
    }

    /**
     * Gli attributi di un personaggio e se è vivo.
     */
    private class PannelloPersonaggio extends JPanel {

        private final String uuid;
        private final JLabel intestazione = new JLabel();
        private final JCheckBox vivo = new JCheckBox("Vivo");
        private final JSpinner livello = new JSpinner(new SpinnerNumberModel(1, 1, 50, 1));
        private final Map<TipoAttributo, JSpinner> attributi = new EnumMap<>(TipoAttributo.class);

        PannelloPersonaggio(String uuid) {
            super(new GridBagLayout());
            this.uuid = uuid;
            setBorder(BorderFactory.createEmptyBorder(MARGINE, MARGINE, MARGINE, MARGINE));
            int riga = 0;
            GridBagConstraints c = cella(0, riga++);
            c.gridwidth = 2;
            add(intestazione, c);

            add(vivo, cella(0, riga++));
            vivo.addActionListener(e -> {
                if (!aggiornando) {
                    strumenti.impostaVivo(uuid, vivo.isSelected());
                }
            });

            add(new JLabel("Livello"), cella(0, riga));
            add(livello, cella(1, riga++));
            livello.addChangeListener(e -> {
                if (!aggiornando) {
                    strumenti.impostaLivello(uuid, (Integer) livello.getValue());
                }
            });

            for (TipoAttributo attributo : strumenti.getAttributiModificabili()) {
                JSpinner spinner = new JSpinner(new SpinnerNumberModel(0, 0, 9999, 1));
                attributi.put(attributo, spinner);
                add(new JLabel(attributo.getNome()), cella(0, riga));
                add(spinner, cella(1, riga++));
                spinner.addChangeListener(e -> {
                    if (!aggiornando) {
                        strumenti.impostaAttributo(uuid, attributo, (Integer) spinner.getValue());
                    }
                });
            }

            JButton puntiAbilita = new JButton("+5 punti abilità");
            puntiAbilita.addActionListener(e -> strumenti.aggiungiPuntiAbilita(uuid, 5));
            add(puntiAbilita, cella(0, riga));

            GridBagConstraints resto = cella(0, riga + 1);
            resto.weighty = 1;
            add(Box.createGlue(), resto);
            allineaASinistra(this, 2);
        }

        void aggiorna() {
            VistaPersonaggio personaggio = personaggio(uuid);
            if (personaggio == null) {
                return;
            }
            intestazione.setText(personaggio.getNome() + " (" + personaggio.getClasse() + ")  -  punti abilità: "
                    + personaggio.getPuntiAbilitaDisponibili());
            vivo.setSelected(personaggio.isVivo());
            imposta(livello, personaggio.getLivello());
            for (Map.Entry<TipoAttributo, JSpinner> voce : attributi.entrySet()) {
                imposta(voce.getValue(), strumenti.getValoreBase(uuid, voce.getKey()));
            }
        }
    }

    /**
     * Non tocca una casella che sta usando chi scrive: il rinfresco la riporterebbe al valore di prima.
     */
    private static void imposta(JSpinner spinner, int valore) {
        if (!spinner.getEditor().getComponent(0).hasFocus()) {
            spinner.setValue(valore);
        }
    }

    /**
     * Le scorte del gruppo del giocatore e i punti esperienza da dargli.
     */
    private class PannelloGruppo extends JPanel {

        private final Map<Scorta, JSpinner> scorte = new EnumMap<>(Scorta.class);
        private final Map<TipoIncantesimo, JSpinner> incantesimi = new EnumMap<>(TipoIncantesimo.class);

        PannelloGruppo() {
            super(new GridBagLayout());
            setBorder(BorderFactory.createEmptyBorder(MARGINE, MARGINE, MARGINE, MARGINE));
            int riga = 0;
            for (Scorta scorta : Scorta.values()) {
                JSpinner spinner = nuovoSpinner();
                scorte.put(scorta, spinner);
                add(new JLabel(scorta.getDescrizione()), cella(0, riga));
                add(spinner, cella(1, riga++));
                spinner.addChangeListener(e -> {
                    if (!aggiornando) {
                        strumenti.impostaScorta(scorta, (Integer) spinner.getValue());
                    }
                });
            }
            for (TipoIncantesimo classe : TipoIncantesimo.values()) {
                JSpinner spinner = nuovoSpinner();
                incantesimi.put(classe, spinner);
                add(new JLabel("Pergamene: " + classe.getNomePlurale()), cella(0, riga));
                add(spinner, cella(1, riga++));
                spinner.addChangeListener(e -> {
                    if (!aggiornando) {
                        strumenti.impostaIncantesimi(classe, (Integer) spinner.getValue());
                    }
                });
            }
            JSpinner esperienza = new JSpinner(new SpinnerNumberModel(100, 1, 1000000, 100));
            JButton dai = new JButton("Dai esperienza al gruppo");
            dai.addActionListener(e -> strumenti.aggiungiEsperienzaAlGruppo((Integer) esperienza.getValue()));
            add(esperienza, cella(1, riga));
            add(dai, cella(0, riga++));
            GridBagConstraints resto = cella(0, riga);
            resto.weighty = 1;
            add(Box.createGlue(), resto);
            allineaASinistra(this, 2);
        }

        void aggiorna() {
            scorte.forEach((scorta, spinner) -> imposta(spinner, strumenti.getScorta(scorta)));
            incantesimi.forEach((classe, spinner) -> imposta(spinner, strumenti.getIncantesimi(classe)));
        }
    }

    /**
     * Il livello del mondo e i suoi punti esperienza.
     */
    private class PannelloStatistiche extends JPanel {

        private final JSpinner livello = new JSpinner(new SpinnerNumberModel(1, 1, 50, 1));
        private final JLabel esperienza = new JLabel();

        PannelloStatistiche() {
            super(new GridBagLayout());
            setBorder(BorderFactory.createEmptyBorder(MARGINE, MARGINE, MARGINE, MARGINE));
            add(new JLabel("Livello del mondo"), cella(0, 0));
            add(livello, cella(1, 0));
            livello.addChangeListener(e -> {
                if (!aggiornando) {
                    strumenti.impostaLivelloDelMondo((Integer) livello.getValue());
                }
            });
            add(new JLabel("Punti esperienza del mondo"), cella(0, 1));
            add(esperienza, cella(1, 1));
            GridBagConstraints resto = cella(0, 2);
            resto.weighty = 1;
            add(Box.createGlue(), resto);
            allineaASinistra(this, 2);
        }

        void aggiorna() {
            imposta(livello, strumenti.getLivelloDelMondo());
            esperienza.setText(String.valueOf(strumenti.getEsperienzaDelMondo()));
        }
    }

    private static String escape(String testo) {
        return testo.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static JSpinner nuovoSpinner() {
        return new JSpinner(new SpinnerNumberModel(0, 0, 1000000, 1));
    }

    /**
     * Le missioni che si offrono in un posto, ognuna col suo stato e il pulsante per forzarne la partenza.
     */
    private class PannelloMissioni extends JPanel {

        final DovePartono dove;
        private final JPanel elenco = new JPanel(new GridBagLayout());
        private List<String> mostrate = new ArrayList<>();

        PannelloMissioni(DovePartono dove) {
            super(new BorderLayout());
            this.dove = dove;
            setBorder(BorderFactory.createEmptyBorder(MARGINE, MARGINE, MARGINE, MARGINE));
            JLabel nota = new JLabel("<html>\"Forza\" fa partire la missione al prossimo controllo in "
                    + luogo(dove) + ", senza aspettarne le condizioni.</html>");
            add(nota, BorderLayout.NORTH);
            add(elenco, BorderLayout.CENTER);
        }

        private String luogo(DovePartono dove) {
            switch (dove) {
                case LOCANDA:
                    return "una locanda";
                case BOSCO:
                    return "un bosco";
                default:
                    return "una città";
            }
        }

        void aggiorna() {
            List<StrumentiDiSviluppo.VoceMissione> voci = strumenti.getMissioni(dove);
            List<String> firma = voci.stream().map(v -> v.getClasse() + ":" + v.getStato() + ":" + v.isForzabile())
                    .collect(Collectors.toList());
            if (firma.equals(mostrate)) {
                return;
            }
            mostrate = firma;
            elenco.removeAll();
            int riga = 0;
            for (StrumentiDiSviluppo.VoceMissione voce : voci) {
                // La descrizione, se c'è, dice più del nome; il nome resta nel suggerimento
                JLabel testo = new JLabel(voce.getDescrizione() != null
                        ? "<html><body style='width: " + LARGHEZZA_DESCRIZIONE + "px'>" + escape(voce.getDescrizione()) + "</body></html>"
                        : voce.getNome());
                testo.setToolTipText(voce.getNome());
                elenco.add(testo, cella(0, riga));
                elenco.add(new JLabel(voce.getStato()), cella(1, riga));
                JButton forza = new JButton("Forza");
                forza.setEnabled(voce.isForzabile());
                forza.addActionListener(e -> strumenti.forzaPartenza(voce.getClasse()));
                elenco.add(forza, cella(2, riga++));
            }
            GridBagConstraints resto = cella(0, riga);
            resto.weighty = 1;
            elenco.add(Box.createGlue(), resto);
            allineaASinistra(elenco, 3);
            elenco.revalidate();
            elenco.repaint();
        }
    }

    /**
     * I trucchi sui dadi: per ogni contesto una casella per accenderlo e il valore voluto.
     */
    private class PannelloDadi extends JPanel {

        PannelloDadi() {
            super(new GridBagLayout());
            setBorder(BorderFactory.createEmptyBorder(MARGINE, MARGINE, MARGINE, MARGINE));
            int riga = 0;
            for (ContestoLancio contesto : ContestoLancio.values()) {
                aggiungiRiga(contesto, riga++);
            }
            JButton spegniTutto = new JButton("Spegni tutti i trucchi");
            spegniTutto.addActionListener(e -> {
                trucchi.spegniTutto();
                ricostruisciITab();
            });
            JPanel basso = new JPanel(new FlowLayout(FlowLayout.LEFT));
            basso.add(spegniTutto);
            GridBagConstraints c = cella(0, riga);
            c.gridwidth = 2;
            c.weighty = 1;
            c.anchor = GridBagConstraints.NORTHWEST;
            add(basso, c);
            allineaASinistra(this, 2);
        }

        private void aggiungiRiga(ContestoLancio contesto, int riga) {
            JCheckBox acceso = new JCheckBox(contesto.getDescrizione(), trucchi.isAcceso(contesto));
            add(acceso, cella(0, riga));
            JComboBox<Object> valore = new JComboBox<>(opzioni(contesto));
            selezionaIlValoreDelTrucco(contesto, valore);
            add(valore, cella(1, riga));
            Runnable applica = () -> {
                if (acceso.isSelected() && valore.getSelectedItem() != null) {
                    Object scelto = valore.getSelectedItem();
                    if (contesto.isScelta()) {
                        trucchi.accendiElemento(contesto, elemento(contesto, scelto));
                    } else {
                        trucchi.accendiNumero(contesto, numero(contesto, valore.getSelectedIndex()));
                    }
                } else {
                    trucchi.spegni(contesto);
                }
            };
            acceso.addActionListener(e -> applica.run());
            valore.addActionListener(e -> applica.run());
        }

        /**
         * Il pannello si ricostruisce spesso (vedi ricostruisciITab): la tendina deve mostrare il valore del trucco già acceso, non la prima voce.
         */
        private void selezionaIlValoreDelTrucco(ContestoLancio contesto, JComboBox<Object> valore) {
            trucchi.getNumero(contesto).ifPresent(numero -> {
                int indice = indiceDelNumero(contesto, numero);
                if (indice >= 0 && indice < valore.getItemCount()) {
                    valore.setSelectedIndex(indice);
                }
            });
            trucchi.getElemento(contesto).ifPresent(voluto -> {
                for (int i = 0; i < valore.getItemCount(); i++) {
                    if (elemento(contesto, valore.getItemAt(i)).equals(voluto)) {
                        valore.setSelectedIndex(i);
                        return;
                    }
                }
            });
        }

        private Object[] opzioni(ContestoLancio contesto) {
            switch (contesto) {
                case CARTA_FORBICI_SASSO:
                    return MossaCartaForbiciSasso.values();
                case TIPO_MOSTRO:
                    return TipoPersonaggio.values();
                case NUMERO_MOSTRI:
                    return numeri(1, 12);
                case COFANO_ESITO:
                    return new Object[]{"0 - niente", "1 - pergamena con un incantesimo", "2 - pergamena con tre incantesimi",
                            "3 - pozione della salute", "4 - pozione della salute grande", "5 - pozione della magia",
                            "6 - monete", "7 - pietre preziose"};
                case COFANO_OGGETTO:
                    return strumenti.getOggettiSpeciali().toArray();
                default:
                    return new Object[0];
            }
        }

        private Object[] numeri(int da, int a) {
            Object[] numeri = new Object[a - da + 1];
            for (int i = 0; i < numeri.length; i++) {
                numeri[i] = da + i;
            }
            return numeri;
        }

        /**
         * Il numero del lancio che corrisponde alla voce scelta: la mossa è la posizione più uno, il cofano parte da zero
         * (da uno nei castelli, dove lo zero viene ignorato), il numero di mostri è il numero stesso.
         */
        private int numero(ContestoLancio contesto, int indice) {
            switch (contesto) {
                case NUMERO_MOSTRI:
                    return indice + 1;
                case COFANO_ESITO:
                    return indice;
                default:
                    return indice + 1;
            }
        }

        /**
         * L'inverso di {@link #numero}: la posizione della voce che dà quel numero.
         */
        private int indiceDelNumero(ContestoLancio contesto, int numero) {
            return contesto == ContestoLancio.COFANO_ESITO ? numero : numero - 1;
        }

        private Object elemento(ContestoLancio contesto, Object scelto) {
            return scelto instanceof StrumentiDiSviluppo.OggettoSpeciale
                    ? ((StrumentiDiSviluppo.OggettoSpeciale) scelto).getCodice()
                    : scelto;
        }
    }
}
