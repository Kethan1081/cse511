package cse511

import org.apache.spark.sql.SparkSession

object SpatialQuery extends App{
  def runRangeQuery(spark: SparkSession, arg1: String, arg2: String): Long = {

    val pointDf = spark.read.format("com.databricks.spark.csv").option("delimiter","\t").option("header","false").load(arg1);
    pointDf.createOrReplaceTempView("point")

    spark.udf.register("ST_Contains",ST_Contains _)

    val resultDf = spark.sql("select * from point where ST_Contains('"+arg2+"',point._c0)")
    resultDf.show()

    return resultDf.count()
  }

  def runRangeJoinQuery(spark: SparkSession, arg1: String, arg2: String): Long = {

    val pointDf = spark.read.format("com.databricks.spark.csv").option("delimiter","\t").option("header","false").load(arg1);
    pointDf.createOrReplaceTempView("point")

    val rectangleDf = spark.read.format("com.databricks.spark.csv").option("delimiter","\t").option("header","false").load(arg2);
    rectangleDf.createOrReplaceTempView("rectangle")

    spark.udf.register("ST_Contains",ST_Contains _)

    val resultDf = spark.sql("select * from rectangle,point where ST_Contains(rectangle._c0,point._c0)")
    resultDf.show()

    return resultDf.count()
  }

  def runDistanceQuery(spark: SparkSession, arg1: String, arg2: String, arg3: String): Long = {

    val pointDf = spark.read.format("com.databricks.spark.csv").option("delimiter","\t").option("header","false").load(arg1);
    pointDf.createOrReplaceTempView("point")

    spark.udf.register("ST_Within",ST_Within _)

    val resultDf = spark.sql("select * from point where ST_Within(point._c0,'"+arg2+"',"+arg3+")")
    resultDf.show()

    return resultDf.count()
  }

  def runDistanceJoinQuery(spark: SparkSession, arg1: String, arg2: String, arg3: String): Long = {

    val pointDf = spark.read.format("com.databricks.spark.csv").option("delimiter","\t").option("header","false").load(arg1);
    pointDf.createOrReplaceTempView("point1")

    val pointDf2 = spark.read.format("com.databricks.spark.csv").option("delimiter","\t").option("header","false").load(arg2);
    pointDf2.createOrReplaceTempView("point2")

    spark.udf.register("ST_Within",ST_Within _)
    val resultDf = spark.sql("select * from point1 p1, point2 p2 where ST_Within(p1._c0, p2._c0, "+arg3+")")
    resultDf.show()

    return resultDf.count()
  }

  def ST_Contains(queryRectangle:String, pointString:String): Boolean = {
    val rect = queryRectangle.split(",")
    val points = pointString.split(",")
    val rx1: Double = rect(0).toDouble
    val ry1: Double = rect(1).toDouble
    val rx2: Double = rect(2).toDouble
    val ry2: Double = rect(3).toDouble

    val px: Double = points(0).toDouble
    val py: Double = points(1).toDouble
    if(rx1 <= px && ry1 <= py && rx2 >= px && ry2 >= py) {
      return true
    }
    false
  }

  def ST_Within(pointString1:String, pointString2:String, distance:Double): Boolean = {
    val p1 = pointString1.split(",")
    val p2 = pointString2.split(",")

    val p1x: Double = p1(0).toDouble
    val p1y: Double = p1(1).toDouble

    val p2x: Double = p2(0).toDouble
    val p2y: Double = p2(1).toDouble

    val dist: Double = math.sqrt(math.pow((p2x - p1x), 2) + math.pow((p2y - p1y), 2))

    if(dist > distance)
      return false
    true
  }
}
