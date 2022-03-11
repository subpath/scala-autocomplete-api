package com.github.subpath

import org.apache.lucene.search.suggest.fst.WFSTCompletionLookup
import org.apache.lucene.store.MMapDirectory
import org.apache.lucene.search.suggest.InputIterator
import org.apache.lucene.util.BytesRef
import scala.collection.JavaConverters._
import java.util
import java.nio.file.Paths
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import scala.io.Source

object BuildWfst {

  def readWeightedStringsAndReturnWFST(): WFSTCompletionLookup = {
    val weightedStringsIterator =
      Source
        .fromResource("weighted_strings.txt")
        .mkString
        .split("\n")
        .map(line => line.split("\t"))
        .map(line => WeightedString(line(0), line(1).toLong))
        .iterator

    val wfst = buildWFSTCompletionLookup(weightedStringsIterator)
    wfst
  }

  case class WeightedString(string: String, weight: Long) {
    def apply(string: String): WeightedString = WeightedString(string, 0L)
  }

  private def buildWFSTCompletionLookup(
      weightedStrings: Iterator[WeightedString]
  ): WFSTCompletionLookup = {

    val suggester =
      new WFSTCompletionLookup(
        new MMapDirectory(Paths.get(tempDir(), "wfst", "lookup")),
        "",
        true
      )

    suggester.build(new WeightedStringIterator(weightedStrings.asJava))

    suggester
  }
  private def tempDir(): String = {
    val sparkLocalDirs = System.getenv("SPARK_LOCAL_DIRS")
    if (sparkLocalDirs != null) sparkLocalDirs
    else System.getProperty("java.io.tmpdir")
  }

  class WeightedStringIterator(
      private val searchQueries: util.Iterator[WeightedString]
  ) extends InputIterator {

    private var currentWeightedString: WeightedString = _
    override def weight(): Long = currentWeightedString.weight

    override def payload(): BytesRef =
      new BytesRef(currentWeightedString.string.getBytes("utf-8"))

    override def hasPayloads: Boolean = false

    override def contexts(): util.Set[BytesRef] = null

    override def hasContexts: Boolean = false

    override def next(): BytesRef = {
      if (searchQueries.hasNext) {
        currentWeightedString = searchQueries.next()
        new BytesRef(currentWeightedString.string.getBytes("UTF8"))
      } else {
        null
      }
    }
  }
}
