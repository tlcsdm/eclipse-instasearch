package com.tlcsdm.eclipse.instasearch.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.tlcsdm.eclipse.instasearch.ui.InstaSearchUI;

public class VisitHomePageHandler extends AbstractHandler {
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        try {
            InstaSearchUI.showHomePage();
        } catch (Exception e) {
            InstaSearchUI.showError(e, "Error Opening Page");
        }
        return null;
    }
}