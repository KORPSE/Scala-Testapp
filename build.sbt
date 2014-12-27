lazy val commonSettings = Seq(
  organization := "com.example",
  version := "0.1.0",
  scalaVersion := "2.11.4"
)

lazy val root = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    name := "hello",
    libraryDependencies ++= {
	    Seq(
			"io.netty" % "netty" % "3.10.0.Final",
	    	"com.typesafe.akka" %% "akka-actor" % "2.3.8"
		)
	}
  )
