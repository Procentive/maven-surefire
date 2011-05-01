package org.apache.maven.plugin.surefire.booterclient;

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
import org.apache.maven.surefire.report.DefaultConsoleReporter;
import org.apache.maven.surefire.report.DirectConsoleReporter;
import org.apache.maven.surefire.report.ReporterConfiguration;
import org.apache.maven.surefire.report.RunListener;

/**
 * @author Kristian Rosenvold
 */
public class TestSetMockReporterFactory
    extends ReporterManagerFactory
{
    public TestSetMockReporterFactory()
    {
        super( Thread.currentThread().getContextClassLoader(),
               new ReporterConfiguration( new File( "." ), Boolean.TRUE ), StartupReportConfiguration.defaultValue() );
    }

    public DirectConsoleReporter createConsoleReporter()
    {
        return new DefaultConsoleReporter( System.out );
    }

    public RunListener createReporter()
    {
        return new MockReporter();
    }
}
