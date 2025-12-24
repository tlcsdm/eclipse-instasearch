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

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
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
			StandardTokenizer source = new StandardTokenizer(null); // splits at ". ", "-"

			TokenStream result = source;
			result = new WordSplitTokenizer(result); // non-alphanumerics
			result = new DotSplitTokenizer(result); // com.package.names
			result = new CamelCaseTokenizer(result); // CamelCaseIdentifiers

			result = new LengthFilter(result, minWordLength, MAX_WORD_LENGTH);

			return new TokenStreamComponents(source, result);

		} else { // PROJECT, EXT fields
			Tokenizer source = new KeywordTokenizer();
			return new TokenStreamComponents(source, source); // return whole stream contents as token
		}
	}

	@Override
	public TokenStream tokenStream(String fieldName, Reader reader) {
		// This method is used by QueryParser
		if (Field.CONTENTS.toString().equals(fieldName)) {
			StandardTokenizer source = new StandardTokenizer(reader);

			TokenStream result = source;
			result = new WordSplitTokenizer(result);
			result = new DotSplitTokenizer(result);
			result = new CamelCaseTokenizer(result);

			result = new LengthFilter(result, minWordLength, MAX_WORD_LENGTH);

			return result;

		} else {
			return new KeywordTokenizer(reader);
		}
	}

}
