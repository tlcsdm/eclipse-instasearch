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

import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.miscellaneous.LengthFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import com.tlcsdm.eclipse.instasearch.indexing.tokenizers.CamelCaseTokenizer;
import com.tlcsdm.eclipse.instasearch.indexing.tokenizers.DotSplitTokenizer;
import com.tlcsdm.eclipse.instasearch.indexing.tokenizers.WordSplitTokenizer;
import com.tlcsdm.eclipse.instasearch.indexing.tokenizers.standard.StandardTokenizer;

public class FileAnalyzer extends Analyzer {

	private int minWordLength;

	public FileAnalyzer(int minWordLength) {
		super();

		this.minWordLength = minWordLength;
	}

	@Override
	protected TokenStreamComponents createComponents(String fieldName) {
		return createComponents(fieldName, null);
	}

	protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
		StandardTokenizer source = new StandardTokenizer(reader); // splits at ". ", etc.

		TokenStream result = source;
		result = new WordSplitTokenizer(result); // non-alphanumerics
		result = new DotSplitTokenizer(result); // all.package.names, hyphen-separated-words
		result = new CamelCaseTokenizer(result); // CamelCaseIdentifiers

		result = new LengthFilter(result, minWordLength, 128);
		result = new LowerCaseFilter(result);

		return new TokenStreamComponents(source, result);
	}

	/**
	 * Create a token stream for the given reader.
	 * This is used when analyzing file contents.
	 * 
	 * @param reader the input reader
	 * @return TokenStream
	 */
	public TokenStream tokenStream(Reader reader) {
		// Use Field.CONTENTS as the default field name since this analyzer
		// is primarily used for file content indexing
		return createComponents(Field.CONTENTS.toString(), reader).getTokenStream();
	}

	@Override
	public TokenStream tokenStream(String fieldName, Reader reader) {
		return tokenStream(reader);
	}

	// used when debugging
	public static class SysoFilter extends TokenFilter {
		private CharTermAttribute termAtt;

		public SysoFilter(TokenStream input) {
			super(input);
			termAtt = addAttribute(CharTermAttribute.class);
		}

		@Override
		public boolean incrementToken() throws IOException {
			if (input.incrementToken()) {
				// CharTermAttribute#toString() returns the current token text
				System.out.println("TERM: " + termAtt.toString());
				return true;
			}

			return false;
		}
	}
}