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

import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.TermQuery;

import com.tlcsdm.eclipse.instasearch.indexing.Field;

/**
 * Converts a boolean query into a phrase query
 */
public class PhraseSearcher extends QueryVisitor {
	/**
	 * 
	 */
	private static final int DEFAULT_SLOP = 10;

	@Override
	public BooleanQuery visit(BooleanQuery boolQuery) {
		PhraseQuery.Builder phraseBuilder = new PhraseQuery.Builder();
		phraseBuilder.setSlop(DEFAULT_SLOP);

		for (BooleanClause clause : boolQuery.clauses()) {
			if (clause.isProhibited() || !clause.isRequired() || !(clause.getQuery() instanceof TermQuery))
				return super.visit(boolQuery); // only consider required terms

			TermQuery tq = (TermQuery) clause.getQuery();

			Field field = Field.getByName(tq.getTerm().field());
			if (field != Field.CONTENTS)
				continue;

			phraseBuilder.add(tq.getTerm());
		}

		PhraseQuery phraseQuery = phraseBuilder.build();

		BooleanQuery.Builder builder = new BooleanQuery.Builder();
		builder.add(phraseQuery, Occur.SHOULD);
		builder.add(new BoostQuery(boolQuery, 0.5f), Occur.SHOULD);

		// BooleanQuery.Builder.build() returns BooleanQuery when clauses have been
		// added
		return builder.build();
	}

}
