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
package com.tlcsdm.eclipse.instasearch.indexing;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.KeywordTokenizer;
import org.apache.lucene.analysis.miscellaneous.LengthFilter;

import com.tlcsdm.eclipse.instasearch.indexing.tokenizers.CamelCaseTokenizer;
import com.tlcsdm.eclipse.instasearch.indexing.tokenizers.DotSplitTokenizer;
import com.tlcsdm.eclipse.instasearch.indexing.tokenizers.WordSplitTokenizer;
import com.tlcsdm.eclipse.instasearch.indexing.tokenizers.standard.StandardTokenizer;

/**
 * Analyzer for user entered search queries
 */
public class QueryAnalyzer extends Analyzer {

	private static final int MAX_WORD_LENGTH = 128;
	private int minWordLength;

	public QueryAnalyzer(int minWordLength) {
		super();

		this.minWordLength = minWordLength;
	}

	@Override
	protected TokenStreamComponents createComponents(String fieldName) {
		if (Field.CONTENTS.toString().equals(fieldName)) {
			StandardTokenizer source = new StandardTokenizer(); // splits at ". ", "-"

			TokenStream result = new WordSplitTokenizer(source); // non-alphanumerics
			result = new DotSplitTokenizer(result); // com.package.names
			result = new CamelCaseTokenizer(result); // CamelCaseIdentifiers

			result = new LengthFilter(result, minWordLength, MAX_WORD_LENGTH);

			return new TokenStreamComponents(source, result);

		} else { // PROJECT, EXT fields
			KeywordTokenizer source = new KeywordTokenizer(); // return whole stream contents as token
			return new TokenStreamComponents(source);
		}
	}

}
