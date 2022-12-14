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

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

/**
 * The persistent entity interface.
 */
@Getter
@Setter
@RegisterForReflection
public abstract class AbstractTraceableDTO<T> implements Serializable {

    /**
     * The UID of this class.
     */
    private static final long serialVersionUID = -8041083748062531412L;

    /**
     * Optimistic lock version
     */
    private Integer version;

    /**
     * The creation date.
     */
    private OffsetDateTime creationDate;
    /**
     * The creation user.
     */
    private String creationUser;
    /**
     * The modification date.
     */
    private OffsetDateTime modificationDate;
    /**
     * The modification user.
     */
    private String modificationUser;

    /**
     * Gets the GUID.
     *
     * @return the GUID.
     */
    public abstract T getId();

    /**
     * Sets the GUID.
     *
     * @param guid the new GUID.
     */
    public abstract void setId(T guid);

    /**
     * Overwrite the {@code toString} method for the logger.
     * @return the className:Id
     */
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + ":" + getId();
    }
}
