/**
 * Copyright (C) 2015 Stratio (http://stratio.com)
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

package com.stratio.sparkta.serving.api.service.http

import akka.pattern.ask
import com.stratio.sparkta.driver.models.TemplateModel
import com.stratio.sparkta.serving.api.actor._
import com.stratio.sparkta.serving.api.constants.HttpConstant
import com.wordnik.swagger.annotations.{Api, ApiOperation, ApiResponse, ApiResponses}
import spray.routing._

import scala.concurrent.Await
import scala.util.{Failure, Success}

@Api(value = HttpConstant.TemplatePath, description = "Operations about templates. One template will have an abstract" +
  " element that represents a validation, a tip, an icon over it.", position = 0)
trait TemplateHttpService extends BaseHttpService {

  override def routes: Route = findByType ~ findByTypeAndName

  @ApiOperation(value = "Find all templates depending ot its type", notes = "Returns a Seq of templates",
    httpMethod = "GET", response = classOf[TemplateModel])
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound, message = HttpConstant.NotFoundMessage)
  ))
  def findByType: Route = {
    path(HttpConstant.TemplatePath / Segment) { (t) =>
      get {
        complete {
          val future = supervisor ? new TemplateSupervisorActor_findByType(t)
          Await.result(future, timeout.duration) match {
            case TemplateSupervisorActor_response_templates(Failure(exception)) => throw exception
            case TemplateSupervisorActor_response_templates(Success(templates)) => templates
          }
        }
      }
    }
  }

  @ApiOperation(value = "Find a template depending ot its type and name", notes = "Returns a template.",
    httpMethod = "GET", response = classOf[TemplateModel])
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound, message = HttpConstant.NotFoundMessage)
  ))
  def findByTypeAndName: Route = {
    path(HttpConstant.TemplatePath / Segment / Segment ) { (t, name) =>
      get {
        complete {
          val future = supervisor ? new TemplateSupervisorActor_findByTypeAndName(t, name)
          Await.result(future, timeout.duration) match {
            case TemplateSupervisorActor_response_template(Failure(exception)) => throw exception
            case TemplateSupervisorActor_response_template(Success(template)) => template
          }
        }
      }
    }
  }
}