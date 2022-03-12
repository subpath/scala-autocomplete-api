package com.github.subpath

import com.typesafe.scalalogging.StrictLogging
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions._

import java.io._
import java.nio.file.{Files, Paths}
import scala.collection.mutable
import scala.io.Source

object BuildHashmap extends StrictLogging {

  def serialise(value: Any): Array[Byte] = {
    val stream: ByteArrayOutputStream = new ByteArrayOutputStream()
    val oos = new ObjectOutputStream(stream)
    oos.writeObject(value)
    oos.close()
    stream.toByteArray
  }

  def deserialise(bytes: Array[Byte]): Any = {
    val ois = new ObjectInputStream(new ByteArrayInputStream(bytes))
    val value = ois.readObject
    ois.close()
    value
  }

  /** Construct HashMap from weighted Strings */
  def constructSuggestions(): Unit = {

    val content =
      Source
        .fromResource("weighted_strings.txt")
        .mkString
        .split("\n")
        .map(line => line.split("\t")(0))
    logger.info("Loading suggestions to HashMap")
    val file = new File("src/main/resources/prepared_hashmap_data.txt")
    val bw = new BufferedWriter(new FileWriter(file))
    for (line <- content) {
      for (idx <- 1 until line.length + 1) {
        bw.write(s"${line.slice(0, idx)}\t$line\n")
      }
    }
    bw.close()

    val preparedStrings: List[(String, String)] =
      Source
        .fromResource("prepared_hashmap_data.txt")
        .mkString
        .split("\n")
        .toList
        .map(line => line.split("\t")(0).trim -> line.split("\t")(1).trim)

    val spark = SparkSession.builder
      .config("spark.driver.maxResultSize", "10g")
      .config("spark.driver.memory", "10g") // < 416GB (n1-highmem-64 memory)
      .config("spark.sql.debug.maxToStringFields", "1000")
      .config("spark.sql.autoBroadcastJoinThreshold", "-1")
      .master("local[*]")
      .getOrCreate()
    spark.sparkContext.setLogLevel("ERROR")
    import spark.implicits._

    val suggestions: Map[String, mutable.Seq[String]] = preparedStrings
      .toDF("key", "value")
      .withColumn(
        "rank",
        row_number().over(Window.partitionBy("key").orderBy("key"))
      )
      .filter("rank <= 10")
      .groupBy("key")
      .agg(collect_list(col("value")))
      .map(r => (r.getAs[String](0), r.getAs[mutable.Seq[String]](1)))
      .collect
      .toMap

    val bytes = serialise(suggestions)
    val filename: String = "src/main/resources/hashMapSuggestions.bin"
    Files.write(Paths.get(filename), bytes)
  }

  def loadHashmapSuggestions(): Map[String, mutable.Seq[String]] = {
    val suggestionBytes =
      Files.readAllBytes(Paths.get("src/main/resources/hashMapSuggestions.bin"))
    val suggestions = deserialise(suggestionBytes)
      .asInstanceOf[Map[String, mutable.Seq[String]]]
    suggestions
  }

}
