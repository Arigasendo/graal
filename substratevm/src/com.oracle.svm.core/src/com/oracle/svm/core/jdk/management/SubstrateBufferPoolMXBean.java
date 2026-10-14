/*
 * Copyright (c) 2026, 2026, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.oracle.svm.core.jdk.management;

import java.lang.management.BufferPoolMXBean;
import java.util.ArrayList;
import java.util.List;

import javax.management.ObjectName;

import org.graalvm.nativeimage.Platform;
import org.graalvm.nativeimage.Platforms;

import jdk.internal.misc.VM;
import sun.management.Util;

/**
 * A {@link BufferPoolMXBean} that is allocated at image build time (like all other platform MX
 * beans, see {@link ManagementSupport}) and delegates to a {@link VM.BufferPool} of the JDK. The
 * bean references its pool, so the pool object is part of the image heap. The values are still
 * live at run time: the pools read static counters ({@code Bits} for the direct buffers, the
 * unmapper classes nested in {@code FileChannelImpl} for the mapped buffers), which are updated
 * while the application runs.
 */
public final class SubstrateBufferPoolMXBean implements BufferPoolMXBean {
    private static final String BUFFER_POOL_MXBEAN_NAME = "java.nio:type=BufferPool";

    private final VM.BufferPool pool;

    @Platforms(Platform.HOSTED_ONLY.class)
    private SubstrateBufferPoolMXBean(VM.BufferPool pool) {
        this.pool = pool;
    }

    /** Creates one bean for each buffer pool of the JDK ("direct", "mapped", ...). */
    @Platforms(Platform.HOSTED_ONLY.class)
    public static List<BufferPoolMXBean> createAll() {
        List<VM.BufferPool> pools = VM.getBufferPools();
        List<BufferPoolMXBean> result = new ArrayList<>(pools.size());
        for (VM.BufferPool pool : pools) {
            result.add(new SubstrateBufferPoolMXBean(pool));
        }
        return result;
    }

    @Override
    public ObjectName getObjectName() {
        return Util.newObjectName(BUFFER_POOL_MXBEAN_NAME + ",name=" + pool.getName());
    }

    @Override
    public String getName() {
        return pool.getName();
    }

    @Override
    public long getCount() {
        return pool.getCount();
    }

    @Override
    public long getTotalCapacity() {
        return pool.getTotalCapacity();
    }

    @Override
    public long getMemoryUsed() {
        return pool.getMemoryUsed();
    }
}
