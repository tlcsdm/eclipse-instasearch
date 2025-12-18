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
 * Splits terms. Returns the original term and its split parts
 */
public abstract class TermSplitTokenizer extends TokenFilter {

    private LinkedList<SimpleToken> tokens = new LinkedList<SimpleToken>();

    private CharTermAttribute termAtt;
    private OffsetAttribute offsetAtt;
    private PositionIncrementAttribute posAtt;

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

    private void splitIntoTokens() {
        String term = termAtt.toString();
        String[] termParts = splitTerm(term);

        if (termParts.length > 1) {
            int termPos = offsetAtt.startOffset();

            for (int i = 0; i < termParts.length; i++) {
                String termPart = termParts[i];
                int termPartPos = termPos + term.indexOf(termPart);
                int termPartEndPos = termPartPos + termPart.length();

                SimpleToken newToken = new SimpleToken(termPart, termPartPos, termPartEndPos, 0);
                tokens.add(newToken);
            }
        }
    }

    private void applyToken(SimpleToken token) {
        termAtt.setEmpty();
        termAtt.append(token.term);
        posAtt.setPositionIncrement(token.posInc);
        offsetAtt.setOffset(token.startOffset, token.endOffset);
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