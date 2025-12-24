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

import org.apache.lucene.search.similarities.ClassicSimilarity;

/**
 * Custom similarity extending ClassicSimilarity (formerly DefaultSimilarity)
 */
public class LengthNormSimilarity extends ClassicSimilarity {

	// In Lucene 9.x, lengthNorm is computed differently
	// Override if needed for custom length normalization behavior

}
