package com.tlcsdm.eclipse.instasearch.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.handlers.HandlerUtil;

import com.tlcsdm.eclipse.instasearch.InstaSearchPlugin;
import com.tlcsdm.eclipse.instasearch.prefs.PreferenceConstants;
import com.tlcsdm.eclipse.instasearch.ui.InstaSearchPage;
import com.tlcsdm.eclipse.instasearch.ui.InstaSearchUI;

public class ShowInstaSearchHandler extends AbstractHandler {
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        boolean dialogOnShortcut = InstaSearchPlugin.getBoolPref(PreferenceConstants.P_DIALOG_ON_SHORTCUT);
        if (dialogOnShortcut) {
            HandlerUtil.getActiveWorkbenchWindow(event);
            // 使用 workbench window 打开搜索对话框
            InstaSearchUI.getWorkbenchWindow(); // 保持与原逻辑一致
            org.eclipse.search.ui.NewSearchUI.openSearchDialog(InstaSearchUI.getWorkbenchWindow(), InstaSearchPage.ID);
        } else {
            InstaSearchUI.showSearchView();
        }
        return null;
    }
}