/**
 * Copyright © 2012 Akiban Technologies, Inc.  All rights
 * reserved.
 *
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This program may also be available under different license terms.
 * For more information, see www.akiban.com or contact
 * licensing@akiban.com.
 *
 * Contributors:
 * Akiban Technologies, Inc.
 */

package com.akiban.sql;

import org.junit.ComparisonFailure;
import org.junit.Ignore;
import static junit.framework.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

@Ignore
public class TestBase
{
    protected TestBase() {
    }

    protected String caseName, sql, expected, error;

    protected TestBase(String caseName, String sql, String expected, String error) {
        this.caseName = caseName;
        this.sql = sql;
        this.expected = expected;
        this.error = error;
    }

    public static File[] listSQLFiles(File dir) {
        File[] result = dir.listFiles(new RegexFilenameFilter(".*\\.sql"));
        Arrays.sort(result, new Comparator<File>() {
                        public int compare(File f1, File f2) {
                            return f1.getName().compareTo(f2.getName());
                        }
                    });
        return result;
    }

    public static File changeSuffix(File sqlFile, String suffix) {
        return new File(sqlFile.getParentFile(),
                        sqlFile.getName().replace(".sql", suffix));
    }

    public static String fileContents(File file) throws IOException {
        FileReader reader = null;
        try {
            reader = new FileReader(file);
            StringBuilder str = new StringBuilder();
            char[] buf = new char[128];
            while (true) {
                int nc = reader.read(buf);
                if (nc < 0) break;
                str.append(buf, 0, nc);
            }
            return str.toString();
        }
        finally {
            if (reader != null) {
                try {
                    reader.close();
                }
                catch (IOException ex) {
                }
            }
        }
    }

    public static String[] fileContentsArray(File file) throws IOException {
        FileReader reader = null;
        List<String> result = new ArrayList<String>();
        try {
            reader = new FileReader(file);
            BufferedReader buffered = new BufferedReader(reader);
            while (true) {
                String line = buffered.readLine();
                if (line == null) break;
                result.add(line);
            }
        }
        finally {
            if (reader != null) {
                try {
                    reader.close();
                }
                catch (IOException ex) {
                }
            }
        }
        return result.toArray(new String[result.size()]);
    }

    public static Collection<Object[]> sqlAndExpected(File dir) 
            throws IOException {
        return sqlAndExpected(dir, false);
    }

    public static Collection<Object[]> sqlAndExpectedAndParams(File dir) 
            throws IOException {
        return sqlAndExpected(dir, true);
    }
    
    static final boolean RUN_FAILING_TESTS = Boolean.getBoolean("akiban.sql.test.runFailing");

    public static Collection<Object[]> sqlAndExpected(File dir, 
                                                      boolean andParams)
            throws IOException {
        Collection<Object[]> result = new ArrayList<Object[]>();
        for (File sqlFile : listSQLFiles(dir)) {
            String caseName = sqlFile.getName().replace(".sql", "");
            if (changeSuffix(sqlFile, ".fail").exists() && !RUN_FAILING_TESTS)
                continue;
            String sql = fileContents(sqlFile);
            String expected, error;
            File expectedFile = changeSuffix(sqlFile, ".expected");
            if (expectedFile.exists())
                expected = fileContents(expectedFile);
            else
                expected = null;
            File errorFile = changeSuffix(sqlFile, ".error");
            if (errorFile.exists())
                error = fileContents(errorFile);
            else
                error = null;
            if (andParams) {
                String[] params = null;
                File paramsFile = changeSuffix(sqlFile, ".params");
                if (paramsFile.exists()) {
                    params = fileContentsArray(paramsFile);
                }
                result.add(new Object[] {
                               caseName, sql, expected, error, params
                           });
            }
            else {
                result.add(new Object[] {
                               caseName, sql, expected, error
                           });
            }
        }
        return result;
    }

    /** A class implementing this can call {@link #generateAndCheckResult(). */
    public interface GenerateAndCheckResult {
        public String generateResult() throws Exception;
        public void checkResult(String result) throws IOException;
    }

    public static void generateAndCheckResult(GenerateAndCheckResult handler,
                                              String caseName, 
                                              String expected, String error) 
            throws Exception {
        if ((expected != null) && (error != null)) {
            fail(caseName + ": both expected result and expected error specified.");
        }
        String result = null;
        Exception errorResult = null;
        try {
            result = handler.generateResult();
        }
        catch (Exception ex) {
            errorResult = ex;
        }
        if (error != null) {
            if (errorResult == null)
                fail(caseName + ": error expected but none thrown");
            else
                assertEquals(caseName, error, errorResult.toString());
        }
        else if (errorResult != null) {
            throw errorResult;
        }
        else if (expected == null) {
            fail(caseName + " no expected result given. actual='" + result + "'");
        }
        else {
            handler.checkResult(result);
        }
    }

    /** @see GenerateAndCheckResult */
    protected void generateAndCheckResult() throws Exception {
        generateAndCheckResult((GenerateAndCheckResult)this, caseName, expected, error);
    }

    public static void assertEqualsWithoutHashes(String caseName,
                                                 String expected, String actual) 
            throws IOException {
        assertEqualsWithoutPattern(caseName, 
                                   expected, actual, 
                                   CompareWithoutHashes.HASH_REGEX);
    }

    public static void assertEqualsWithoutPattern(String caseName,
                                                  String expected, String actual, 
                                                  String regex) 
            throws IOException {
        CompareWithoutHashes comparer = new CompareWithoutHashes(regex);
        if (!comparer.match(new StringReader(expected), new StringReader(actual)))
            throw new ComparisonFailure(caseName, comparer.converter(expected,actual), actual);
    }

}
