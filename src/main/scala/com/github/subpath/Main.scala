package com.github.subpath
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.{post, _}
import akka.http.scaladsl.server.Route
import spray.json.RootJsonFormat

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContextExecutor
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import com.typesafe.scalalogging.StrictLogging
import org.apache.lucene.search.suggest.fst.WFSTCompletionLookup
import spray.json.DefaultJsonProtocol._

import scala.io.StdIn

object Main extends StrictLogging {

  implicit val system: ActorSystem[Nothing] =
    ActorSystem(Behaviors.empty, "AutocompleteAPI")
  implicit val executionContext: ExecutionContextExecutor =
    system.executionContext
  final case class Query(term: String)
  implicit val queryFormat: RootJsonFormat[Query] = jsonFormat1(Query)
  val suggestionsWFST: WFSTCompletionLookup =
    BuildWfst.readWeightedStringsAndReturnWFST()
  val suggestionsHashmap: Map[String, scala.collection.mutable.Seq[String]] =
    BuildHashmap.loadHashmapSuggestions()

  def makeWfstLookup(query: Query): Seq[String] = {
    suggestionsWFST
      .lookup(query.term, false, 10)
      .asScala
      .toSeq
      .map(s => s.toString.split("/").head)
  }

  def makeHashmapLookup(query: Query): Seq[String] = {
    suggestionsHashmap.get(query.term).toSeq.head.toSeq.take(10)
  }

  def main(args: Array[String]): Unit = {
    val route: Route =
      concat(
        post {
          pathPrefix("autocomplete") {
            path("wfst") {
              entity(as[Query]) { query =>
                val results: Seq[String] = makeWfstLookup(query)
                complete(results)

              }
            }
          }
        },
        post {
          pathPrefix("autocomplete") {
            path("hashmap") {
              entity(as[Query]) { query =>
                val results: Seq[String] = makeHashmapLookup(query)
                complete(results)
              }
            }
          }
        }
      )

    val bindingFuture = Http().newServerAt("0.0.0.0", 3030).bind(route)
    logger.info(s"Server online at http://0.0.0.0:3030/")
  }
}
