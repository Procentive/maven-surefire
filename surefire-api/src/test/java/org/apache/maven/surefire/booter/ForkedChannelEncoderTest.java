package org.apache.maven.surefire.booter;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.surefire.report.ReportEntry;
import org.apache.maven.surefire.report.SafeThrowable;
import org.apache.maven.surefire.report.StackTraceWriter;
import org.apache.maven.surefire.util.internal.ObjectUtils;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Map;

import static java.nio.charset.Charset.defaultCharset;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.codec.binary.Base64.encodeBase64String;
import static org.apache.maven.surefire.booter.ForkedChannelEncoder.encode;
import static org.apache.maven.surefire.booter.ForkedChannelEncoder.encodeHeader;
import static org.apache.maven.surefire.booter.ForkedChannelEncoder.encodeMessage;
import static org.apache.maven.surefire.booter.ForkedChannelEncoder.encodeOpcode;
import static org.apache.maven.surefire.booter.ForkedProcessEvent.BOOTERCODE_SYSPROPS;
import static org.apache.maven.surefire.booter.ForkedProcessEvent.MAGIC_NUMBER;
import static org.apache.maven.surefire.report.RunMode.NORMAL_RUN;
import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:tibordigana@apache.org">Tibor Digana (tibor17)</a>
 * @since 3.0.0-M4
 */
public class ForkedChannelEncoderTest
{

    @Test
    public void shouldBeFailSafe()
    {
        assertThat( ForkedChannelEncoder.toBase64( null ) ).isEqualTo( "-" );
        assertThat( ForkedChannelEncoder.toBase64( "" ) ).isEqualTo( "" );
    }

    @Test
    public void shouldHaveSystemProperty()
    {
        StringBuilder actualEncoded = encode( BOOTERCODE_SYSPROPS, NORMAL_RUN, "arg1", "arg2" );
        String expected = MAGIC_NUMBER + "normal-run:" + BOOTERCODE_SYSPROPS.getOpcode() + ":UTF-8:YXJnMQ==:YXJnMg==";

        assertThat( actualEncoded.toString() )
                .isEqualTo( expected );
    }

    @Test
    public void safeThrowableShouldBeEncoded()
    {
        final String exceptionMessage = "msg";
        final String encodedExceptionMsg = encodeBase64String( toArray( UTF_8.encode( exceptionMessage ) ) );

        final String smartStackTrace = "MyTest:86 >> Error";
        final String encodedSmartStackTrace = encodeBase64String( toArray( UTF_8.encode( smartStackTrace ) ) );

        final String stackTrace = "trace line 1\ntrace line 2";
        final String encodedStackTrace = encodeBase64String( toArray( UTF_8.encode( stackTrace ) ) );

        final String trimmedStackTrace = "trace line 1\ntrace line 2";
        final String encodedTrimmedStackTrace = encodeBase64String( toArray( UTF_8.encode( trimmedStackTrace ) ) );

        SafeThrowable safeThrowable = new SafeThrowable( new Exception( exceptionMessage ) );
        StackTraceWriter stackTraceWriter = mock( StackTraceWriter.class );
        when( stackTraceWriter.getThrowable() ).thenReturn( safeThrowable );
        when( stackTraceWriter.smartTrimmedStackTrace() ).thenReturn( smartStackTrace );
        when( stackTraceWriter.writeTrimmedTraceToString() ).thenReturn( trimmedStackTrace );
        when( stackTraceWriter.writeTraceToString() ).thenReturn( stackTrace );

        StringBuilder encoded = new StringBuilder();
        encode( encoded, stackTraceWriter, false );
        assertThat( encoded.toString() )
                .isEqualTo( ":" + encodedExceptionMsg + ":" + encodedSmartStackTrace + ":" + encodedStackTrace );

        encoded = new StringBuilder();
        encode( encoded, stackTraceWriter, true );
        assertThat( encoded.toString() )
                .isEqualTo( ":" + encodedExceptionMsg + ":" + encodedSmartStackTrace + ":" + encodedTrimmedStackTrace );
    }

