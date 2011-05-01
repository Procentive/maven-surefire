package org.apache.maven.surefire.report;

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

import java.io.File;
import org.apache.maven.plugin.surefire.report.ReporterManagerFactory;
import org.apache.maven.surefire.booter.StartupReportConfiguration;

import junit.framework.TestCase;

/**
 * @author Kristian Rosenvold
 */
public class TestConsoleOutputRunListenerTest
    extends TestCase
{
    public void testGetCurrentReportEntry()
        throws Exception
    {
        MockReporter mockReporter = new MockReporter();
        ReporterFactory reporterFactory = createReporterFactory();
        TestConsoleOutputRunListener testConsoleOutputRunListener =
            TestConsoleOutputRunListener.createInstance( reporterFactory, true );

/*        assertNull( TestConsoleOutputRunListener.getCurrentReportEntry() );
        ReportEntry reportEntry = new SimpleReportEntry( "srcClass", "testName" );
        testConsoleOutputRunListener.testStarting( reportEntry );
        assertEquals( reportEntry, TestConsoleOutputRunListener.getCurrentReportEntry() );
        testConsoleOutputRunListener.testSucceeded( reportEntry );
        assertNull( TestConsoleOutputRunListener.getCurrentReportEntry() );
  */
    }

    private ReporterFactory createReporterFactory()
    {
        ReporterConfiguration reporterConfiguration = getTestReporterConfiguration();
        return new ReporterManagerFactory( this.getClass().getClassLoader(), reporterConfiguration,
                                           StartupReportConfiguration.defaultValue() );
    }

    public static ReporterConfiguration getTestReporterConfiguration()
    {
        return new ReporterConfiguration( new File( "." ), Boolean.TRUE, ConsoleReporter.class.getName(), null, null, null );
    }

}
