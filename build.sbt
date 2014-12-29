lazy val commonSettings = Seq(
  organization := "ru.korpse",
  version := "0.1.0",
  scalaVersion := "2.11.4"
)

lazy val root = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    name := "WebSocketListener",
    libraryDependencies ++= {
	    Seq(
			"io.netty" % "netty" % "3.10.0.Final",
	    	"com.typesafe.akka" %% "akka-actor" % "2.3.8",
	    	"com.typesafe.akka" %% "akka-slf4j" % "2.3.8",
	    	"io.spray" %%  "spray-json" % "1.3.1",
	    	"ch.qos.logback" % "logback-classic" % "1.1.2"
		)
	}
  )

