/*
 * Copyright 2015-2016 IBM Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package whisk.core.containerpool

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

import spray.json.JsObject
import whisk.common.TransactionId
import whisk.core.container.Interval
import whisk.core.entity.ActivationResponse
import whisk.core.entity.ByteSize

/**
 * An OpenWhisk biased container abstraction. This is **not only** an abstraction
 * for different container providers, but the implementation also needs to include
 * OpenWhisk specific behavior, especially for initialize and run.
 */
trait Container {
    /** Stops the container from consuming CPU cycles. */
    def suspend()(implicit transid: TransactionId): Future[Unit]

    /** Dual of halt. */
    def resume()(implicit transid: TransactionId): Future[Unit]

    /** Completely destroys this instance of the container. */
    def destroy()(implicit transid: TransactionId): Future[Unit]

    /** Initializes code in the container. */
    def initialize(initializer: JsObject, timeout: FiniteDuration)(implicit transid: TransactionId): Future[Interval]

    /** Runs code in the container. */
    def run(parameters: JsObject, environment: JsObject, timeout: FiniteDuration)(implicit transid: TransactionId): Future[(Interval, ActivationResponse)]

    /** Obtains logs up to a given threshold from the container. Optionally waits for a sentinel to appear. */
    def logs(limit: ByteSize, waitForSentinel: Boolean)(implicit transid: TransactionId): Future[List[String]]
}

/** Indicates a general error with the container */
sealed abstract class ContainerError(msg: String) extends Exception(msg)

/** Indicates an error while starting a container */
sealed abstract class ContainerStartupError(msg: String) extends ContainerError(msg)

/** Indicates an error while starting a container of a managed runtime */
case class WhiskContainerStartupError(msg: String) extends ContainerStartupError(msg)

/** Indicates an error while starting a blackbox container */
case class BlackboxStartupError(msg: String) extends ContainerStartupError(msg)

/** Indicates an error while initializing a container */
case class InitializationError(interval: Interval, response: ActivationResponse) extends Exception(response.toString)
