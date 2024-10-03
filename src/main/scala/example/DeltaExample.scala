package example

import io.delta.tables._
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{col, lit}

import scala.jdk.CollectionConverters._


object DeltaExample extends Greeting with App {
  val spark = SparkSession.builder
    .appName(APP_NAME)
    .master("local[*]")
    .config("spark.sql.extensions", "io.delta.sql.DeltaSparkSessionExtension")
    .config("spark.sql.catalog.spark_catalog", "org.apache.spark.sql.delta.catalog.DeltaCatalog")
    .config("spark.sql.warehouse.dir", "target/spark-warehouse")
    .getOrCreate()

  case class Model(id: Int, value: String)

  val data = Seq(Model(1, "A"), Model(2, "B"), Model(3, "C"))

  val df = spark.createDataFrame(data)

  df.show()

  val tableName = "default.delta_table"

  df.write.format("delta").saveAsTable(tableName)

  val deltaTable = DeltaTable.forName(spark, tableName)

  deltaTable.update(condition = col("id") === "2", set = Map("value" -> lit("B_updated")))

  deltaTable.history(10).show(10)

  deltaTable.delete(col("id") === "3")

  deltaTable.history(10).show(10)

  deltaTable.optimize().executeZOrderBy("id")

  deltaTable.optimize().executeCompaction()

  deltaTable.history(10).show(10)

  deltaTable.as("delta_table")

  spark.read.format("delta").option("versionAsOf", 0 ).table(tableName).show()
  spark.read.format("delta").option("versionAsOf", 1 ).table(tableName).show()
  spark.read.format("delta").option("versionAsOf", 2 ).table(tableName).show()

}

trait Greeting {
  lazy val APP_NAME: String = "DeltaExample"
}
