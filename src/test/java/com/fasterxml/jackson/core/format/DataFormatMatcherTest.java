package com.fasterxml.jackson.core.format;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.io.MergedStream;
import com.fasterxml.jackson.core.json.ReaderBasedJsonParser;

/**
 * Unit tests for class {@link DataFormatMatcher}.
 */
public class DataFormatMatcherTest extends com.fasterxml.jackson.core.BaseTest
{
    private final JsonFactory JSON_F = new JsonFactory();
    
  public void testGetDataStreamWithOriginalStream() throws IOException {
    byte[] byteArray = new byte[2];
    MatchStrength matchStrength = MatchStrength.WEAK_MATCH;
    DataFormatMatcher dataFormatMatcher = new DataFormatMatcher(new ByteArrayInputStream(byteArray),
            byteArray,
            0,
            0,
            null,
            matchStrength);
    InputStream inputStream = dataFormatMatcher.getDataStream();
    assertEquals(0, inputStream.available());
    assertType(inputStream, MergedStream.class);
    inputStream.close();
  }

  public void testGetDataStreamNoOriginalStream() throws IOException {
      byte[] byteArray = new byte[2];
      MatchStrength matchStrength = MatchStrength.WEAK_MATCH;
      DataFormatMatcher dataFormatMatcher = new DataFormatMatcher(null,
              byteArray,
              2,
              0,
              null,
              matchStrength);
      InputStream inputStream = dataFormatMatcher.getDataStream();
      assertEquals(0, inputStream.available());
      assertType(inputStream, ByteArrayInputStream.class);
      inputStream.close();
  }

  public void testCreatesDataFormatMatcherTwo() {
    // test buffered start + buffered length longer than total buffer
    try {
        @SuppressWarnings("unused")
        DataFormatMatcher dataFormatMatcher = new DataFormatMatcher(null,
                new byte[2], 2, 1,
                JSON_F, MatchStrength.NO_MATCH);
        // this assertion should fail if a DataFormatMatcher is produced
        // it should fail earlier with the proper Exception and go to the
        // catch statement
        assertType(dataFormatMatcher, String.class);
    } catch (IllegalArgumentException e) {
        verifyException(e, "Illegal start/length");
    }
    // test negative buffered start
    try {
        @SuppressWarnings("unused")
        DataFormatMatcher dataFormatMatcher = new DataFormatMatcher(null,
            new byte[10], -10, 1,
            JSON_F, MatchStrength.NO_MATCH);
        // this assertion should fail if a DataFormatMatcher is produced
        // it should fail earlier with the proper Exception and go to the
        // catch statement
        assertType(dataFormatMatcher, String.class);
    } catch (IllegalArgumentException e) {
        verifyException(e, "Illegal start/length");
    }
    // test negative buffered length
    try {
      @SuppressWarnings("unused")
      DataFormatMatcher dataFormatMatcher = new DataFormatMatcher(null,
              new byte[10], 5, -1,
              JSON_F, MatchStrength.NO_MATCH);
      // this assertion should fail if a DataFormatMatcher is produced
      // it should fail earlier with the proper Exception and go to the
      // catch statement
      assertType(dataFormatMatcher, String.class);
    } catch (IllegalArgumentException e) {
      verifyException(e, "Illegal start/length");
    }
    // test negative buffered start and negative bufferedLength
    try {
      @SuppressWarnings("unused")
      DataFormatMatcher dataFormatMatcher = new DataFormatMatcher(null,
              new byte[10], -2, -1,
              JSON_F, MatchStrength.NO_MATCH);
      // this assertion should fail if a DataFormatMatcher is produced
      // it should fail earlier with the proper Exception and go to the
      // catch statement
      assertType(dataFormatMatcher, String.class);
    } catch (IllegalArgumentException e) {
      verifyException(e, "Illegal start/length");
    }
  }

  public void testGetMatchedFormatNameReturnsNameWhenMatches() {
      DataFormatMatcher dataFormatMatcher = new DataFormatMatcher(null,
              new byte[2],
              2,
              0,
              JSON_F,
              MatchStrength.SOLID_MATCH);
      assertEquals(JsonFactory.FORMAT_NAME_JSON, dataFormatMatcher.getMatchedFormatName());
  }

    public void testCreateParserWithMatchNoJsonFactory() throws IOException {
        DataFormatMatcher dataFormatMatcher = new DataFormatMatcher(null,
                new byte[2],
                2,
                0,
                null,
                MatchStrength.SOLID_MATCH);
        JsonParser jsonParser = dataFormatMatcher.createParserWithMatch();
        assertNull(jsonParser);
    }

  public void testCreateParserWithMatchNoInputStream() throws IOException {
      byte[] byteArray = new byte[2];
      DataFormatMatcher dataFormatMatcher = new DataFormatMatcher(null,
              byteArray,
              2,
              0,
              JSON_F,
              MatchStrength.SOLID_MATCH);
      JsonParser jsonParser = dataFormatMatcher.createParserWithMatch();
      assertType(jsonParser, JsonParser.class);
  }

    public void testCreateParserWithMatchAllArguments() throws IOException {
      byte[] byteArray = new byte[3];
      ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArray);
        DataFormatMatcher dataFormatMatcher = new DataFormatMatcher(inputStream,
                byteArray,
                0,
                0,
                JSON_F,
                MatchStrength.SOLID_MATCH);
        JsonParser jsonParser = dataFormatMatcher.createParserWithMatch();
        assertType(jsonParser, ReaderBasedJsonParser.class);
    }
  public void testDetectorConfiguration() {
      DataFormatDetector df0 = new DataFormatDetector(JSON_F);

      // Defaults are: SOLID for optimal, WEAK for minimum, so:
      assertSame(df0, df0.withOptimalMatch(MatchStrength.SOLID_MATCH));
      assertSame(df0, df0.withMinimalMatch(MatchStrength.WEAK_MATCH));
      assertSame(df0, df0.withMaxInputLookahead(DataFormatDetector.DEFAULT_MAX_INPUT_LOOKAHEAD));

      // but will change
      assertNotSame(df0, df0.withOptimalMatch(MatchStrength.FULL_MATCH));
      assertNotSame(df0, df0.withMinimalMatch(MatchStrength.SOLID_MATCH));
      assertNotSame(df0, df0.withMaxInputLookahead(DataFormatDetector.DEFAULT_MAX_INPUT_LOOKAHEAD + 5));

      // regardless, we should be able to use `toString()`
      assertNotNull(df0.toString());
  }
}
