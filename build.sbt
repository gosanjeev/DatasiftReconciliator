name := """DatasiftReconciliator"""

version := "1.0"

scalaVersion := "2.10.3"

scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-Xlint",
  "-Ywarn-dead-code",
  "-language:_",
  "-encoding", "UTF-8"
)

libraryDependencies ++= Seq(
  "ch.qos.logback"      	% "logback-classic"  	% "1.0.13",
  "org.specs2"         		%% "specs2"           	% "1.14"       % "test",
  "com.novocode"        	% "junit-interface"  	% "0.7"        % "test->default",
  "com.typesafe" 	    	% "config" 	 	% "1.0.0",
  "net.databinder.dispatch" 	%% "dispatch-core" 	% "0.10.0",
  "org.mongodb" 		%% "casbah" 		% "2.6.1",
  "org.json4s" 			%% "json4s-native" 	% "3.2.4",
  "org.json4s" 			%% "json4s-jackson"  	% "3.2.4",
  "commons-codec" 		% "commons-codec" 	% "1.8",
  "org.scalatest" 		%% "scalatest" 		% "1.9.1" 	% "test"
)

ideaExcludeFolders += ".idea"

ideaExcludeFolders += ".idea_modules"
