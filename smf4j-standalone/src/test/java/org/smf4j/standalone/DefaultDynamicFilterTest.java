/*
 * Copyright 2012 Russell Morris (wrussellmorris@gmail.com).
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
package org.smf4j.standalone;

import org.smf4j.standalone.DefaultDynamicFilter;
import java.util.regex.Pattern;
import static org.junit.Assert.*;

import org.junit.Test;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class DefaultDynamicFilterTest {

    @Test
    public void patterMatching() {
        // All glob
        assertTrue(matches("**", ""));
        assertTrue(matches("**", "foo"));
        assertTrue(matches("**", "foo.bar"));
        assertTrue(matches("**", "foo.bar.baz"));

        // Prefixed glob
        assertFalse(matches("foo.**", ""));
        assertFalse(matches("foo.**", "foo"));
        assertFalse(matches("foo.**", "blah.boo"));
        assertFalse(matches("foo.**", "blah.foo.bar"));
        assertTrue(matches("foo.**", "foo.bar"));
        assertTrue(matches("foo.**", "foo.bar.baz"));

        // Simple star
        assertFalse(matches("foo.x*.bar", ""));
        assertFalse(matches("foo.x*.bar", "foo"));
        assertFalse(matches("foo.x*.bar", "foo.bar"));
        assertFalse(matches("foo.x*.bar", "foo.blah.bar"));
        assertTrue(matches("foo.x*.bar", "foo.xblah.bar"));

        // Simple question mark
        assertFalse(matches("foo.x???.bar", ""));
        assertFalse(matches("foo.x???.bar", "foo"));
        assertFalse(matches("foo.x???.bar", "foo.bar"));
        assertFalse(matches("foo.x???.bar", "foo.blah.bar"));
        assertFalse(matches("foo.x???.bar", "foo.xblah.bar"));
        assertTrue(matches("foo.x???h.bar", "foo.xblah.bar"));
    }

    private boolean matches(String pattern, String test) {
        Pattern p = DefaultDynamicFilter.createPattern(pattern);
        return p.matcher(test).matches();
    }
}
