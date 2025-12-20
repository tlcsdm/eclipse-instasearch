package com.tlcsdm.eclipse.instasearch.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;

public class ShowExceptionAction extends Action {
	public static final String ID = "com.tlcsdm.eclipse.instasearch.ui.InstaSearchUI.ShowExceptionAction";

	private Throwable exception;
	private IStatus status;

	public ShowExceptionAction(Throwable exception) {
		this.exception = exception;

		setId(ID);
		setText("Exception");
		setDescription(exception.getMessage());
		setToolTipText(exception.getMessage());
	}

	public ShowExceptionAction(Exception exception, String text) {
		this(exception);

		setText(text);
	}

	public ShowExceptionAction(IStatus status) {
		this(status.getException());

		this.status = status;
	}

	@Override
	public void run() {
		if (status != null)
			InstaSearchUI.showError(status, getText());
		else
			InstaSearchUI.showError(exception, getText());
	}
}
