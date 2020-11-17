package com.fasterxml.jackson.core.format;

import java.io.*;
import java.util.ArrayList;

import com.fasterxml.jackson.core.*;

public class TestJsonFormatDetection extends com.fasterxml.jackson.core.BaseTest
{
    public void testSimpleValidArray() throws Exception
    {
        JsonFactory jsonF = new JsonFactory();
        DataFormatDetector detector = new DataFormatDetector(jsonF);
        final String ARRAY_JSON = "[ 1, 2 ]";
        DataFormatMatcher matcher = detector.findFormat(new ByteArrayInputStream(ARRAY_JSON.getBytes("UTF-8")));
        // should have match
        assertTrue(matcher.hasMatch());
        assertEquals("JSON", matcher.getMatchedFormatName());
        assertSame(jsonF, matcher.getMatch());
        // no "certain" match with JSON, but solid:
        assertEquals(MatchStrength.SOLID_MATCH, matcher.getMatchStrength());
        // and thus:
        JsonParser jp = matcher.createParserWithMatch();
        assertToken(JsonToken.START_ARRAY, jp.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
        assertToken(JsonToken.END_ARRAY, jp.nextToken());
        assertNull(jp.nextToken());
        jp.close();
    }

    public void testSimpleValidObject() throws Exception
    {
        JsonFactory jsonF = new JsonFactory();
        DataFormatDetector detector = new DataFormatDetector(jsonF);
        final String JSON = "{  \"field\" : true }";
        DataFormatMatcher matcher = detector.findFormat(new ByteArrayInputStream(JSON.getBytes("UTF-8")));
        // should have match
        assertTrue(matcher.hasMatch());
        assertEquals("JSON", matcher.getMatchedFormatName());
        assertSame(jsonF, matcher.getMatch());
        // no "certain" match with JSON, but solid:
        assertEquals(MatchStrength.SOLID_MATCH, matcher.getMatchStrength());
        // and thus:
        JsonParser jp = matcher.createParserWithMatch();
        assertToken(JsonToken.START_OBJECT, jp.nextToken());
        assertToken(JsonToken.FIELD_NAME, jp.nextToken());
        assertEquals("field", jp.getCurrentName());
        assertToken(JsonToken.VALUE_TRUE, jp.nextToken());
        assertToken(JsonToken.END_OBJECT, jp.nextToken());
        assertNull(jp.nextToken());
        jp.close();
    }

    /**
     * test that same DataFormatDetector will be returned if same optimal or minimal match
     * MatchStrength is requested and different one will be returned if different
     * optimal or minimal match MatchStrength is requested
     * test that the same DataFormatDetector will be returned if same look ahead bytes
     * is requested and a different one will be returned if a different look ahead bytes
     * count is requested
     */
    public void testChangeInternalProperties() throws Exception
    {
        DataFormatDetector defaultDetector = new DataFormatDetector(new JsonFactory());
        assertSame(defaultDetector, defaultDetector.withOptimalMatch(MatchStrength.SOLID_MATCH));
        assertNotSame(defaultDetector, defaultDetector.withOptimalMatch(MatchStrength.FULL_MATCH));
        assertType(defaultDetector.withOptimalMatch(MatchStrength.FULL_MATCH), DataFormatDetector.class);
        assertSame(defaultDetector, defaultDetector.withMinimalMatch(MatchStrength.WEAK_MATCH));
        assertNotSame(defaultDetector, defaultDetector.withMinimalMatch(MatchStrength.SOLID_MATCH));
        assertType(defaultDetector.withMinimalMatch(MatchStrength.SOLID_MATCH), DataFormatDetector.class);
        assertSame(defaultDetector,
                defaultDetector.withMaxInputLookahead(DataFormatDetector.DEFAULT_MAX_INPUT_LOOKAHEAD));
        assertNotSame(defaultDetector,
                defaultDetector.withMaxInputLookahead(DataFormatDetector.DEFAULT_MAX_INPUT_LOOKAHEAD+1));
        assertType(defaultDetector.withMaxInputLookahead(DataFormatDetector.DEFAULT_MAX_INPUT_LOOKAHEAD+1),
                DataFormatDetector.class);
    }

    public void testToString() throws Exception
    {
        JsonFactory jsonF1 = new JsonFactory();
        JsonFactory jsonF2 = new JsonFactory();
        JsonFactory jsonF3 = new JsonFactory();
        ArrayList<JsonFactory> jsonFCollection = new ArrayList<JsonFactory>();
        DataFormatDetector emptyArrayDetector = new DataFormatDetector(jsonFCollection);
        assertEquals("[]", emptyArrayDetector.toString());
        jsonFCollection.add(jsonF1);
        jsonFCollection.add(jsonF2);
        jsonFCollection.add(jsonF3);
        DataFormatDetector detector = new DataFormatDetector(jsonFCollection);
        assertEquals("[JSON, JSON, JSON]", detector.toString());
    }

    /**
     * While JSON String is not a strong match alone, it should
     * be detected unless some better match is available
     */
    public void testSimpleValidString() throws Exception
    {
        JsonFactory jsonF = new JsonFactory();
        DataFormatDetector detector = new DataFormatDetector(jsonF);
        final String JSON = "\"JSON!\"";
        final byte[] bytes = JSON.getBytes("UTF-8");

        _testSimpleValidString(jsonF, detector.findFormat(bytes));
        _testSimpleValidString(jsonF, detector.findFormat(bytes, 0, 7));
        _testSimpleValidString(jsonF, detector.findFormat(new ByteArrayInputStream(bytes)));
    }

    private void _testSimpleValidString(JsonFactory jsonF, DataFormatMatcher matcher) throws Exception
    {
        // should have match
        assertTrue(matcher.hasMatch());
        assertEquals("JSON", matcher.getMatchedFormatName());
        assertSame(jsonF, matcher.getMatch());
        assertEquals(MatchStrength.WEAK_MATCH, matcher.getMatchStrength());
        JsonParser jp = matcher.createParserWithMatch();
        assertToken(JsonToken.VALUE_STRING, jp.nextToken());
        assertEquals("JSON!", jp.getText());
        assertNull(jp.nextToken());
        jp.close();
    }
    
    public void testSimpleInvalid() throws Exception
    {
        DataFormatDetector detector = new DataFormatDetector(new JsonFactory());
        final String NON_JSON = "<root />";
        DataFormatMatcher matcher = detector.findFormat(new ByteArrayInputStream(NON_JSON.getBytes("UTF-8")));
        // should not have match
        assertFalse(matcher.hasMatch());
        assertNull(matcher.getMatchedFormatName());
        // and thus:
        assertEquals(MatchStrength.INCONCLUSIVE, matcher.getMatchStrength());
        // also:
        assertNull(matcher.createParserWithMatch());
    }

}
