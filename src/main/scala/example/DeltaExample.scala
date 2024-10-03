package example

import io.delta.tables.DeltaTable
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{col, lit}

import scala.jdk.CollectionConverters._


object DeltaExample extends Greeting with App {
  val spark = SparkSession.builder
    .appName(APP_NAME)
    .master("local[*]")
    .config("spark.sql.extensions", "io.delta.sql.DeltaSparkSessionExtension")
    .config("spark.sql.catalog.spark_catalog", "org.apache.spark.sql.delta.catalog.DeltaCatalog")
    .getOrCreate()

  case class Model(id: Int, value: String)

  val data = Seq(Model(1, "A"), Model(2, "B"), Model(3, "C"))

  val df = spark.createDataFrame(data)

  df.show()

  val outputPath = "./target/delta-table-python"

  df.write.format("delta").save(outputPath)

  val deltaTable = DeltaTable.forPath(spark, outputPath)

  deltaTable.update(condition = col("id") === "2", set = Map("value" -> lit("B_updated")))

  val updatedData = spark.read.format("delta").load(outputPath)

  updatedData.show()

  deltaTable.history(10).show(10)

  deltaTable.delete(col("id") === "3")

  val updatedData2 = spark.read.format("delta").load(outputPath)

  updatedData2.show()

  deltaTable.history(10).show(10)

  deltaTable.optimize()

  deltaTable.history(10).show(10)

  deltaTable.restoreToVersion(0).show()

  deltaTable.restoreToVersion(1).show()

  deltaTable.restoreToVersion(2).show()

  deltaTable.history(10).show(10)



}

trait Greeting {
  lazy val APP_NAME: String = "DeltaExample"
}
