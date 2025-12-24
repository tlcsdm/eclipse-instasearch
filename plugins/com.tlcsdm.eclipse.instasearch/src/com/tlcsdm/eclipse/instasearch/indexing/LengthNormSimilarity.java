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

import org.apache.lucene.search.similarities.BM25Similarity;

/**
 * Custom similarity class extending BM25Similarity
 * In Lucene 9.x, DefaultSimilarity has been replaced with BM25Similarity
 */
public class LengthNormSimilarity extends BM25Similarity {

	/**
	 * Creates a new LengthNormSimilarity with default parameters.
	 */
	public LengthNormSimilarity() {
		super();
	}

}
