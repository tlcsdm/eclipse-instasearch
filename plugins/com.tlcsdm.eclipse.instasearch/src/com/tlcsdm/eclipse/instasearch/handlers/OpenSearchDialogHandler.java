package com.tlcsdm.eclipse.instasearch.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.tlcsdm.eclipse.instasearch.ui.InstaSearchPage;
import com.tlcsdm.eclipse.instasearch.ui.InstaSearchUI;

public class OpenSearchDialogHandler extends AbstractHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// activate current editor so NewSearchUI can use it (与旧实现一致)
		try {
			InstaSearchUI.getWorkbenchWindow().getActivePage().activate(InstaSearchUI.getActiveEditor());
		} catch (Exception e) {
			// ignore
		}
		org.eclipse.search.ui.NewSearchUI.openSearchDialog(InstaSearchUI.getWorkbenchWindow(), InstaSearchPage.ID);
		return null;
	}
}