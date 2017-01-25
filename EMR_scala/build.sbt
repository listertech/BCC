name := "BuildingSBT"
version := "1.0"
scalaVersion := "2.11.7"
mainClass := Some("com.spark.core.Campaign")

libraryDependencies += "org.apache.spark" % "spark-core_2.11" % "2.0.2" % "provided"
libraryDependencies += "org.apache.spark" % "spark-streaming_2.11" % "2.0.2" % "provided"
libraryDependencies += "org.scala-lang" % "scala-library" % "2.11.7" % "provided"
libraryDependencies += "org.scala-lang" % "scala-reflect" % "2.11.7" % "provided"
libraryDependencies += "org.apache.spark" % "spark-sql_2.11" % "2.0.2" % "provided"
libraryDependencies += "org.apache.spark" % "spark-hive_2.11" % "2.0.2" % "provided"
libraryDependencies += "com.typesafe" % "config" % "1.3.1" % "provided"
libraryDependencies += "org.apache.spark" % "spark-network-shuffle_2.11" % "2.0.2" % "provided"
libraryDependencies += "com.databricks" % "spark-csv_2.11" % "1.5.0" % "provided"






