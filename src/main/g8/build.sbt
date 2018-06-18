version := "1.0-SNAPSHOT"

val scalaVersionNum= "2.12.6"


val playJsonVersion = "2.6.9"

scalaVersion in ThisBuild  := scalaVersionNum

lazy val crossType = CrossType.Full

val macwire = "com.softwaremill.macwire" %% "macros" % "2.2.5" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.1" % Test

val endpointsJvm = "org.julienrf" %% "endpoints-algebra" % "0.4.0"
val endpointsJs = "org.julienrf" %%%! "endpoints-algebra" % "0.4.0"
val endpointsPlayServer = "org.julienrf" %% "endpoints-play-server" % "0.4.0"
val endpointsXhrClientJs = "org.julienrf" %%%! "endpoints-xhr-client" % "0.4.0"

val playJsonJs = "com.typesafe.play" %%%! "play-json" % playJsonVersion

val scalaJsDom = "org.scala-js" %%%! "scalajs-dom" % "0.9.4"

val scalaJsScripts="com.vmunier" %% "scalajs-scripts" % "1.1.1"

val bindingScalaJs = "com.thoughtworks.binding" %%%! "dom" % "11.0.1"
val bindingScalaFutureJs = "com.thoughtworks.binding" %%%! "futurebinding" % "11.0.1"



lazy val webServer = webApp.jvm
  .enablePlugins(PlayScala && LagomPlay,LagomNettyServer)
  .disablePlugins(LagomAkkaHttpServer)
  .settings(
    name := "web-server",
  scalaVersion := scalaVersionNum,
  scalaJSProjects := Seq(webApp.js),
  EclipseKeys.eclipseOutput := Some("eclipse_target"),
  pipelineStages in Assets := Seq(scalaJSPipeline),
  pipelineStages := Seq(digest, gzip),
  // triggers scalaJSPipeline when using compile or continuous compilation
  compile in Compile := ((compile in Compile) dependsOn scalaJSPipeline).value,
  libraryDependencies ++= Seq(
    scalaJsScripts,
    macwire,
    endpointsJvm,
    endpointsPlayServer,
    specs2 % Test
  ),
  // Compile the project before generating Eclipse files, so that generated .scala or .class files for views and routes are present
  EclipseKeys.preTasks := Seq(compile in Compile),
  javaOptions in Universal ++= Seq(
    "-Dpidfile.path=/dev/null"
  )
)

lazy val webClient = webApp.js
  .enablePlugins(ScalaJSPlugin, ScalaJSWeb)
  .settings(
    name := "web-client",
    resolvers += Resolver.bintrayRepo("cibotech", "public"),
    scalaVersion := scalaVersionNum,
    EclipseKeys.eclipseOutput := Some("eclipse_target"),
    scalaJSUseMainModuleInitializer := true,
    relativeSourceMaps := true,
    skip in packageJSDependencies := false,
    scalacOptions ++= Seq("-Xmax-classfile-name","78","-P:scalajs:sjsDefinedByDefault"),
    addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
    libraryDependencies ++= Seq(
      playJsonJs,
      scalaJsDom,
      bindingScalaJs,
      bindingScalaFutureJs,
      endpointsJs,
      endpointsXhrClientJs
    ),
    jsDependencies ++= Seq(
      "org.webjars.bower" % "jquery" % "3.3.1" / "jquery/3.3.1/dist/jquery.min.js"
    )
  )


lazy val webApp = (crossProject in file("webApp"))
  .settings(
    EclipseKeys.eclipseOutput := Some("eclipse_target"),
    unmanagedSourceDirectories in Compile :=
      Seq((scalaSource in Compile).value) ++
        crossType.sharedSrcDir(baseDirectory.value, "main"),
    unmanagedSourceDirectories in Test :=
      Seq((scalaSource in Test).value) ++
        crossType.sharedSrcDir(baseDirectory.value, "test"),
    testOptions in Test := Seq(Tests.Filter(_.endsWith("Test"))))


// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.example.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.example.binders._"
