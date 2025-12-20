package com.tlcsdm.eclipse.instasearch.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.search.ui.NewSearchUI;

import com.tlcsdm.eclipse.instasearch.InstaSearchPlugin;
import com.tlcsdm.eclipse.instasearch.prefs.PreferenceConstants;
import com.tlcsdm.eclipse.instasearch.ui.InstaSearchPage;
import com.tlcsdm.eclipse.instasearch.ui.InstaSearchUI;

public class ShowInstaSearchHandler extends AbstractHandler implements Runnable {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		run();
		return null;
	}

	@Override
	public void run() {
		boolean dialogOnShortcut = InstaSearchPlugin.getBoolPref(PreferenceConstants.P_DIALOG_ON_SHORTCUT);
		if (dialogOnShortcut)
			NewSearchUI.openSearchDialog(InstaSearchUI.getWorkbenchWindow(), InstaSearchPage.ID);
		else
			InstaSearchUI.showSearchView();
	}

}