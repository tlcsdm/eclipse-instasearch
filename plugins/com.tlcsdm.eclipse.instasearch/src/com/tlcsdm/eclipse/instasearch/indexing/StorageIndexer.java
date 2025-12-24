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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TieredMergePolicy;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/**
 * Indexes documents of type IStorage
 */
public class StorageIndexer {
	/** */
	public static final String NO_VALUE = "<none>";
	/** */
	public static final int MIN_WORD_LENGTH = 1;
	protected static final FileAnalyzer fileAnalyzer = new FileAnalyzer(MIN_WORD_LENGTH);

	private IndexChangeListener changeListener = new NullIndexChangeListener();

	private static final ClassicSimilarity similarity = new ClassicSimilarity();
	private static final int MAX_RETRY_ATTEMPTS = 10;
	private Directory indexDir;

	/**
	 * @throws IOException
	 * 
	 */
	public StorageIndexer() throws IOException {
		checkLock();
	}

	private void checkLock() throws IOException {
		Directory indexDir = getIndexDir();

		// In Lucene 9.x, IndexWriter.isLocked() and unlock() were removed.
		// We try to obtain and immediately release the lock to clear any stale lock.
		try {
			indexDir.obtainLock(IndexWriter.WRITE_LOCK_NAME).close();
		} catch (IOException e) {
			// Lock cannot be obtained, may need to recreate index
		}
	}

	public Directory getIndexDir() throws IOException {
		if (indexDir == null)
			indexDir = new ByteBuffersDirectory();

		return indexDir;
	}

	/**
	 * @param create index
	 * @return IndexWriter
	 * @throws IOException
	 */
	public IndexWriter createIndexWriter(boolean create) throws IOException {
		IndexWriterConfig config = new IndexWriterConfig(fileAnalyzer);
		// OpenMode
		config.setOpenMode(create ? IndexWriterConfig.OpenMode.CREATE : IndexWriterConfig.OpenMode.APPEND);
		// Similarity
		config.setSimilarity(similarity);
		// MergePolicy
		TieredMergePolicy mergePolicy = new TieredMergePolicy();
		mergePolicy.setSegmentsPerTier(2);
		mergePolicy.setMaxMergeAtOnce(2);
		config.setMergePolicy(mergePolicy);

		return new IndexWriter(getIndexDir(), config);
	}

	/**
	 * @return isIndexed
	 * @throws IOException
	 */
	public boolean isIndexed() throws IOException {
		return DirectoryReader.indexExists(getIndexDir());
	}

	/**
	 * Check if the index can be read
	 * 
	 * @return whether the index is readable
	 */
	public boolean isReadable() {

		try {
			DirectoryReader reader = DirectoryReader.open(getIndexDir());
			reader.close();

		} catch (IOException readingException) {
			return false;
		}

		return true;
	}

	/**
	 * Delethe the whole index
	 * 
	 * @throws Exception
	 */
	public void deleteIndex() throws Exception {

		RetryingRunnable runnable = new RetryingRunnable() {
			public void run() throws Exception {
				IndexWriter w = createIndexWriter(true); // open for writing and close (make empty)
				w.deleteAll();
				w.commit();
				w.close();

				Directory dir = getIndexDir();
				for (String file : dir.listAll()) {
					dir.deleteFile(file);
				}
				dir.close();
			}

			public boolean handleException(Throwable e) {
				return true;
			}
		};

		changeListener.onIndexReset(); // close searcher because index is deleted

		runRetryingRunnable(runnable); // delete index with retry
	}

	/**
	 * @throws Exception
	 */
	public void optimizeIndex() throws Exception {
		if (!isIndexed())
			return;

		IndexWriter w = createIndexWriter(false);
		w.forceMerge(1, true);
		w.close();

		changeListener.onIndexUpdate();
	}

	/**
	 * @param changeListener the changeListener to set
	 */
	public void setIndexChangeListener(IndexChangeListener changeListener) {
		this.changeListener = changeListener;
	}

	/**
	 * @return the changeListener
	 */
	protected IndexChangeListener getIndexChangeListener() {
		return changeListener;
	}

	/**
	 * 
	 * @param indexWriter
	 * @param storage
	 * @param projectName
	 * @param modificationStamp
	 * @param jar               path to jar file containing this file or null
	 * @throws CoreException
	 * @throws IOException
	 */
	public void indexStorage(IndexWriter indexWriter, IStorage storage, String projectName, long modificationStamp,
			String jar) throws IOException {
		InputStream contents;
		try {
			contents = storage.getContents();
		} catch (Exception e) {
			throw new IOException(e);
		}
		BufferedReader isReader = new BufferedReader(new InputStreamReader(contents));
		IPath fullPath = storage.getFullPath();
		String ext = fullPath.getFileExtension();
		if (ext == null)
			ext = NO_VALUE;

		Document doc = new Document();
		doc.add(createLuceneTextField(Field.CONTENTS, isReader));
		doc.add(createLuceneStoredField(Field.FILE, fullPath.toString()));
		doc.add(createLuceneStoredField(Field.PROJ, projectName));
		doc.add(createLuceneStoredField(Field.NAME, fullPath.lastSegment()));
		doc.add(createLuceneStoredField(Field.EXT, ext.toLowerCase(Locale.ENGLISH)));
		doc.add(createLuceneStoredField(Field.MODIFIED, Long.toString(modificationStamp)));
		doc.add(createLuceneStoredField(Field.JAR, (jar == null) ? NO_VALUE : jar));

		indexWriter.addDocument(doc);
	}

