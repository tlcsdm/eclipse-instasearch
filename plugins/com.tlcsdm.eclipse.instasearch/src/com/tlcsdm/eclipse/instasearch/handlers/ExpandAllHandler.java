package com.tlcsdm.eclipse.instasearch.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.handlers.HandlerUtil;

import com.tlcsdm.eclipse.instasearch.ui.InstaSearchView;

public class ExpandAllHandler extends AbstractHandler {
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkbenchPage page = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();
        IViewPart v = page.findView("com.tlcsdm.eclipse.instasearch.ui.InstaSearchView");
        if (v instanceof InstaSearchView) {
            ((InstaSearchView) v).expandAll();
        }
        return null;
    }
}