package com.threeamigos.foresta.android;

import android.os.Bundle;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.threeamigos.foresta.libgdx.EsecutoreGdx;
import com.threeamigos.foresta.libgdx.ForestaGdx;
import com.threeamigos.foresta.libgdx.ForestaLibGDX;
import com.threeamigos.foresta.motore.AvvioDelMotore;
import com.threeamigos.foresta.tipi.StileDellaBarra;

/**
 * Il punto d'ingresso Android, gemello di <code>MainLibgdx</code> per il desktop: collega il motore e l'interfaccia libGDX. Il dispositivo si usa in
 * orizzontale (l'attivita' e' forzata in orizzontale nel manifest); la barra delle icone sta dove l'ha messa il giocatore nelle opzioni, o, la prima
 * volta, in basso sul tablet se ci sta e a destra sul telefono.
 */
public class AndroidLauncher extends AndroidApplication {

    // Le scelte di avvio che sul desktop vengono dagli argomenti (OpzioniDiAvvio): su Android non ci sono argomenti, e si decidono qui
    /** La lingua non e' decisa in anticipo: vale quella scelta nelle opzioni (o l'italiano, la prima volta) */
    private static final boolean LINGUA_DECISA = false;
    /** Il joystick a schermo sulla mappa non si chiede: col dito lo accende comunque ForestaLibGDX */
    private static final boolean JOYSTICK_VIRTUALE = false;
    /** Il logo iniziale si mostra */
    private static final boolean SALTA_LOGO = false;
    /** Si gioca con il dito */
    private static final boolean TOCCO_RICHIESTO = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Il gioco salva e scrive il log in <user.home>/.foresta: su Android la "home" e' la cartella privata dell'app
        System.setProperty("user.home", getFilesDir().getAbsolutePath());

        // Per provare un pezzo dell'interfaccia senza il motore: `adb shell am start -n com.threeamigos.foresta/.android.AndroidLauncher --es pagina 12`
        // apre la pagina di prova numero 12 (la mappa), come MainLibgdx PAGINE_DI_PROVA -Dforesta.pagina=12 sul desktop (vedi ForestaGdx)
        String pagina = getIntent().getStringExtra("pagina");
        if (pagina != null) {
            System.setProperty("foresta.pagina", pagina);
            AndroidApplicationConfiguration prova = new AndroidApplicationConfiguration();
            prova.useImmersiveMode = true;
            initialize(new ForestaGdx(), prova);
            return;
        }

        EsecutoreGdx esecutore = new EsecutoreGdx();
        // Il motore chiede di uscire con NotificaUscitaDalGioco: si chiude l'applicazione
        AvvioDelMotore motore = new AvvioDelMotore(esecutore, e -> Gdx.app.exit(), LINGUA_DECISA);
        // La barra dove l'ha messa il giocatore nelle opzioni; se non l'ha mai scelta la decide il gioco, dallo schermo: in basso sul tablet se ci sta,
        // a destra sul telefono. Ed è il Dock, se il giocatore non ha scelto la barra normale
        ForestaLibGDX foresta = new ForestaLibGDX(motore.getGestoreOpzioni().getPosizioneDellaBarra(), null, esecutore, motore.getControlloreDiGioco(),
                motore.getGestorePunteggi(), motore.getVistaPartita(), AvvioDelMotore.nuovoTemporizzatore(),
                motore.getGestoreOpzioni().getStileDellaBarra() != StileDellaBarra.NORMALE, JOYSTICK_VIRTUALE, SALTA_LOGO,
                TOCCO_RICHIESTO);

        AndroidApplicationConfiguration configurazione = new AndroidApplicationConfiguration();
        configurazione.useImmersiveMode = true;
        configurazione.useWakelock = true;
        initialize(foresta, configurazione);
    }
}
