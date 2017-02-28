package de.mtc.jira;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.mtc.jira.api.MyPluginComponent;
import de.mtc.jira.impl.MyPluginComponentImpl;

public class MyComponentUnitTest
{
    @Test
    public void testMyName()
    {
        MyPluginComponent component = new MyPluginComponentImpl(null);
        assertEquals("names do not match!", "myComponent",component.getName());
    }
}