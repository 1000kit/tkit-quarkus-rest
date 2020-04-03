/*
 * Copyright 2020 tkit.org.
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
package org.tkit.quarkus.rs.models;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The rest exception DTO model.
 */
@Getter
@Setter
public class RestExceptionDTO {

    /**
     * The error code.
     */
    private String errorCode;

    /**
     * The message.
     */
    private String message;

    /**
     * The error parameters.
     */
    private List<Object> parameters;

    /**
     * The named parameters.
     */
    private Map<String, Object> namedParameters = new HashMap<>();

}

