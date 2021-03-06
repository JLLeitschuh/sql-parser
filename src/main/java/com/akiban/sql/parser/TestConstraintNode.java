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

/* The original from which this derives bore the following: */

/*

   Derby - Class org.apache.derby.impl.sql.compile.TestConstraintNode

   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to you under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */

package com.akiban.sql.parser;

import com.akiban.sql.StandardException;

/**
 * A TestConstraintNode is used to determine when a constraint
 * has been violated.
 *
 */

public class TestConstraintNode extends UnaryLogicalOperatorNode
{
    private String sqlState;
    private String tableName;
    private String constraintName;

    /**
     * Initializer for a TestConstraintNode
     *
     * @param booleanValue The operand of the constraint test
     * @param sqlState The SQLState of the exception to throw if the
     *                               constraint has failed
     * @param tableName The name of the table that the constraint is on
     * @param constraintName The name of the constraint being checked
     */

    public void init(Object booleanValue,
                     Object sqlState,
                     Object tableName,
                     Object constraintName) 
            throws StandardException {
        super.init(booleanValue, "throwExceptionIfFalse");
        this.sqlState = (String)sqlState;
        this.tableName = (String)tableName;
        this.constraintName = (String)constraintName;
    }

    /**
     * Fill this node with a deep copy of the given node.
     */
    public void copyFrom(QueryTreeNode node) throws StandardException {
        super.copyFrom(node);

        TestConstraintNode other = (TestConstraintNode)node;
        this.sqlState = other.sqlState;
        this.tableName = other.tableName;
        this.constraintName = other.constraintName;
    }

}
