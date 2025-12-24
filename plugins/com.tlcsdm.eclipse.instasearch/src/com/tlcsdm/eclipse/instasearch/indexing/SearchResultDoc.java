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
import java.util.Collection;
import java.util.Locale;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.BytesRef;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import com.tlcsdm.eclipse.instasearch.InstaSearchPlugin;

public class SearchResultDoc {

	private Document doc;
	private int docId;
	private float score;
	private int matchCount;
	private Directory indexDir;

	public SearchResultDoc(Directory dir, Document doc, int docId, float score) {
		this.indexDir = dir;
		this.docId = docId;
		this.doc = doc;
		this.score = score;

		matchCount = 0;
	}

	private String getFieldValue(Field field) {
		return doc.get(field.toString());
	}

	public String getFilePath() {
		return getFieldValue(Field.FILE);
	}

	public String getFileName() {
		return getFieldValue(Field.NAME);
	}

	public String getFileExtension() {
		return getFieldValue(Field.EXT);
	}

	public boolean isInJar() {
		if (doc.getField(Field.JAR.toString()) == null)
			return false;

		String jarField = getFieldValue(Field.JAR);

		if (StorageIndexer.NO_VALUE.equals(jarField))
			return false;

		if (jarField.toLowerCase(Locale.ENGLISH).endsWith(".jar"))
			return true;

		return false;
	}

	public String getJarName() {

		if (isInJar())
			return getFieldValue(Field.JAR);

		return null;
	}

	public IPath getProject() {
		return new Path(getFieldValue(Field.PROJ));
	}

	public String getProjectName() {
		return getProject().lastSegment();
	}

	/**
	 * Get term score for a specific term
	 * 
	 * @param term
	 * @return term score
	 * @throws IOException
	 */
	public double getTermScore(String term) throws IOException {
		DirectoryReader reader = DirectoryReader.open(indexDir);
		Terms terms = reader.termVectors().get(docId, Field.CONTENTS.toString());
		
		if (terms == null) {
			reader.close();
			return 0;
		}
		
		TermsEnum termsEnum = terms.iterator();
		BytesRef termBytes = new BytesRef(term);
		
		if (!termsEnum.seekExact(termBytes)) {
			reader.close();
			return 0;
		}
		
		// Get term frequency from postings
		int termFreq = 0;
		PostingsEnum postings = termsEnum.postings(null, PostingsEnum.FREQS);
		if (postings != null && postings.nextDoc() != DocIdSetIterator.NO_MORE_DOCS) {
			termFreq = postings.freq();
		}
		
		int numDocs = reader.numDocs();
		int docFreq = reader.docFreq(new Term(Field.CONTENTS.toString(), term));
		
		float tf = (float) Math.sqrt(termFreq);
		float idf = (float) (Math.log(numDocs / (double) (docFreq + 1)) + 1.0);
		
		reader.close();
		return tf * idf;
	}

	public IFile getFile() {
		if (isInJar())
			return null;

		Path path = new Path(getFilePath());
		IWorkspaceRoot workspaceRoot = InstaSearchPlugin.getWorkspaceRoot();
		IFile file = workspaceRoot.getFile(path);

		if (file == null || file.getRawLocation() == null)
			file = workspaceRoot.getFileForLocation(path);

		return file;
	}

	/**
	 * @return the score
	 */
	public float getScore() {
		return score;
	}

	/**
	 * @return the doc
	 */
	public Document getDoc() {
		return doc;
	}

	/**
	 * @return the docId
	 */
	public int getDocId() {
		return docId;
	}

	/**
	 * @return the matchCount
	 */
	public int getMatchCount() {
		return matchCount;
	}

	/**
	 * Computes match count as SUM( tf ) of all query terms in the document Accesses
	 * the index thus affects performance
	 * 
	 * @param reader
	 * @param queryTerms
	 * @throws IOException
	 */
	public void computeMatchCount(IndexReader reader, Collection<String> queryTerms) throws IOException {
		Terms terms = reader.termVectors().get(docId, Field.CONTENTS.toString());

		if (terms == null)
			return;

		int freqSum = 0;
		TermsEnum termsEnum = terms.iterator();

		for (String queryTerm : queryTerms) {
			BytesRef termBytes = new BytesRef(queryTerm);
			if (termsEnum.seekExact(termBytes)) {
				// Get term frequency from postings
				PostingsEnum postings = termsEnum.postings(null, PostingsEnum.FREQS);
				if (postings != null && postings.nextDoc() != DocIdSetIterator.NO_MORE_DOCS) {
					freqSum += postings.freq();
				}
			}
		}

		matchCount = freqSum;
	}

	@Override
	public String toString() {
		return getFilePath();
	}
}
