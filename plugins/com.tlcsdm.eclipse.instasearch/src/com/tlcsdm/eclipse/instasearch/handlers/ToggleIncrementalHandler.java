package com.tlcsdm.eclipse.instasearch.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.tlcsdm.eclipse.instasearch.InstaSearchPlugin;
import com.tlcsdm.eclipse.instasearch.prefs.PreferenceConstants;

public class ToggleIncrementalHandler extends AbstractHandler {
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        boolean current = InstaSearchPlugin.getBoolPref(PreferenceConstants.P_INCREMENTAL_SEARCH);
        InstaSearchPlugin.setBoolPref(PreferenceConstants.P_INCREMENTAL_SEARCH, !current);
        return null;
    }
}