    @Test
    public void emptySafeThrowable()
    {
        SafeThrowable safeThrowable = new SafeThrowable( new Exception( "" ) );

        StackTraceWriter stackTraceWriter = mock( StackTraceWriter.class );
        when( stackTraceWriter.getThrowable() ).thenReturn( safeThrowable );
        when( stackTraceWriter.smartTrimmedStackTrace() ).thenReturn( "" );
        when( stackTraceWriter.writeTraceToString() ).thenReturn( "" );

        StringBuilder encoded = new StringBuilder();
        encode( encoded, stackTraceWriter, false );

        assertThat( encoded.toString() )
                .isEqualTo( ":::" );
    }

    @Test
    public void nullSafeThrowable()
    {
        SafeThrowable safeThrowable = new SafeThrowable( new Exception() );

        StackTraceWriter stackTraceWriter = mock( StackTraceWriter.class );
        when( stackTraceWriter.getThrowable() ).thenReturn( safeThrowable );

        StringBuilder encoded = new StringBuilder();
        encode( encoded, stackTraceWriter, false );

        assertThat( encoded.toString() )
                .isEqualTo( ":-:-:-" );
    }

    @Test
    public void reportEntry() throws IOException
    {
        final String exceptionMessage = "msg";
        final String encodedExceptionMsg = encodeBase64String( toArray( UTF_8.encode( exceptionMessage ) ) );

        final String smartStackTrace = "MyTest:86 >> Error";
        final String encodedSmartStackTrace = encodeBase64String( toArray( UTF_8.encode( smartStackTrace ) ) );

        final String stackTrace = "trace line 1\ntrace line 2";
        final String encodedStackTrace = encodeBase64String( toArray( UTF_8.encode( stackTrace ) ) );

        final String trimmedStackTrace = "trace line 1\ntrace line 2";
        final String encodedTrimmedStackTrace = encodeBase64String( toArray( UTF_8.encode( trimmedStackTrace ) ) );

        SafeThrowable safeThrowable = new SafeThrowable( new Exception( exceptionMessage ) );
        StackTraceWriter stackTraceWriter = mock( StackTraceWriter.class );
        when( stackTraceWriter.getThrowable() ).thenReturn( safeThrowable );
        when( stackTraceWriter.smartTrimmedStackTrace() ).thenReturn( smartStackTrace );
        when( stackTraceWriter.writeTrimmedTraceToString() ).thenReturn( trimmedStackTrace );
        when( stackTraceWriter.writeTraceToString() ).thenReturn( stackTrace );


        ReportEntry reportEntry = mock( ReportEntry.class );
        when( reportEntry.getElapsed() ).thenReturn( 102 );
        when( reportEntry.getGroup() ).thenReturn( "this group" );
        when( reportEntry.getMessage() ).thenReturn( "skipped test" );
        when( reportEntry.getName() ).thenReturn( "my test" );
        when( reportEntry.getNameWithGroup() ).thenReturn( "name with group" );
        when( reportEntry.getSourceName() ).thenReturn( "pkg.MyTest" );
        when( reportEntry.getStackTraceWriter() ).thenReturn( stackTraceWriter );

        String encodedSourceName = encodeBase64String( toArray( UTF_8.encode( reportEntry.getSourceName() ) ) );
        String encodedName = encodeBase64String( toArray( UTF_8.encode( reportEntry.getName() ) ) );
        String encodedGroup = encodeBase64String( toArray( UTF_8.encode( reportEntry.getGroup() ) ) );
        String encodedMessage = encodeBase64String( toArray( UTF_8.encode( reportEntry.getMessage() ) ) );

        StringBuilder encode = encode( "X", "normal-run", reportEntry, false );
        assertThat( encode.toString() )
                .isEqualTo( ":maven:surefire:std:out:normal-run:X:UTF-8:"
                                    + encodedSourceName
                                    + ":"
                                    + encodedName
                                    + ":"
                                    + encodedGroup
                                    + ":"
                                    + encodedMessage
                                    + ":"
                                    + 102
                                    + ":"

                                    + encodedExceptionMsg
                                    + ":"
                                    + encodedSmartStackTrace
                                    + ":"
                                    + encodedStackTrace
                );

        encode = encode( "X", "normal-run", reportEntry, true );
        assertThat( encode.toString() )
                .isEqualTo( ":maven:surefire:std:out:normal-run:X:UTF-8:"
                                    + encodedSourceName
                                    + ":"
                                    + encodedName
                                    + ":"
                                    + encodedGroup
                                    + ":"
                                    + encodedMessage
                                    + ":"
                                    + 102
                                    + ":"

                                    + encodedExceptionMsg
                                    + ":"
                                    + encodedSmartStackTrace
                                    + ":"
                                    + encodedTrimmedStackTrace
                );

        Stream out = Stream.newStream();
        ForkedChannelEncoder forkedChannelEncoder = new ForkedChannelEncoder( out );

        forkedChannelEncoder.testSetStarting( reportEntry, true );
        LineNumberReader printedLines = out.newReader( defaultCharset() );
        assertThat( printedLines.readLine() )
                .isEqualTo( ":maven:surefire:std:out:normal-run:testset-starting:UTF-8:"
                                    + encodedSourceName
                                    + ":"
                                    + encodedName
                                    + ":"
                                    + encodedGroup
                                    + ":"
                                    + encodedMessage
                                    + ":"
                                    + 102
                                    + ":"

                                    + encodedExceptionMsg
                                    + ":"
                                    + encodedSmartStackTrace
                                    + ":"
                                    + encodedTrimmedStackTrace
                );
        assertThat( printedLines.readLine() ).isNull();

        out = Stream.newStream();
        forkedChannelEncoder = new ForkedChannelEncoder( out );

        forkedChannelEncoder.testSetStarting( reportEntry, false );
        printedLines = out.newReader( defaultCharset() );
        assertThat( printedLines.readLine() )
                .isEqualTo( ":maven:surefire:std:out:normal-run:testset-starting:UTF-8:"
                                    + encodedSourceName
                                    + ":"
                                    + encodedName
                                    + ":"
                                    + encodedGroup
                                    + ":"
                                    + encodedMessage
                                    + ":"
                                    + 102
                                    + ":"

                                    + encodedExceptionMsg
                                    + ":"
                                    + encodedSmartStackTrace
                                    + ":"
                                    + encodedStackTrace
                );
        assertThat( printedLines.readLine() ).isNull();
    }

