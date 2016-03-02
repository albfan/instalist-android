/*
 * Copyright 2016 Tino Siegmund, Michael Wodniok
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.noorganization.instalist.presenter;

/**
 * The Actions that could be made by the {@link IPluginController}s broadcast instance.
 * Created by tinos_000 on 07.01.2016.
 */
public class PluginControllerActions {
    public static final String ACTION_PING = "org.noorganization.instalist.action.PING_PLUGIN";
    public static final String ACTION_PONG = "org.noorganization.instalist.action.PONG_PLUGIN";
}
