/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.learning.utils.io.location.strategies;

import java.net.URL;
import java.nio.file.FileSystem;
import org.learning.utils.io.location.FileLocator;
import org.learning.utils.io.location.LocatorUtils;


/**
 * <p>
 * A specialized implementation of {@code FileLocationStrategy} which uses the
 * passed in {@link FileSystem} to locate a file.
 * </p>
 * <p>
 * This strategy implementation ignores the URL of the passed in
 * {@link FileLocator} and operates on its base path and file name. These
 * properties are passed to the {@code locateFromFileSystemURL()} method of
 * {@code FileSystem}. So the burden of resolving the file is delegated to the
 * {@code FileSystem}.
 * </p>
 *
 * @version $Id: FileSystemLocationStrategy.java 1624601 2014-09-12 18:04:36Z oheger $
 * @since 2.0
 */
public class FileSystemLocationStrategy implements FileLocationStrategy
{
    /**
     * {@inheritDoc} This implementation delegates to the {@code FileSystem}.
     */
    @Override
    public URL locate(FileSystem fileSystem, FileLocator locator)
    {
       return LocatorUtils.locateFromFileSystemURL(locator.getBasePath(),
               locator.getFileName());
    }
}
