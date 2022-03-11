package com.github.subpath

import scala.io.Source
import java.io._
import scala.collection.mutable
import com.typesafe.scalalogging.StrictLogging
import pb.ProgressBar

object BuildHashmap extends StrictLogging {
  // https://stackoverflow.com/questions/66731236/mutable-map-with-multiple-values-in-scala
  def convert[K, V](iterable: Iterable[(K, V)]): mutable.MultiDict[K, V] = {
    iterable.foldLeft(mutable.MultiDict.empty[K, V])(_ addOne _)
  }

  def constructSuggestions(): Unit = {
    val content =
      Source
        .fromResource("weighted_strings.txt")
        .mkString
        .split("\n")
        .map(line => line.split("\t")(0))
    var listOfMappings: List[(String, String)] = List[(String, String)]()
    var pb = new ProgressBar(content.length)
    pb.showSpeed = false
    logger.info("Loading suggestions to HashMap")
    val file = new File("src/main/resources/prepared_hashmap_data.txt")
    val bw = new BufferedWriter(new FileWriter(file))
    for (line <- content) {
      for (idx <- 1 until line.length + 1) {
        //        listOfMappings = listOfMappings :+ (line.slice(0, idx) -> line)
        bw.write(s"${line.slice(0, idx)}\t$line\n")
      }
      pb += 1
    }
    bw.close()

  }

  def loadHashmapSuggestions(): mutable.MultiDict[String, String] = {
    val content: List[(String, String)] =
      Source
        .fromResource("prepared_hashmap_data.txt")
        .mkString
        .split("\n")
        .toList
        .filter(line => line.split("\t")(0).equals("s"))
        .map(line => line.split("\t")(0).trim -> line.split("\t")(1).trim)

    println(content)
    val suggestions = convert(content)
    val foo: List[(String, String)] = List("f" -> "foo", "f" -> "f1")
    println(suggestions.get("s"))
    println(suggestions.get("sa"))
    suggestions
  }

}
