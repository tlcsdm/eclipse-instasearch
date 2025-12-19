package com.tlcsdm.eclipse.instasearch.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.tlcsdm.eclipse.instasearch.InstaSearchPlugin;

public class DeleteIndexHandler extends AbstractHandler {
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        InstaSearchPlugin.getInstaSearch().deleteIndex();
        return null;
    }
}