package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.temporizzatore.ThreadPrincipale;

import javax.swing.*;

/**
 * Il thread principale di Swing: l'Event Dispatch Thread.
 *
 * @author Stefano Reksten
 */
public class EsecutoreSwing implements ThreadPrincipale.Esecutore {

    @Override
    public boolean eIlThreadCorrente() {
        return SwingUtilities.isEventDispatchThread();
    }

    @Override
    public void accoda(Runnable operazione) {
        SwingUtilities.invokeLater(operazione);
    }
}
