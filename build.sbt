resolvers in ThisBuild += "Sonatype OSS Snapshots" at
  "https://oss.sonatype.org/content/repositories/snapshots"

organization in ThisBuild := "be.tzbob"
scalaVersion in ThisBuild := "2.12.4"
version in ThisBuild := "0.0.1-SNAPSHOT"

scalacOptions in ThisBuild ++= Seq(
  "-encoding",
  "UTF-8",
  "-feature",
  "-deprecation",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Xfuture",
  "-language:higherKinds",
  "-language:implicitConversions"
)

lazy val example = crossProject
  .in(file("."))
  .enablePlugins(BuildInfoPlugin)
  .settings(
    name := "kooi-examples",
    buildInfoKeys := Seq[BuildInfoKey](name),
    buildInfoPackage := "be.tzbob.examples",
    addCompilerPlugin(
      "org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
    libraryDependencies ++= Seq(
      "be.tzbob" %%% "kooi" % "0.3.9-SNAPSHOT"
    ),
    useYarn := true,
    scalaJSUseMainModuleInitializer := true,
    emitSourceMaps := true,
    webpackBundlingMode := BundlingMode.LibraryOnly(),
    mainClass in Compile := Some("be.tzbob.proman.ProMan")
  )
  .jvmSettings(
    libraryDependencies ++= Seq(
      "org.tpolecat" %% "doobie-core" % "0.5.1",
      "org.tpolecat" %% "doobie-h2" % "0.5.1"
    )
  )

// Needed, so sbt finds the projects
lazy val exampleJS = example.js
  .enablePlugins(ScalaJSBundlerPlugin, ScalaJSWeb)

lazy val exampleJVM = example.jvm
  .settings(
    scalaJSProjects := Seq(exampleJS),
    pipelineStages in Assets := Seq(scalaJSPipeline),
    managedClasspath in Runtime += (packageBin in Assets).value
  )
  .enablePlugins(WebScalaJSBundlerPlugin)

onLoad in Global ~= (_ andThen ("project exampleJVM" :: _))
