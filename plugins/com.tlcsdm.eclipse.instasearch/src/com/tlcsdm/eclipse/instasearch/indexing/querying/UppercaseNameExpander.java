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
import org.apache.lucene.search.WildcardQuery;

import com.tlcsdm.eclipse.instasearch.indexing.Field;

/**
 * Converts camel-cased file name patterns to wildcard queries. Eg. converts ABC
 * to A*B*C in order to match AbstractBaseClass also AbsBC will match
 */
public class UppercaseNameExpander extends QueryVisitor {

	@Override
	public Query visit(TermQuery termQuery, Field field) {

		if (field != Field.NAME)
			return termQuery;

		Term term = termQuery.getTerm();
		String text = term.text();

		if (text.matches("[A-Z][a-z0-9_]*[A-Z].*")) { // if has several uppercase letters
			BooleanQuery.Builder bqBuilder = new BooleanQuery.Builder();

			bqBuilder.add(termQuery, Occur.SHOULD);

			String wcText = text.replaceAll("([A-Z][a-z0-9_]*)", "$1*");

			Term wcTerm = Field.NAME.createTerm(wcText);
			WildcardQuery wcQuery = new WildcardQuery(wcTerm);
			bqBuilder.add(wcQuery, Occur.SHOULD);

			return bqBuilder.build();
		}

		return super.visit(termQuery, field);
	}
}
