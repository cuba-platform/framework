/*
 * Copyright 2000-2021 Vaadin Ltd.
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
package com.vaadin.v7.client.widget.grid.events;

import com.vaadin.v7.client.widget.grid.events.AbstractGridKeyEventHandler.GridKeyUpHandler;

/**
 * Handler for {@link GridKeyUpEvent}s that happen when the focused cell is in
 * the header of the Grid.
 *
 * @since 7.4
 * @author Vaadin Ltd
 */
public interface HeaderKeyUpHandler extends GridKeyUpHandler {
}
