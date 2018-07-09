/*
 * Copyright 2000-2016 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.tests.components.radiobuttongroup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.Keys;
import org.openqa.selenium.interactions.Actions;

import com.vaadin.testbench.elements.ButtonElement;
import com.vaadin.testbench.elements.CheckBoxGroupElement;
import com.vaadin.testbench.elements.RadioButtonGroupElement;
import com.vaadin.tests.components.FocusTest;

public class RadioButtonGroupFocusTest extends FocusTest {

    @Test
    public void focusOnInit() {
        openTestURL();
        RadioButtonGroupElement radioButtonGroup = $(
                RadioButtonGroupElement.class).first();
        assertTrue(isFocusInsideElement(radioButtonGroup));
    }

    @Test
    public void moveFocusAfterClick() {
        openTestURL();
        $(ButtonElement.class).first().click();
        RadioButtonGroupElement radioButtonGroup2 = $(
                RadioButtonGroupElement.class).last();
        assertTrue(isFocusInsideElement(radioButtonGroup2));
    }

    @Test
    public void focusDoesNotGoIntoWrapperElement() {
        openTestURL();
        new Actions(getDriver()).sendKeys(Keys.TAB).perform();
        assertTrue("Focus not in the second radio button group.",
                isFocusInsideElement($(RadioButtonGroupElement.class).last()));
        assertEquals("Focus should not be in the wrapping div.", "input",
                getFocusedElement().getTagName());
    }
}
