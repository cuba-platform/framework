/*
 * Copyright 2000-2013 Vaadin Ltd.
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
package com.vaadin.tests.components.calendar;

import org.openqa.selenium.WebElement;

import com.vaadin.testbench.By;
import com.vaadin.tests.components.table.DndTableTargetDetailsTest;

/**
 * Test for mouse details in CalendarTargetDetails class when DnD target is a
 * calendar.
 * 
 * @author Vaadin Ltd
 */
public class DndCalendarTargetDetailsTest extends DndTableTargetDetailsTest {

    @Override
    protected WebElement getTarget() {
        return findElement(By.className("v-datecellslot-even"));
    }

}