	private static void runRetryingRunnable(RetryingRunnable runnable) throws Exception {
		Throwable lastException = null;

		for (int i = 1; i <= MAX_RETRY_ATTEMPTS; i++) {
			try {
				runnable.run();
				lastException = null;
				break;
			} catch (Throwable e) // exception during run occured
			{
				lastException = e;

				if (!runnable.handleException(e))
					break;
			}

			try {
				Thread.sleep(i * 1000); // wait a bit longer each time for files to be freed
			} catch (Exception e) {
				break;
			}
		}

		if (lastException != null) {
			if (lastException instanceof Exception)
				throw (Exception) lastException;
			else
				throw new Exception(lastException);
		}
	}

	/**
	 * Makes several attempts to index storage. Occasionally the index files get
	 * locked (by other processes) and are temporarily not writable.
	 * 
	 * @param indexWriter
	 * @param storage
	 * @param projectName
	 * @param modificationStamp
	 * @param jar
	 * @throws IOException
	 */
	protected void indexStorageWithRetry(final IndexWriter indexWriter, final IStorage storage,
			final String projectName, final long modificationStamp, final String jar) throws Exception {
		RetryingRunnable runnable = new RetryingRunnable() {
			private boolean oomRetried = false;

			@Override
			public void run() throws Exception {
				indexStorage(indexWriter, storage, projectName, modificationStamp, jar);
			}

			@Override
			public boolean handleException(Throwable e) {

				if (e instanceof OutOfMemoryError) {
					if (!oomRetried) {
						oomRetried = true;
						return true; // retry once
					}
					return false; // give up
				}

				if (e instanceof IOException) {
					changeListener.onIndexReset(); // close searcher
					return true;
				}

				return false;
			}
		};

		runRetryingRunnable(runnable);
	}

	public interface RetryingRunnable {
		public void run() throws Exception;

		/**
		 * If exception occurs during run()
		 * 
		 * @param e
		 * @return true if should run again, false if stop and re-throw exception
		 */
		public boolean handleException(Throwable e);
	}

	public void deleteStorage(IStorage storage) throws Exception {
		IndexWriter writer = createIndexWriter(false);

		String filePath = storage.getFullPath().toString();

		Term term = Field.FILE.createTerm(filePath);
		writer.deleteDocuments(term);

		writer.close();
	}

	private static org.apache.lucene.document.Field createLuceneStoredField(Field fieldName, String value) {
		// Create a StringField that is stored and indexed but not tokenized
		FieldType fieldType = new FieldType();
		fieldType.setStored(true);
		fieldType.setIndexOptions(IndexOptions.DOCS);
		fieldType.setTokenized(false);
		fieldType.freeze();
		return new org.apache.lucene.document.Field(fieldName.toString(), value, fieldType);
	}

	private static org.apache.lucene.document.Field createLuceneTextField(Field fieldName, Reader reader) {
		// Create a TextField with term vectors for highlight support
		FieldType fieldType = new FieldType(TextField.TYPE_NOT_STORED);
		fieldType.setStoreTermVectors(true);
		fieldType.freeze();
		return new org.apache.lucene.document.Field(fieldName.toString(), reader, fieldType);
	}

	/**
	 * Extracts terms from text
	 * 
	 * @param text
	 * @return a map of terms to their offsets in text
	 * @throws IOException
	 */
	public static Map<String, List<Integer>> extractTextTerms(String text) throws IOException {
		Map<String, List<Integer>> terms = new HashMap<String, List<Integer>>();
		TokenStream tokenStream = fileAnalyzer.tokenStream(Field.CONTENTS.toString(), new StringReader(text));

		CharTermAttribute termAtt = tokenStream.addAttribute(CharTermAttribute.class);
		OffsetAttribute offsetAtt = tokenStream.addAttribute(OffsetAttribute.class);

		tokenStream.reset();
		while (tokenStream.incrementToken()) {
			String termText = termAtt.toString().toLowerCase(Locale.ENGLISH);// get token text
			int offset = offsetAtt.startOffset();

			List<Integer> offsets = terms.get(termText);

			if (offsets == null) {
				offsets = new LinkedList<Integer>();
				terms.put(termText, offsets);
			}

			offsets.add(offset);
		}
		tokenStream.end();
		tokenStream.close();

		return terms;
	}

	/**
	 * Listener that gets called when index has changed
	 */
	public interface IndexChangeListener {
		/** Index was updated with files or files were removed */
		public void onIndexUpdate();

		/** Index was reset - created or deleted */
		public void onIndexReset();
	}

	/** Empty implementation to void null checks (Null Object pattern) */
	private static class NullIndexChangeListener implements IndexChangeListener {
		public void onIndexUpdate() {
		}

		public void onIndexReset() {
		}
	}

}