    @Test
    public void testSendOpcode()
    {
        StringBuilder encoded = encodeOpcode( "normal-run", "some-opcode" );
        assertThat( encoded.toString() )
                .isEqualTo( ":maven:surefire:std:out:normal-run:some-opcode" );

        encoded = encodeHeader( "some-opcode", "normal-run" );
        assertThat( encoded.toString() )
                .isEqualTo( ":maven:surefire:std:out:normal-run:some-opcode:UTF-8" );

        encoded = encodeMessage( "some-opcode", "normal-run", "msg" );
        assertThat( encoded.toString() )
                .isEqualTo( ":maven:surefire:std:out:normal-run:some-opcode:UTF-8:msg" );

        Stream out = Stream.newStream();
        ForkedChannelEncoder encoder = new ForkedChannelEncoder( out );
        encoded = encoder.print( "some-opcode", "normal-run", "msg" );
        assertThat( encoded.toString() )
                .isEqualTo( ":maven:surefire:std:out:normal-run:some-opcode:UTF-8:bXNn" );

        encoded = encoder.print( "some-opcode", "normal-run", new String[] { null } );
        assertThat( encoded.toString() )
                .isEqualTo( ":maven:surefire:std:out:normal-run:some-opcode:UTF-8:-" );
    }

    @Test
    public void testConsole()
    {
        Stream out = Stream.newStream();
        ForkedChannelEncoder forkedChannelEncoder = new ForkedChannelEncoder( out );

        forkedChannelEncoder.console( "msg" );

        String encoded = new String( out.toByteArray(), defaultCharset() );

        String expected = ":maven:surefire:std:out:normal-run:console:UTF-8:"
                                  + encodeBase64String( toArray( UTF_8.encode( "msg" ) ) )
                                  + "\n";

        assertThat( encoded )
                .isEqualTo( expected );
    }

