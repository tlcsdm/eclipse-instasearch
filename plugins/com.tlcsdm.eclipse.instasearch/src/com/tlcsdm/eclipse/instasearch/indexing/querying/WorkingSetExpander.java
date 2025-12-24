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
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import com.tlcsdm.eclipse.instasearch.InstaSearchPlugin;
import com.tlcsdm.eclipse.instasearch.indexing.Field;

/**
 * Replaces working set name with a list of enclosed projects
 */
public class WorkingSetExpander extends QueryVisitor {

	@Override
	public Query visit(TermQuery termQuery, Field field) {
		Term term = termQuery.getTerm();

		if (field == Field.WS) {
			BooleanQuery.Builder bqBuilder = new BooleanQuery.Builder();
			String workingSet = term.text();

			String projects[] = InstaSearchPlugin.getWorkingSetProjects(workingSet);

			if (projects.length == 0)
				return termQuery;

			for (String proj : projects) {
				if ("".equals(proj))
					continue;
				TermQuery tq = new TermQuery(Field.PROJ.createTerm(proj));
				bqBuilder.add(tq, Occur.SHOULD);
			}

			return bqBuilder.build();
		}

		return super.visit(termQuery, field);
	}
}
