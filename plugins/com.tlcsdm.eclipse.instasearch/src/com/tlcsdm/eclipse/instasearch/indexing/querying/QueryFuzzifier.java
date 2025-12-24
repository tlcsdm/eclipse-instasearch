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
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;

import com.tlcsdm.eclipse.instasearch.indexing.Field;

/**
 * Makes the query fuzzy. Replaces each TermQuery by Query of the form: term* OR
 * *term* OR term~
 */
public class QueryFuzzifier extends QueryVisitor {

	@Override
	public Query visit(TermQuery termQuery, Field field) {

		if (field != Field.CONTENTS)
			return termQuery;

		// turn term query into: (term* OR *term* OR term~)

		Term term = termQuery.getTerm();

		PrefixQuery prefixQuery = new PrefixQuery(term);

		Term wildcardTerm = Field.CONTENTS.createTerm("*" + term.text() + "*");
		WildcardQuery wildcardQuery = new WildcardQuery(wildcardTerm);

		FuzzyQuery fuzzyQuery = new FuzzyQuery(term);

		BooleanQuery.Builder builder = new BooleanQuery.Builder();
		builder.add(prefixQuery, Occur.SHOULD);
		builder.add(new BoostQuery(wildcardQuery, 0.75f), Occur.SHOULD);
		builder.add(new BoostQuery(fuzzyQuery, 0.5f), Occur.SHOULD);

		return builder.build();
	}

	@Override
	public Query visit(PhraseQuery phraseQuery) {
		BooleanQuery.Builder builder = new BooleanQuery.Builder();

		for (Term t : phraseQuery.getTerms()) {
			Field f = Field.getByName(t.field());

			if (f != Field.CONTENTS)
				return phraseQuery;

			builder.add(new FuzzyQuery(t), Occur.SHOULD);
		}

		return builder.build();
	}

	@Override
	public boolean visit(BooleanClause boolClause) {
		if (boolClause.getOccur() == Occur.MUST_NOT) // skip prohibited, don't fuzzify
			return false;

		return true;
	}

}
