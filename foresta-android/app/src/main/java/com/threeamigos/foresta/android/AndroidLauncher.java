package com.threeamigos.foresta.android;

import android.os.Bundle;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.threeamigos.foresta.libgdx.EsecutoreGdx;
import com.threeamigos.foresta.libgdx.ForestaGdx;
import com.threeamigos.foresta.libgdx.ForestaLibGDX;
import com.threeamigos.foresta.motore.AvvioDelMotore;
import com.threeamigos.foresta.tipi.Orientamento;

/**
 * Il punto d'ingresso Android, gemello di <code>MainLibgdx</code> per il desktop: collega il motore e l'interfaccia libGDX. Il telefono si usa in
 * orizzontale (l'attivita' e' forzata in orizzontale nel manifest) con la disposizione {@link Orientamento#VERTICALE}, cioe' la barra delle icone sul lato
 * destro.
 */
public class AndroidLauncher extends AndroidApplication {

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
        AvvioDelMotore motore = new AvvioDelMotore(esecutore, e -> Gdx.app.exit(), false);
        ForestaLibGDX foresta = new ForestaLibGDX(Orientamento.VERTICALE, esecutore, motore.getControlloreDiGioco(), motore.getGestorePunteggi(),
                motore.getVistaPartita(), AvvioDelMotore.nuovoTemporizzatore(), false, false, false, true);

        AndroidApplicationConfiguration configurazione = new AndroidApplicationConfiguration();
        configurazione.useImmersiveMode = true;
        configurazione.useWakelock = true;
        initialize(foresta, configurazione);
    }
}
