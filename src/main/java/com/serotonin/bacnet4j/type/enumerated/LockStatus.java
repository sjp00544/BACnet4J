/*
 * ============================================================================
 * GNU General Public License
 * ============================================================================
 *
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * When signing a commercial license with Infinite Automation Software,
 * the following extension to GPL is made. A special exception to the GPL is
 * included to allow you to distribute a combined work that includes BAcnet4J
 * without being obliged to provide the source code for any proprietary components.
 *
 * See www.infiniteautomation.com for commercial license options.
 *
 * @author Matthew Lohbihler
 */
package com.serotonin.bacnet4j.type.enumerated;

import com.serotonin.bacnet4j.type.primitive.Enumerated;
import com.serotonin.bacnet4j.util.sero.ByteQueue;

/**
 * @author Matthew Lohbihler
 */
public class LockStatus extends Enumerated {
    private static final long serialVersionUID = -1433958074950622510L;
    public static final LockStatus locked = new LockStatus(0);
    public static final LockStatus unlocked = new LockStatus(1);
    public static final LockStatus fault = new LockStatus(2);
    public static final LockStatus unused = new LockStatus(3);
    public static final LockStatus unknown = new LockStatus(4);

    public static final LockStatus[] ALL = { locked, unlocked, fault, unused, unknown, };

    public LockStatus(final int value) {
        super(value);
    }

    public LockStatus(final ByteQueue queue) {
        super(queue);
    }

    @Override
    public String toString() {
        final int type = intValue();
        if (type == locked.intValue())
            return "locked";
        if (type == unlocked.intValue())
            return "unlocked";
        if (type == fault.intValue())
            return "fault";
        if (type == unused.intValue())
            return "unused";
        if (type == unknown.intValue())
            return "unknown";
        return "Unknown(" + type + ")";
    }
}
