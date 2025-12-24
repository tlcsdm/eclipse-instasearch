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
package com.tlcsdm.eclipse.instasearch.indexing.tokenizers;

import java.io.IOException;
import java.util.LinkedList;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

/**
 * Splits terms. Returns the original term and its split parts.
 * 
 * In Lucene 9.x, offsets must be strictly non-decreasing. When returnOriginalTerm()
 * is true, we return split parts at the same position (position increment 0)
 * using the original term's offsets to maintain monotonic offset ordering.
 */
public abstract class TermSplitTokenizer extends TokenFilter {

    private LinkedList<SimpleToken> tokens = new LinkedList<SimpleToken>();

    private CharTermAttribute termAtt;
    private OffsetAttribute offsetAtt;
    private PositionIncrementAttribute posAtt;
    
    // Track the last offset we emitted to ensure monotonic offsets
    private int lastStartOffset = 0;
    private int lastEndOffset = 0;

    public TermSplitTokenizer(TokenStream in) {
        super(in);

        assert (in.hasAttribute(CharTermAttribute.class));
        assert (in.hasAttribute(OffsetAttribute.class));
        assert (in.hasAttribute(PositionIncrementAttribute.class));

        termAtt = (CharTermAttribute) addAttribute(CharTermAttribute.class);
        offsetAtt = (OffsetAttribute) addAttribute(OffsetAttribute.class);
        posAtt = (PositionIncrementAttribute) addAttribute(PositionIncrementAttribute.class);
    }

    @Override
    public boolean incrementToken() throws IOException {
        if (!tokens.isEmpty()) {
            applyToken(tokens.removeFirst());
        } else if (input.incrementToken()) {
            // Update tracking from input token
            lastStartOffset = offsetAtt.startOffset();
            lastEndOffset = offsetAtt.endOffset();
            
            splitIntoTokens();

            if (!tokens.isEmpty()) {
                if (!returnOriginalTerm())
                    applyToken(tokens.removeFirst());
            }
        } else {
            return false; // does not have any more tokens
        }

        return true;
    }
    
    @Override
    public void reset() throws IOException {
        super.reset();
        tokens.clear();
        lastStartOffset = 0;
        lastEndOffset = 0;
    }

    private void splitIntoTokens() {
        String term = termAtt.toString();
        String[] termParts = splitTerm(term);

        if (termParts.length > 1) {
            // Use the parent token's offsets for all split tokens.
            // In Lucene 9.x, offsets must be monotonically non-decreasing.
            // Split tokens share the same position (posInc=0) and use parent offsets.
            int parentStartOffset = lastStartOffset;
            int parentEndOffset = lastEndOffset;

            for (int i = 0; i < termParts.length; i++) {
                String termPart = termParts[i];
                if (termPart != null && !termPart.isEmpty()) {
                    // All split tokens use the parent's offsets to maintain monotonic ordering
                    SimpleToken newToken = new SimpleToken(termPart, parentStartOffset, parentEndOffset, 0);
                    tokens.add(newToken);
                }
            }
        }
    }

    private void applyToken(SimpleToken token) {
        termAtt.setEmpty();
        termAtt.append(token.term);
        posAtt.setPositionIncrement(token.posInc);
        // Ensure offsets are monotonic
        int startOff = Math.max(token.startOffset, lastStartOffset);
        int endOff = Math.max(token.endOffset, startOff);
        offsetAtt.setOffset(startOff, endOff);
        lastStartOffset = startOff;
        lastEndOffset = endOff;
    }

    // Simple container replacing deprecated Token
    private static class SimpleToken {
        final String term;
        final int startOffset;
        final int endOffset;
        final int posInc;

        SimpleToken(String term, int startOffset, int endOffset, int posInc) {
            this.term = term;
            this.startOffset = startOffset;
            this.endOffset = endOffset;
            this.posInc = posInc;
        }
    }

    /**
     * Return original term together with the parts
     * 
     * @return returnOriginalTerm
     */
    protected boolean returnOriginalTerm() {
        return false;
    }

    /**
     * Split term into an array of terms
     * 
     * @param term
     * @return split term
     */
    public abstract String[] splitTerm(String term);
}