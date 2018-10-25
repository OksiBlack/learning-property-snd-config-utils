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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.learning.utils.io.location.FileLocator;
import org.learning.utils.io.location.LocatorUtils;
import org.learning.utils.langext.StringUtils;


/**
 * <p>
 * A specialized implementation of {@code FileLocationStrategy} which checks
 * whether the provided file name is already an absolute file name.
 * </p>
 * <p>
 * This strategy ignores the URL and the base path stored in the passed in
 * {@link FileLocator}. It is only triggered by absolute names in the locator's
 * {@code fileName} component.
 * </p>
 *
 * @version $Id: AbsoluteNameLocationStrategy.java 1624601 2014-09-12 18:04:36Z oheger $
 * @since 2.0
 */
public class AbsoluteNameLocationStrategy implements FileLocationStrategy
{
    /**
     * {@inheritDoc} This implementation constructs a {@code File} object from
     * the locator's file name (if defined). If this results in an absolute file
     * name pointing to an existing file, the corresponding URL is returned.
     */
    @Override
    public URL locate(FileSystem fileSystem, FileLocator locator)
    {
        if (StringUtils.isNotEmpty(locator.getFileName()))
        {
            Path file = Paths.get(locator.getFileName());
            if (file.isAbsolute() && Files.exists(file))
            {
                return LocatorUtils.convertPathToURL(file);
            }
        }

        return null;
    }
}
