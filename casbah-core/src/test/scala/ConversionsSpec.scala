/**
 * Copyright (c) 2010 10gen, Inc. <http://10gen.com>
 * Copyright (c) 2009, 2010 Novus Partners, Inc. <http://novus.com>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * For questions and comments about this product, please see the project page at:
 *
 *     http://github.com/mongodb/casbah
 * 
 */

package com.mongodb.casbah.commons
package conversions

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.conversions.scala._

import org.scala_tools.time.Imports._
import com.mongodb.casbah.commons.test.CasbahSpecification
import org.specs2.specification.BeforeExample
import org.bson.BSON

class ConversionsSpec extends CasbahSpecification with BeforeExample {

  type JDKDate = java.util.Date

  def before = {
    DeregisterConversionHelpers()
    DeregisterJodaTimeConversionHelpers()
  }

  "Casbah's Conversion Helpers" should {

    implicit val mongoDB = MongoConnection()("casbahTest")
    mongoDB.dropDatabase()

    "Properly save Option[_] to MongoDB" in {
      val mongo = mongoDB("optionSerialization")
      mongo.dropCollection()

      mongo += MongoDBObject("some" -> Some("foo"), "none" -> None)

      val optDoc = mongo.findOne().getOrElse(
        throw new IllegalArgumentException("No document"))

      optDoc.getAs[String]("some") must beSome("foo")
      optDoc.getAs[String]("none") must beNone
    }

    val jodaDate: DateTime = DateTime.now
    val jdkDate: JDKDate = new JDKDate(jodaDate.getMillis)

    jodaDate.getMillis must beEqualTo(jdkDate.getTime)

/*    "Fail to serialize Joda DateTime Objects unless explicitly loaded." in {
      val mongo = mongoDB("dateFail")
      mongo.dropCollection()

      lazy val saveDate = { mongo += MongoDBObject("date" -> jodaDate, "type" -> "joda") }

      log.info("Save Date: %s", saveDate)
      saveDate must throwA[Exception]

      mongo += MongoDBObject("date" -> jdkDate, "type" -> "jdk")

      val jdkEntry = mongo.findOne(MongoDBObject("type" -> "jdk"),
        MongoDBObject("date" -> 1))

      jdkEntry must beSome

      jdkEntry.get.getAs[JDKDate]("date") must beSome(jdkDate)
    }*/

/*    "Successfully serialize & deserialize Joda DateTime Objects when convertors are loaded." in {
      val mongo = mongoDB("jodaSerDeser")
      mongo.dropCollection()
      RegisterConversionHelpers()
      RegisterJodaTimeConversionHelpers()
      // TODO - Matcher verification of these being loaded

      mongo += MongoDBObject("date" -> jodaDate, "type" -> "joda")

      val jodaEntry = mongo.findOne(MongoDBObject("type" -> "joda"),
        MongoDBObject("date" -> 1))

      jodaEntry must beSome
      //jodaEntry.get.get("date") must beSome[DateTime]
      jodaEntry.get.getAs[DateTime]("date") must beSome(jodaDate)
      // Casting it as something it isn't will fail
      lazy val getDate = { jodaEntry.get.getAs[JDKDate]("date") }
      // Note - exceptions are wrapped by Some() and won't be thrown until you .get 
      getDate.get must throwA[ClassCastException]
    }*/

    //"Be successfully deregistered." in {
      //val mongo = mongoDB("conversionDeReg")
      //mongo.dropCollection()
      //RegisterConversionHelpers()
      //RegisterJodaTimeConversionHelpers()
      //DeregisterConversionHelpers()
      //DeregisterJodaTimeConversionHelpers()
      //lazy val testJodaInsert = { mongo += MongoDBObject("date" -> jodaDate, "type" -> "joda") }

      //testJodaInsert must throwA[IllegalArgumentException]

      //// Normal JDK Date should work
      //mongo += MongoDBObject("date" -> jdkDate, "type" -> "jdk")

      //val jdkEntry = mongo.findOne(MongoDBObject("type" -> "jdk"),
        //MongoDBObject("date" -> 1))

      //jdkEntry must beSome

      //jdkEntry.get.getAs[JDKDate]("date") must beSome(jdkDate)
      //// Casting it as something it isn't will fail
      //lazy val getDate = { jdkEntry.get.getAs[DateTime]("date") }
      //// Note - exceptions are wrapped by Some() and won't be thrown until you .get 
      //getDate.get must throwA[ClassCastException]
    //}
    "Inserting a JDKDate should still allow retrieval as JodaTime after Conversions load" in {
      val mongo = mongoDB("conversionConversion")
      mongo.dropCollection()
      RegisterConversionHelpers()

      mongo += MongoDBObject("date" -> jdkDate, "type" -> "jdk")

      val jdkEntry = mongo.findOne(MongoDBObject("type" -> "jdk"),
        MongoDBObject("date" -> 1))

      jdkEntry must beSome

      jdkEntry.get.getAs[JDKDate]("date") must beSome(jdkDate)
      // Casting it as something it isn't will fail
      lazy val getDate = { jdkEntry.get.getAs[DateTime]("date") }
      // Note - exceptions are wrapped by Some() and won't be thrown until you .get 
      getDate.get must throwA[ClassCastException]

      RegisterJodaTimeConversionHelpers()

      val jodaEntry = mongo.findOne(MongoDBObject("type" -> "jdk"),
        MongoDBObject("date" -> 1))

      jodaEntry must beSome

      jodaEntry.get.getAs[DateTime]("date") must beSome(jodaDate)
      // Casting it as something it isn't will fail
      lazy val getConvertedDate = { jodaEntry.get.getAs[JDKDate]("date") }
      // Note - exceptions are wrapped by Some() and won't be thrown until you .get 
      getConvertedDate.get must throwA[ClassCastException]
    }
/*    "toString-ing a JODA Date with JODA Conversions loaded doesn't choke horribly." in {
      RegisterConversionHelpers()
      val jodaEntry: DBObject = MongoDBObject("type" -> "jdk",
        "date" -> jdkDate)

      /*jodaEntry.getAs[DateTime]("date") must beSome(jdkDate)
      // Casting it as something it isn't will fail
      lazy val getDate = { jodaEntry.getAs[JDKDate]("date") } 
      // Note - exceptions are wrapped by Some() and won't be thrown until you .get 
      getDate.get must throwA[ClassCastException] */
      RegisterJodaTimeConversionHelpers()

      val json = jodaEntry.toString

    }*/
  }
}
// vim: set ts=2 sw=2 sts=2 et:
