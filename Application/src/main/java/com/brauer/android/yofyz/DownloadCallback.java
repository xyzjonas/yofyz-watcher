/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.brauer.android.yofyz;

import java.util.List;

/**
 * Sample interface containing bare minimum methods needed for an asynchronous task
 * to updateItem the UI Context.
 */

public interface DownloadCallback {

    /**
     * Indicates that the callback handler needs to updateItem its appearance or information based on
     * the result of the task. Expected to be called from the main thread.
     */
    void updateFromDownload(List<YogaClass> result, List<YogaClass> newOnly);

}
