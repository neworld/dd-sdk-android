/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-2020 Datadog, Inc.
 */

package com.datadog.android.rum.assertj

import com.datadog.android.rum.internal.domain.RumEvent
import com.datadog.android.rum.internal.domain.RumEventData
import org.assertj.core.api.AbstractObjectAssert
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset

internal class RumEventAssert(actual: RumEvent) :
    AbstractObjectAssert<RumEventAssert, RumEvent>(
        actual,
        RumEventAssert::class.java
    ) {

    fun hasTimestamp(expected: Long, offset: Long = TIMESTAMP_THRESHOLD_MS): RumEventAssert {
        assertThat(actual.timestamp)
            .overridingErrorMessage(
                "Expected event to have timestamp $expected but was ${actual.timestamp}"
            )
            .isCloseTo(expected, Offset.offset(offset))
        return this
    }

    fun hasAttributes(attributes: Map<String, Any?>): RumEventAssert {
        assertThat(actual.attributes)
            .containsAllEntriesOf(attributes)
        return this
    }

    fun hasContext(assert: RumContextAssert.() -> Unit): RumEventAssert {
        RumContextAssert(actual.context).assert()
        return this
    }

    fun hasViewData(assert: RumEventDataViewAssert.() -> Unit): RumEventAssert {
        assertThat(actual.eventData)
            .isInstanceOf(RumEventData.View::class.java)

        RumEventDataViewAssert(actual.eventData as RumEventData.View).assert()

        return this
    }

    fun hasResourceData(assert: RumEventDataResourceAssert.() -> Unit): RumEventAssert {
        assertThat(actual.eventData)
            .isInstanceOf(RumEventData.Resource::class.java)

        RumEventDataResourceAssert(actual.eventData as RumEventData.Resource).assert()

        return this
    }

    fun hasUserActionData(assert: RumEventDataActionAssert.() -> Unit): RumEventAssert {
        assertThat(actual.eventData)
            .isInstanceOf(RumEventData.UserAction::class.java)

        RumEventDataActionAssert(actual.eventData as RumEventData.UserAction).assert()

        return this
    }

    fun hasErrorData(assert: RumEventDataErrorAssert.() -> Unit): RumEventAssert {
        assertThat(actual.eventData)
            .isInstanceOf(RumEventData.Error::class.java)

        RumEventDataErrorAssert(actual.eventData as RumEventData.Error).assert()

        return this
    }

    companion object {

        internal const val TIMESTAMP_THRESHOLD_MS = 50L

        internal fun assertThat(actual: RumEvent): RumEventAssert =
            RumEventAssert(actual)
    }
}