    @Test
    public void testError()
    {
        Stream out = Stream.newStream();
        ForkedChannelEncoder forkedChannelEncoder = new ForkedChannelEncoder( out );

        forkedChannelEncoder.error( "msg" );

        String encoded = new String( out.toByteArray(), defaultCharset() );

        String expected = ":maven:surefire:std:out:normal-run:error:UTF-8:"
                                  + encodeBase64String( toArray( UTF_8.encode( "msg" ) ) )
                                  + "\n";

        assertThat( encoded )
                .isEqualTo( expected );
    }

    @Test
    public void testDebug()
    {
        Stream out = Stream.newStream();
        ForkedChannelEncoder forkedChannelEncoder = new ForkedChannelEncoder( out );

        forkedChannelEncoder.debug( "msg" );

        String encoded = new String( out.toByteArray(), defaultCharset() );

        String expected = ":maven:surefire:std:out:normal-run:debug:UTF-8:"
                                  + encodeBase64String( toArray( UTF_8.encode( "msg" ) ) )
                                  + "\n";

        assertThat( encoded )
                .isEqualTo( expected );
    }

    @Test
    public void testWarning()
    {
        Stream out = Stream.newStream();
        ForkedChannelEncoder forkedChannelEncoder = new ForkedChannelEncoder( out );

        forkedChannelEncoder.warning( "msg" );

        String encoded = new String( out.toByteArray(), defaultCharset() );

        String expected = ":maven:surefire:std:out:normal-run:warning:UTF-8:"
                                  + encodeBase64String( toArray( UTF_8.encode( "msg" ) ) )
                                  + "\n";

        assertThat( encoded )
                .isEqualTo( expected );
    }

    @Test
    public void testStdOutStream() throws IOException
    {
        Stream out = Stream.newStream();
        ForkedChannelEncoder forkedChannelEncoder = new ForkedChannelEncoder( out );

        forkedChannelEncoder.stdOut( "msg", false );

        String expected = ":maven:surefire:std:out:normal-run:std-out-stream:UTF-8:bXNn";

        LineNumberReader printedLines = out.newReader( UTF_8 );
        assertThat( printedLines.readLine() )
                .isEqualTo( expected );
        assertThat( printedLines.readLine() )
                .isNull();
    }

    @Test
    public void testStdErrStream() throws IOException
    {
        Stream out = Stream.newStream();
        ForkedChannelEncoder forkedChannelEncoder = new ForkedChannelEncoder( out );

        forkedChannelEncoder.stdErr( "msg", false );

        String expected = ":maven:surefire:std:out:normal-run:std-err-stream:UTF-8:bXNn";

        LineNumberReader printedLines = out.newReader( UTF_8 );
        assertThat( printedLines.readLine() )
                .isEqualTo( expected );
        assertThat( printedLines.readLine() )
                .isNull();
    }

    @Test
    public void shouldCountSameNumberOfSystemProperties() throws IOException
    {
        Stream out = Stream.newStream();
        ForkedChannelEncoder forkedChannelEncoder = new ForkedChannelEncoder( out );

        Map<String, String> sysProps = ObjectUtils.systemProps();
        int expectedSize = sysProps.size();
        forkedChannelEncoder.sendSystemProperties( sysProps );

        LineNumberReader printedLines = out.newReader( UTF_8 );

        int size = 0;
        for ( String line; ( line = printedLines.readLine() ) != null; size++ )
        {
            assertThat( line )
                    .startsWith( ":maven:surefire:std:out:normal-run:sys-prop:UTF-8:" );
        }

        assertThat( size )
                .isEqualTo( expectedSize );
    }

    private static class Stream extends PrintStream
    {
        private final ByteArrayOutputStream out;

        Stream( ByteArrayOutputStream out )
        {
            super( out, true );
            this.out = out;
        }

        byte[] toByteArray()
        {
            return out.toByteArray();
        }

        LineNumberReader newReader( Charset streamCharset )
        {
            return new LineNumberReader( new StringReader( new String( toByteArray(), streamCharset ) ) );
        }

        static Stream newStream()
        {
            return new Stream( new ByteArrayOutputStream() );
        }
    }

    private static byte[] toArray( ByteBuffer buffer )
    {
        return Arrays.copyOfRange( buffer.array(), buffer.arrayOffset(), buffer.arrayOffset() + buffer.remaining() );
    }

}
