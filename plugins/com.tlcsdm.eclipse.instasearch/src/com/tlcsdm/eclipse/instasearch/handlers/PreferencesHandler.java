package com.tlcsdm.eclipse.instasearch.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;

import com.tlcsdm.eclipse.instasearch.prefs.InstaSearchPreferencePage;

public class PreferencesHandler extends AbstractHandler {
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        org.eclipse.jface.preference.PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(shell, InstaSearchPreferencePage.ID, null, null);
        if (dialog != null) {
            dialog.open();
        }
        return null;
    }
}