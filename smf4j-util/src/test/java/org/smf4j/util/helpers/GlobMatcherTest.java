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
package org.smf4j.util.helpers;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.junit.Test;
import org.smf4j.Accumulator;
import org.smf4j.Calculator;
import org.smf4j.RegistryNode;
import org.smf4j.nop.NopAccumulator;
import org.smf4j.nop.NopCalculator;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class GlobMatcherTest {

    @Test
    public void basicMatching() {
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

        // Glob spans .
        assertTrue(matches("foo.**.bar", "foo.baz.bot.bar"));

        // Simple star
        assertFalse(matches("foo.x*.bar", ""));
        assertFalse(matches("foo.x*.bar", "foo"));
        assertFalse(matches("foo.x*.bar", "foo.bar"));
        assertFalse(matches("foo.x*.bar", "foo.blah.bar"));
        assertTrue(matches("foo.x*.bar", "foo.xblah.bar"));

        // Star doesn't span .
        assertFalse(matches("foo.*bar", "foo.test.bar"));

        // Simple question mark
        assertFalse(matches("foo.x???.bar", ""));
        assertFalse(matches("foo.x???.bar", "foo"));
        assertFalse(matches("foo.x???.bar", "foo.bar"));
        assertFalse(matches("foo.x???.bar", "foo.blah.bar"));
        assertFalse(matches("foo.x???.bar", "foo.xblah.bar"));
        assertTrue(matches("foo.x???h.bar", "foo.xblah.bar"));

        // Question mark doesn't span .
        assertFalse(matches("foo?????bar", "foo.baz.bar"));
    }

    private boolean matches(String pattern, String test) {
        Pattern p = GlobMatcher.createPattern(pattern);
        return p.matcher(test).matches();
    }

    @Test
    public void dualMatching() {
        RegistryNode node = createMockNode("my.name",
                new String[] { "acc1", "acc2" },
                new String[] { "calc1", "calc2" },
                new String[0]);

        globMatch(node, "**:**")
                .n(node)
                .a("acc1", "acc2")
                .c("calc1.intProperty", "calc1.stringProperty",
                "calc2.intProperty", "calc2.stringProperty")
                .end();

        globMatch(node, "my.name:**")
                .n(node)
                .a("acc1", "acc2")
                .c("calc1.intProperty", "calc1.stringProperty",
                "calc2.intProperty", "calc2.stringProperty")
                .end();

        globMatch(node, "**:calc1.*")
                .n(node)
                .c("calc1.intProperty", "calc1.stringProperty")
                .end();

        globMatch(node, "some.name,my.name:calc1.*")
                .n(node)
                .c("calc1.intProperty", "calc1.stringProperty")
                .end();

        globMatch(node, "my.name:calc1.*,calc2.*")
                .n(node)
                .c("calc1.intProperty", "calc1.stringProperty",
                "calc2.intProperty", "calc2.stringProperty")
                .end();

        globMatch(node, "foo,my.name:calc1.*,calc2.*")
                .n(node)
                .c("calc1.intProperty", "calc1.stringProperty",
                "calc2.intProperty", "calc2.stringProperty")
                .end();

        globMatch(node, "foo:**").n(node).end();
        globMatch(node, "**:**foobar**").n(node).end();
    }

    @Test
    public void nodeProxy() {
        RegistryNode node = createMockNode("my.name",
                new String[] { "acc1", "acc2" },
                new String[] { "calc1", "calc2" },
                new String[] { "child" });

        // Full match
        GlobMatch match = globMatch(node, "my.name:**");
        assertTrue(match.isNodeMatched());
        assertTrue(match.isMembersMatched());
        assertEquals(2, match.getAccumulators().size());
        assertNotNull(match.getAccumulator("acc1"));
        assertNotNull(match.getAccumulator("acc2"));

        assertEquals(2, match.getCalculators().size());
        assertNotNull(match.getCalculator("calc1"));
        assertNotNull(match.getCalculator("calc2"));

        assertEquals(1, match.getChildNodes().size());
        assertNotNull(match.getChildNode("child"));

        // Partial match (node and some members)
        match = globMatch(node, "my.name:*1,*1*.**");
        assertTrue(match.isNodeMatched());
        assertTrue(match.isMembersMatched());
        assertEquals(1, match.getAccumulators().size());
        assertNotNull(match.getAccumulator("acc1"));
        assertSame(NopAccumulator.INSTANCE, match.getAccumulator("acc2"));
        assertEquals(1, match.getCalculators().size());
        assertNotNull(match.getCalculator("calc1"));
        assertSame(NopCalculator.INSTANCE, match.getCalculator("calc2"));
        assertEquals(1, match.getChildNodes().size());
        assertNotNull(match.getChildNode("child"));

        // Partial match (node and no members)
        match = globMatch(node, "my.name:asdf");
        assertTrue(match.isNodeMatched());
        assertFalse(match.isMembersMatched());
        assertEquals(0, match.getAccumulators().size());
        assertEquals(0, match.getCalculators().size());
        assertEquals(1, match.getChildNodes().size());
        assertNotNull(match.getChildNode("child"));

        // No matches
        match = globMatch(node, "asdf:asdf");
        assertFalse(match.isNodeMatched());
        assertFalse(match.isMembersMatched());
        assertEquals(0, match.getAccumulators().size());
        assertEquals(0, match.getCalculators().size());
        assertEquals(1, match.getChildNodes().size());
    }

    private GlobMatchHelper globMatch(RegistryNode node, String pattern) {
        GlobMatcher matcher = new GlobMatcher(pattern);
        return new GlobMatchHelper(matcher.match(node));
    }

    private RegistryNode createMockNode(String name,
            String[] accNames, String[] calcNames, String[] childNames) {
        RegistryNode node = createNiceMock(RegistryNode.class);
        expect(node.getName()).andStubReturn(name);

        final Map<String, Accumulator> accs =
                new HashMap<String, Accumulator>();
        for(String accName : accNames) {
            Accumulator mock = createMock(Accumulator.class);
            accs.put(accName, mock);
            expect(node.getAccumulator(accName)).andStubReturn(mock);
            for(String calcName : calcNames) {
                expect(node.getAccumulator(calcName)).andStubReturn(
                        NopAccumulator.INSTANCE);
            }
        }

        Map<String, Calculator> calcs =
                new HashMap<String, Calculator>();
        for(String calcName : calcNames) {
            MockCalculator mock = new MockCalculator();
            calcs.put(calcName, mock);
            expect(node.getCalculator(calcName)).andStubReturn(mock);
            for(String accName : accNames) {
                expect(node.getCalculator(accName)).andStubReturn(
                        NopCalculator.INSTANCE);
            }
        }

        Map<String, RegistryNode> children =
                new HashMap<String, RegistryNode>();
        for(String child : childNames) {
            RegistryNode mockChild = createMockNode(child, new String[0],
                    new String[0], new String[0]);
            children.put(child, mockChild);
            expect(node.getChildNode(child)).andStubReturn(node);
        }

        expect(node.getAccumulators()).andStubReturn(accs);
        expect(node.getCalculators()).andStubReturn(calcs);
        expect(node.getChildNodes()).andStubReturn(children);

        replay(node);
        return node;
    }

    private static class Result {
        private final int intProperty;
        private final String stringProperty;

        Result(int intProperty, String stringProperty) {
            this.intProperty = intProperty;
            this.stringProperty = stringProperty;
        }

        public int getIntProperty() {
            return intProperty;
        }

        public String getStringProperty() {
            return stringProperty;
        }
    }

    private static class MockCalculator implements Calculator {

        public Result calculate(Map<String, Long> values,
                Map<String, Accumulator> accumulators) {
            return null;
        }

        public String getUnits() {
            return null;
        }
    }

    private static class GlobMatchHelper extends GlobMatch {
        public GlobMatchHelper(GlobMatch globMatch) {
            super(globMatch.isNodeMatched(), globMatch.getNode(),
                    globMatch.getMemberNames());
        }

        GlobMatchHelper n(RegistryNode node) {
            if(getNode() != node) {
                throw new RuntimeException();
            }
            return this;
        }

        GlobMatchHelper a(String... accumulators) {
            for(String accumulator : accumulators) {
                if(!getMemberNames().contains(accumulator)) {
                    throw new RuntimeException();
                }
                getMemberNames().remove(accumulator);
            }
            return this;
        }

        GlobMatchHelper c(String... calculators) {
            for(String calculator : calculators) {
                if(!getMemberNames().contains(calculator)) {
                    throw new RuntimeException();
                }
                getMemberNames().remove(calculator);
            }
            return this;
        }

        GlobMatchHelper end() {
            if(getMemberNames() != null && !getMemberNames().isEmpty()) {
                throw new RuntimeException();
            }
            return this;
        }
    }
}
