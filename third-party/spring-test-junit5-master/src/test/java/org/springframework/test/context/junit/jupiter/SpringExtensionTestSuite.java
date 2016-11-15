/*
 * Copyright 2002-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.test.context.junit.jupiter;

import org.junit.platform.runner.IncludeEngines;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.runner.SelectPackages;
import org.junit.runner.RunWith;

/**
 * Test suite which demonstrates that the Spring TestContext Framework can
 * be used with the current JUnit 5 snapshots via a single {@link SpringExtension}.
 *
 * @author Sam Brannen
 * @since 5.0
 * @see SpringExtensionTests
 * @see ComposedSpringExtensionTests
 * @see org.springframework.test.context.junit.jupiter.web.WebSpringExtensionTests
 */
@RunWith(JUnitPlatform.class)
@IncludeEngines("junit-jupiter")
@SelectPackages("org.springframework.test.context.junit.jupiter")
public class SpringExtensionTestSuite {
}
