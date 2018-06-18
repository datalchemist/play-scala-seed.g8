package org.euratlas.hpolities

import com.thoughtworks.binding.Binding.{Vars,Var}
import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.document
import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.raw.{Node, MouseEvent, Event, HTMLElement, Element}
import org.scalajs.dom.{window,console}

import scala.scalajs.js.annotation._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSON
import scala.scalajs.js.JSConverters._


import play.api.libs.json.{Reads,Writes,Format, Json, JsObject}
import play.api.libs.json.{JsValue,JsString,JsNumber,JsBoolean}

import scala.util.{Success,Failure}
import scala.concurrent.Future


object AppClient {
  
  
  @JSExportTopLevel("AppClient")
  protected def getInstance(): this.type = this
  
  @dom
  def render = {
    <div class="container-fluid">
      <div id="main-container" class="row">
        <div class="col">     
      
        </div>
      </div>
    </div>
  }

  @JSExport
  def main(args: Array[String]): Unit ={
    dom.render(document.body, render)
  }
}
