package com.github.subpath
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.Done
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives.{post, _}
import akka.http.scaladsl.model.StatusCodes
import spray.json.RootJsonFormat

import java.nio.file.Paths
import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.concurrent.ExecutionContextExecutor
// for JSON serialization/deserialization following dependency is required:
// "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.7"
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._
import org.apache.lucene.search.suggest.fst.WFSTCompletionLookup
import org.apache.lucene.store.MMapDirectory
import scala.io.StdIn
import com.github.subpath.BuildWfst
import scala.concurrent.Future
import com.github.subpath.BuildHashmap

object Main {

  implicit val system: ActorSystem[Nothing] =
    ActorSystem(Behaviors.empty, "AutocompleteAPI")
  implicit val executionContext: ExecutionContextExecutor =
    system.executionContext
  final case class Query(term: String)
  implicit val queryFormat: RootJsonFormat[Query] = jsonFormat1(Query)
  val suggestionsWFST: WFSTCompletionLookup =
    BuildWfst.readWeightedStringsAndReturnWFST()
  val suggestionsHashmap: mutable.MultiDict[String, String] =
    BuildHashmap.loadHashmapSuggestions()

  def makeWfstLookup(query: Query): Seq[String] = {
    suggestionsWFST
      .lookup(query.term, false, 10)
      .asScala
      .toSeq
      .map(s => s.toString.split("/").head)
  }

  def makeHashmapLookup(query: Query): Seq[String] = {
    suggestionsHashmap.get(query.term).toSeq.take(10)
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

    val bindingFuture = Http().newServerAt("localhost", 8080).bind(route)
    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }
}

//curl -H "Content-Type: application/json" -X POST -d '{"term":"foo"}' http://localhost:8080/autocomplete/wfst
