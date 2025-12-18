/*
 * Copyright (c) 2009 Andrejs Jermakovics.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrejs Jermakovics - initial implementation
 */
package com.tlcsdm.eclipse.instasearch.indexing.querying;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import com.tlcsdm.eclipse.instasearch.indexing.Field;
import com.tlcsdm.eclipse.instasearch.indexing.Searcher;

/**
 * Replaces '.' with the name of the active project (as a shortcut)
 */
public class CurrentProjectSetter extends QueryVisitor {

	private String currentProject;

	public CurrentProjectSetter() {
	}

	public CurrentProjectSetter(String currentProject) {
		this.currentProject = currentProject;
	}

	/**
	 * @param currentProject the current Project to set
	 */
	public void setCurrentProject(String currentProject) {
		this.currentProject = currentProject;
	}

	@Override
	public Query visit(TermQuery termQuery, Field field) {

		Term term = termQuery.getTerm();

		if (Searcher.CURRENT_PROJECT_CHAR.equals(term.text())) {
			if (field == Field.PROJ || field == Field.CONTENTS) {
				return new TermQuery(Field.PROJ.createTerm(currentProject));
			}
		}

		return super.visit(termQuery, field);
	}
}
